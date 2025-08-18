@echo off
echo "Checking Java installations..."

echo "Current Java version:"
java -version

echo.
echo "Checking for Java 17+ installations:"

where java

echo.
echo "Checking Android Studio bundled JDK:"
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -version
    echo JBR Path: C:\Program Files\Android\Android Studio\jbr
)

echo.
echo "Checking common JDK locations:"
for %%d in ("C:\Program Files\Java\jdk*" "C:\Program Files\Eclipse Adoptium\jdk*") do (
    if exist "%%d\bin\java.exe" (
        echo Found: %%d
        "%%d\bin\java.exe" -version
        echo.
    )
)