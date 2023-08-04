package org.example;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import io.restassured.RestAssured;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class TestReport implements ITestListener {
    // ExtentReports variables
    private ExtentReports extent;
    private ExtentTest test;

    //... Other test methods as before

    @BeforeSuite
    public void setUp() {
        RestAssured.baseURI = "https://run.mocky.io"; // API base URL

        // Set up ExtentReports
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("test-output/ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @AfterSuite
    public void tearDown() {
        // Clean up ExtentReports resources
        extent.flush();
    }

    // ... Other methods as before

    // Utility method to log test results to ExtentReports
    private void logTestResult(Status status, String message) {
        if (test == null) {
            test = extent.createTest("API Test");
        }
        test.log(status, message);
    }
}
