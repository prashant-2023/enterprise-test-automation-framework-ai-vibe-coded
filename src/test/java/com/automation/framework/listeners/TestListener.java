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
import java.util.List;
import java.util.Map;

public class TestListener implements ITestListener {
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();
    private static final Map<String, List<String>> DATA_PARAMETER_LABELS = Map.of(
            "createNewContactAndVerify", List.of(
                    "First Name", "Last Name", "Middle Name", "Category", "Status", "Email",
                    "Phone Number", "Street Address", "City", "State", "Postal Code", "Country"),
            "createDeleteAndVerifyContact", List.of(
                    "First Name", "Last Name", "Middle Name", "Category", "Status", "Email",
                    "Phone Number", "Street Address", "City", "State", "Postal Code", "Country"),
            "createNewCompanyAndVerify", List.of(
                    "Name", "Website", "Phone", "Email", "Description", "Industry",
                    "Num. of Employees", "Stock Symbol", "Annual Revenue", "Status", "Source"),
            "createDeleteAndVerifyCompany", List.of(
                    "Name", "Website", "Phone", "Email", "Description", "Industry",
                    "Num. of Employees", "Stock Symbol", "Annual Revenue", "Status", "Source"),
            "createNewDealAndVerify", List.of("Name", "Amount", "Description"),
            "createDeleteAndVerifyDeal", List.of("Name", "Amount", "Description")
    );

    public static void attachScreenshot(String imagePath) {
        ExtentTest test = currentTest.get();
        File sourceFile = imagePath == null ? null : new File(imagePath);
        if (test == null) {
            return;
        }
        if (sourceFile == null || !sourceFile.isFile()) {
            test.warning("Screenshot was not attached because the file does not exist: " + imagePath);
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

    public static void logStep(String step) {
        ExtentTest test = currentTest.get();
        if (test != null) {
            test.info(step);
        }
    }

    public static void addFailureArtifacts(String currentUrl, String screenshotPath, Throwable failure) {
        ExtentTest test = currentTest.get();
        if (test == null) {
            return;
        }

        test.fail("Failure URL: " + currentUrl);
        if (failure != null) {
            test.fail("Exception: " + failure.getClass().getName() + ": " + failure.getMessage());
        }
        if (screenshotPath != null) {
            attachScreenshot(screenshotPath);
        }
    }

    public static void logArtifactCaptureFailure(String description, RuntimeException failure) {
        ExtentTest test = currentTest.get();
        if (test != null) {
            test.warning(description + ": " + failure.getMessage());
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
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        currentTest.set(test);

        Object[] parameters = result.getParameters();
        List<String> labels = DATA_PARAMETER_LABELS.get(result.getMethod().getMethodName());
        if (parameters.length > 0 && labels != null) {
            ExtentTest testData = test.createNode("Test Data");
            for (int i = 0; i < parameters.length; i++) {
                String label = i < labels.size() ? labels.get(i) : "Parameter " + (i + 1);
                testData.info(label + ": " + String.valueOf(parameters[i]));
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = currentTest.get();
        test.log(Status.PASS, "Test passed");
        currentTest.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = currentTest.get();
        test.log(Status.FAIL, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = currentTest.get();
        test.log(Status.SKIP, "Test skipped");
        currentTest.remove();
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
