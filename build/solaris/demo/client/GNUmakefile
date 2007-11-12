#
#
# Makefile for building all of demo/client
#

BUILDDIR = ../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

MAIN = SimpleClient

ALT_SRCDIR = demo/client
ALT_CLASSBINDIR = $(BUILDDIR)/demo_classes/client
DEPENDS = $(CLASSDIR):$(SWING_JAR)

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)demo_client.jmk

include $(BUILDDIR)/makefiles/Classes.gmk

all :: classes

clean clobber :: classes.clean
