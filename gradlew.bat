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
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

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

set WRAPPER_JAR="%APP_HOME%\gradle\wrapper\gradle-wrapper.jar"

@REM Extension to allow automatically downloading the gradle-wrapper.jar from
@REM https://services.gradle.org/distributions/
@REM This allows using the gradle wrapper in projects that prohibit checking in binary data.
@REM Copied and modified from the Maven wrapper
set GRADLEWD_SOURCE="%APP_HOME%\gradle\wrapper\GradleWrapperDownloader.java"
set GRADLEWD_CLASS="%APP_HOME%\gradle\wrapper\GradleWrapperDownloader.class"
if exist %WRAPPER_JAR% (
    if "%GRADLEW_VERBOSE%" == "true" (
        echo "Found %WRAPPER_JAR%"
    )
) else (
    if "%GRADLEW_VERBOSE%" == "true" (
        echo "Couldn't find %WRAPPER_JAR%, downloading it ..."
    )

    if exist "%GRADLEWD_SOURCE%" (
        if not exist "%GRADLEWD_CLASS%" (
            if "%GRADLEW_VERBOSE%" == "true" (
              echo " - Compiling GradleWrapperDownloader.java ..."
            )
            "%JAVA_HOME%/bin/javac.exe" --release 11 -encoding UTF-8 "%GRADLEWD_SOURCE%"
        )
        if exist "%GRADLEWD_CLASS%" (
            if "%GRADLEW_VERBOSE%" == "true" (
              echo " - Running GradleWrapperDownloader.java ..."
            )
            "%JAVA_HOME%/bin/java.exe" -classpath gradle\wrapper -Dfile.encoding=UTF-8 GradleWrapperDownloader "%APP_HOME%"
        )
    )
)
@REM End of extension

set CLASSPATH=%WRAPPER_JAR%


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
