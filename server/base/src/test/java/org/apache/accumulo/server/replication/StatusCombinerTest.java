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
package org.apache.accumulo.server.replication;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.Combiner;
import org.apache.accumulo.core.iterators.DevNull;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.IteratorUtil.IteratorScope;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.replication.ReplicationSchema.StatusSection;
import org.apache.accumulo.core.replication.StatusUtil;
import org.apache.accumulo.core.replication.proto.Replication.Status;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * 
 */
public class StatusCombinerTest {

  private StatusCombiner combiner;
  private Key key;
  private Status.Builder builder;

  @Before
  public void initCombiner() throws IOException {
    key = new Key();
    combiner = new StatusCombiner();
    builder = Status.newBuilder();
    combiner.init(new DevNull(), ImmutableMap.of(Combiner.COLUMNS_OPTION, StatusSection.NAME.toString()), new IteratorEnvironment() {

      public AccumuloConfiguration getConfig() {
        return null;
      }

      public IteratorScope getIteratorScope() {
        return null;
      }

      public boolean isFullMajorCompaction() {
        return false;
      }

      public void registerSideChannel(SortedKeyValueIterator<Key,Value> arg0) {

      }

      public SortedKeyValueIterator<Key,Value> reserveMapFileReader(String arg0) throws IOException {
        return null;
      }
    });
  }

  @Test
  public void returnsSameObject() {
    Status status = StatusUtil.ingestedUntil(10);
    // When combining only one message, we should get back the same instance
    Status ret = combiner.typedReduce(key, Collections.singleton(status).iterator());
    Assert.assertEquals(status, ret);
    Assert.assertTrue(status == ret);
  }

  @Test
  public void newStatusWithNewIngest() {
    Status orig = StatusUtil.newFile();
    Status status = StatusUtil.replicatedAndIngested(10, 20);
    Status ret = combiner.typedReduce(key, Arrays.asList(orig, status).iterator());
    Assert.assertEquals(10l, ret.getBegin());
    Assert.assertEquals(20l, ret.getEnd());
    Assert.assertEquals(false, ret.getClosed());
  }

  @Test
  public void newStatusWithNewIngestSingleBuilder() {
    Status orig = StatusUtil.newFile();
    Status status = StatusUtil.replicatedAndIngested(builder, 10, 20);
    Status ret = combiner.typedReduce(key, Arrays.asList(orig, status).iterator());
    Assert.assertEquals(10l, ret.getBegin());
    Assert.assertEquals(20l, ret.getEnd());
    Assert.assertEquals(false, ret.getClosed());
  }

