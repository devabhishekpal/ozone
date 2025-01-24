package org.apache.hadoop.ozone.recon.ratis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.hadoop.ozone.om.execution.flowcontrol.ExecutionContext;
import org.apache.hadoop.ozone.om.helpers.OMRatisHelper;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.OMRequest;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.OMResponse;
import org.apache.hadoop.ozone.recon.spi.OzoneManagerServiceProvider;
import org.apache.hadoop.util.concurrent.HadoopExecutors;
import org.apache.ratis.proto.RaftProtos;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.protocol.TermIndex;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.statemachine.impl.SimpleStateMachineStorage;
import org.apache.ratis.util.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public class ReconStateMachine extends BaseStateMachine {

  private static final Logger LOG = LoggerFactory.getLogger(ReconStateMachine.class);
  private final Path rocksDBDir;
  private long lastAppliedIdx = 0;
  private long lastAppliedTerm = 0;
  private final OzoneManagerServiceProvider reconOMManager;
  private final ExecutorService executorService;
  private final ExecutorService installSnapshotExecutor;
  private final SimpleStateMachineStorage storage =
      new SimpleStateMachineStorage();


  public ReconStateMachine(Path rocksDBDir, OzoneManagerServiceProvider reconOMManager) {
    this.rocksDBDir = rocksDBDir;
    this.reconOMManager = reconOMManager;

    loadSnapshotFromDB();

    ThreadFactory build = new ThreadFactoryBuilder().setDaemon(true)
        .setNameFormat("ReconStateMachineApplyTransactionThread - %d").build();
    this.executorService = HadoopExecutors.newSingleThreadExecutor(build);

    ThreadFactory installSnapshotThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("ReconStateMachineInstallSnapshotThread").build();
    this.installSnapshotExecutor = HadoopExecutors.newSingleThreadExecutor(installSnapshotThreadFactory);
  }

  @Override
  public void initialize(RaftServer server, RaftGroupId id, RaftStorage raftStorage) throws IOException {
    getLifeCycle().startAndTransition(() -> {
      super.initialize(server, id, raftStorage);
      storage.init(raftStorage);
      LOG.info("Initialized Recon Server {} : with {}", getId(), getLastAppliedTermIndex());
    });
  }

  @Override
  public synchronized void reinitialize() throws IOException {
    loadSnapshotFromDB();
    if (getLifeCycleState() == LifeCycle.State.PAUSED) {
      final TermIndex lastApplied = getLastAppliedTermIndex();
      unpause(lastApplied.getIndex(), lastApplied.getTerm());
      LOG.info("Re-initialized Recon Server: {}, with {}", getId(), lastApplied);
    }
  }

  @Override
  public CompletableFuture<TermIndex> notifyInstallSnapshotFromLeader(
      RaftProtos.RoleInfoProto roleInfoProto, TermIndex firstLogTermIdx) {
    String leaderNodeId = RaftPeerId.valueOf(roleInfoProto.getFollowerInfo().getLeaderInfo().getId().getId())
        .toString();
    LOG.info("Received install snapshot notification from OM leader: {} with " +
        "term index: {}", leaderNodeId, firstLogTermIdx);
    return CompletableFuture.supplyAsync(() -> ozoneManager.installSnapshotFromLeader(leaderNodeId),
        installSnapshotExecutor);
  }


  /**
   * To apply the transaction we
   * @param txn
   * @return
   */
  @Override
  public CompletableFuture<Message> applyTransaction(TransactionContext txn) {
    try {
      final OMRequest request = OMRatisHelper.convertByteStringToOMRequest(
          txn.getStateMachineLogEntry().getLogData());

      final TermIndex termIndex = TermIndex.valueOf(txn.getLogEntry());


      //reconTransaction.flush

      return CompletableFuture.supplyAsync(() -> addRequestToTransactions(request, termIndex))
    } catch (Exception e) {
      return completeExceptionally(e);
    }
  }

  private OMResponse addRequestToTransactions(OMRequest request, TermIndex termIdx) {
    try {
      ExecutionContext context = ExecutionContext.of(termIdx.getIndex());
    }
  }

  private Message processResponse(OMResponse omResponse) {
    // For successful response and non-critical errors, convert the response.
    return
  }

  private static <T> CompletableFuture<T> completeExceptionally(Exception e) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    future.completeExceptionally(e);
    return future;
  }

}
