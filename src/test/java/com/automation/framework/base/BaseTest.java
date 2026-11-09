package com.automation.framework.base;

import com.automation.framework.config.ConfigManager;
import com.automation.framework.listeners.TestListener;
import com.automation.framework.pages.LoginPage;
import com.automation.framework.utils.LoggerUtil;
import com.automation.framework.utils.ScreenshotUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Listeners(TestListener.class)
public class BaseTest {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    protected Logger log;

    public WebDriver getDriver() {
        return DRIVER.get();
    }

    protected String captureScreenshot(String screenshotName) {
        String screenshotPath = ScreenshotUtil.captureScreenshot(getDriver(), screenshotName);
        TestListener.attachScreenshot(screenshotPath);
        log.info("Screenshot captured: {}", screenshotPath);
        return screenshotPath;
    }
    @Parameters ("browser")
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("chrome") String browser) {
        log = LoggerUtil.getLogger(this.getClass());
        WebDriver driver = initializeDriver(browser);
        DRIVER.set(driver);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getPageLoadTimeout()));
        driver.manage().window().maximize();
        //Login to the application
        log.info("Logging in to FreeCRM");
        LoginPage loginPage = new LoginPage(getDriver());
        TestListener.logStep("Open the login page");
        //loginPage.open();
        TestListener.logStep("Launching" + browser + " browser and navigating to the application URL");
        driver.get(ConfigManager.getBaseUrl());
        TestListener.logStep("Log in with configured credentials");
        loginPage.login(ConfigManager.getUsername(), ConfigManager.getDecodedPassword());
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should be successful");
        log.info("Application launched: {}", ConfigManager.getBaseUrl());
    }

    
    private WebDriver initializeDriver(String browser) {
        //browser = ConfigManager.getBrowser().toLowerCase();
        switch (browser) {
            case "chrome":
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
                if (ConfigManager.isRemote()) {
                    return createRemoteDriver(chromeOptions);
                }
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver(chromeOptions);
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (ConfigManager.isHeadless()) {
                    firefoxOptions.addArguments("-headless");
                }
                if (ConfigManager.isRemote()) {
                    return createRemoteDriver(firefoxOptions);
                }
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver(firefoxOptions);
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                if (ConfigManager.isRemote()) {
                    return createRemoteDriver(edgeOptions);
                }
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver(edgeOptions);
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    private WebDriver createRemoteDriver(Capabilities capabilities) {
        try {
            return new RemoteWebDriver(URI.create(ConfigManager.getRemoteUrl()).toURL(), capabilities);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid Selenium Grid URL: " + ConfigManager.getRemoteUrl(), e);
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
