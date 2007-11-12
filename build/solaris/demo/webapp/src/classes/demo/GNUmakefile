#
#
# Makefile for building all of webapp classes
#

BUILDDIR = ../../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

ALT_SRCDIR = demo/webapp/src/classes
ALT_CLASSBINDIR = $(BUILDDIR)/docroot/WEB-INF/classes
DEPENDS = $(CLASSDIR):$(SERVLET_JAR):$(JNDI_JAR)

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)webapp_classes.jmk

include $(BUILDDIR)/makefiles/Classes.gmk

all :: classes

clean clobber :: classes.clean
