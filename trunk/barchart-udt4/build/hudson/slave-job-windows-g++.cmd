@ECHO OFF

REM
REM custom windows hudson build to set mingw path first
REM

REM
REM must provide maven home via
REM http://wiki.hudson-ci.org/display/HUDSON/Tool+Environment+Plugin
REM

ECHO ### PWD=%CD%

SET MINGW_HOME=c:\mingw\bin
ECHO ### MINGW_HOME=%MINGW_HOME%

IF [%jdk%]==[java32] (
ECHO ### mingw for java32
PATH=%MINGW_HOME%;%PATH%
ECHO ### PATH=%PATH%
)

IF [%jdk%]==[java64] (
ECHO "### mingw for java64: NOT tested"
exit -1
)

"%APACHE_MAVEN_3_0_1_HOME%\bin\mvn" clean deploy --activate-profiles nar --show-version --update-snapshots
