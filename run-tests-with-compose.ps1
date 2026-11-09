$ErrorActionPreference = "Continue"

Push-Location $PSScriptRoot
$testExitCode = 1
$cleanupExitCode = 0

try {
    & docker compose up --build --abort-on-container-exit --exit-code-from test-runner
    $testExitCode = $LASTEXITCODE
}
finally {
    try {
        & docker compose down --remove-orphans
        $cleanupExitCode = $LASTEXITCODE
    }
    finally {
        Pop-Location
    }
}

if ($cleanupExitCode -ne 0) {
    Write-Error "Docker Compose cleanup failed with exit code $cleanupExitCode."
    exit $cleanupExitCode
}

exit $testExitCode
