package com.automation.framework.tests;

import com.automation.framework.base.BaseTest;
import com.automation.framework.config.ConfigManager;
import com.automation.framework.pages.LoginPage;
import com.automation.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    private final Logger log = LoggerUtil.getLogger(LoginTest.class);

    @Test
    public void validLoginFlow() {
        log.info("Running valid FreeCRM login flow");

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.open();
        loginPage.login(ConfigManager.getUsername(), ConfigManager.getDecodedPassword());

        Assert.assertTrue(loginPage.isLoginSuccessful(), "Valid login should redirect to dashboard");
    }
}
