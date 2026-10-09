package com.automation.framework.utils;

import com.automation.framework.config.ConfigManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ScreenshotUtil {
    private ScreenshotUtil() {}

    public static String captureScreenshot(WebDriver driver, String screenshotName) {
        Path screenshotDirectory = Path.of(ConfigManager.getScreenshotPath());
        try {
            Files.createDirectories(screenshotDirectory);
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String safeName = screenshotName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path screenshotFile = screenshotDirectory.resolve(
                    safeName + "_" + timestamp + "_" + UUID.randomUUID() + ".png");
            Files.copy(srcFile.toPath(), screenshotFile);
            return screenshotFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to capture screenshot", e);
        }
    }
}
