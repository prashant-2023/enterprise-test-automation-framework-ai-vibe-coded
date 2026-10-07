package com.automation.framework.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.stream.Collectors;

public class CompanyPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By createButton = By.xpath("//button[normalize-space()='Create']");
    private final By companyNameField = field("Name", "input");
    private final By websiteField = field("Website", "input");
    private final By phoneField = field("Phone", "input", "Phone number", "Phone Number");
    private final By emailField = field("Email", "input");
    private final By descriptionField = field("Description", "textarea");
    private final By industryField = field("Industry", "input");
    private final By employeeCountField = field("Num. of Employees", "input");
    private final By stockSymbolField = field("Stock Symbol", "input");
    private final By annualRevenueField = field("Annual Revenue", "input");
    private final By statusField = field("Status", "select");
    private final By sourceField = field("Source", "select");
    private final By saveButton = By.xpath("//button[normalize-space()='Save']");

    public CompanyPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void openCompaniesPage() {
        driver.get("https://ui.freecrm.com/companies");
        wait.until(ExpectedConditions.visibilityOfElementLocated(createButton));
    }

    public void createCompany(String name, String website, String phone, String email, String description,
                              String industry, String employees, String stockSymbol, String annualRevenue,
                              String status, String source) {
        wait.until(ExpectedConditions.elementToBeClickable(createButton)).click();
        fillRequired(companyNameField, name);
        fillRequired(websiteField, website);
        fillRequired(phoneField, phone);
        fillRequired(emailField, email);
        fillRequired(descriptionField, description);
        fillRequired(industryField, industry);
        fillRequired(employeeCountField, employees);
        fillRequired(stockSymbolField, stockSymbol);
        fillRequired(annualRevenueField, annualRevenue);
        selectRequired(statusField, status);
        selectRequired(sourceField, source);
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
    }

    public boolean isCompanyNameVisible(String name) {
        By companyName = By.xpath("//*[normalize-space(text())=" + xpathLiteral(name) + "]");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(companyName)).isDisplayed();
    }

    private static By field(String label, String element, String... aliases) {
        StringBuilder labels = new StringBuilder("normalize-space(.)=").append(xpathLiteral(label));
        StringBuilder accessibleAttributes = new StringBuilder(
                "@placeholder=" + xpathLiteral(label) + " or @aria-label=" + xpathLiteral(label));
        appendAttributeName(accessibleAttributes, label);

        for (String alias : aliases) {
            labels.append(" or normalize-space(.)=").append(xpathLiteral(alias));
            accessibleAttributes.append(" or @placeholder=").append(xpathLiteral(alias))
                    .append(" or @aria-label=").append(xpathLiteral(alias));
            appendAttributeName(accessibleAttributes, alias);
        }

        String labelXPath = "//label[" + labels + "]";
        String labelledControl = labelXPath + "/following-sibling::" + element
                + " | " + labelXPath + "/following-sibling::*//" + element
                + " | " + labelXPath + "/parent::*//" + element;
        String accessibleControl = "//" + element + "[" + accessibleAttributes + "]";
        return By.xpath("(" + labelledControl + " | " + accessibleControl + ")[1]");
    }

    private static void appendAttributeName(StringBuilder attributes, String label) {
        String attributeName = label.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        attributes.append(" or @name=").append(xpathLiteral(attributeName))
                .append(" or @id=").append(xpathLiteral(attributeName));
    }

    private static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'", -1);
        StringBuilder literal = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                literal.append(", \"'\", ");
            }
            literal.append("'").append(parts[i]).append("'");
        }
        return literal.append(")").toString();
    }

    private void fillRequired(By selector, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
        element.clear();
        element.sendKeys(value);
    }

    private void selectRequired(By selector, String value) {
        Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(selector)));
        for (WebElement option : select.getOptions()) {
            if (option.getText().contains(value)) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }
        String availableOptions = select.getOptions().stream()
                .map(WebElement::getText)
                .collect(Collectors.joining(", "));
        throw new IllegalArgumentException(
                "No option containing '" + value + "' was found for " + selector
                        + ". Available options: " + availableOptions);
    }
}
