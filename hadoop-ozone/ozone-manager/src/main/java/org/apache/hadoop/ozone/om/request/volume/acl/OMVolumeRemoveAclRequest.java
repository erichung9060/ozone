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

package org.apache.hadoop.ozone.om.request.volume.acl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.ozone.OzoneAcl;
import org.apache.hadoop.ozone.audit.AuditLogger;
import org.apache.hadoop.ozone.audit.OMAction;
import org.apache.hadoop.ozone.om.OzoneManager;
import org.apache.hadoop.ozone.om.execution.flowcontrol.ExecutionContext;
import org.apache.hadoop.ozone.om.helpers.OmVolumeArgs;
import org.apache.hadoop.ozone.om.request.util.OmResponseUtil;
import org.apache.hadoop.ozone.om.response.OMClientResponse;
import org.apache.hadoop.ozone.om.response.volume.OMVolumeAclOpResponse;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.OMRequest;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.OMResponse;
import org.apache.hadoop.ozone.security.acl.OzoneObj;
import org.apache.hadoop.ozone.security.acl.OzoneObjInfo;
import org.apache.hadoop.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles volume remove acl request.
 */
public class OMVolumeRemoveAclRequest extends OMVolumeAclRequest {
  private static final Logger LOG =
      LoggerFactory.getLogger(OMVolumeRemoveAclRequest.class);

  private static final VolumeAclOp VOLUME_REMOVE_ACL_OP =
      (acls, volArgs) -> volArgs.removeAcl(acls.get(0));

  private final List<OzoneAcl> ozoneAcls;
  private final String volumeName;
  private final OzoneObj obj;

  @Override
  public OMRequest preExecute(OzoneManager ozoneManager) throws IOException {
    long modificationTime = Time.now();
    OzoneManagerProtocolProtos.RemoveAclRequest.Builder removeAclRequestBuilder
        = getOmRequest().getRemoveAclRequest().toBuilder()
            .setModificationTime(modificationTime);

    return getOmRequest().toBuilder()
        .setRemoveAclRequest(removeAclRequestBuilder)
        .setUserInfo(getUserInfo())
        .build();
  }

  public OMVolumeRemoveAclRequest(OMRequest omRequest) {
    super(omRequest, VOLUME_REMOVE_ACL_OP);
    OzoneManagerProtocolProtos.RemoveAclRequest removeAclRequest =
        getOmRequest().getRemoveAclRequest();
    Preconditions.checkNotNull(removeAclRequest);
    ozoneAcls = Lists.newArrayList(
        OzoneAcl.fromProtobuf(removeAclRequest.getAcl()));
    obj = OzoneObjInfo.fromProtobuf(removeAclRequest.getObj());
    volumeName = obj.getPath().substring(1);
  }

  @Override
  public List<OzoneAcl> getAcls() {
    return ozoneAcls;
  }

  @Override
  public String getVolumeName() {
    return volumeName;
  }

  private OzoneAcl getAcl() {
    return ozoneAcls.get(0);
  }

  @Override
  OzoneObj getObject() {
    return obj;
  }

  @Override
  OMResponse.Builder onInit() {
    return OmResponseUtil.getOMResponseBuilder(getOmRequest());
  }

  @Override
  OMClientResponse onSuccess(OMResponse.Builder omResponse,
      OmVolumeArgs omVolumeArgs, boolean aclApplied) {
    omResponse.setRemoveAclResponse(OzoneManagerProtocolProtos.RemoveAclResponse
        .newBuilder().setResponse(aclApplied).build());
    return new OMVolumeAclOpResponse(omResponse.build(), omVolumeArgs);
  }

  @Override
  OMClientResponse onFailure(OMResponse.Builder omResponse,
      Exception ex) {
    return new OMVolumeAclOpResponse(createErrorOMResponse(omResponse, ex));
  }

  @Override
  void onComplete(Result result, Exception ex, long trxnLogIndex,
      AuditLogger auditLogger, Map<String, String> auditMap) {
    switch (result) {
    case SUCCESS:
      if (LOG.isDebugEnabled()) {
        LOG.debug("Remove acl: {} from volume: {} success!", getAcl(),
            getVolumeName());
      }
      break;
    case FAILURE:
      LOG.error("Remove acl {} from volume {} failed!", getAcl(),
          getVolumeName(), ex);
      break;
    default:
      LOG.error("Unrecognized Result for OMVolumeRemoveAclRequest: {}",
          getOmRequest());
    }
    markForAudit(auditLogger, buildAuditMessage(OMAction.REMOVE_ACL, auditMap,
        ex, getOmRequest().getUserInfo()));
  }

  @Override
  public OMClientResponse validateAndUpdateCache(OzoneManager ozoneManager, ExecutionContext context) {
    ozoneManager.getMetrics().incNumRemoveAcl();
    return super.validateAndUpdateCache(ozoneManager, context);
  }
}
