# Makefile for jGL extension codes

# jGL 3D Graphics Library for Java
# Version:  2.5
# Copyright (C) 1996-2002  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../..

FILES = gle_context gle_pointer gle_vertex

### RULES ###

.SUFFIXES: .java

.java:
	@if [ -f $< ] ; then \
	    echo "Compile" $< ; \
	    $(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $< ; \
	fi

### TARGETS ###

all:	$(FILES)
#	@cd attrib ; $(MAKE) $@
#	@cd clipping ; $(MAKE) $@
	@cd geometry ; $(MAKE) $@
	@cd render ; $(MAKE) $@
	@echo "Compile the EXTENSION of jGL ok."

clean:
#	@cd attrib ; $(MAKE) $@
#	@cd clipping ; $(MAKE) $@
	@cd geometry ; $(MAKE) $@
	@cd render ; $(MAKE) $@
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../Make-config

