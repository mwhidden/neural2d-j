# Makefile for jGL GLJ3D core

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 2000 Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../..

# LOCALCLASSPATH = ../..:$(CLASSPATH)

FILES = GLJ3DGeometry

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile GLJ3D of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../Make-config

