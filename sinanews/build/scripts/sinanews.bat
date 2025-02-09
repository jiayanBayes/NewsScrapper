@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  sinanews startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and SINANEWS_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\sinanews.jar;%APP_HOME%\lib\common.jar;%APP_HOME%\lib\org.ektorp-1.5.0.jar;%APP_HOME%\lib\jackson-core-2.18.2.jar;%APP_HOME%\lib\jackson-databind-2.18.2.jar;%APP_HOME%\lib\jackson-annotations-2.18.2.jar;%APP_HOME%\lib\quartz-2.3.2.jar;%APP_HOME%\lib\jsoup-1.18.3.jar;%APP_HOME%\lib\selenium-java-4.28.1.jar;%APP_HOME%\lib\httpclient-cache-4.3.jar;%APP_HOME%\lib\httpclient-4.3.jar;%APP_HOME%\lib\commons-io-2.0.1.jar;%APP_HOME%\lib\jcl-over-slf4j-1.6.4.jar;%APP_HOME%\lib\HikariCP-java7-2.4.13.jar;%APP_HOME%\lib\slf4j-api-1.7.21.jar;%APP_HOME%\lib\annotations-1.3.2.jar;%APP_HOME%\lib\c3p0-0.9.5.4.jar;%APP_HOME%\lib\mchange-commons-java-0.2.15.jar;%APP_HOME%\lib\selenium-chrome-driver-4.28.1.jar;%APP_HOME%\lib\selenium-devtools-v130-4.28.1.jar;%APP_HOME%\lib\selenium-devtools-v131-4.28.1.jar;%APP_HOME%\lib\selenium-devtools-v132-4.28.1.jar;%APP_HOME%\lib\selenium-firefox-driver-4.28.1.jar;%APP_HOME%\lib\selenium-devtools-v85-4.28.1.jar;%APP_HOME%\lib\selenium-edge-driver-4.28.1.jar;%APP_HOME%\lib\selenium-ie-driver-4.28.1.jar;%APP_HOME%\lib\selenium-safari-driver-4.28.1.jar;%APP_HOME%\lib\selenium-support-4.28.1.jar;%APP_HOME%\lib\selenium-chromium-driver-4.28.1.jar;%APP_HOME%\lib\selenium-remote-driver-4.28.1.jar;%APP_HOME%\lib\selenium-manager-4.28.1.jar;%APP_HOME%\lib\selenium-http-4.28.1.jar;%APP_HOME%\lib\selenium-json-4.28.1.jar;%APP_HOME%\lib\selenium-os-4.28.1.jar;%APP_HOME%\lib\selenium-api-4.28.1.jar;%APP_HOME%\lib\httpcore-4.3.jar;%APP_HOME%\lib\commons-codec-1.6.jar;%APP_HOME%\lib\jspecify-1.0.0.jar;%APP_HOME%\lib\auto-service-annotations-1.1.1.jar;%APP_HOME%\lib\guava-33.4.0-jre.jar;%APP_HOME%\lib\opentelemetry-semconv-1.28.0-alpha.jar;%APP_HOME%\lib\opentelemetry-exporter-logging-1.46.0.jar;%APP_HOME%\lib\opentelemetry-sdk-extension-autoconfigure-1.46.0.jar;%APP_HOME%\lib\opentelemetry-sdk-extension-autoconfigure-spi-1.46.0.jar;%APP_HOME%\lib\opentelemetry-sdk-1.46.0.jar;%APP_HOME%\lib\opentelemetry-sdk-trace-1.46.0.jar;%APP_HOME%\lib\opentelemetry-sdk-metrics-1.46.0.jar;%APP_HOME%\lib\opentelemetry-sdk-logs-1.46.0.jar;%APP_HOME%\lib\opentelemetry-sdk-common-1.46.0.jar;%APP_HOME%\lib\opentelemetry-api-incubator-1.46.0-alpha.jar;%APP_HOME%\lib\opentelemetry-api-1.46.0.jar;%APP_HOME%\lib\opentelemetry-context-1.46.0.jar;%APP_HOME%\lib\byte-buddy-1.15.11.jar;%APP_HOME%\lib\failureaccess-1.0.2.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-3.43.0.jar;%APP_HOME%\lib\error_prone_annotations-2.36.0.jar;%APP_HOME%\lib\j2objc-annotations-3.0.0.jar;%APP_HOME%\lib\commons-exec-1.4.0.jar


@rem Execute sinanews
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %SINANEWS_OPTS%  -classpath "%CLASSPATH%" scraping.SchedulerApp %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable SINANEWS_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%SINANEWS_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
