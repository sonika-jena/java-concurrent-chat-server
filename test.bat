@echo off
echo Cleaning old test compilation...
if exist "out" rd /s /q "out"
mkdir out

echo Compiling source and test files...
javac -d out -cp "lib/*" src/server/*.java test/server/*.java
if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    exit /b %ERRORLEVEL%
)

echo.
echo Running tests...
java -jar lib/junit-platform-console-standalone-1.10.2.jar -cp out --scan-class-path
