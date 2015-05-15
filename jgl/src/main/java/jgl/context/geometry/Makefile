# Makefile for jGL geometry codes

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 1996-2000  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../../..

# LOCALCLASSPATH = ../../..:$(CLASSPATH)

FILES = gl_geometry gl_depth_geo \
	gl_smooth_geo gl_smooth_z_geo \
	gl_tex_geo gl_tex_z_geo \
	gl_lit_tex_geo gl_lit_tex_z_geo

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile the GEOMETRY of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../../Make-config

