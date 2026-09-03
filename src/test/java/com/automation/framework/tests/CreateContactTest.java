package com.automation.framework.tests;

import com.automation.framework.base.BaseTest;
import com.automation.framework.config.ConfigManager;
import com.automation.framework.listeners.TestListener;
import com.automation.framework.pages.ContactPage;
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

public class CreateContactTest extends BaseTest {
    private final Logger log = LoggerUtil.getLogger(CreateContactTest.class);

    @DataProvider(name = "createContactData")
    public Object[][] createContactDataProvider() throws IOException {
        List<Map<String, String>> data = JsonDataUtil.readJsonData("src/test/resources/data/user-data.json");
        List<Object[]> rows = new ArrayList<>();

        for (Map<String, String> item : data) {
            if ("create_contact".equalsIgnoreCase(item.get("testCaseName"))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String dynamicFirstName = item.get("firstName") + "_" + timestamp;
                String dynamicLastName = item.get("lastName") + "_" + timestamp;
                String dynamicEmail = item.get("email").replace("@", "+" + timestamp + "@");
                String dynamicPhoneNumber = item.get("phoneNumber").substring(0, 7) + timestamp.substring(timestamp.length() - 3);

                rows.add(new Object[] {
                        dynamicFirstName,
                        dynamicLastName,
                        item.get("middleName"),
                        item.get("category"),
                        item.get("status"),
                        dynamicEmail,
                        dynamicPhoneNumber,
                        item.get("streetAddress"),
                        item.get("city"),
                        item.get("state"),
                        item.get("postalCode"),
                        item.get("country")
                });
            }
        }

        return rows.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "createContactData")
    public void createNewContactAndVerify(String firstName, String lastName, String middleName, String category,
                                         String status, String email, String phoneNumber, String streetAddress,
                                         String city, String state, String postalCode, String country) {
        log.info("Logging in to FreeCRM");
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.open();
        loginPage.login(ConfigManager.getUsername(), ConfigManager.getDecodedPassword());
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should be successful");

        log.info("Navigating to Contacts page");
        ContactPage contactPage = new ContactPage(getDriver());
        contactPage.openContactsPage();

        log.info("Creating a new contact with address details");
        contactPage.createContact(firstName, lastName, middleName, category, status, email, phoneNumber,
                streetAddress, city, state, postalCode, country);

        String screenshotPath = com.automation.framework.utils.ScreenshotUtil.captureScreenshot(getDriver(), "contact_created");
        TestListener.attachScreenshot(screenshotPath);
        log.info("Contact creation screenshot captured: {}", screenshotPath);

        boolean result = contactPage.isContactCreated(firstName, lastName);
        Assert.assertTrue(result, "Contact should be created successfully and visible after saving");
        TestListener.attachScreenshot(screenshotPath);
        log.info("Contact created successfully.");
    }
}
