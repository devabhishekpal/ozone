package org.apache.hadoop.ozone.reconv2;

import com.google.inject.Injector;
import org.apache.hadoop.hdds.cli.GenericCli;
import org.apache.hadoop.ozone.recon.ReconHttpServer;
import org.apache.hadoop.util.JvmPauseMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class ReconServer extends GenericCli implements Callable<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(ReconServer.class);
  private Injector injector;

  private JvmPauseMonitor jvmPauseMonitor;
  private ReconHttpServer reconHttpServer;
  private ReconDBProvider reconDBProvider;


}
