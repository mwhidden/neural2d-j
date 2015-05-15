# Makefile for jGL Application Examples

# jGL 3D Graphics Library for Java
# Version:  2.4
# Copyright (C) 1996-2003  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = jgl.jar

FILES = bezcurve bezmesh bezsurf checker clip colormat cube doublebuffer \
	hello light lines list material mipmap model movelight pickdepth \
	picksquare planet polys quadric robot scene select smooth stroke \
	surface tea teapots texbind texgen texsub texture3d texturesurf \
	torus unproject
# drawf

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $<
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<

### TARGETS ###

all:	jgl.jar $(FILES)
	@echo "Compile Examples of jGL in Application Mode ok."

jgl.jar:
	@$(CP) ../Example/jgl.jar .

clean:
	@$(RM) jgl.jar
	@$(RM) *.class core

include Make-config
