@echo off
rem Makefile for jGL Examples

rem jGL 3D Graphics Library for Java
rem Version:  2.4
rem Copyright (C) 1996-2001  Robin Bing-Yu Chen

rem ### GLOBAL SETTING ###

set JAVA_HOME=c:\j2sdk1.4.1_01
set HTMLCONV=%JAVA_HOME%\bin\HtmlConverter.exe

rem ### RULES ###

if "%1"=="clean" goto clean
if "%1"=="" goto all

	@echo off
	@echo Compile %1.java and Convert %1.html
	@%JAVAHOME%\bin\javac -g:none -classpath jgl.jar %1.java
	@copy %1.html %1-pi.html
	@%HTMLCONV% %1-pi.html
	@del ../Example_BAK/%1-pi.html
	goto end

rem ### TARGETS ###

:all
	@echo off
	@call make.bat bezcurve
	@call make.bat bezmesh
	@call make.bat bezsurf
	@call make.bat checker
	@call make.bat clip
	@call make.bat colormat
	@call make.bat cube
	@call make.bat doublebuffer
	@call make.bat light
	@call make.bat lines
	@call make.bat list
	@call make.bat material
	@call make.bat mipmap
	@call make.bat model
	@call make.bat movelight
	@call make.bat pickdepth
	@call make.bat picksquare
	@call make.bat planet
	@call make.bat polys
	@call make.bat robot
	@call make.bat select
	@call make.bat simple
	@call make.bat smooth
	@call make.bat stroke
	@call make.bat surface
	@call make.bat tea
	@call make.bat teapots
	@call make.bat texgen
	@call make.bat texturesurf
	@cd ..
	@rd Example_BAK
	@cd Example
	@echo Compile Examples of JavaGL ok.
	goto end

:clean
	@echo off
	@del *.class
	@del *-pi.html
	@cd ..
	@rd Example_BAK
	@cd Example
	goto end

:end

set HTMLCONV=
set JAVA_HOME=
