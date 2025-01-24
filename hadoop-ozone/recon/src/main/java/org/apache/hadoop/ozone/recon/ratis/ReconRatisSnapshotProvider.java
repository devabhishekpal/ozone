package org.apache.hadoop.ozone.recon.ratis;

import com.google.inject.Inject;
import org.apache.hadoop.hdds.conf.OzoneConfiguration;
import org.apache.hadoop.hdds.server.http.HttpConfig;
import org.apache.hadoop.hdds.utils.HAUtils;
import org.apache.hadoop.hdds.utils.LegacyHadoopConfigurationSource;
import org.apache.hadoop.hdds.utils.RDBSnapshotProvider;
import org.apache.hadoop.hdfs.web.URLConnectionFactory;
import org.apache.hadoop.ozone.om.helpers.OMNodeDetails;
import org.apache.hadoop.ozone.recon.ReconServerConfigKeys;
import org.apache.hadoop.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.hadoop.ozone.OzoneConsts.OM_DB_NAME;
import static org.apache.hadoop.ozone.om.OMConfigKeys.OZONE_OM_HTTP_AUTH_TYPE;
import static org.apache.hadoop.ozone.om.OMConfigKeys.OZONE_OM_SNAPSHOT_PROVIDER_CONNECTION_TIMEOUT_DEFAULT;
import static org.apache.hadoop.ozone.om.OMConfigKeys.OZONE_OM_SNAPSHOT_PROVIDER_CONNECTION_TIMEOUT_KEY;
import static org.apache.hadoop.ozone.om.OMConfigKeys.OZONE_OM_SNAPSHOT_PROVIDER_REQUEST_TIMEOUT_DEFAULT;
import static org.apache.hadoop.ozone.om.OMConfigKeys.OZONE_OM_SNAPSHOT_PROVIDER_REQUEST_TIMEOUT_KEY;
import static org.apache.hadoop.ozone.om.ratis_snapshot.OmRatisSnapshotProvider.writeFormData;
import static org.apache.hadoop.ozone.recon.ReconServerConfigKeys.OZONE_RECON_OM_SNAPSHOT_TASK_FLUSH_PARAM;

public class ReconRatisSnapshotProvider extends RDBSnapshotProvider {

  private static Logger LOG = LoggerFactory.getLogger(ReconRatisSnapshotProvider.class);

  private OzoneConfiguration configuration;
  private final Map<String, OMNodeDetails> peerNodesMap;
  private final HttpConfig.Policy httpPolicy;
  private final boolean spnegoEnabled;
  private final URLConnectionFactory connectionFactory;

  public ReconRatisSnapshotProvider(OzoneConfiguration configuration,
      File reconOmSnapshotDir, Map<String, OMNodeDetails> peerNodeDetails) {
    super(reconOmSnapshotDir, OM_DB_NAME);
    LOG.info("Initializing Recon's OM Snapshot Provider");
    this.peerNodesMap = new ConcurrentHashMap<>();
    peerNodesMap.putAll(peerNodeDetails);

    this.httpPolicy = HttpConfig.getHttpPolicy(configuration);
    this.spnegoEnabled = configuration.get(OZONE_OM_HTTP_AUTH_TYPE, "simple")
        .equals("kerberos");

    TimeUnit connectionTimeoutUnit = OZONE_OM_SNAPSHOT_PROVIDER_CONNECTION_TIMEOUT_DEFAULT.getUnit();
    int connectionTimeout = (int) configuration.getTimeDuration(
        OZONE_OM_SNAPSHOT_PROVIDER_CONNECTION_TIMEOUT_KEY,
        OZONE_OM_SNAPSHOT_PROVIDER_CONNECTION_TIMEOUT_DEFAULT.getDuration(),
        connectionTimeoutUnit
    );

    TimeUnit requestTimeoutUnit = OZONE_OM_SNAPSHOT_PROVIDER_REQUEST_TIMEOUT_DEFAULT.getUnit();
    int requestTimeout = (int) configuration.getTimeDuration(
        OZONE_OM_SNAPSHOT_PROVIDER_REQUEST_TIMEOUT_KEY,
        OZONE_OM_SNAPSHOT_PROVIDER_CONNECTION_TIMEOUT_DEFAULT.getDuration(),
        requestTimeoutUnit
    );

    connectionFactory = URLConnectionFactory.newDefaultURLConnectionFactory(connectionTimeout,
        requestTimeout, LegacyHadoopConfigurationSource.asHadoopConfiguration(configuration));
  }

  @Override
  public void downloadSnapshot(String leaderNodeID, File targetFile) throws IOException {
    OMNodeDetails leader = peerNodesMap.get(leaderNodeID);
    boolean shouldFlush = configuration.getBoolean(
        OZONE_RECON_OM_SNAPSHOT_TASK_FLUSH_PARAM,
        configuration.getBoolean(
            ReconServerConfigKeys.OZONE_RECON_OM_SNAPSHOT_TASK_FLUSH_PARAM,
            false)
    );
    URL omCheckpointUrl = leader.getOMDBCheckpointEndpointUrl(
        httpPolicy.isHttpEnabled(), shouldFlush
    );

    LOG.info("Downloading latest checkpoint from Leader OM {}. Checkpoint URL: {}", leaderNodeID, omCheckpointUrl);
    SecurityUtil.doAsLoginUser(() -> {
      HttpURLConnection connection = (HttpURLConnection)  connectionFactory.openConnection(
          omCheckpointUrl, spnegoEnabled);
      connection.setRequestMethod("POST");
      String contentType = "multipart/form-data; boundary=---XXX";
      connection.setRequestProperty("Content-Type", contentType);
      connection.setDoOutput(true);
      writeFormData(connection, HAUtils.getExistingSstFiles(getCandidateDir()));
      connection.connect();
      int errCode = connection.getResponseCode();
      if ((errCode != HTTP_OK) || (HTTP_CREATED != errCode)) {
        throw new IOException(
            "Encountered exception while trying to download latest checkpoint from: " +
            omCheckpointUrl + ". Error Code: " + errCode);
      }
    })
  }
}
