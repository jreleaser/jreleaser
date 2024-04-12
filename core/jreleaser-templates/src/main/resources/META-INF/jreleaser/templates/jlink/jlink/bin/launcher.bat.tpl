@echo off
rem {{jreleaserCreationStamp}}

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto WinNTGetScriptDir

@REM The 4NT Shell from jp software
:4NTArgs
set CMD_LINE_ARGS=%$
goto WinNTGetScriptDir

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto Win9xGetScriptDir
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

:Win9xGetScriptDir
set SAVEDIR=%CD%
%0\
cd %0\..\..
set BASEDIR=%CD%
cd %SAVEDIR%
set SAVE_DIR=
goto javaSetup

:WinNTGetScriptDir
for %%i in ("%~dp0..") do set "BASEDIR=%%~fi"

:javaSetup

set JAVA_HOME=%BASEDIR%
set JAVACMD="%JAVA_HOME%\bin\java"
set JARSDIRS=%BASEDIR%\jars
{{#distributionJavaMainModule}}
set CLASSPATH=%JARSDIRS%
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
set CLASSPATH=%JARSDIRS%\*
{{/distributionJavaMainModule}}
{{#distributionJavaOptions}}
set JAVA_OPTS=%JAVA_OPTS% {{.}}
{{/distributionJavaOptions}}

@REM Reaching here means variables are defined and arguments have been captured
:endInit

{{#distributionJavaMainModule}}
%JAVACMD% %DEFAULT_JAVA_OPTS% %JAVA_OPTS% -p "%CLASSPATH%" -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}} %CMD_LINE_ARGS%
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
{{#distributionJavaMainClass}}
%JAVACMD% %DEFAULT_JAVA_OPTS% %JAVA_OPTS% -cp "%CLASSPATH%" {{distributionJavaMainClass}} %CMD_LINE_ARGS%
{{/distributionJavaMainClass}}
{{^distributionJavaMainClass}}
%JAVACMD% %DEFAULT_JAVA_OPTS% %JAVA_OPTS% -cp "%CLASSPATH%" -jar "%JARSDIRS%\{{distributionJavaMainJar}}" %CMD_LINE_ARGS%
{{/distributionJavaMainClass}}
{{/distributionJavaMainModule}}

if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=%ERRORLEVEL%

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set CMD_LINE_ARGS=
goto postExec

:endNT
@REM If error code is set to 1 then the endlocal was done already in :error.
if %ERROR_CODE% EQU 0 @endlocal

:postExec

if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
