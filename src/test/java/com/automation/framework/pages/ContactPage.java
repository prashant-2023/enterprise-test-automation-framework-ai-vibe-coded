package com.automation.framework.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

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
    private final By countryDropdown = By.xpath("//div[contains(.,'Country') and .//select]//select");
    private final By saveButton = By.xpath("//button[normalize-space()='Save']");
    private final By successBanner = By.xpath("//*[contains(text(),'Contact created')]");

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
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameField)).clear();
        driver.findElement(firstNameField).sendKeys(firstName);

        driver.findElement(lastNameField).clear();
        driver.findElement(lastNameField).sendKeys(lastName);

        WebElement middleNameElement = driver.findElement(middleNameField);
        middleNameElement.clear();
        middleNameElement.sendKeys(middleName);

        selectByVisibleText(categoryDropdown, category);
        selectByVisibleText(statusDropdown, status);

        WebElement emailElement = driver.findElement(emailField);
        emailElement.clear();
        emailElement.sendKeys(email);

        WebElement phoneElement = driver.findElement(phoneField);
        phoneElement.clear();
        phoneElement.sendKeys(phoneNumber);

        fillIfPresent(streetAddressField, streetAddress);
        fillIfPresent(cityField, city);
        fillIfPresent(stateField, state);
        fillIfPresent(postalCodeField, postalCode);
        selectIfPresent(countryDropdown, country);
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
    }

    private void selectByVisibleText(By selector, String visibleText) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
        Select select = new Select(element);

        for (WebElement option : select.getOptions()) {
            String optionText = option.getText();
            if (optionText.contains(visibleText)) {
                select.selectByVisibleText(optionText);
                return;
            }
        }

        select.selectByVisibleText(visibleText);
    }

    private void fillIfPresent(By selector, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        if (!driver.findElements(selector).isEmpty()) {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
            element.clear();
            element.sendKeys(value);
        }
    }

    private void selectIfPresent(By selector, String value) {
        if (value == null || value.trim().isEmpty() || driver.findElements(selector).isEmpty()) {
            return;
        }

        try {
            selectByVisibleText(selector, value);
        } catch (Exception e) {
            // Some CRM versions render country options using a different label or without the field.
        }
    }

    public boolean isContactCreated(String firstName, String lastName) {
        String fullName = firstName + " " + lastName;
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/contacts/"),
                ExpectedConditions.visibilityOfElementLocated(successBanner)
        ));

        boolean urlValid = driver.getCurrentUrl().contains("/contacts/");
        boolean nameVisible = driver.getPageSource().contains(fullName);

        return urlValid && (nameVisible || driver.findElements(successBanner).size() > 0);
    }
}
