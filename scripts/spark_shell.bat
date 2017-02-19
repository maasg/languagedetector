@echo off

call %~dp0\..\settings.bat

set ENV=%1

if "%ENV%"=="" (
    echo "You didn't specify the environment to which to deploy."
    exit /b 1
)

set APP_ENV_DIR=%APP_DIR%\%ENV%
set APP_CONFIG=%APP_ENV_DIR%\%ENV%.conf

set LOG_DIR=%APP_ENV_DIR%\logs
if not exist %LOG_DIR% mkdir %LOG_DIR%
set BUSLOGFILE=businessLog_spark_shell.log

%SPARK_HOME%\bin\spark-shell --driver-memory 1g --driver-java-options '-Dconfig.file=%APP_CONFIG%' --driver-class-path %APP_ENV_DIR%\%APP_NAME%.jar;%APP_ENV_DIR%\%APP_NAME%-deps.jar -i %~dp0\spark_shell_init.scala
