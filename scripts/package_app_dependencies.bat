@echo off

call %~dp0\..\settings.bat

echo "Creating package for dependencies..."
pushd %SOURCE_DIR%
sbt assemblyPackageDependency
popd

rem note: in a real world scenario the package would be uploaded to a corporate repository
echo "Done."
