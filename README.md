# Enterprise Test Automation Framework

A production-ready Selenium + Java + TestNG + Maven automation framework designed with industry best practices.

## Included
- Page Object Model (POM)
- TestNG annotations and data-driven testing
- JSON-based test data input
- Properties-based configuration
- Log4j2 logging
- ExtentReports reporting
- Screenshot capture on failure
- Browser driver management with WebDriverManager

## Project Structure

```text
src/
  test/
    java/
      com/automation/framework/
        base/
        config/
        listeners/
        pages/
        tests/
        utils/
    resources/
      config/
      data/
      testng.xml
```

## Run tests

```bash
mvn clean test
```

## Report location

```text
target/extent-reports/extent-report.html
```
