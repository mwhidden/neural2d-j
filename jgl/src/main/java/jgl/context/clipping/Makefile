# Makefile for jGL clipping codes

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 1996-2000  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../../..

# LOCALCLASSPATH = ../../..:$(CLASSPATH)

FILES = gl_clipping    gl_2d_clipping \
	gl_vp_clipping gl_vp_color_clipping \
	gl_vp_tex_clipping gl_vp_lit_tex_clipping \
	gl_cp_clipping gl_cp_color_clipping \
	gl_cp_tex_clipping gl_cp_lit_tex_clipping

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile the CLIPPING of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../../Make-config

