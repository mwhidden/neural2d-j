# Makefile for jGL GLU core

# jGL 3D Graphics Library for Java
# Version:  2.3
# Copyright (C) 1996-2000  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = ../..

# LOCALCLASSPATH = ../..:$(CLASSPATH)

FILES = GLUquadricObj GLUnurbsObj \
	nurbs_bz_curv nurbs_bz_surf nurbs_nurbs nurbs_obj \
	nurbs_curve nurbs_curves nurbs_surface nurbs_surfaces \
	nurbs_knot \
	nurbs_ct_curv nurbs_ct_curvs nurbs_ct_surf nurbs_ct_surfs

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	$(FILES)
	@echo "Compile GLU of jGL ok."

clean:
	@$(RM) *.class core

update:
	@cd .. ; $(MAKE) $@

include ../Make-config

