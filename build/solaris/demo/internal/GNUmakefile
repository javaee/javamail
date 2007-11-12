#
#
# Makefile for building all of demo/internal
#

BUILDDIR = ../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

MAIN = answer

ALT_SRCDIR = demo/internal
ALT_CLASSBINDIR = $(BUILDDIR)/demo_classes/internal
DEPENDS = $(CLASSDIR)

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)internal.jmk

all :: classes

clean clobber :: classes.clean

include $(BUILDDIR)/makefiles/Classes.gmk
