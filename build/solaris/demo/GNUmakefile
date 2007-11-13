#
#
# Makefile for building all of demo
#

BUILDDIR = ..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

SUBDIRS = client servlet webapp

MAIN = folderlist msgsend msgshow msgsendsample msgmultisendsample
ALT_SRCDIR = demo
ALT_CLASSBINDIR = $(BUILDDIR)/demo_classes
DEPENDS = $(CLASSDIR)

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)demo.jmk

all :: classes

clean clobber :: classes.clean

include $(BUILDDIR)/makefiles/Classes.gmk

include $(BUILDDIR)/makefiles/Subdirs.gmk
