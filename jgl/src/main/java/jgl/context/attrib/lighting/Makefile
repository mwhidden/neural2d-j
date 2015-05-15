# Makefile for jGL lighting attribute codes

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 1996-2000  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../../../..

# LOCALCLASSPATH = ../../../..:$(CLASSPATH)

FILES = gl_light gl_material

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile the LIGHTING ATTRIBUTE of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../../../Make-config

