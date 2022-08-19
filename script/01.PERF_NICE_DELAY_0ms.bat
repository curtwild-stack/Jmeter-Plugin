@echo off

setlocal

rem set JMETER_HOME DIR
call setEnv.bat

rem ============== JMETER Property ===================
rem Target User Count (MAX Thread)
set JMETER_PROPERTIES=%JMETER_PROPERTIES% -JTARGET_USERS=100

rem Ramp Up Time (secs)
set JMETER_PROPERTIES=%JMETER_PROPERTIES% -JRAMPUP_SECONDS=10

rem Ramp Up Steps
set JMETER_PROPERTIES=%JMETER_PROPERTIES% -JRAMPUP_STEPS_COUNT=0

rem Hold Target Seconds
set JMETER_PROPERTIES=%JMETER_PROPERTIES% -JHOLD_TARGET_SECONDS=120

rem Throughput per Second (1 user)
set JMETER_PROPERTIES=%JMETER_PROPERTIES% -JTPS_PER_USER=60

rem jmeter property override. print out non gui mode summary
set JMETER_PROPERTIES=%JMETER_PROPERTIES% -Jsummariser.interval=5

rem ===================================================


set LOG_LEVEL=INFO

set JMX=%JMETER_HOME%\jmx\nice_perf_test.jmx

set LOG_FILE=jmeter.log

set JTL_FILE=result.jtl

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

call "%JMETER_HOME%"\bin\jmeter -n -L %LOG_LEVEL% -t "%JMX%" -j "%LOG_FILE%" -l "%JTL_FILE%" -JHEAP="%HEAP%" %JMETER_PROPERTIES%

popd