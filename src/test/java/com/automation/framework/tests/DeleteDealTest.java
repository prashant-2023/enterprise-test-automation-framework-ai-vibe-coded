package com.automation.framework.tests;

import com.automation.framework.base.BaseTest;
import com.automation.framework.config.ConfigManager;
import com.automation.framework.listeners.TestListener;
import com.automation.framework.pages.DealPage;
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

public class DeleteDealTest extends BaseTest {
    @DataProvider(name = "deleteDealData")
    public Object[][] deleteDealDataProvider() throws IOException {
        List<Map<String, String>> data = JsonDataUtil.readJsonData();
        List<Object[]> rows = new ArrayList<>();

        for (Map<String, String> item : data) {
            if ("create_deal".equalsIgnoreCase(item.get("testCaseName"))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                rows.add(new Object[]{
                        item.get("name") + "_Delete_" + timestamp,
                        item.get("amount"),
                        item.get("description")
                });
            }
        }
        return rows.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "deleteDealData")
    public void createDeleteAndVerifyDeal(String name, String amount, String description) {
        LoginPage loginPage = new LoginPage(getDriver());
        TestListener.logStep("Open the login page");
        loginPage.open();
        TestListener.logStep("Log in with configured credentials");
        loginPage.login(ConfigManager.getUsername(), ConfigManager.getDecodedPassword());
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should be successful before deal deletion");

        DealPage dealPage = new DealPage(getDriver());
        TestListener.logStep("Open the Deals page");
        dealPage.openDealsPage();
        TestListener.logStep("Create a uniquely named deal to delete");
        dealPage.createDeal(name, amount, description);
        TestListener.logStep("Verify the deal exists before deletion");
        Assert.assertTrue(dealPage.isDealNameVisible(name), "Deal should exist before deletion: " + name);
        TestListener.logStep("Delete the deal and verify it is no longer visible");
        Assert.assertTrue(dealPage.deleteDealAndVerify(name), "Deleted deal should no longer be visible: " + name);
        TestListener.logStep("Capture a screenshot after deal deletion is confirmed");
        captureScreenshot("deal_deleted");
    }
}
