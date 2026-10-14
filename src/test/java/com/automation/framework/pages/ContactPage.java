package com.automation.framework.pages;

import com.automation.framework.utils.SeleniumFormUtil;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ContactPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By contactsNav = By.xpath("//a[contains(@href,'/contacts') and normalize-space()='Contacts']");
    private final By createButton = By.xpath("//button[normalize-space()='Create']");
    private final By firstNameField = By.id("first-name");
    private final By lastNameField = By.id("last-name");
    private final By middleNameField = By.id("middle-name(s)");
    private final By categoryDropdown = By.id("category");
    private final By statusDropdown = By.id("status");
    private final By emailField = By.xpath("//input[@placeholder='Email']");
    private final By phoneField = By.xpath("//input[@placeholder='Phone number']");
    private final By streetAddressField = By.xpath("//input[@placeholder='Street address']");
    private final By cityField = By.xpath("//input[@placeholder='City']");
    private final By stateField = By.xpath("//input[@placeholder='State / Province']");
    private final By postalCodeField = By.xpath("//input[@placeholder='Postal code']");
    private final By countryDropdown = By.xpath("//input[@placeholder='Postal code']/following-sibling::select");
    private final By saveButton = By.xpath("//button[normalize-space()='Save']");
    private final By deleteButton = By.xpath(
            "//*[self::button or self::a or @role='button'][" +
                    "contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delete') " +
                    "or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delete') " +
                    "or contains(translate(@data-tooltip,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delete')]");
    private final By deleteConfirmationButton = By.xpath(
            "//*[@role='dialog']//button[normalize-space()='Delete' or normalize-space()='Confirm' or normalize-space()='Yes']" +
                    " | //div[contains(@class,'modal')]//button[normalize-space()='Delete' or normalize-space()='Confirm' or normalize-space()='Yes']");
    private final By deletionConfirmation = By.xpath(
            "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'contact deleted')]");
    private final By successBanner = By.xpath("//*[contains(text(),'Contact created')]");
   // private final By countRecord = By.xpath("//span[text()='35 records']");

    public ContactPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void openContactsPage() {
        driver.get("https://ui.freecrm.com/contacts");
        wait.until(ExpectedConditions.visibilityOfElementLocated(createButton));
    }

    public void createContact(String firstName, String lastName, String middleName, String category, String status,
                             String email, String phoneNumber, String streetAddress, String city,
                             String state, String postalCode, String country) {
        wait.until(ExpectedConditions.elementToBeClickable(createButton)).click();
        SeleniumFormUtil.fillRequired(wait, firstNameField, firstName);
        SeleniumFormUtil.fillRequired(wait, lastNameField, lastName);
        SeleniumFormUtil.fillRequired(wait, middleNameField, middleName);

        SeleniumFormUtil.selectRequired(wait, categoryDropdown, category);
        SeleniumFormUtil.selectRequired(wait, statusDropdown, status);

        SeleniumFormUtil.fillRequired(wait, emailField, email);
        SeleniumFormUtil.fillRequired(wait, phoneField, phoneNumber);

        SeleniumFormUtil.fillIfPresent(driver, wait, streetAddressField, streetAddress);
        SeleniumFormUtil.fillIfPresent(driver, wait, cityField, city);
        SeleniumFormUtil.fillIfPresent(driver, wait, stateField, state);
        SeleniumFormUtil.fillIfPresent(driver, wait, postalCodeField, postalCode);
        SeleniumFormUtil.selectIfPresent(driver, wait, countryDropdown, country);
       //acceptAlertIfPresent();
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
       // wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//span[contains(text(), 'records')]"))));
    }
/*
    private void acceptAlertIfPresent() {
        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (NoAlertPresentException ignored) {
            // No alert is present; continue with the form submission.
        }
    }
*/
    public boolean isContactNameVisible(String firstName, String middleName, String lastName) {
        String fullName = firstName + " " + middleName + " " + lastName;
        By contactName = By.xpath("//*[normalize-space(.)="
                + SeleniumFormUtil.xpathLiteral(fullName) + "]//h1");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(contactName)).isDisplayed();
    }

    public boolean deleteContactAndVerify(String firstName, String middleName, String lastName) {
        String fullName = firstName + " " + middleName + " " + lastName;
        By contactName = By.xpath("//*[normalize-space(.)="
                + SeleniumFormUtil.xpathLiteral(fullName) + "]//h1");
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
        confirmDeletion();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(deletionConfirmation),
                ExpectedConditions.invisibilityOfElementLocated(contactName)
        ));
        return driver.findElements(contactName).stream().noneMatch(WebElement::isDisplayed);
    }

    private void confirmDeletion() {
        wait.until(currentDriver -> {
            try {
                Alert alert = currentDriver.switchTo().alert();
                alert.accept();
                return true;
            } catch (NoAlertPresentException ignored) {
                List<WebElement> buttons = currentDriver.findElements(deleteConfirmationButton);
                for (WebElement button : buttons) {
                    if (button.isDisplayed() && button.isEnabled()) {
                        button.click();
                        return true;
                    }
                }
                return !currentDriver.findElements(deletionConfirmation).isEmpty();
            }
        });
    }
}
