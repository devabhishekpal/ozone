package org.apache.hadoop.ozone.recon;

import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

public class ReconRatisServer {
  private static final Logger LOG = LoggerFactory.getLogger(ReconRatisServer.class);

  private final int port;
  private final RaftServer server;
  private final Supplier<RaftServer.Division> serverDivision;
  private final RaftGroupId raftGroupId;
  private final RaftGroup raftGroup;
  private final RaftPeerId raftPeerId;
  private final Map<String, RaftPeer> raftPeerMap;


}
