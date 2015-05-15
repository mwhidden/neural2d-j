# Makefile for jGL render codes

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 1996-2000  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../../..

# LOCALCLASSPATH = ../../..:$(CLASSPATH)

FILES = gl_render gl_depth gl_smooth gl_smooth_z \
	gl_tex gl_tex_z gl_lit_tex gl_lit_tex_z \
	gl_select_render

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@cd pixel ; $(MAKE) $@
	@echo "Compile the RENDER of jGL ok."

clean:
	@cd pixel ; $(MAKE) $@
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../../Make-config

