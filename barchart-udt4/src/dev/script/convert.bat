pexports.exe -o udt.DLL > udt.def
dlltool -d udt.def --dllname udt.dll --output-lib libudt.a
