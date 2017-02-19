@echo off

call %~dp0\..\settings.bat

set ENV=%1

if "%ENV%"=="" (
    echo "You didn't specify the environment to which to deploy."
    exit /b 1
)

set CLASS=%2

if "%CLASS%"=="" (
    echo "You didn't specify the workflow or module to execute."
    exit /b 1
)

shift
shift

for /f "tokens=2,* delims= " %%a in ("%*") do set REMAINING_ARGS=%%b

set LOGFILE=log_%random%.log
set BUSLOGFILE=businessLog_%random%.log

set APP_ENV_DIR=%APP_DIR%\%ENV%
set APP_CONFIG=%APP_ENV_DIR%\%ENV%.conf

set LOG_DIR=%APP_ENV_DIR%\logs
if not exist %LOG_DIR% mkdir %LOG_DIR%
if not exist \tmp\spark-events mkdir \tmp\spark-events

rem add the dependencies to the class path using "--driver-class-path" otherwise the guava lib v14.0 from spark is taken while languagedetect needs >= 18.0
%SPARK_HOME%\bin\spark-submit --jars %APP_ENV_DIR%\%APP_NAME%-deps.jar --class biz.meetmatch.%CLASS% --driver-memory 1g --driver-java-options '-Dconfig.file=%APP_CONFIG%' --driver-java-options '-DbusinessLogFileName=%LOG_DIR%\%BUSLOGFILE%' --driver-class-path %APP_ENV_DIR%\%APP_NAME%-deps.jar %APP_ENV_DIR%\%APP_NAME%.jar %REMAINING_ARGS% > %LOG_DIR%\%LOGFILE%
