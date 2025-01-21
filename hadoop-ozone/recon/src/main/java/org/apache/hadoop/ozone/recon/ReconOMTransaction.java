package org.apache.hadoop.ozone.recon;

import org.apache.hadoop.hdds.utils.TransactionInfo;
import org.apache.hadoop.hdds.utils.db.BatchOperation;
import org.apache.hadoop.ozone.om.response.OMClientResponse;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.OMRequest;
import org.apache.hadoop.ozone.recon.recovery.ReconOMMetadataManager;
import org.apache.ratis.server.protocol.TermIndex;
import org.apache.ratis.util.function.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.apache.hadoop.ozone.OzoneConsts.TRANSACTION_INFO_KEY;

public class ReconOMTransaction {

  private static final Logger LOG = LoggerFactory.getLogger(ReconOMTransaction.class);
  private ReconOMMetadataManager metadataManager;

  private static class Entry {
    private final TermIndex termIndex;
    private final OMClientResponse response;

    Entry(TermIndex termIndex, OMClientResponse response) {
      this.termIndex = termIndex;
      this.response = response;
    }

    TermIndex getTermIndex() {
      return termIndex;
    }
    OMClientResponse getResponse() {
      return response;
    }
  }

  public ReconOMTransaction(ReconOMMetadataManager metadataManager) {
    this.metadataManager = metadataManager;
  }

  /**
   * This is working on the same logic as
   * {@link org.apache.hadoop.ozone.om.ratis.OzoneManagerDoubleBuffer#splitReadyBufferAtCreateSnapshot}.
   * We want to separate out CreateSnapshot and PurgeSnapshot requests from other requests to maintain
   * unique checkpoint at these operations. Other requests can be batched together and flushed
   * @param batch A set of {@link Entry} we want to split
   * @return A {@link List} of {@link Queue} split at CreateSnapshot and PurgeSnapshot requests
   */
  private List<Queue<Entry>> splitBatchAtSnapshot(Queue<Entry> batch) {
    final List<Queue<Entry>> splitBatches = new ArrayList<>();
    OMResponse previousOMResponse = null;
    for(final Entry entry: batch) {
      OMResponse omResponse = entry.getResponse().getOMResponse();
      if (splitBatches.isEmpty() || isStandaloneCmdType(omResponse) || isStandaloneCmdType(previousOMResponse)) {
        splitBatches.add(new LinkedList<>());
      }
      splitBatches.get(splitBatches.size() - 1).add(entry);
      previousOMResponse = omResponse;
    }

    return splitBatches;
  }
  private boolean isStandaloneCmdType(OMResponse response) {
    if (null == response) {
      return false;
    }

    final OzoneManagerProtocolProtocolProtos.Type type = response.getCmdType();
    return type == OzoneManagerProtocolProtos.Type.CreateSnapshot
        || type == OzoneManagerProtocolProtos.Type.SnapshotPurge;
  }

  public void writeBatchToReconDB(Queue<Entry> batch) {
    try {
      final List<Queue<Entry>> splitBatches = splitBatchAtSnapshot(batch);
      for (Queue<Entry> batch: splitBatches) {
        flushBatch(batch);
      }
      //All operations are written, clear the batch
      batch.clear();
    } catch (IOException e) {
      LOG.error("Encountered exception while writing batch to Recon DB.", e);
    }
  }

  private void executeTransactionInfoInBatch(long transactionIdx, CheckedRunnable<IOException> operation)
      throws IOException {
    operation.run();
  }

  private void flushBatch(Queue<Entry> batch) throws IOException {
    //Commit Transactions to Recon DB
    final List<TermIndex> transactions = batch.stream()
        .map(Entry::getTermIndex)
        .sorted().collect(Collectors.toList());
    final int transactionsSize = transactions.size();
    final TermIndex lastTransaction = transactions.get(transactionsSize - 1);

    try (BatchOperation batchOp = metadataManager.getStore().initBatchOperation()) {
      executeTransactionInfoInBatch(lastTransaction.getIndex(),
          () -> metadataManager.getTransactionInfoTable().putWithBatch(
              batchOp, TRANSACTION_INFO_KEY, TransactionInfo.valueOf(lastTransaction)
          ));
    }
  }
}
