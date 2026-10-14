package com.automation.framework.pages;

import com.automation.framework.config.ConfigManager;
import com.automation.framework.utils.SeleniumFormUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TaskPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By createButton = By.xpath("//button[normalize-space()='Create']");
    private final By titleField = SeleniumFormUtil.field("Title", "input");
    private final By descriptionField = SeleniumFormUtil.field("Description", "textarea");
    private final By priorityField = SeleniumFormUtil.field("Priority", "select");
    private final By statusField = SeleniumFormUtil.field("Status", "select");
    private final By companyField = By.xpath("//label[text()='Company']//following::button[1]");
    private final By contactField = By.xpath("//label[text()='Contact']//following::button[1]");
    private final By saveButton = By.xpath("//button[normalize-space()='Save']");

    public TaskPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void openTasksPage() {
        String baseUrl = ConfigManager.getBaseUrl().replaceFirst("/login/?$", "");
        driver.get(baseUrl + "/tasks");
        wait.until(ExpectedConditions.visibilityOfElementLocated(createButton));
    }

    public void createTask(String title, String description, String priority, String status,
                           String company, String contact) {
        wait.until(ExpectedConditions.elementToBeClickable(createButton)).click();
        SeleniumFormUtil.fillRequired(wait, titleField, title);
        SeleniumFormUtil.fillRequired(wait, descriptionField, description);
        SeleniumFormUtil.selectValue(driver, wait, priorityField, priority);
        SeleniumFormUtil.selectValue(driver, wait, statusField, status);
        SeleniumFormUtil.selectRecord(driver, wait, companyField, company);
        //driver.findElement(companyField).sendKeys(company);
        
        SeleniumFormUtil.selectRecord(driver, wait, contactField, contact);
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
    }

    public boolean isTaskVisible(String title, String description, String priority, String status,
                                 String company, String contact) {
        By taskTitle = By.xpath("//*[normalize-space(text())=" + SeleniumFormUtil.xpathLiteral(title) + "]");
        By taskDescription = By.xpath("//*[normalize-space(text())=" + SeleniumFormUtil.xpathLiteral(description) + "]");
        By taskPriority = By.xpath("//*[normalize-space(text())=" + SeleniumFormUtil.xpathLiteral(priority) + "]");
        By taskStatus = By.xpath("//*[normalize-space(text())=" + SeleniumFormUtil.xpathLiteral(status) + "]");
        By taskCompany = By.xpath("//*[" + SeleniumFormUtil.xpathContainsWords(company) + "]");
        By taskContact = By.xpath("//*[" + SeleniumFormUtil.xpathContainsWords(contact) + "]");
        return isVisible(taskTitle)
                && isVisible(taskDescription)
                && isVisible(taskPriority)
                && isVisible(taskStatus)
                && isVisible(taskCompany)
                && isVisible(taskContact);
    }

    private boolean isVisible(By selector) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(selector)).isDisplayed();
    }

}
