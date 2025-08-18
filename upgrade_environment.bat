@echo off
setlocal enabledelayedexpansion

echo ========================================
echo     Java 环境自动升级脚本
echo ========================================
echo.

echo [1] 检查当前Java版本...
java -version 2>&1 | findstr "version"

echo.
echo [2] 下载Java 17 (Eclipse Temurin)...
set DOWNLOAD_URL=https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%%2B11/OpenJDK17U-jdk_x64_windows_hotspot_17.0.13_11.msi
set TEMP_FILE=%TEMP%\temurin-17.msi

echo 正在下载到: %TEMP_FILE%
powershell.exe -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%TEMP_FILE%' -UseBasicParsing}"

if not exist "%TEMP_FILE%" (
    echo ❌ 下载失败，请检查网络连接
    echo 请手动访问: https://adoptium.net/temurin/releases/?version=17
    goto :manual_install
)

echo ✅ 下载完成

echo.
echo [3] 安装Java 17...
echo 正在安装，请稍候...
msiexec /i "%TEMP_FILE%" /quiet ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /norestart

echo 等待安装完成...
timeout /t 30 /nobreak > nul

echo.
echo [4] 查找安装路径...
for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
    if exist "%%d\bin\java.exe" (
        set JAVA17_HOME=%%d
        echo 找到Java 17: !JAVA17_HOME!
        goto :found_java17
    )
)

echo ❌ 未找到Java 17安装，可能安装失败
goto :manual_install

:found_java17
echo.
echo [5] 配置环境变量...
setx JAVA_HOME "!JAVA17_HOME!" /M >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  需要管理员权限设置系统环境变量
    echo 请以管理员身份运行此脚本，或手动设置:
    echo JAVA_HOME=!JAVA17_HOME!
) else (
    echo ✅ JAVA_HOME 已设置
)

echo.
echo [6] 更新PATH变量...
for /f "tokens=2*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v PATH 2^>nul') do set SYSTEM_PATH=%%b

echo !SYSTEM_PATH! | findstr /i "!JAVA17_HOME!\bin" >nul
if %errorlevel% neq 0 (
    set NEW_PATH=!JAVA17_HOME!\bin;!SYSTEM_PATH!
    setx PATH "!NEW_PATH!" /M >nul 2>&1
    if %errorlevel% neq 0 (
        echo ⚠️  需要管理员权限更新PATH
        echo 请手动添加到PATH: !JAVA17_HOME!\bin
    ) else (
        echo ✅ PATH 已更新
    )
) else (
    echo ✅ PATH 已包含Java 17
)

echo.
echo [7] 验证新的Java版本...
echo 请打开新的命令提示符窗口并运行: java -version
echo.
echo [8] 测试项目构建...
echo 在新的终端中运行以下命令:
echo   cd "%cd%"
echo   gradlew --stop
echo   gradlew clean build
echo.

echo ========================================
echo 安装完成！请重启命令行窗口以使用新的Java版本
echo ========================================
goto :end

:manual_install
echo.
echo ========================================
echo          手动安装指南
echo ========================================
echo 1. 访问: https://adoptium.net/temurin/releases/?version=17
echo 2. 下载 Windows x64 JDK
echo 3. 运行安装程序，勾选 "Add to PATH" 和 "Set JAVA_HOME"
echo 4. 安装完成后重启命令行
echo 5. 运行: java -version 验证
echo ========================================

:end
echo.
echo 按任意键退出...
pause >nul