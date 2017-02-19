@echo off

call %~dp0\..\settings.bat

set ENV=test
set APP_ENV_DIR=%APP_DIR%\%ENV%
set APP_CONFIG=%APP_ENV_DIR%\%ENV%.conf

pushd %SOURCE_DIR%
sbt "-Dconfig.file=%APP_CONFIG%" "run-main biz.meetmatch.util.DataDependencyPrinter"
popd
