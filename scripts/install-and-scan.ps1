# SonarScanner Auto Install and Scan Script
$ErrorActionPreference = "Stop"

$installDir = "D:\software\sonar-scanner"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SonarScanner Install and Scan Tool" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Try different download sources
$sources = @(
    "https://github.com/SonarSource/sonar-scanner-cli/releases/download/6.1.0.4479/sonar-scanner-cli-6.1.0.4479-windows.zip",
    "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-6.1.0.4479-windows.zip",
    "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-4.8.0.2856-windows.zip"
)

$zipFile = "$env:TEMP\sonar-scanner.zip"
$downloaded = $false

# Check if already installed
if (Test-Path "$installDir\bin\sonar-scanner.bat") {
    Write-Host "[OK] SonarScanner already installed at: $installDir" -ForegroundColor Green
    $downloaded = $true
} else {
    Write-Host "[INFO] Downloading SonarScanner..." -ForegroundColor Yellow

    foreach ($url in $sources) {
        try {
            Write-Host "[INFO] Trying: $url" -ForegroundColor Gray
            New-Item -ItemType Directory -Force -Path $installDir | Out-Null

            $ProgressPreference = 'SilentlyContinue'
            Invoke-WebRequest -Uri $url -OutFile $zipFile -UseBasicParsing -UserAgent "Mozilla/5.0"
            $ProgressPreference = 'Continue'

            Write-Host "[INFO] Extracting..." -ForegroundColor Cyan
            Expand-Archive -Path $zipFile -DestinationPath $installDir -Force

            # Handle nested directory structure
            $extractedDirs = Get-ChildItem $installDir -Directory
            if ($extractedDirs.Count -eq 1) {
                $dir = $extractedDirs[0]
                Get-ChildItem "$($dir.FullName)" | Move-Item -Destination "$installDir\" -Force
                Remove-Item $dir.FullName -Force
            }

            Remove-Item $zipFile -Force
            Write-Host "[OK] Installation complete" -ForegroundColor Green
            $downloaded = $true
            break
        } catch {
            Write-Host "[WARN] Failed: $_" -ForegroundColor Yellow
            continue
        }
    }
}

if (-not $downloaded) {
    Write-Host "[ERROR] All download sources failed" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please download manually from:" -ForegroundColor Yellow
    Write-Host "https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner/" -ForegroundColor Gray
    Write-Host "Extract to: $installDir" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting Scan..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "D:\python_project\xuanjiao2"
Set-Location $projectRoot

if (Test-Path "sonar-project.properties") {
    Write-Host "[OK] Found config: sonar-project.properties" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Config not found" -ForegroundColor Red
    exit 1
}

$scannerBin = "$installDir\bin\sonar-scanner.bat"
Write-Host "[INFO] Running scan..." -ForegroundColor Cyan
Write-Host ""

& $scannerBin

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Scan Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
