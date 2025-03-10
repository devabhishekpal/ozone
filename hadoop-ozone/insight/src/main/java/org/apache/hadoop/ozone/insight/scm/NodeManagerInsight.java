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

package org.apache.hadoop.ozone.insight.scm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.hdds.protocol.proto.HddsProtos.NodeOperationalState;
import org.apache.hadoop.hdds.protocol.proto.HddsProtos.NodeState;
import org.apache.hadoop.hdds.scm.node.SCMNodeManager;
import org.apache.hadoop.ozone.insight.BaseInsightPoint;
import org.apache.hadoop.ozone.insight.Component.Type;
import org.apache.hadoop.ozone.insight.LoggerSource;
import org.apache.hadoop.ozone.insight.MetricDisplay;
import org.apache.hadoop.ozone.insight.MetricGroupDisplay;

/**
 * Insight definition to check node manager / node report events.
 */
public class NodeManagerInsight extends BaseInsightPoint {

  @Override
  public List<LoggerSource> getRelatedLoggers(boolean verbose,
      Map<String, String> filters) {
    List<LoggerSource> loggers = new ArrayList<>();
    loggers.add(
        new LoggerSource(Type.SCM, SCMNodeManager.class,
            defaultLevel(verbose)));
    return loggers;
  }

  @Override
  public List<MetricGroupDisplay> getMetrics(Map<String, String> filters) {
    List<MetricGroupDisplay> display = new ArrayList<>();

    MetricGroupDisplay nodes =
        new MetricGroupDisplay(Type.SCM, "Node counters");

    for (NodeOperationalState opState : NodeOperationalState.values()) {
      for (NodeState healthState : NodeState.values()) {
        String metricId = String.format("scm_node_manager_%s_%s_nodes",
            opState, healthState).toLowerCase();
        String metricDescription = String.format("%s %s Nodes",
            opState, healthState);
        nodes.addMetrics(new MetricDisplay(metricDescription, metricId));
      }
    }
    display.add(nodes);

    MetricGroupDisplay hb =
        new MetricGroupDisplay(Type.SCM, "HB processing stats");
    hb.addMetrics(
        new MetricDisplay("HB processed", "scm_node_manager_num_hb_processed"));
    hb.addMetrics(new MetricDisplay("HB processing failed",
        "scm_node_manager_num_hb_processing_failed"));
    display.add(hb);

    return display;
  }

  @Override
  public String getDescription() {
    return "SCM Datanode management related information.";
  }

}
