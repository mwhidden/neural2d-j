# Makefile for jGL pixel attribute codes

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 1996-2000  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../../../..

# LOCALCLASSPATH = ../../../..:$(CLASSPATH)

FILES =  gl_pixel_map gl_pixel_pack gl_pixel_transfer

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile the PIXEL ATTRIBUTE of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../../../Make-config

