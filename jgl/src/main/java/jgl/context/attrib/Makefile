# Makefile for jGL attribute codes

# jGL 3D Graphics Library for Java
# Version:  2.4
# Copyright (C) 1996-2001  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../../..

# LOCALCLASSPATH = ../../..:$(CLASSPATH)

FILES = gl_current gl_eval gl_lighting \
	gl_pixel gl_raster gl_texture gl_transform gl_viewport \
	gl_colorbuffer gl_depthbuffer gl_stencilbuffer

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@cd eval ; $(MAKE) $@
	@cd lighting ; $(MAKE) $@
	@cd pixel ; $(MAKE) $@
	@cd texture ; $(MAKE) $@
	@echo "Compile the ATTRIBUTE of jGL ok."

clean:
	@cd eval ; $(MAKE) $@
	@cd lighting ; $(MAKE) $@
	@cd pixel ; $(MAKE) $@
	@cd texture ; $(MAKE) $@
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../../Make-config

