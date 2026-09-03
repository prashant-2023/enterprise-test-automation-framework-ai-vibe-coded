package com.automation.framework.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By emailField = By.id("email");
    private final By passwordField = By.id("password");
    private final By loginButton = By.cssSelector("button[type='submit']");
    private final By errorMessage = By.cssSelector(".ui.red.message");
    private final By homeGreeting = By.xpath("//*[contains(text(),'Good evening') or contains(text(),'Good morning') or contains(text(),'Good afternoon')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void open() {
        driver.get("https://ui.freecrm.com/login");
    }

    public void login(String username, String password) {
        WebElement emailElement = wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        emailElement.clear();
        emailElement.sendKeys(username);

        WebElement passwordElement = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
        passwordElement.clear();
        passwordElement.sendKeys(password);

        WebElement loginButtonElement = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButtonElement.click();
    }

    public boolean isLoginSuccessful() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(homeGreeting)).isDisplayed();
    }

    public String getErrorMessage() {
        if (driver.findElements(errorMessage).size() > 0) {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
        }
        return "";
    }
}
