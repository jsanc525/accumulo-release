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
package org.apache.accumulo.test.merkle.cli;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.accumulo.core.cli.ClientOpts;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;

import com.beust.jcommander.Parameter;

/**
 * 
 */
public class ManualComparison {

  public static class ManualComparisonOpts extends ClientOpts {
    @Parameter(names={"--table1"}, required = true, description = "First table")
    public String table1;
    
    @Parameter(names={"--table2"}, required = true, description = "First table")
    public String table2;
  }

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    ManualComparisonOpts opts = new ManualComparisonOpts();
    opts.parseArgs("ManualComparison", args);

    Connector conn = opts.getConnector();

    Scanner s1 = conn.createScanner(opts.table1, Authorizations.EMPTY), s2 = conn.createScanner(opts.table2, Authorizations.EMPTY);
    Iterator<Entry<Key,Value>> iter1 = s1.iterator(), iter2 = s2.iterator();
    boolean incrementFirst = true, incrementSecond = true;
    
    Entry<Key,Value> entry1 = iter1.next(), entry2 = iter2.next();
    while (iter1.hasNext() && iter2.hasNext()) {
      if (incrementFirst) {
        entry1 = iter1.next();
      }
      if (incrementSecond) {
        entry2 = iter2.next();
      }
      incrementFirst = false;
      incrementSecond = false;
      Key k1 = entry1.getKey(), k2 = entry2.getKey();
      Value v1 = entry1.getValue(), v2 = entry2.getValue();

      if (!k1.equals(k2) || !v1.equals(v2)) {
        if (k1.compareTo(k2) < 0) {
          System.out.println("Exist in original " + entry1);
          incrementFirst = true;
        } else if (k2.compareTo(k1) < 0) {
          System.out.println("Exist in replica " + entry2);
          incrementSecond = true;
        } else {
          System.out.println("Differ... [" + entry1 + "] [" + entry2 + "], keys equal=" + k1.equals(k2) + ", values equal=" + v1.equals(v2));
          incrementFirst = true;
          incrementSecond = true;
        }
      } else {
        incrementFirst = true;
        incrementSecond = true;
      }
    }

    System.out.println("\nExtra entries from " + opts.table1);
    while (iter1.hasNext()) {
      System.out.println(iter1.next());
    }

    System.out.println("\nExtra entries from " + opts.table2);
    while (iter2.hasNext()) {
      System.out.println(iter2.next());
    }
  }
}
