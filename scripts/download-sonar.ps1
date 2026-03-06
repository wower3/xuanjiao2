# Download SonarScanner via proxy
$ErrorActionPreference = "Stop"

# Proxy settings
$proxyUrl = "http://127.0.0.1:7890"

# Create proxy object
$proxy = New-Object System.Net.WebProxy
$proxy.Address = $proxyUrl
$proxy.UseDefaultCredentials = $true

# Create web client with proxy
$webClient = New-Object System.Net.WebClient
$webClient.Proxy = $proxy

$installDir = "D:\software\sonar-scanner"
$zipFile = "$installDir\sonar-scanner.zip"

# Download URLs to try
$urls = @(
    "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-6.1.0.4479-windows.zip",
    "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-5.0.1.3006-windows.zip",
    "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.8.0.2856-windows.zip"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Downloading SonarScanner..." -ForegroundColor Cyan
Write-Host "  Proxy: $proxyUrl" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create directory
New-Item -ItemType Directory -Force -Path $installDir | Out-Null

$downloaded = $false
foreach ($url in $urls) {
    try {
        Write-Host "[INFO] Trying: $url" -ForegroundColor Yellow
        $webClient.DownloadFile($url, $zipFile)
        Write-Host "[OK] Downloaded to: $zipFile" -ForegroundColor Green
        $downloaded = $true
        break
    } catch {
        Write-Host "[WARN] Failed: $_" -ForegroundColor Red
    }
}

if (-not $downloaded) {
    Write-Host "[ERROR] All download attempts failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[INFO] Extracting..." -ForegroundColor Cyan
Expand-Archive -Path $zipFile -DestinationPath $installDir -Force

# Handle nested directory
$extractedDirs = Get-ChildItem $installDir -Directory
if ($extractedDirs.Count -eq 1) {
    $dir = $extractedDirs[0]
    Get-ChildItem "$($dir.FullName)" | Move-Item -Destination "$installDir\" -Force
    Remove-Item $dir.FullName -Force
}

Remove-Item $zipFile -Force

Write-Host "[OK] Installation complete: $installDir" -ForegroundColor Green
Write-Host ""
Write-Host "To add to PATH for current session:" -ForegroundColor Yellow
Write-Host '  $env:Path += ";D:\software\sonar-scanner\bin"' -ForegroundColor Gray
