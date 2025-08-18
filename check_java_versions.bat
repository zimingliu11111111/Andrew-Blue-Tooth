@echo off
echo ====== 检查系统Java版本 ======

echo [1] 当前PATH中的Java版本:
java -version 2>&1

echo.
echo [2] 查找所有java.exe位置:
where java 2>nul

echo.
echo [3] 检查Android Studio JBR:
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    echo 找到Android Studio JBR
    "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -version 2>&1
) else (
    echo Android Studio JBR未找到
)

echo.
echo [4] 检查常见Java安装位置:
for /d %%d in ("C:\Program Files\Java\*") do (
    if exist "%%d\bin\java.exe" (
        echo 找到: %%d
        "%%d\bin\java.exe" -version 2>&1
        echo.
    )
)

for /d %%d in ("C:\Program Files\Eclipse Adoptium\*") do (
    if exist "%%d\bin\java.exe" (
        echo 找到: %%d
        "%%d\bin\java.exe" -version 2>&1
        echo.
    )
)

echo.
echo [5] 检查环境变量:
echo JAVA_HOME: %JAVA_HOME%

pause