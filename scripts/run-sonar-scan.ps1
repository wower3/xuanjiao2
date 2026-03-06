# SonarQube 代码质量扫描脚本
# 使用方法：在 PowerShell 中运行 .\scripts\run-sonar-scan.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  xuanjiao-backend 代码质量扫描工具" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否安装了 SonarScanner
$sonarScanner = Get-Command sonar-scanner -ErrorAction SilentlyContinue

if (-not $sonarScanner) {
    Write-Host "❌ 未检测到 SonarScanner" -ForegroundColor Red
    Write-Host ""
    Write-Host "请选择扫描方式：" -ForegroundColor Yellow
    Write-Host "  1. 【推荐】使用 VS Code 扩展扫描（无需安装）" -ForegroundColor Green
    Write-Host "  2. 安装 SonarScanner CLI 工具" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "VS Code 扫描步骤：" -ForegroundColor Cyan
    Write-Host "  1. 按 Ctrl+Shift+P 打开命令面板" -ForegroundColor White
    Write-Host "  2. 输入: SonarQube: Analyze All Files in Project" -ForegroundColor White
    Write-Host "  3. 点击左侧 SonarQube 图标查看结果" -ForegroundColor White
    Write-Host ""
    Write-Host "SonarScanner 下载地址：" -ForegroundColor Cyan
    Write-Host "  https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner/" -ForegroundColor Blue
    Write-Host ""
    exit
}

Write-Host "✅ 检测到 SonarScanner: $($sonarScanner.Source)" -ForegroundColor Green
Write-Host ""

# 检查是否配置了服务器
$hasServer = $env:SONAR_HOST_URL -or $env:SONAR_TOKEN

if ($hasServer) {
    Write-Host "📡 检测到 SonarQube 服务器配置" -ForegroundColor Green
    Write-Host "   服务器: $env:SONAR_HOST_URL" -ForegroundColor Gray
} else {
    Write-Host "⚠️  未检测到服务器配置（将运行本地分析模式）" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "如需连接服务器，请设置环境变量：" -ForegroundColor Cyan
    Write-Host '  $env:SONAR_HOST_URL="http://localhost:9000"' -ForegroundColor Gray
    Write-Host '  $env:SONAR_TOKEN="your-token-here"' -ForegroundColor Gray
}

Write-Host ""
Write-Host "🔍 开始扫描..." -ForegroundColor Cyan
Write-Host ""

# 切换到项目根目录
$projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $projectRoot

Write-Host "工作目录: $projectRoot" -ForegroundColor Gray
Write-Host ""

# 运行扫描
& sonar-scanner

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  扫描完成！" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
