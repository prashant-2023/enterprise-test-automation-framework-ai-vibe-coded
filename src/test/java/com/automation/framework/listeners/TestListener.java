package com.automation.framework.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TestListener implements ITestListener {
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    public static void attachScreenshot(String imagePath) {
        ExtentTest test = currentTest.get();
        File sourceFile = imagePath == null ? null : new File(imagePath);
        if (test == null || sourceFile == null || !sourceFile.exists()) {
            return;
        }

        try {
            Path reportScreenshots = Path.of("target", "extent-reports", "screenshots");
            Files.createDirectories(reportScreenshots);
            Path reportImage = reportScreenshots.resolve(sourceFile.getName());
            Files.copy(sourceFile.toPath(), reportImage, StandardCopyOption.REPLACE_EXISTING);
            test.addScreenCaptureFromPath(Path.of("screenshots", sourceFile.getName()).toString());
        } catch (IOException e) {
            test.fail("Unable to attach screenshot: " + e.getMessage());
        }
    }

    static {
        String reportPath = "target/extent-reports/extent-report.html";
        File reportFile = new File(reportPath);
        reportFile.getParentFile().mkdirs();

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
    }

    @Override
    public void onTestStart(ITestResult result) {
        currentTest.set(extent.createTest(result.getMethod().getMethodName()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = currentTest.get();
        test.log(Status.PASS, "Test passed");
        addScreenshotIfExists(result, Status.PASS);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = currentTest.get();
        test.log(Status.FAIL, result.getThrowable());
        addScreenshotIfExists(result, Status.FAIL);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = currentTest.get();
        test.log(Status.SKIP, "Test skipped");
    }

    private void addScreenshotIfExists(ITestResult result, Status status) {
        String screenshotPath = "target/screenshots/" + result.getName() + "_" + result.getEndMillis() + ".png";
        File screenshotFile = new File(screenshotPath);
        if (screenshotFile.exists()) {
            ExtentTest test = currentTest.get();
            test.log(status, "Screenshot: " + screenshotFile.getAbsolutePath());
            attachScreenshot(screenshotFile.getAbsolutePath());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
