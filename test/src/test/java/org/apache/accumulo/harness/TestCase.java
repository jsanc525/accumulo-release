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
package org.apache.accumulo.harness;

import java.util.ArrayList;
import java.util.List;
/**
*
* POJO to capture testcase attributes
*/
public class TestCase {

  private String testCaseStatus;
  private String testSuiteName;
  private String testCaseStartTime;
  private String testCaseEndTime;
  private String testCaseName;
  private List<String> testCaseAnnotations = new ArrayList<String>();

  public String getTestCaseName() {
    return testCaseName;
  }

  public void setTestCaseName(String testCaseName) {
    this.testCaseName = testCaseName;
  }

  /**
   *
   * @return The testCaseStatus
   */
  public String getTestCaseStatus() {
    return testCaseStatus;
  }

  /**
   *
   * @param testCaseStatus
   *          The testCaseStatus
   */
  public void setTestCaseStatus(String testCaseStatus) {
    this.testCaseStatus = testCaseStatus;
  }

  /**
   *
   * @return The testSuiteName
   */
  public String getTestSuiteName() {
    return testSuiteName;
  }

  /**
   *
   * @param testSuiteName
   *          The testSuiteName
   */
  public void setTestSuiteName(String testSuiteName) {
    this.testSuiteName = testSuiteName;
  }

  /**
   *
   * @return The testCaseStartTime
   */
  public String getTestCaseStartTime() {
    return testCaseStartTime;
  }

  /**
   *
   * @param testCaseStartTime
   *          The testCaseStartTime
   */
  public void setTestCaseStartTime(String testCaseStartTime) {
    this.testCaseStartTime = testCaseStartTime;
  }

  /**
   *
   * @return The testCaseEndTime
   */
  public String getTestCaseEndTime() {
    return testCaseEndTime;
  }

  /**
   *
   * @param testCaseEndTime
   *          The testCaseEndTime
   */
  public void setTestCaseEndTime(String testCaseEndTime) {
    this.testCaseEndTime = testCaseEndTime;
  }

  /**
   *
   * @return The testCaseAnnotations
   */
  public List<String> getTestCaseAnnotations() {
    return testCaseAnnotations;
  }

  /**
   *
   * @param testCaseAnnotations
   *          The testCaseAnnotations
   */
  public void setTestCaseAnnotations(List<String> testCaseAnnotations) {
    this.testCaseAnnotations = testCaseAnnotations;
  }

}
