package com.automation.framework.tests;

import com.automation.framework.base.BaseTest;
import com.automation.framework.config.ConfigManager;
import com.automation.framework.listeners.TestListener;
import com.automation.framework.pages.CompanyPage;
import com.automation.framework.pages.LoginPage;
import com.automation.framework.utils.JsonDataUtil;
import com.automation.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateCompanyTest extends BaseTest {
    private final Logger log = LoggerUtil.getLogger(CreateCompanyTest.class);

    @DataProvider(name = "createCompanyData")
    public Object[][] createCompanyDataProvider() throws IOException {
        List<Map<String, String>> data = JsonDataUtil.readJsonData();
        List<Object[]> rows = new ArrayList<>();

        for (Map<String, String> item : data) {
            if ("create_company".equalsIgnoreCase(item.get("testCaseName"))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String companyName = item.get("name") + "_" + timestamp;
                String email = item.get("email").replace("@", "+" + timestamp + "@");

                rows.add(new Object[]{
                        companyName,
                        item.get("website"),
                        item.get("phone"),
                        email,
                        item.get("description"),
                        item.get("industry"),
                        item.get("employees"),
                        item.get("stockSymbol"),
                        item.get("annualRevenue"),
                        item.get("status"),
                        item.get("source")
                });
            }
        }

        return rows.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "createCompanyData")
    public void createNewCompanyAndVerify(String name, String website, String phone, String email,
                                          String description, String industry, String employees,
                                          String stockSymbol, String annualRevenue, String status, String source) {
        log.info("Logging in to FreeCRM");
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.open();
        loginPage.login(ConfigManager.getUsername(), ConfigManager.getDecodedPassword());
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should be successful");

        CompanyPage companyPage = new CompanyPage(getDriver());
        companyPage.openCompaniesPage();
        companyPage.createCompany(name, website, phone, email, description, industry, employees,
                stockSymbol, annualRevenue, status, source);

        Assert.assertTrue(companyPage.isCompanyNameVisible(name),
                "Created company name should be visible after saving: " + name);

        String screenshotPath = com.automation.framework.utils.ScreenshotUtil.captureScreenshot(
                getDriver(), "company_created");
        TestListener.attachScreenshot(screenshotPath);
        log.info("Company created successfully. Screenshot captured: {}", screenshotPath);
    }
}
