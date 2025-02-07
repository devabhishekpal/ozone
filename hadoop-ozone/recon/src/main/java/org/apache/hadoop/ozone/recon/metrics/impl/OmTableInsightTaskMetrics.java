package org.apache.hadoop.ozone.recon.metrics.impl;

import org.apache.hadoop.metrics2.MetricsInfo;
import org.apache.hadoop.metrics2.annotation.Metric;
import org.apache.hadoop.metrics2.annotation.Metrics;
import org.apache.hadoop.metrics2.lib.DefaultMetricsSystem;
import org.apache.hadoop.metrics2.lib.Interns;
import org.apache.hadoop.metrics2.lib.MutableCounterLong;
import org.apache.hadoop.metrics2.lib.MutableRate;
import org.apache.hadoop.ozone.recon.metrics.ReconOmTaskMetrics;

@Metrics(about="Metrics for OmTableInsight task", context="recon")
public class OmTableInsightTaskMetrics extends ReconOmTaskMetrics {
  private static final String SOURCE_NAME = OmTableInsightTaskMetrics.class.getSimpleName();
  private static final MetricsInfo TASK_INFO = Interns.info(
      "ReconTaskMetrics", "Task metrics for OmTableInsight"
  );

  OmTableInsightTaskMetrics() {
    super(TASK_INFO, "OmTableInsightTask", SOURCE_NAME);
  }

  public static OmTableInsightTaskMetrics register() {
    return DefaultMetricsSystem.instance().register(
        SOURCE_NAME,
        "OmTableInsightTask metrics",
        new OmTableInsightTaskMetrics()
    );
  }

  public void unregister() {
    DefaultMetricsSystem.instance().unregisterSource(SOURCE_NAME);
  }

  private @Metric MutableCounterLong putEventCount;
  private @Metric MutableCounterLong deleteEventCount;
  private @Metric MutableCounterLong updateEventCount;

  private @Metric MutableCounterLong taskWriteToDBCount;
  private @Metric MutableRate writeToDBLatency;

  public void incrPutEventCount() {
    this.putEventCount.incr();
  }

  public void incrDeleteEventCount() {
    this.deleteEventCount.incr();
  }

  public void incrUpdateEventCount() {
    this.updateEventCount.incr();
  }

  public void incrTaskWriteToDBCount() {
    this.taskWriteToDBCount.incr();
  }

  public void updateWriteToDBLatency(long time) {
    this.writeToDBLatency.add(time);
  }
}
