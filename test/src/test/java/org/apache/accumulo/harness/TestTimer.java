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

import com.google.gson.Gson;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.System;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.accumulo.harness.TestCase;
import org.apache.accumulo.harness.TestTimer;
import org.apache.accumulo.harness.TestSuiteReport;
import org.apache.commons.io.FileUtils;
import org.junit.AssumptionViolatedException;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods, setup and/or infrastructure which are common to any enable Accumulo more granular test timing
 */
public class TestTimer extends TestWatcher {

   Class testClass;
   Long start;
   Long end;

   @Override
   protected void starting(Description description) {
        start = System.currentTimeMillis();
   }

   @Override
   protected void finished (Description description) {
        end = System.currentTimeMillis();
   }

   @Override
   protected void failed(Throwable e, Description description) {
       testClass = description.getTestClass();
       writeToTestJSON("failed",description.getDisplayName());
   }

   @Override
   protected void succeeded(Description description) {
       testClass = description.getTestClass();
       writeToTestJSON("pass", description.getDisplayName());
   }


   @Override
   protected void skipped(AssumptionViolatedException e, Description description) {
       testClass = description.getTestClass();
       writeToTestJSON("skipped", description.getDisplayName());
   }


   @Rule public TestName name = new TestName();

   private void writeToTestJSON(String testResult, String methodName) {
       TestCase testCase = new TestCase();

       // Get annotations for method and class
       Method method = null;
       ArrayList testAnnotations = new ArrayList();
       try {
           //String methodName = name.getMethodName();
           method = testClass.getMethod(methodName);
           Category category = method.getAnnotation(Category.class);
           if (category != null) {
               Class<?>[] annotations = category.value();
               for (Class annotation : annotations) {
                   testAnnotations.add(annotation.getSimpleName());
               }
           }

           Annotation annotation = testClass.getDeclaredAnnotation(Category.class);
           if (annotation != null) {
               String annotations = annotation.toString().substring(annotation.toString().indexOf("value=") + 7);
               annotations = annotations.substring(0, annotations.indexOf("]"));
               String[] allAnnotations = annotations.split(",");
               for (String classAnnotation : allAnnotations) {
                   testAnnotations.add(classAnnotation.substring(classAnnotation.lastIndexOf(".") + 1));
               }
           }
           testCase.setTestCaseAnnotations(testAnnotations);
           testCase.setTestSuiteName(testResult);
           testCase.setTestCaseName(methodName);
           saveInTimeDurationsJSON(testCase);
       } catch (NoSuchMethodException e) {
           e.printStackTrace();
       }
   }

    public void saveInTimeDurationsJSON(TestCase testCase) {
       File timeDurations = new File(new File(System.getProperty("user.dir"), "target"), "testTimeDurations.json");

       try {
           Gson gson = new Gson();       
           testCase.setTestCaseStartTime(new SimpleDateFormat("yyyy-mm-dd HH:mm:ss,SSS").format(start));
           testCase.setTestCaseEndTime(new SimpleDateFormat("yyyy-mm-dd HH:mm:ss,SSS").format(end));

           TestSuiteReport report = new TestSuiteReport();
           List<TestCase> testCases = new ArrayList<>();
           if (timeDurations.exists()) {
               String existingJSON = FileUtils.readFileToString(timeDurations);
               report = gson.fromJson(existingJSON, TestSuiteReport.class);
               testCases = report.getTestCases();
           }

           testCases.add(testCase);
           report.setTestCases(testCases);
           String testResult = gson.toJson(report);
           FileUtils.writeStringToFile(timeDurations, testResult);
       } catch (IOException e) {
           e.printStackTrace();
       }
    }

}
