/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ozone.fs.http.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.hadoop.hdds.annotation.InterfaceAudience;
import org.apache.hadoop.util.StringUtils;
import org.apache.ozone.fs.http.HttpFSConstants;

/**
 * Filter that Enforces the content-type to be application/octet-stream for
 * POST and PUT requests.
 */
@InterfaceAudience.Private
public class CheckUploadContentTypeFilter implements Filter {

  private static final Set<String> UPLOAD_OPERATIONS = new HashSet<String>();

  static {
    UPLOAD_OPERATIONS.add(HttpFSConstants.Operation.APPEND.toString());
    UPLOAD_OPERATIONS.add(HttpFSConstants.Operation.CREATE.toString());
  }

  /**
   * Initializes the filter.
   * <p>
   * This implementation is a NOP.
   *
   * @param config filter configuration.
   *
   * @throws ServletException thrown if the filter could not be initialized.
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
  }

  /**
   * Enforces the content-type to be application/octet-stream for
   * POST and PUT requests.
   *
   * @param request servlet request.
   * @param response servlet response.
   * @param chain filter chain.
   *
   * @throws IOException thrown if an IO error occurs.
   * @throws ServletException thrown if a servlet error occurs.
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain)
      throws IOException, ServletException {
    boolean contentTypeOK = true;
    HttpServletRequest httpReq = (HttpServletRequest) request;
    HttpServletResponse httpRes = (HttpServletResponse) response;
    String method = httpReq.getMethod();
    if (method.equals("PUT") || method.equals("POST")) {
      String op = httpReq.getParameter(HttpFSConstants.OP_PARAM);
      if (op != null && UPLOAD_OPERATIONS.contains(
          StringUtils.toUpperCase(op))) {
        if ("true".equalsIgnoreCase(httpReq
            .getParameter(HttpFSParametersProvider.DataParam.NAME))) {
          String contentType = httpReq.getContentType();
          contentTypeOK =
              HttpFSConstants.UPLOAD_CONTENT_TYPE.equalsIgnoreCase(contentType);
        }
      }
    }
    if (contentTypeOK) {
      chain.doFilter(httpReq, httpRes);
    } else {
      httpRes.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      httpRes.setContentType("application/json");
      httpRes.setCharacterEncoding("UTF-8");

      String errorMessage = "Data upload requests must have content-type set to '" +
          HttpFSConstants.UPLOAD_CONTENT_TYPE + "'";

      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("error", errorMessage);

      String jsonResponse = JsonUtil.toJsonString(errorMap);

      PrintWriter writer = httpRes.getWriter();
      writer.write(jsonResponse);
      writer.flush();
      writer.close();
    }
  }

  /**
   * Destroys the filter.
   * <p>
   * This implementation is a NOP.
   */
  @Override
  public void destroy() {
  }

}
