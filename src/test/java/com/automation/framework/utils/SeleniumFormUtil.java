package com.automation.framework.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class SeleniumFormUtil {
    // Prevents instances of this static helper class from being created.
    private SeleniumFormUtil() {
    }

    // Clears a required field and enters its value after it becomes visible.
    public static void fillRequired(WebDriverWait wait, By selector, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
        element.clear();
        element.sendKeys(value);
    }

    // Fills a field only when it exists and a non-blank value was provided.
    public static void fillIfPresent(WebDriver driver, WebDriverWait wait, By selector, String value) {
        if (value == null || value.isBlank() || driver.findElements(selector).isEmpty()) {
            return;
        }
        fillRequired(wait, selector, value);
    }

    // Selects the first native dropdown option containing the requested text.
    public static void selectRequired(WebDriverWait wait, By selector, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
        if (!"select".equalsIgnoreCase(element.getTagName())) {
            throw new IllegalArgumentException("Expected a native select element for " + selector);
        }

        Select select = new Select(element);
        for (WebElement option : select.getOptions()) {
            if (option.getText().contains(value)) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }

        StringBuilder availableOptions = new StringBuilder();
        for (WebElement option : select.getOptions()) {
            if (!availableOptions.isEmpty()) {
                availableOptions.append(", ");
            }
            availableOptions.append(option.getText());
        }
        throw new IllegalArgumentException(
                "No option containing '" + value + "' was found for " + selector
                        + ". Available options: " + availableOptions);
    }

    // Selects a native dropdown value only when the field and value are present.
    public static void selectIfPresent(WebDriver driver, WebDriverWait wait, By selector, String value) {
        if (value == null || value.isBlank() || driver.findElements(selector).isEmpty()) {
            return;
        }
        selectRequired(wait, selector, value);
    }

    // Selects an option from a native dropdown or a custom dropdown widget.
    public static void selectValue(WebDriver driver, WebDriverWait wait, By selector, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
        if ("select".equalsIgnoreCase(element.getTagName())) {
            selectRequired(wait, selector, value);
            return;
        }

        element.click();
        selectOption(wait, value);
    }

    // Opens a record picker or searches an editable field, then selects the matching record.
    public static void selectRecord(WebDriver driver, WebDriverWait wait, By selector, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
        String tagName = element.getTagName();
        if ("input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName)) {
            element.clear();
            element.sendKeys(value);
        } else if ("button".equalsIgnoreCase(tagName)) {
            element.click();
            WebElement searchField = wait.until(currentDriver -> {
                WebElement activeElement = currentDriver.switchTo().activeElement();
                String activeTag = activeElement.getTagName();
                return "input".equalsIgnoreCase(activeTag) || "textarea".equalsIgnoreCase(activeTag)
                        ? activeElement
                        : null;
            });
            searchField.sendKeys(value);
        } else {
            throw new IllegalArgumentException(
                    "Expected an editable record search field or picker button for " + selector
                            + " but found <" + tagName + ">");
        }

        selectOption(wait, value);
    }

    // Builds a locator for a form control using its label or accessible attributes.
    public static By field(String label, String element, String... aliases) {
        StringBuilder labels = new StringBuilder("normalize-space(.)=").append(xpathLiteral(label));
        StringBuilder attributes = new StringBuilder(
                "@placeholder=" + xpathLiteral(label) + " or @aria-label=" + xpathLiteral(label));
        appendFieldAttributes(attributes, label);

        for (String alias : aliases) {
            labels.append(" or normalize-space(.)=").append(xpathLiteral(alias));
            attributes.append(" or @placeholder=").append(xpathLiteral(alias))
                    .append(" or @aria-label=").append(xpathLiteral(alias));
            appendFieldAttributes(attributes, alias);
        }

        String labelXPath = "//label[" + labels + "]";
        String labelledControl = labelXPath + "/following-sibling::" + element
                + " | " + labelXPath + "/following-sibling::*//" + element
                + " | " + labelXPath + "/parent::*//" + element;
        String accessibleControl = "//" + element + "[" + attributes + "]";
        return By.xpath("(" + labelledControl + " | " + accessibleControl + ")[1]");
    }

    // Builds an XPath condition requiring all whitespace-separated words to be present.
    public static String xpathContainsWords(String value) {
        StringBuilder condition = new StringBuilder();
        for (String word : value.trim().split("\\s+")) {
            if (!condition.isEmpty()) {
                condition.append(" and ");
            }
            condition.append("contains(normalize-space(.), ")
                    .append(xpathLiteral(word)).append(")");
        }
        return condition.toString();
    }

    // Escapes text so it can safely be used as an XPath string literal.
    public static String xpathLiteral(String value) {
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

    // Waits for and clicks an autocomplete/dropdown option matching the value.
    private static void selectOption(WebDriverWait wait, String value) {
        By option = By.xpath("//*[" + xpathContainsWords(value)
                + " and (@role='option' or @role='menuitem' or self::a or self::button or self::li"
                + " or contains(concat(' ', normalize-space(@class), ' '), ' item '))]");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    // Adds name and id attribute matches for a field label to the locator.
    private static void appendFieldAttributes(StringBuilder attributes, String label) {
        String attributeName = label.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        attributes.append(" or @name=").append(xpathLiteral(attributeName))
                .append(" or @id=").append(xpathLiteral(attributeName));
    }
}
