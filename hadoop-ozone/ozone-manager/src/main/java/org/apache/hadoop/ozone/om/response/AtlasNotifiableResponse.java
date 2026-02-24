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

package org.apache.hadoop.ozone.om.response;

import java.util.List;
import org.apache.hadoop.ozone.om.atlas.OzoneAtlasNotification;

/**
 * Marker interface for OM responses that can emit Atlas notifications.
 * Implementations are expected to build notifications only when the response
 * represents a successful, committed metadata change.
 */
public interface AtlasNotifiableResponse {

  /**
   * Build notifications to be sent to Atlas.
   *
   * @param clusterName logical cluster identifier to include in qualifiedName
   * @return notifications to publish; empty if none
   */
  List<OzoneAtlasNotification> buildAtlasNotifications(String clusterName);
}
