@echo off

setlocal

rem set JMETER_HOME DIR
call setEnv.bat

set LOG_LEVEL=WARN

set JMX=%JMETER_HOME%\jmx\view_jtl.jmx

set LOG=%JMETER_HOME%\%~n0.log

echo JMX         : %JMX%
echo LOG         : %LOG%
echo JMETER_HOME : %JMETER_HOME%
echo LOG_LEVEL   : %LOG_LEVEL%

call "%JMETER_HOME%"\bin\jmeter -L %LOG_LEVEL% -t "%JMX%" -j "%LOG%"