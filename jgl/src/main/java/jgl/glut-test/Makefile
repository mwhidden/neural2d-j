# Makefile for jGL GLUT Tests

# jGL 3D Graphics Library for Java
# Version:  2.4
# Copyright (C) 1996-2003  Robin Bing-Yu Chen

### Local Setting ###

LOCALCLASSPATH = jgl.jar

BACKUPDIR = glut-test_BAK

FILES = keyup_test menu_test timer_test

### RULES ###

.SUFFIXES: .java

.java:
	@echo "Compile" $< "and Convert" $*.html
	@$(JAVAC) $(FLAGS) -classpath $(LOCALCLASSPATH) $<
	@$(CP) $*.html $*-pi.html 
	@$(HTMLCONV) $*-pi.html
	@cd .. ; $(RM) $(BACKUPDIR)

### TARGETS ###

all:	jgl.jar $(FILES)
	@echo "Compile GLUT Tests of jGL ok."

jgl.jar:
	@$(CP) ../Example/jgl.jar .

clean:
	@$(RM) jgl.jar
	@$(RM) *.class core
	@$(RM) *-pi.html

include Make-config
