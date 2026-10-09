package com.automation.framework.tests;

import com.automation.framework.base.BaseTest;
import com.automation.framework.config.ConfigManager;
import com.automation.framework.listeners.TestListener;
import com.automation.framework.pages.ContactPage;
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

public class DeleteContactTest extends BaseTest {
    @DataProvider(name = "deleteContactData")
    public Object[][] deleteContactDataProvider() throws IOException {
        List<Map<String, String>> data = JsonDataUtil.readJsonData();
        List<Object[]> rows = new ArrayList<>();

        for (Map<String, String> item : data) {
            if ("create_contact".equalsIgnoreCase(item.get("testCaseName"))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String firstName = item.get("firstName") + "_Delete_" + timestamp;
                String lastName = item.get("lastName") + "_" + timestamp;
                String email = item.get("email").replace("@", "+" + timestamp + "@");
                String phoneNumber = item.get("phoneNumber").substring(0, 7)
                        + timestamp.substring(timestamp.length() - 3);

                rows.add(new Object[]{
                        firstName, lastName, item.get("middleName"), item.get("category"), item.get("status"),
                        email, phoneNumber, item.get("streetAddress"), item.get("city"), item.get("state"),
                        item.get("postalCode"), item.get("country")
                });
            }
        }

        return rows.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "deleteContactData")
    public void createDeleteAndVerifyContact(String firstName, String lastName, String middleName, String category,
                                             String status, String email, String phoneNumber, String streetAddress,
                                             String city, String state, String postalCode, String country) {
        LoginPage loginPage = new LoginPage(getDriver());
        TestListener.logStep("Open the login page");
        loginPage.open();
        TestListener.logStep("Log in with configured credentials");
        loginPage.login(ConfigManager.getUsername(), ConfigManager.getDecodedPassword());
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should be successful before contact deletion");

        ContactPage contactPage = new ContactPage(getDriver());
        TestListener.logStep("Open the Contacts page");
        contactPage.openContactsPage();
        TestListener.logStep("Create a uniquely named contact to delete");
        contactPage.createContact(firstName, lastName, middleName, category, status, email, phoneNumber,
                streetAddress, city, state, postalCode, country);

        TestListener.logStep("Verify the contact exists before deletion");
        Assert.assertTrue(contactPage.isContactNameVisible(firstName, middleName, lastName),
                "Contact should be present before deletion");
        TestListener.logStep("Delete the contact and verify it is no longer visible");
        Assert.assertTrue(contactPage.deleteContactAndVerify(firstName, middleName, lastName),
                "Deleted contact should no longer be visible");
        TestListener.logStep("Capture a screenshot after contact deletion is confirmed");
        captureScreenshot("contact_deleted");
    }
}
