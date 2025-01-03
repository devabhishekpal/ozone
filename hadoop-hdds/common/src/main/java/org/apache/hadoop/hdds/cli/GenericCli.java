/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.hadoop.hdds.cli;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdds.conf.OzoneConfiguration;

import com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.security.UserGroupInformation;
import picocli.CommandLine;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

/**
 * This is a generic parent class for all the ozone related cli tools.
 */
public class GenericCli implements Callable<Void>, GenericParentCommand {

  public static final int EXECUTION_ERROR_EXIT_CODE = -1;

  @Option(names = {"--verbose"},
      description = "More verbose output. Show the stack trace of the errors.")
  private boolean verbose;

  @Option(names = {"-D", "--set"})
  private Map<String, String> configurationOverrides = new HashMap<>();

  @Option(names = {"-conf"})
  private String configurationPath;

  private final CommandLine cmd;
  private OzoneConfiguration conf;
  private UserGroupInformation user;

  public GenericCli() {
    this(CommandLine.defaultFactory());
  }

  public GenericCli(CommandLine.IFactory factory) {
    cmd = new CommandLine(this, factory);
    cmd.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
      printError(ex);
      return EXECUTION_ERROR_EXIT_CODE;
    });

    ExtensibleParentCommand.addSubcommands(cmd);
  }

  /**
   * Handle the error when subcommand is required but not set.
   */
  public static void missingSubcommand(CommandSpec spec) {
    System.err.println("Incomplete command");
    spec.commandLine().usage(System.err);
    System.exit(EXECUTION_ERROR_EXIT_CODE);
  }

  public void run(String[] argv) {
    int exitCode = execute(argv);

    if (exitCode != ExitCode.OK) {
      System.exit(exitCode);
    }
  }

  @VisibleForTesting
  public int execute(String[] argv) {
    return cmd.execute(argv);
  }

  protected void printError(Throwable error) {
    //message could be null in case of NPE. This is unexpected so we can
    //print out the stack trace.
    if (verbose || error.getMessage() == null
        || error.getMessage().length() == 0) {
      error.printStackTrace(System.err);
    } else {
      System.err.println(error.getMessage().split("\n")[0]);
    }
  }

  @Override
  public Void call() throws Exception {
    throw new MissingSubcommandException(cmd);
  }

  public OzoneConfiguration createOzoneConfiguration() {
    OzoneConfiguration ozoneConf = new OzoneConfiguration();
    if (configurationPath != null) {
      ozoneConf.addResource(new Path(configurationPath));
    }
    if (configurationOverrides != null) {
      for (Entry<String, String> entry : configurationOverrides.entrySet()) {
        ozoneConf.set(entry.getKey(), entry.getValue());
      }
    }
    return ozoneConf;
  }

  @Override
  public OzoneConfiguration getOzoneConf() {
    if (conf == null) {
      conf = createOzoneConfiguration();
    }
    return conf;
  }

  public UserGroupInformation getUser() throws IOException {
    if (user == null) {
      user = UserGroupInformation.getCurrentUser();
    }
    return user;
  }

  @VisibleForTesting
  public picocli.CommandLine getCmd() {
    return cmd;
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }
}
