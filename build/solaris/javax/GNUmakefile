#
#
# Makefile for building all of javax
#

BUILDDIR = ..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

SUBDIRS = mail

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Subdirs.gmk

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Project.gmk

clean ::
	rm -fr $(LIBDIR) $(BINDIR) $(CLASSBINDIR)
