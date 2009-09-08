set CMD_PATH=%~dp0

echo %CMD_PATH%

assoc .SH=bashscript
assoc .SH

ftype bashscript="%CYGWIN_HOME%\bin\bash.exe" "%%1"
ftype bashscript

pause

%CMD_PATH%associate-cygwin.bat-test.sh

pause
