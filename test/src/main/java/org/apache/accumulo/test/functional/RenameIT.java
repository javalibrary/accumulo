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
package org.apache.accumulo.test.functional;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.clientImpl.ClientContext;
import org.apache.accumulo.harness.AccumuloClusterHarness;
import org.apache.accumulo.test.TestIngest;
import org.apache.accumulo.test.VerifyIngest;
import org.junit.Test;

public class RenameIT extends AccumuloClusterHarness {

  @Override
  protected int defaultTimeoutSeconds() {
    return 2 * 60;
  }

  @Test
  public void renameTest() throws Exception {
    String[] tableNames = getUniqueNames(2);
    String name1 = tableNames[0];
    String name2 = tableNames[1];
    TestIngest.Opts opts = new TestIngest.Opts();
    opts.createTable = true;
    opts.setTableName(name1);
    opts.setClientProperties(cluster.getClientProperties());

    try (AccumuloClient c = createAccumuloClient()) {
      TestIngest.ingest(c, opts);
      c.tableOperations().rename(name1, name2);
      TestIngest.ingest(c, opts);
      VerifyIngest.Opts vopts = new VerifyIngest.Opts();
      vopts.setClientProperties(cluster.getClientProperties());
      vopts.setTableName(name2);
      VerifyIngest.verifyIngest(c, vopts);
      c.tableOperations().delete(name1);
      c.tableOperations().rename(name2, name1);
      vopts.setTableName(name1);
      VerifyIngest.verifyIngest(c, vopts);

      FunctionalTestUtils.assertNoDanglingFateLocks((ClientContext) c, getCluster());
    }
  }

}
