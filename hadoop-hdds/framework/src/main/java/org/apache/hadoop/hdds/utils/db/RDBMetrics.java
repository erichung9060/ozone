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

package org.apache.hadoop.hdds.utils.db;

import com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.metrics2.MetricsSystem;
import org.apache.hadoop.metrics2.annotation.Metric;
import org.apache.hadoop.metrics2.lib.DefaultMetricsSystem;
import org.apache.hadoop.metrics2.lib.MutableCounterLong;

/**
 * Class to hold RocksDB metrics.
 */
public class RDBMetrics {

  private static final String SOURCE_NAME = RDBMetrics.class.getSimpleName();

  private static RDBMetrics instance;

  private @Metric MutableCounterLong numDBKeyMayExistChecks;
  private @Metric MutableCounterLong numDBKeyMayExistMisses;

  private @Metric MutableCounterLong numDBKeyGets;
  private @Metric MutableCounterLong numDBKeyGetIfExistChecks;
  private @Metric MutableCounterLong numDBKeyGetIfExistMisses;
  private @Metric MutableCounterLong numDBKeyGetIfExistGets;
  // WAL Update data size and sequence count
  private @Metric MutableCounterLong walUpdateDataSize;
  private @Metric MutableCounterLong walUpdateSequenceCount;

  public RDBMetrics() {
  }

  public static synchronized RDBMetrics create() {
    if (instance != null) {
      return instance;
    }
    MetricsSystem ms = DefaultMetricsSystem.instance();
    instance = ms.register(SOURCE_NAME,
        "Rocks DB Metrics",
        new RDBMetrics());
    return instance;
  }

  public long getNumDBKeyGets() {
    return numDBKeyGets.value();
  }

  public void incNumDBKeyGets() {
    this.numDBKeyGets.incr();
  }

  public long getNumDBKeyGetIfExistGets() {
    return numDBKeyGetIfExistGets.value();
  }

  public void incNumDBKeyGetIfExistGets() {
    this.numDBKeyGetIfExistGets.incr();
  }

  public long getNumDBKeyGetIfExistChecks() {
    return numDBKeyGetIfExistChecks.value();
  }

  public void incNumDBKeyGetIfExistChecks() {
    this.numDBKeyGetIfExistChecks.incr();
  }

  public long getNumDBKeyGetIfExistMisses() {
    return numDBKeyGetIfExistMisses.value();
  }

  public void incNumDBKeyGetIfExistMisses() {
    this.numDBKeyGetIfExistMisses.incr();
  }

  public void incNumDBKeyMayExistChecks() {
    numDBKeyMayExistChecks.incr();
  }

  public void incNumDBKeyMayExistMisses() {
    numDBKeyMayExistMisses.incr();
  }

  @VisibleForTesting
  public long getNumDBKeyMayExistChecks() {
    return numDBKeyMayExistChecks.value();
  }

  @VisibleForTesting
  public long getNumDBKeyMayExistMisses() {
    return numDBKeyMayExistMisses.value();
  }

  public void incWalUpdateDataSize(long size) {
    walUpdateDataSize.incr(size);
  }

  public long getWalUpdateDataSize() {
    return walUpdateDataSize.value();
  }

  public void incWalUpdateSequenceCount(long count) {
    walUpdateSequenceCount.incr(count);
  }

  public long getWalUpdateSequenceCount() {
    return walUpdateSequenceCount.value();
  }

  public static synchronized void unRegister() {
    instance = null;
    MetricsSystem ms = DefaultMetricsSystem.instance();
    ms.unregisterSource(SOURCE_NAME);
  }

}
