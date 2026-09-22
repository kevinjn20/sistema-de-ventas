@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper
@REM ----------------------------------------------------------------------------
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set WRAPPER_JAR="%APP_HOME%.mvn\wrapper\maven-wrapper.jar"
if not exist "%WRAPPER_JAR%" (
    set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
    echo Downloading Maven Wrapper...
    bitsadmin /transfer "MavenWrapper" %WRAPPER_URL% "%WRAPPER_JAR%" 2>nul
    if exist "%WRAPPER_JAR%" goto run
    echo Error: unable to download Maven Wrapper
    exit /b 1
)

:run
"%JAVA_EXE%" %MAVEN_OPTS% "-Dmaven.multiModuleProjectDirectory=%APP_HOME%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
:end
@REM End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal
