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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AssumptionViolatedException;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Methods, setup and/or infrastructure which are common to any enable Accumulo more granular test timing
 */
public class TestTimer extends TestWatcher {
  private static final Logger log = LoggerFactory.getLogger(TestTimer.class);
  private static final Gson gson = new Gson();

  private Class<?> testClass;
  private long start;
  private long end;

  @Override
  protected void starting(Description description) {
    testClass = description.getTestClass();
    start = System.currentTimeMillis();
  }

  @Override
  protected void finished(Description description) {
    // Finished is invoked after failed/succeeded/skipped are invoked
  }

  @Override
  protected void failed(Throwable e, Description description) {
    end = System.currentTimeMillis();
    StringWriter strWrtr = new StringWriter();
    PrintWriter prntWrtr = new PrintWriter(strWrtr);

    e.printStackTrace(prntWrtr);
    prntWrtr.close();
    writeToTestJSON("failed", description, strWrtr.toString());
  }

  @Override
  protected void succeeded(Description description) {
    end = System.currentTimeMillis();
    writeToTestJSON("pass", description, null);
  }

  @Override
  protected void skipped(AssumptionViolatedException e, Description description) {
    end = System.currentTimeMillis();
    writeToTestJSON("skipped", description, null);
  }

  private void writeToTestJSON(String testResult, Description description, String failureReason) {
    TestCase testCase = new TestCase();

    // Get annotations for method and class
    Method method = null;
    ArrayList<String> testAnnotations = new ArrayList<>();
    try {
      method = testClass.getMethod(description.getMethodName());
      Category category = method.getAnnotation(Category.class);
      if (category != null) {
        Class<?>[] annotations = category.value();
        for (Class<?> annotation : annotations) {
          testAnnotations.add(annotation.getSimpleName());
        }
      }

      Annotation annotation = testClass.getAnnotation(Category.class);
      if (annotation != null) {
        Category categoryAnnotation = (Category) annotation;
        Class<?>[] testInterfaces = categoryAnnotation.value();
        for (Class<?> testInterface : testInterfaces) {
          testAnnotations.add(testInterface.getSimpleName());
        }
      }
      testCase.setTestCaseAnnotations(testAnnotations);
      testCase.setTestSuiteName(description.getClassName());
      testCase.setTestCaseName(description.getMethodName());
      testCase.setTestCaseStatus(testResult);
      if (failureReason != null) {
        testCase.setTestFailureReason(failureReason);
      }
      saveInTimeDurationsJSON(testCase);
    } catch (Exception e) {
      log.warn("Failed to extract test case information", e);
    }
  }

  public void saveInTimeDurationsJSON(TestCase testCase) {
    File timeDurations = new File(new File(System.getProperty("user.dir"), "target"), "testTimeDurations.txt");

    try {
      testCase.setTestCaseStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(start));
      testCase.setTestCaseEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(end));

      TestSuiteReport report = new TestSuiteReport();
      List<TestCase> testCases = new ArrayList<>();

      testCases.add(testCase);
      report.setTestCases(testCases);
      String testResult = gson.toJson(report);

      FileUtils.writeStringToFile(timeDurations, testResult + "\n", true);
    } catch (IOException e) {
      log.warn("Failed to serialize test case to a file", e);
    }
  }
}
