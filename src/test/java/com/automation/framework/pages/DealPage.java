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

public class DealPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By createButton = By.xpath("//button[normalize-space()='Create']");
    private final By dealNameField = SeleniumFormUtil.field("Title", "input", "Name");
    private final By amountField = SeleniumFormUtil.field("Amount", "input");
    private final By descriptionField = SeleniumFormUtil.field("Description", "textarea");
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
            "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'deal deleted')]");

    public DealPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void openDealsPage() {
        String baseUrl = ConfigManager.getBaseUrl().replaceFirst("/login/?$", "");
        driver.get(baseUrl + "/deals");
        wait.until(ExpectedConditions.visibilityOfElementLocated(createButton));
    }

    public void createDeal(String name, String amount, String description) {
        wait.until(ExpectedConditions.elementToBeClickable(createButton)).click();
        SeleniumFormUtil.fillRequired(wait, dealNameField, name);
        SeleniumFormUtil.fillRequired(wait, amountField, amount);
        if (!driver.findElements(descriptionField).isEmpty()) {
            SeleniumFormUtil.fillRequired(wait, descriptionField, description);
        }
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
    }

    public boolean isDealNameVisible(String name) {
        By dealName = By.xpath("//*[normalize-space(text())=" + SeleniumFormUtil.xpathLiteral(name) + "]");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dealName)).isDisplayed();
    }

    public boolean deleteDealAndVerify(String name) {
        By dealName = By.xpath("//*[normalize-space(text())=" + SeleniumFormUtil.xpathLiteral(name) + "]");
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
        confirmDeletion();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(deletionConfirmation),
                ExpectedConditions.invisibilityOfElementLocated(dealName)
        ));
        return driver.findElements(dealName).stream().noneMatch(WebElement::isDisplayed);
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
