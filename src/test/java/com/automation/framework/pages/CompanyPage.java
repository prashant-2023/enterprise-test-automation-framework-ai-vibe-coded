package com.automation.framework.pages;

import com.automation.framework.config.ConfigManager;
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

public class CompanyPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
  
    private final By createButton = By.xpath("//button[normalize-space()='Create']");
    private final By companyNameField = SeleniumFormUtil.field("Name", "input");
    private final By websiteField = SeleniumFormUtil.field("Website", "input");
    private final By phoneField = SeleniumFormUtil.field("Phone", "input", "Phone number", "Phone Number");
    private final By emailField = SeleniumFormUtil.field("Email", "input");
    private final By descriptionField = SeleniumFormUtil.field("Description", "textarea");
    private final By industryField = SeleniumFormUtil.field("Industry", "input");
    private final By employeeCountField = SeleniumFormUtil.field("Num. of Employees", "input");
    private final By stockSymbolField = SeleniumFormUtil.field("Stock Symbol", "input");
    private final By annualRevenueField = SeleniumFormUtil.field("Annual Revenue", "input");
    private final By statusField = SeleniumFormUtil.field("Status", "select");
    private final By sourceField = SeleniumFormUtil.field("Source", "select");
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
            "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'company deleted')]");

    public CompanyPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void openCompaniesPage() {
        String baseUrl = ConfigManager.getBaseUrl().replaceFirst("/login/?$", "");
        driver.get(baseUrl + "/companies");
        wait.until(ExpectedConditions.visibilityOfElementLocated(createButton));
    }

    public void createCompany(String name, String website, String phone, String email, String description,
                              String industry, String employees, String stockSymbol, String annualRevenue,
                              String status, String source) {
        wait.until(ExpectedConditions.elementToBeClickable(createButton)).click();
        SeleniumFormUtil.fillRequired(wait, companyNameField, name);
        SeleniumFormUtil.fillRequired(wait, websiteField, website);
        SeleniumFormUtil.fillRequired(wait, phoneField, phone);
        SeleniumFormUtil.fillRequired(wait, emailField, email);
        SeleniumFormUtil.fillRequired(wait, descriptionField, description);
        SeleniumFormUtil.fillRequired(wait, industryField, industry);
        SeleniumFormUtil.fillRequired(wait, employeeCountField, employees);
        SeleniumFormUtil.fillRequired(wait, stockSymbolField, stockSymbol);
        SeleniumFormUtil.fillRequired(wait, annualRevenueField, annualRevenue);
        SeleniumFormUtil.selectRequired(wait, statusField, status);
        SeleniumFormUtil.selectRequired(wait, sourceField, source);
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
    }

    public boolean isCompanyNameVisible(String name) {
        By companyName = By.xpath("//*[normalize-space(text())="
                + SeleniumFormUtil.xpathLiteral(name) + "]");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(companyName)).isDisplayed();
    }

    public boolean deleteCompanyAndVerify(String name) {
        By companyName = By.xpath("//*[normalize-space(text())="
                + SeleniumFormUtil.xpathLiteral(name) + "]");
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
        confirmDeletion();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(deletionConfirmation),
                ExpectedConditions.invisibilityOfElementLocated(companyName)
        ));
        return driver.findElements(companyName).stream().noneMatch(WebElement::isDisplayed);
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
