@echo off
echo ====== 下载并安装Java 17 ======
echo.

echo [1] 使用winget安装Eclipse Temurin Java 17...
echo 命令: winget install EclipseAdoptium.Temurin.17.JDK

winget install EclipseAdoptium.Temurin.17.JDK --accept-package-agreements --accept-source-agreements

echo.
echo [2] 检查安装结果...
if exist "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\java.exe" (
    echo ✅ Java 17安装成功！
    for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
        echo 安装路径: %%d
        "%%d\bin\java.exe" -version
        echo.
        echo 请手动设置环境变量:
        echo JAVA_HOME=%%d
        echo PATH=%%d\bin;%PATH%
    )
) else (
    echo ❌ 安装可能失败，请手动下载安装
    echo 下载地址: https://adoptium.net/temurin/releases/?version=17
)

echo.
echo 安装完成后，请重启终端并运行: java -version
pause