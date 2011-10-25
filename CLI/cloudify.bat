@if "%DEBUG%" == "" @echo off
setlocal

goto :BEGIN

@rem following are 'functions'

:SETUP_AND_CLEANUP
	if not "%~1" == "" goto :END
	title GigaSpaces Cloudify Shell
  mode con:cols=130 lines=2000
  cls
	del /Q %temp%\jansi.dll >nul 2>&1
goto :END


:SET_JSHOME
	@set SCRIPT_PATH=%~dp0
	@set JSHOMEDIR=%SCRIPT_PATH%..\..
goto :END


:SET_ENV
	@call "%JSHOMEDIR%\bin\setenv.bat"
goto :END


:SET_CLOUDIFY_JAVA_OPTIONS
	set CLOUDIFY_DEBUG_OPTIONS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9000 -Xnoagent -Djava.compiler=NONE
	set CLOUDIFY_JAVA_OPTIONS=-Xmx500m -Dcom.gigaspaces.logger.RollingFileHandler.debug-level=WARNING %EXT_CLOUDIFY_JAVA_OPTIONS%
goto :END


:SET_CLOUDIFY_CLASSPATH
	set CLI_JARS="%JSHOMEDIR%\tools\cli\cli.jar"
	set SIGAR_JARS="%JSHOMEDIR%\lib\platform\sigar\sigar.jar"
	set GROOVY_JARS="%JSHOMEDIR%\tools\groovy\lib\*"
	set ESC_JARS="%JSHOMEDIR%\lib\platform\esm\esc.jar"
	
	set PLUGIN_JARS=
	
	pushd "%SCRIPT_PATH%\plugins"
		for /D %%G in (*) do call:ITERATE_JARS %SCRIPT_PATH%plugins\%%G
	popd
	
	set CLOUDIFY_CLASSPATH=%CLI_JARS%;%GS_JARS%;%SIGAR_JARS%;%GROOVY_JARS%;%ESC_JARS%;%PLUGIN_JARS%
goto :END

:ITERATE_JARS
	set dirname=%~1
	pushd %dirname%
		for %%G in (*.jar) do call:APPEND_TO_PLUGIN_JARS %dirname%\%%G
	popd
goto :END

:APPEND_TO_PLUGIN_JARS
	set filename=%~1
	set PLUGIN_JARS=%PLUGIN_JARS%;%filename%
goto :END


:SET_COMMAND_LINE
	set CLI_ENTRY_POINT=org.openspaces.shell.GigaShellMain
	set COMMAND_LINE=%JAVACMD% %GS_LOGGING_CONFIG_FILE_PROP% %RMI_OPTIONS% %LOOKUP_LOCATORS_PROP% %LOOKUP_GROUPS_PROP% %CLOUDIFY_JAVA_OPTIONS% -classpath %PRE_CLASSPATH%;%CLOUDIFY_CLASSPATH%;%POST_CLASSPATH% %CLI_ENTRY_POINT% %*
goto :END


:INIT
	call :SETUP_AND_CLEANUP %*
	call :SET_JSHOME
	call :SET_ENV
	call :SET_CLOUDIFY_JAVA_OPTIONS
	call :SET_CLOUDIFY_CLASSPATH
	call :SET_COMMAND_LINE %*
goto :END


@rem Here the execution starts	
:BEGIN

call :INIT %*

rem add one padding line before the logo
echo.

%COMMAND_LINE%

endlocal

:END