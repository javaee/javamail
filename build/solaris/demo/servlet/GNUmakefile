#
#
# Makefile for building all of demo/servlet
#

BUILDDIR = ../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

ALT_SRCDIR = demo/servlet
ALT_CLASSBINDIR = $(BUILDDIR)/demo_classes/servlet
DEPENDS = $(CLASSDIR):$(SERVLET_JAR)

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)demo_servlet.jmk

include $(BUILDDIR)/makefiles/Classes.gmk

all :: classes

clean clobber :: classes.clean
