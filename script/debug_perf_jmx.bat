@echo off

setlocal

rem set JMETER_HOME DIR
call setEnv.bat

set LOG_LEVEL=INFO

set JMX=%JMETER_HOME%\jmx\nice_perf_test.jmx

set LOG_FILE=%~n0.log

set JTL_FILE=%~n0.jtl

set WORKING_DIR=%~n0_%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%

set HEAP=-Xms1g -Xmx2g -XX:MaxMetaspaceSize=256m

echo JMX         : %JMX%
echo JTL_FILE    : %JTL_FILE%
echo LOG_FILE    : %LOG_FILE%
echo JMETER_HOME : %JMETER_HOME%
echo LOG_LEVEL   : %LOG_LEVEL%
echo WORKING_DIR : %WORKING_DIR%

mkdir "%WORKING_DIR%"

pushd "%WORKING_DIR%"

call "%JMETER_HOME%"\bin\jmeter -L %LOG_LEVEL% -t "%JMX%" -j "%LOG_FILE%" -l "%JTL_FILE%" -JHEAP="%HEAP%"

popd