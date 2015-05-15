# Makefile for jGL GLAUX core

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 1996-2000  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../..

# LOCALCLASSPATH = ../..:$(CLASSPATH)

FILES = MODELPTR mat_t teapot

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile GLAUX of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../Make-config

