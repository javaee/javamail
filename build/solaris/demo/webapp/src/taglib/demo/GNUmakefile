#
#
# Makefile for building all of webapp taglib
#

BUILDDIR = ../../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

ALT_SRCDIR = demo/webapp/src/taglib
ALT_CLASSBINDIR = ../obj
DEPENDS = $(CLASSDIR):$(SERVLET_JAR)
DOCROOT = $(BUILDDIR)/docroot
SRC = $(SHARE_SRC)/classes/$(SRCDIR)

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)webapp_taglib.jmk

all :: classes

clean clobber :: classes.clean

include $(BUILDDIR)/makefiles/Classes.gmk
