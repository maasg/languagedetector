@echo off

call %~dp0\..\settings.bat

set ENV=%1

if "%ENV%"=="" (
    echo "You didn't specify the environment to which to deploy."
    exit /b 1
)

set APP_VERSION=%2

if "%APP_VERSION%"=="" (
    echo "You didn't specify the version number to be deployed."
    exit /b 1
)

set APP_ENV_DIR=%APP_DIR%\%ENV%
echo "Deploying %APP_NAME% %APP_VERSION% to %APP_ENV_DIR%..."
if not exist %APP_ENV_DIR% mkdir %APP_ENV_DIR%

rem fix the random to a timestamp
if exist %APP_ENV_DIR%\%APP_NAME%.jar move %APP_ENV_DIR%\%APP_NAME%.jar %APP_ENV_DIR%\%APP_NAME%_%random%.jar

move %SOURCE_DIR%\target\scala-2.11\%APP_NAME%_2.11-%APP_VERSION%.jar %APP_ENV_DIR%\%APP_NAME%.jar

rem note: in a real world scenario the package would be downloaded from a corporate repository instead of taking it from a local path

echo "Done."