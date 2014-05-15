/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.minicluster;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.util.UtilWaitThread;
import org.apache.accumulo.server.gc.SimpleGarbageCollector;
import org.apache.accumulo.server.master.Master;
import org.apache.accumulo.server.tabletserver.TabletServer;
import org.apache.accumulo.server.util.Initialize;
import org.apache.accumulo.server.util.PortUtils;
import org.apache.accumulo.server.util.time.SimpleTimer;
import org.apache.accumulo.start.Main;
import org.apache.log4j.Logger;
import org.apache.zookeeper.server.ZooKeeperServerMain;

/**
 * A utility class that will create Zookeeper and Accumulo processes that write all of their data to a single local directory. This class makes it easy to test
 * code against a real Accumulo instance. Its much more accurate for testing than {@link org.apache.accumulo.core.client.mock.MockAccumulo}, but much slower.
 * 
 * @since 1.5.0
 */
public class MiniAccumuloCluster {

  private MiniAccumuloClusterImpl impl;

  private MiniAccumuloCluster(MiniAccumuloConfigImpl config) throws IOException {
    impl = new MiniAccumuloClusterImpl(config);
  }

  /**
   * 
   * @param dir
   *          An empty or nonexistant temp directoy that Accumulo and Zookeeper can store data in. Creating the directory is left to the user. Java 7, Guava,
   *          and Junit provide methods for creating temporary directories.
   * @param rootPassword
   *          Initial root password for instance.
   */
  public MiniAccumuloCluster(File dir, String rootPassword) throws IOException {
    this(new MiniAccumuloConfigImpl(dir, rootPassword));
  }

  /**
   * @param config
   *          initial configuration
   */
  public MiniAccumuloCluster(MiniAccumuloConfig config) throws IOException {
    this(config.getImpl());
  }

  /**
   * Starts Accumulo and Zookeeper processes. Can only be called once.
   * 
   * @throws IllegalStateException
   *           if already started
   */
  public void start() throws IOException, InterruptedException {
    impl.start();
  }

  /**
   * @return generated remote debug ports if in debug mode.
   * @since 1.6.0
   */
  public Set<Pair<ServerType,Integer>> getDebugPorts() {
    return impl.getDebugPorts();
  }

  /**
   * @return Accumulo instance name
   */
  public String getInstanceName() {
    return impl.getInstanceName();
  }

  /**
   * @return zookeeper connection string
   */
  public String getZooKeepers() {
    return impl.getZooKeepers();
  }

  /**
   * Stops Accumulo and Zookeeper processes. If stop is not called, there is a shutdown hook that is setup to kill the processes. However its probably best to
   * call stop in a finally block as soon as possible.
   */
  public void stop() throws IOException, InterruptedException {
    impl.stop();
  }

  /**
   * @since 1.6.0
   */
  public MiniAccumuloConfig getConfig() {
    return new MiniAccumuloConfig(impl.getConfig());
  }

  /**
   * Utility method to get a connector to the MAC.
   * 
   * @since 1.6.0
   */
  public Connector getConnector(String user, String passwd) throws AccumuloException, AccumuloSecurityException {
    return impl.getConnector(user, passwd);
  }

  /**
   * @since 1.6.0
   */
  public ClientConfiguration getClientConfig() {
    return impl.getClientConfig();
  }
}
