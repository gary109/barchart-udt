REM
REM http://blog.idm.fr/2009/03/setting-up-a-windows-hudson-slave.html
REM http://support.microsoft.com/kb/137890
REM http://www.microsoft.com/downloads/en/details.aspx?familyid=9d467a69-57ff-4ae7-96ee-b18c4790cffd&displaylang=en
REM

@ECHO OFF

cd c:\hudson\

ECHO start >> slave-agent.log

call sleep 30

java -jar c:\hudson\slave.jar -jnlpUrl file:///c:/hudson/slave-agent.jnlp 1> hudson.log 2>&1

GOTO :EOF

REM
REM	http://www.paulsadowski.com/wsh/cmdutils.htm
REM

:: Subroutines

:sleep
:: sleep for x number of seconds
ping -n %1 127.0.0.1 > NUL 2>&1
goto :EOF

