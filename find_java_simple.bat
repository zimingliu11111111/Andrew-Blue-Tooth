@echo off
echo ====== Java版本检查 ======
java -version
echo.
echo ====== 查找java.exe位置 ======  
where java
echo.
echo ====== 检查Android Studio JBR ======
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    echo 找到Android Studio JBR
    "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -version
) else (
    echo Android Studio JBR不存在
)
echo.
echo ====== 环境变量 ======
echo JAVA_HOME=%JAVA_HOME%