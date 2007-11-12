#
#
# Makefile for building webapp
#

BUILDDIR = ../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

SRC = $(SHARE_SRC)/classes/demo/webapp/src/docroot
DOCROOT = $(BUILDDIR)/docroot

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)webapp.jmk

all ::	docroot

clean clobber ::
	rm -rf $(DOCROOT)

FILES_docroot = $(FILES_src:%=$(DOCROOT)/%)

$(DOCROOT)/%:	$(SRC)/%
	rm -f $@; cp $? $@

docroot:	$(DOCROOT) $(FILES_docroot)

$(DOCROOT):
	@mkdir -p $(DOCROOT)/WEB-INF/classes
	@mkdir -p $(DOCROOT)/WEB-INF/lib

war:
	@echo making war file
	@cd $(DOCROOT); $(JAR) cvf ../javamail.war *

SUBDIRS = src

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Subdirs.gmk
