@echo off
setlocal enabledelayedexpansion
set scriptdir=%~dp0
echo %scriptdir%
for /f %%i in ('dir /b %scriptdir%..\lib\*.jar') do if not defined neural2dclasspath (set neural2dclasspath=%scriptdir%..\lib\%%i) else (set neural2dclasspath=!neural2dclasspath!;%scriptdir%..\lib\%%i)

%JAVA_HOME%\bin\java -classpath %neural2dclasspath% neural2d.Neural2D %*
