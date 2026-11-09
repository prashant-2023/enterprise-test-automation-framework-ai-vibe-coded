# Enterprise Test Automation Framework

Selenium WebDriver automation framework using Java, TestNG, and Maven. Docker Compose provides a Selenium Grid with Chrome and Firefox nodes and a containerized Maven test runner.

## Project structure

```text
.
├── .dockerignore
├── DockerFile
├── docker-compose.yml
├── pom.xml
├── run-tests-with-compose.ps1
├── src/
│   └── test/
│       ├── java/com/automation/framework/
│       │   ├── base/        # WebDriver setup and teardown
│       │   ├── config/      # Test configuration
│       │   ├── listeners/   # TestNG listener and ExtentReports
│       │   ├── pages/       # Page Object Model classes
│       │   ├── tests/       # TestNG test cases
│       │   └── utils/       # Logging, JSON data, screenshots, helpers
│       └── resources/
│           ├── config/      # config.properties
│           ├── data/        # JSON test data
│           └── testng.xml   # Test suite and browser parameters
└── target/                  # Generated reports and test artifacts
```

## Run tests with Docker

Requirements: Docker Desktop with Docker Compose.

From PowerShell at the project root:

```powershell
.\run-tests-with-compose.ps1
```

The script builds the test-runner image, starts the Selenium Hub and browser nodes, runs the Maven test suite, and removes the containers and network afterward. The test-runner copies generated files to the local `target/` directory, including when tests fail.

To start the stack without the wrapper:

```powershell
docker compose up --build --abort-on-container-exit --exit-code-from test-runner
```

Then remove the containers and network with:

```powershell
docker compose down --remove-orphans
```

## Run tests with local Maven

Requirements: Java 17 and Maven. The configured remote WebDriver URL must point to a reachable Selenium Grid (by default, `http://localhost:4444`).

```powershell
mvn clean test
```

## Test artifacts

- Extent report: `target/extent-reports/extent-report.html`
- Maven Surefire reports: `target/surefire-reports/`
- Screenshots: `target/screenshots/` (when captured)
