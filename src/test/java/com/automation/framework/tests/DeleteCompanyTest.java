package com.automation.framework.tests;

import com.automation.framework.base.BaseTest;
import com.automation.framework.config.ConfigManager;
import com.automation.framework.listeners.TestListener;
import com.automation.framework.pages.CompanyPage;
import com.automation.framework.pages.LoginPage;
import com.automation.framework.utils.JsonDataUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteCompanyTest extends BaseTest {
    @DataProvider(name = "deleteCompanyData")
    public Object[][] deleteCompanyDataProvider() throws IOException {
        List<Map<String, String>> data = JsonDataUtil.readJsonData();
        List<Object[]> rows = new ArrayList<>();

        for (Map<String, String> item : data) {
            if ("create_company".equalsIgnoreCase(item.get("testCaseName"))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String name = item.get("name") + "_Delete_" + timestamp;
                String email = item.get("email").replace("@", "+" + timestamp + "@");

                rows.add(new Object[]{
                        name, item.get("website"), item.get("phone"), email, item.get("description"),
                        item.get("industry"), item.get("employees"), item.get("stockSymbol"),
                        item.get("annualRevenue"), item.get("status"), item.get("source")
                });
            }
        }

        return rows.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "deleteCompanyData")
    public void createDeleteAndVerifyCompany(String name, String website, String phone, String email,
                                             String description, String industry, String employees,
                                             String stockSymbol, String annualRevenue, String status, String source) {
        LoginPage loginPage = new LoginPage(getDriver());
        TestListener.logStep("Open the login page");
        loginPage.open();
        TestListener.logStep("Log in with configured credentials");
        loginPage.login(ConfigManager.getUsername(), ConfigManager.getDecodedPassword());
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should be successful before company deletion");

        CompanyPage companyPage = new CompanyPage(getDriver());
        TestListener.logStep("Open the Companies page");
        companyPage.openCompaniesPage();
        TestListener.logStep("Create a uniquely named company to delete");
        companyPage.createCompany(name, website, phone, email, description, industry, employees,
                stockSymbol, annualRevenue, status, source);

        TestListener.logStep("Verify the company exists before deletion");
        Assert.assertTrue(companyPage.isCompanyNameVisible(name),
                "Company should be present before deletion: " + name);
        TestListener.logStep("Delete the company and verify it is no longer visible");
        captureScreenshot("company_deleted");//before deleting the company.
        Assert.assertTrue(companyPage.deleteCompanyAndVerify(name),
                "Deleted company should no longer be visible: " + name);
        TestListener.logStep("Capture a screenshot after company deletion is confirmed");
        captureScreenshot("company_deleted");
    }
}
