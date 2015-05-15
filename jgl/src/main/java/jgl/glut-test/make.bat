@echo off
rem Makefile for jGL GLUT Tests

rem jGL 3D Graphics Library for Java
rem Version:  2.4
rem Copyright (C) 1996-2003  Robin Bing-Yu Chen

rem ### GLOBAL SETTING ###

set JAVAHOME=c:\j2sdk1.4.1_01
set HTMLCONV=%JAVA_HOME%\bin\HtmlConverter.exe

rem ### RULES ###

if "%1"=="clean" goto clean
if "%1"=="" goto all

	@echo off
	@echo Compile %1.java
	@%JAVAHOME%\bin\javac -g:none -classpath jgl.jar %1.java
	@copy %1.html %1-pi.html
	@%HTMLCONV% %1-pi.html
	@del ../glut-test_BAK/%1-pi.html
	goto end

rem ### TARGETS ###

:all
	@echo off
	@call make.bat keyup_test
	@call make.bat menu_test
	@call make.bat timer_test
	@cd ..
	@rd glut-test_BAK
	@cd glut-test
	@echo Compile GLUT Tests of jGL ok.
	goto end

:clean
	@echo off
	@del *.class
	@del *-pi.html
	goto end

:end

set JAVAHOME=
