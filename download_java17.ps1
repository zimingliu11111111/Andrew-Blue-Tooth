# Download and Install Java 17
Write-Host "====== Downloading Java 17 ======" -ForegroundColor Green

$jdk17Url = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jdk_x64_windows_hotspot_17.0.13_11.msi"
$downloadPath = "$env:TEMP\temurin-17.msi"

Write-Host "[1] Downloading Java 17..." -ForegroundColor Yellow

try {
    Invoke-WebRequest -Uri $jdk17Url -OutFile $downloadPath -UseBasicParsing
    Write-Host "Download completed: $downloadPath" -ForegroundColor Green
    
    Write-Host "[2] Installing..." -ForegroundColor Yellow    
    Start-Process msiexec.exe -ArgumentList "/i", $downloadPath, "/quiet" -Wait
    
    Write-Host "[3] Checking installation..." -ForegroundColor Yellow
    
    if (Test-Path "C:\Program Files\Eclipse Adoptium\") {
        $jdkPath = Get-ChildItem "C:\Program Files\Eclipse Adoptium\" -Directory -Name "jdk-17*" | Select-Object -First 1
        
        if ($jdkPath) {
            $fullPath = "C:\Program Files\Eclipse Adoptium\$jdkPath"
            Write-Host "Java 17 installed successfully!" -ForegroundColor Green
            Write-Host "Installation path: $fullPath"
            
            & "$fullPath\bin\java.exe" -version
            
            Write-Host "[4] Setting environment variables..." -ForegroundColor Yellow
            [Environment]::SetEnvironmentVariable("JAVA_HOME", $fullPath, "Machine")
            
            $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
            $newBinPath = "$fullPath\bin"
            if ($currentPath -notlike "*$newBinPath*") {
                [Environment]::SetEnvironmentVariable("PATH", "$newBinPath;$currentPath", "Machine")
            }
            
            Write-Host "Environment variables set. Please restart terminal." -ForegroundColor Green
            
        } else {
            Write-Host "Java 17 installation not found" -ForegroundColor Red
        }
    } else {
        Write-Host "Eclipse Adoptium folder not found" -ForegroundColor Red
    }
    
} catch {
    Write-Host "Download failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Please manually download from: https://adoptium.net/temurin/releases/?version=17"
}