package com.automation.framework.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtil {
    private ScreenshotUtil() {}

    public static String captureScreenshot(WebDriver driver, String screenshotName) {
        String folderPath = "target/screenshots";
        try {
            Files.createDirectories(Paths.get(folderPath));
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = screenshotName + "_" + timestamp + ".png";
            String fullPath = folderPath + File.separator + fileName;
            FileUtils.copyFile(srcFile, new File(fullPath));
            return fullPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to capture screenshot", e);
        }
    }
}