  @Test
  public void commutativeNewFile() {
    Status newFile = StatusUtil.newFile(), firstSync = StatusUtil.ingestedUntil(100), secondSync = StatusUtil.ingestedUntil(200);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, firstSync, secondSync).iterator()), order2 = combiner.typedReduce(key,
        Arrays.asList(secondSync, firstSync, newFile).iterator());

    Assert.assertEquals(order1, order2);
  }

  @Test
  public void commutativeNewFileSingleBuilder() {
    Status newFile = StatusUtil.newFile(), firstSync = StatusUtil.ingestedUntil(builder, 100), secondSync = StatusUtil.ingestedUntil(builder, 200);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, firstSync, secondSync).iterator()), order2 = combiner.typedReduce(key,
        Arrays.asList(secondSync, firstSync, newFile).iterator());

    Assert.assertEquals(order1, order2);
  }

  @Test
  public void commutativeNewUpdates() {
    Status newFile = StatusUtil.newFile(), firstSync = StatusUtil.ingestedUntil(100), secondSync = StatusUtil.ingestedUntil(200);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, firstSync, secondSync).iterator()), order2 = combiner.typedReduce(key,
        Arrays.asList(newFile, secondSync, firstSync).iterator());

    Assert.assertEquals(order1, order2);
  }

  @Test
  public void commutativeNewUpdatesSingleBuilder() {
    Status newFile = StatusUtil.newFile(), firstSync = StatusUtil.ingestedUntil(builder, 100), secondSync = StatusUtil.ingestedUntil(builder, 200);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, firstSync, secondSync).iterator()), order2 = combiner.typedReduce(key,
        Arrays.asList(newFile, secondSync, firstSync).iterator());

    Assert.assertEquals(order1, order2);
  }

  @Test
  public void commutativeWithClose() {
    Status newFile = StatusUtil.newFile(), closed = StatusUtil.fileClosed(System.currentTimeMillis()), secondSync = StatusUtil.ingestedUntil(200);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, closed, secondSync).iterator()), order2 = combiner.typedReduce(key,
        Arrays.asList(newFile, secondSync, closed).iterator());

    Assert.assertEquals(order1, order2);
  }

  @Test
  public void commutativeWithCloseSingleBuilder() {
    Status newFile = StatusUtil.newFile(), closed = StatusUtil.fileClosed(System.currentTimeMillis()), secondSync = StatusUtil.ingestedUntil(builder, 200);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, closed, secondSync).iterator()), order2 = combiner.typedReduce(key,
        Arrays.asList(newFile, secondSync, closed).iterator());

    Assert.assertEquals(order1, order2);
  }

  @Test
  public void commutativeWithMultipleUpdates() {
    Status newFile = StatusUtil.newFile(), update1 = StatusUtil.ingestedUntil(100), update2 = StatusUtil.ingestedUntil(200), repl1 = StatusUtil.replicated(50), repl2 = StatusUtil
        .replicated(150);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, update1, repl1, update2, repl2).iterator());

    // Got all replication updates before ingest updates
    Status permutation = combiner.typedReduce(key, Arrays.asList(newFile, repl1, update1, repl2, update2).iterator());

    Assert.assertEquals(order1, permutation);

    // All replications before updates
    permutation = combiner.typedReduce(key, Arrays.asList(newFile, repl1, repl2, update1, update2).iterator());

    Assert.assertEquals(order1, permutation);

    // All updates before replications
    permutation = combiner.typedReduce(key, Arrays.asList(newFile, update1, update2, repl1, repl2, update1, update2).iterator());

    Assert.assertEquals(order1, permutation);
  }

  @Test
  public void commutativeWithMultipleUpdatesSingleBuilder() {
    Status newFile = StatusUtil.newFile(), update1 = StatusUtil.ingestedUntil(builder, 100), update2 = StatusUtil.ingestedUntil(builder, 200), repl1 = StatusUtil
        .replicated(builder, 50), repl2 = StatusUtil.replicated(builder, 150);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, update1, repl1, update2, repl2).iterator());

    // Got all replication updates before ingest updates
    Status permutation = combiner.typedReduce(key, Arrays.asList(newFile, repl1, update1, repl2, update2).iterator());

    Assert.assertEquals(order1, permutation);

    // All replications before updates
    permutation = combiner.typedReduce(key, Arrays.asList(newFile, repl1, repl2, update1, update2).iterator());

    Assert.assertEquals(order1, permutation);

    // All updates before replications
    permutation = combiner.typedReduce(key, Arrays.asList(newFile, update1, update2, repl1, repl2).iterator());

    Assert.assertEquals(order1, permutation);
  }

  @Test
  public void duplicateStatuses() {
    Status newFile = StatusUtil.newFile(), update1 = StatusUtil.ingestedUntil(builder, 100), update2 = StatusUtil.ingestedUntil(builder, 200), repl1 = StatusUtil
        .replicated(builder, 50), repl2 = StatusUtil.replicated(builder, 150);

    Status order1 = combiner.typedReduce(key, Arrays.asList(newFile, update1, repl1, update2, repl2).iterator());

    // Repeat the same thing more than once
    Status permutation = combiner.typedReduce(key, Arrays.asList(newFile, repl1, update1, update1, repl2, update2, update2).iterator());

    Assert.assertEquals(order1, permutation);
  }
}
