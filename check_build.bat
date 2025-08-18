@echo off
echo 开始检查项目编译状态...
echo.

cd /d "C:\Users\Lenovo\AndroidStudioProjects\bluetoothremote"

echo 1. 检查Gradle Wrapper...
if exist gradlew.bat (
    echo ✓ gradlew.bat 存在
) else (
    echo ✗ gradlew.bat 不存在
    goto :error
)

echo.
echo 2. 检查主要源文件...
if exist "app\src\main\java\com\example\bluetoothremote\TestActivity.kt" (
    echo ✓ TestActivity.kt 存在
) else (
    echo ✗ TestActivity.kt 不存在
)

if exist "app\src\main\AndroidManifest.xml" (
    echo ✓ AndroidManifest.xml 存在
) else (
    echo ✗ AndroidManifest.xml 不存在
    goto :error
)

echo.
echo 3. 尝试清理项目...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo ✗ 清理失败
    goto :error
)

echo.
echo 4. 尝试编译...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ✗ 编译失败
    goto :error
) else (
    echo ✓ 编译成功
)

echo.
echo 所有检查完成！
goto :end

:error
echo.
echo 发现错误，请检查上面的输出信息
exit /b 1

:end
echo.
echo 项目准备就绪
pause