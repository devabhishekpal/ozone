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

package org.apache.hadoop.ozone.om.atlas;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import org.apache.hadoop.ozone.om.response.AtlasNotifiableResponse;
import org.apache.hadoop.ozone.om.response.OMClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Best-effort notification publisher. Currently logs payloads; can be wired to
 * Atlas Hook or REST client without touching response classes.
 */
public class AtlasNotificationPublisher {

  private static final Logger LOG =
      LoggerFactory.getLogger(AtlasNotificationPublisher.class);

  private final boolean enabled;
  private final ExecutorService executor;

  public AtlasNotificationPublisher(boolean enabled) {
    this.enabled = enabled;
    this.executor = enabled ? Executors.newFixedThreadPool(2) : null;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void publishIfPresent(OMClientResponse response, String clusterName) {
    if (!enabled || !(response instanceof AtlasNotifiableResponse)) {
      return;
    }
    try {
      executor.submit(() -> {
        try {
          List<OzoneAtlasNotification> notifications =
              ((AtlasNotifiableResponse) response)
                  .buildAtlasNotifications(clusterName);
          if (notifications == null || notifications.isEmpty()) {
            return;
          }
          // For now, just log. Replace with Atlas hook/REST call as needed.
          if (LOG.isDebugEnabled()) {
            LOG.debug("Atlas notifications ready: {}", notifications);
          } else {
            LOG.info("Atlas notifications prepared for {}: count={}",
                response.getClass().getSimpleName(), notifications.size());
          }
        } catch (Exception e) {
          LOG.warn("Failed to build/send Atlas notification for {}",
              response.getClass().getSimpleName(), e);
        }
      });
    } catch (RejectedExecutionException rex) {
      LOG.warn("Atlas notification executor rejected task", rex);
    }
  }
}
