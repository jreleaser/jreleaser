@echo off
rem {{jreleaserCreationStamp}}

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS to pass JVM options to this script.
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
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set JARSDIRS=%APP_HOME%\lib
{{#distributionJavaMainModule}}
set CLASSPATH=%JARSDIRS%
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
set CLASSPATH=%JARSDIRS%\*
{{/distributionJavaMainModule}}
{{#distributionJavaOptions}}
set JAVA_OPTS=%JAVA_OPTS% {{.}}
{{/distributionJavaOptions}}

@rem Execute
{{#distributionJavaMainModule}}
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -p "%CLASSPATH%" -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}} %*
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
{{#distributionJavaMainClass}}
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -cp "%CLASSPATH%" {{distributionJavaMainClass}} %*
{{/distributionJavaMainClass}}
{{^distributionJavaMainClass}}
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -cp "%CLASSPATH%" -jar "%JARSDIRS%\{{distributionJavaMainJar}}" %*
{{/distributionJavaMainClass}}
{{/distributionJavaMainModule}}

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable JRELEASER_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%JRELEASER_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
