@echo off

setlocal

rem set JMETER_HOME DIR
call setEnv.bat

pushd "%JMETER_HOME%\bin"

java -cp ApacheJMeter.jar org.apache.jmeter.util.ShutdownClient Shutdown %*
