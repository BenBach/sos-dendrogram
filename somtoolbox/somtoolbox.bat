@ECHO OFF
REM Adjust JAVA_MEMORY to your needs...
SET JAVA_MEMORY=1024
REM No more user-settings below here.

REM the equivalent of dirname($0)
FOR %%F IN (%0) do set BASE_DIR=%%~dpF

IF EXIST "%BASE_DIR%\somtoolbox.jar" (
  SET CP="%BASE_DIR%\somtoolbox.jar"
  ECHO Using somtoolbox.jar
) ELSE (
  SET CP="%BASE_DIR%\bin\core"
  ECHO Using class-files
)

REM add the optional-component if available.
IF EXIST "%BASE_DIR%\lib\somtoolbox+opt.jar" (
  SET CP=%CP%;"%BASE_DIR%\lib\somtoolbox+opt.jar"
  ECHO Enabling additional components from jar-file
) ELSE (
  IF EXIST "%BASE_DIR%\bin\optional\" (
    SET CP=%CP%;"%BASE_DIR%\bin\optional\"
    ECHO Enabling additional components from class-files
  )
)

REM Create the classpath (recursive)
SET LIB_DIR=%BASE_DIR%\lib
FOR /R "%LIB_DIR%" %%f IN (*.jar) DO CALL :AddLib "%%f"

GOTO :StartApp

:AddLib
SET CP=%CP%;%1
REM acts like a return:
GOTO :EOF 

:StartApp
ECHO java -Xmx%JAVA_MEMORY%M -cp %CP% at.tuwien.ifs.somtoolbox.apps.SOMToolboxMain %*
@echo ON
SET SOMTOOLBOX_BASEDIR=%BASE_DIR%
java -Xmx%JAVA_MEMORY%M -cp %CP% at.tuwien.ifs.somtoolbox.apps.SOMToolboxMain %*
:Ende

