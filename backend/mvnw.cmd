@echo off
setlocal

set "BASE_DIR=%~dp0"
if "%BASE_DIR:~-1%"=="\" set "BASE_DIR=%BASE_DIR:~0,-1%"

set "WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper"
set "WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties"
set "MAVEN_VERSION=3.9.9"
set "MAVEN_HOME=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_ZIP=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%-bin.zip"
set "DISTRIBUTION_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

if exist "%WRAPPER_PROPERTIES%" (
    for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPERTIES%") do (
        if "%%A"=="distributionUrl" set "DISTRIBUTION_URL=%%B"
    )
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    if not exist "%MAVEN_ZIP%" (
        echo Downloading Apache Maven %MAVEN_VERSION%...
        powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MAVEN_ZIP%'"
        if not exist "%MAVEN_ZIP%" exit /b 1
    )

    echo Extracting Apache Maven %MAVEN_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%WRAPPER_DIR%' -Force"
    if not exist "%MAVEN_HOME%\bin\mvn.cmd" exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
