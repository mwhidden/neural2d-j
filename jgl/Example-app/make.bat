@echo off
rem Makefile for jGL Application Examples

rem jGL 3D Graphics Library for Java
rem Version:  2.4
rem Copyright (C) 1996-2003  Robin Bing-Yu Chen

rem ### GLOBAL SETTING ###

set JAVAHOME=c:\j2sdk1.4.1_01

rem ### RULES ###

if "%1"=="clean" goto clean
if "%1"=="" goto all

	@echo off
	@echo Compile %1.java
	@%JAVAHOME%\bin\javac -g:none -classpath jgl.jar %1.java
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
	@call make.bat hello
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
	@call make.bat quadric
	@call make.bat robot
	@call make.bat scene
	@call make.bat select
	@call make.bat smooth
	@call make.bat stroke
	@call make.bat surface
	@call make.bat tea
	@call make.bat teapots
	@call make.bat texbind
	@call make.bat texgen
	@call make.bat texsub
	@call make.bat texture3d
	@call make.bat texturesurf
	@call make.bat torus
	@call make.bat unproject
	@echo Compile Examples of jGL in Application Mode ok.
	goto end

:clean
	@echo off
	@del *.class
	goto end

:end

set JAVAHOME=
