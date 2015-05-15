# Makefile for jGL context codes

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 1996-2000  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../..

# LOCALCLASSPATH = ../..:$(CLASSPATH)

FILES = gl_object gl_context gl_list gl_list_item gl_pointer gl_util \
	gl_vertex gl_polygon gl_image \
	gl_select gl_feedback 

### RULES ###

.SUFFIXES: .java

.java:
	@if [ -f $< ] ; then \
	    echo "Compile" $< ; \
	    $(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $< ; \
	fi

### TARGETS ###

all:	$(FILES)
	@cd attrib ; $(MAKE) $@
	@cd clipping ; $(MAKE) $@
	@cd geometry ; $(MAKE) $@
	@cd render ; $(MAKE) $@
	@echo "Compile the CONTEXT of jGL ok."

clean:
	@cd attrib ; $(MAKE) $@
	@cd clipping ; $(MAKE) $@
	@cd geometry ; $(MAKE) $@
	@cd render ; $(MAKE) $@
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../Make-config

