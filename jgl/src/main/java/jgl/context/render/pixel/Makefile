# Makefile for jGL pixel render codes

# jGL 3D Graphics Library for Java
# Version:  2.4
# Copyright (C) 1996-2006  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../../../..

# LOCALCLASSPATH = ../../../..:$(CLASSPATH)

FILES = gl_blend_pixel gl_render_pixel gl_render_point gl_select_pixel \
	gl_stipple_pixel gl_stipple_line_pixel gl_stipple_poly_pixel

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile the PIXEL RENDER of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../../../Make-config
