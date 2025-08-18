@echo off
echo 修复Java 21与Gradle版本兼容性问题...
echo.

cd /d "C:\Users\Lenovo\AndroidStudioProjects\bluetoothremote"

echo 1. 当前配置:
echo   - Gradle: 8.5
echo   - AGP: 8.2.2
echo   - Kotlin: 1.9.22
echo   - Java Target: 17
echo.

echo 2. 检查Java版本...
java -version
echo.

echo 3. 清理之前的构建缓存...
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo ✓ 清理 .gradle 文件夹
)

if exist "app\build" (
    rmdir /s /q "app\build"
    echo ✓ 清理 app\build 文件夹
)

echo.
echo 4. 重新下载Gradle Wrapper...
call gradlew.bat wrapper --gradle-version 8.5
if %errorlevel% neq 0 (
    echo ✗ Gradle Wrapper下载失败
    goto :error
)

echo.
echo 5. 尝试同步项目...
call gradlew.bat build --dry-run
if %errorlevel% neq 0 (
    echo ✗ 项目同步失败
    echo.
    echo 请检查以下事项:
    echo - 确保使用Java 17-19版本 (当前检测到Java 21)
    echo - 在Android Studio中设置Project JDK为Java 17
    echo - 或安装Java 17并设置JAVA_HOME环境变量
    echo.
    echo 解决方案:
    echo 1. 下载Java 17: https://adoptium.net/
    echo 2. 设置环境变量 JAVA_HOME 指向Java 17安装目录
    echo 3. 重新运行此脚本
    goto :error
) else (
    echo ✓ 项目同步成功
)

echo.
echo 6. 尝试编译...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ✗ 编译失败
    goto :error
) else (
    echo ✓ 编译成功
)

echo.
echo ========================================
echo 所有版本兼容性问题已修复！
echo ========================================
echo.
echo 项目现在使用:
echo - Gradle 8.5 (兼容Java 17-19)
echo - Android Gradle Plugin 8.2.2
echo - Kotlin 1.9.22
echo.
echo 如果还有问题，请:
echo 1. 在Android Studio中设置Project JDK为Java 17
echo 2. 或安装Java 17并设置JAVA_HOME
echo.
goto :end

:error
echo.
echo ========================================
echo 修复失败，请按照上面的说明手动处理
echo ========================================
pause
exit /b 1

:end
pause