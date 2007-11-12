#
#
# Makefile for building webapp taglib
#

BUILDDIR = ../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

ALT_SRCDIR = demo/webapp/src/taglib
ALT_CLASSBINDIR = ./obj
DOCROOT = $(BUILDDIR)/docroot
SRC = $(SHARE_SRC)/classes/$(SRCDIR)
META = $(CLASSBINDIR)/META-INF $(CLASSBINDIR)/META-INF/taglib.tld
TAGLIB = $(DOCROOT)/WEB-INF/lib/jtl.jar

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

SUBDIRS = demo

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Subdirs.gmk

$(CLASSBINDIR)/META-INF/%: $(SRC)/META-INF/%
	rm -f $@; cp $? $@

all :: $(META) $(TAGLIB)

clean clobber ::
	echo rm -rf $(CLASSBINDIR)

$(CLASSBINDIR)/META-INF:
	mkdir -p $(CLASSBINDIR)/META-INF

$(TAGLIB):	$(META) demo
	cd $(CLASSBINDIR); \
	$(JAR) cvf ../$(TAGLIB) META-INF demo/*.class
