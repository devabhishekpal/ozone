package org.apache.hadoop.ozone.recon.tasks;

import org.apache.hadoop.ozone.recon.metrics.ReconTaskStatusCounter;
import org.hadoop.ozone.recon.schema.tables.daos.ReconTaskStatusDao;
import org.hadoop.ozone.recon.schema.tables.pojos.ReconTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * This class provides utilities to update/modify Recon Task related data
 * like updating table, incrementing counter etc.
 */
public class ReconTaskStatusUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(ReconTaskStatusUpdater.class);

  private ReconTaskStatus reconTaskStatus;

  private ReconTaskStatusDao reconTaskStatusDao;
  private ReconTaskStatusCounter taskStatusCounter;

  private String taskName;

  public ReconTaskStatusUpdater(ReconTaskStatusDao reconTaskStatusDao,
                                ReconTaskStatusCounter reconTaskStatusCounter) {
    this.reconTaskStatusDao = reconTaskStatusDao;
    this.taskStatusCounter = reconTaskStatusCounter;
    this.reconTaskStatus = new ReconTaskStatus(null, 0L, 0L, 0, 0);
  }

  public ReconTaskStatusUpdater(ReconTaskStatusDao reconTaskStatusDao,
                                ReconTaskStatusCounter taskStatusCounter,
                                String taskName) {
    this.taskName = taskName;
    this.reconTaskStatusDao = reconTaskStatusDao;
    this.taskStatusCounter = taskStatusCounter;
    this.reconTaskStatus = new ReconTaskStatus(taskName, 0L, 0L, 0, 0);
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
    this.reconTaskStatus.setTaskName(taskName);
  }

  public void setLastUpdatedSeqNumber(long lastUpdatedSeqNumber) {
    this.reconTaskStatus.setLastUpdatedSeqNumber(lastUpdatedSeqNumber);
  }

  public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
    this.reconTaskStatus.setLastUpdatedTimestamp(lastUpdatedTimestamp);
  }

  public void setLastTaskRunStatus(int lastTaskRunStatus) {
    this.reconTaskStatus.setLastTaskRunStatus(lastTaskRunStatus);
  }

  public void setIsCurrentTaskRunning(int isCurrentTaskRunning) {
    this.reconTaskStatus.setIsCurrentTaskRunning(isCurrentTaskRunning);
  }

  public ReconTaskStatusUpdater getInstanceWithTask(String taskName) {
    return new ReconTaskStatusUpdater(reconTaskStatusDao, taskStatusCounter, taskName);
  }

  public void updateDetails() {
    if (!reconTaskStatusDao.existsById(this.taskName)){
      // First time getting the task, so insert value
      reconTaskStatusDao.insert(this.reconTaskStatus);
      LOG.info("Registered Task: {}", this.taskName);
    } else {
      // We already have row for the task in the table, update the row
      reconTaskStatusDao.update(this.reconTaskStatus);
      if (null != this.reconTaskStatus.getLastTaskRunStatus()) {
        taskStatusCounter.updateCounter(taskName, this.reconTaskStatus.getLastTaskRunStatus() > -1);
      }
    }
  }
}
