package com.automation.framework.base;

import com.automation.framework.config.ConfigManager;
import com.automation.framework.listeners.TestListener;
import com.automation.framework.utils.LoggerUtil;
import com.automation.framework.utils.ScreenshotUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Listeners(TestListener.class)
public class BaseTest {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    protected Logger log;

    protected WebDriver getDriver() {
        return DRIVER.get();
    }

    protected String captureScreenshot(String screenshotName) {
        String screenshotPath = ScreenshotUtil.captureScreenshot(getDriver(), screenshotName);
        TestListener.attachScreenshot(screenshotPath);
        log.info("Screenshot captured: {}", screenshotPath);
        return screenshotPath;
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        log = LoggerUtil.getLogger(this.getClass());
        WebDriver driver = initializeDriver();
        DRIVER.set(driver);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getPageLoadTimeout()));
        driver.manage().window().maximize();
        driver.get(ConfigManager.getBaseUrl());
        log.info("Application launched: {}", ConfigManager.getBaseUrl());
    }

    private WebDriver initializeDriver() {
        String browser = ConfigManager.getBrowser().toLowerCase();
        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
                chromeOptions.setExperimentalOption("useAutomationExtension", false);
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                chromeOptions.addArguments("--disable-features=AutomationControlled");

                Map<String, Object> prefs = new HashMap<>();
                prefs.put("credentials_enable_service", false);
                prefs.put("profile.password_manager_enabled", false);
                chromeOptions.setExperimentalOption("prefs", prefs);

                if (ConfigManager.isHeadless()) {
                    chromeOptions.addArguments("--headless=new");
                    chromeOptions.addArguments("--window-size=1920,1080");
                }
                return new ChromeDriver(chromeOptions);
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (ConfigManager.isHeadless()) {
                    firefoxOptions.addArguments("-headless");
                }
                return new FirefoxDriver(firefoxOptions);
            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        WebDriver driver = getDriver();

        if (result.getStatus() == ITestResult.FAILURE) {
            String currentUrl = "Unavailable";
            if (driver != null) {
                try {
                    currentUrl = driver.getCurrentUrl();
                } catch (WebDriverException e) {
                    log.error("Unable to retrieve the current URL for failed test {}", result.getName(), e);
                    TestListener.logArtifactCaptureFailure("Unable to retrieve current URL", e);
                }
            }

            String screenshotPath = null;
            if (driver != null) {
                try {
                    screenshotPath = ScreenshotUtil.captureScreenshot(driver, result.getName());
                    log.info("Failure screenshot captured: {}", screenshotPath);
                } catch (RuntimeException e) {
                    log.error("Unable to capture failure screenshot for test {}", result.getName(), e);
                    TestListener.logArtifactCaptureFailure("Unable to capture failure screenshot", e);
                }
            }
            TestListener.addFailureArtifacts(currentUrl, screenshotPath, result.getThrowable());
            log.error("Test failed: {}. Current URL: {}", result.getName(), currentUrl);
        }

        if (driver != null) {
            driver.quit();
            DRIVER.remove();
            log.info("Browser closed.");
        }
    }
}
