# Makefile for jGL GLUT core

# jGL 3D Graphics Library for Java
# Version:  2.4
# Copyright (C) 1996-2001  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../..

# LOCALCLASSPATH = ../..:$(CLASSPATH)

FILES = glut_menu

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile GLUT of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../Make-config

