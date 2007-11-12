#
#
# Makefile for building (copying) the files in META-INF
#

BUILDDIR = ..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

SUBDIRS = sun

include $(MINCLUDES)/resources.res
META_INF_DIR = $(TOPDIR)/build/$(PLATFORM)/classes/META-INF

all ::
	@if [ ! -d $(META_INF_DIR) ] ; then mkdir $(META_INF_DIR); fi
	@echo Copying $(RESOURCES) to $(META_INF_DIR)
	@cp -f $(RESOURCES) $(META_INF_DIR)

clean ::
	@rm -rf $(META_INF_DIR)

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Project.gmk
