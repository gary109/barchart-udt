@ECHO OFF

REM
REM custom windows hudson build to set mingw path first
REM

REM
REM must provide maven home via
REM http://wiki.hudson-ci.org/display/HUDSON/Tool+Environment+Plugin
REM

REM #####################

ECHO ### PWD=%CD%

SET MINGW_HOME=c:\mingw\bin

ECHO ### MINGW_HOME=%MINGW_HOME%

IF [%jdk%]==[java32] call :do_mingw_32

IF [%jdk%]==[java64] call :do_mingw_64

"%APACHE_MAVEN_3_HOME%\bin\mvn" clean deploy --activate-profiles nar --show-version --update-snapshots

goto :EOF

REM #####################

:do_mingw_32
ECHO ### mingw for java32
SET PATH=%MINGW_HOME%;%PATH%
ECHO ### PATH=%PATH%
goto :EOF

:do_mingw_64
ECHO ### mingw for java64
ECHO ### NOT TESTED
goto :EOF
