#
#
# Makefile for building all of com.sun.mail.dsn.*
#

BUILDDIR = ../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = com.sun.mail.dsn
ALT_CLASSBINDIR = $(BUILDDIR)/dsn_classes
DEPENDS = $(BUILDDIR)/classes
META_INF_DIR = $(TOPDIR)/build/$(PLATFORM)/dsn_classes/META-INF

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

default: all

include $(MINCLUDES)com_sun_mail_dsn.jmk
include $(BUILDDIR)/makefiles/Classes.gmk

all: classes resources

resources ::
	@if [ ! -d $(META_INF_DIR) ] ; then mkdir $(META_INF_DIR); fi
	@echo Copying $(RESOURCES) to $(META_INF_DIR)
	@cp -f $(RESOURCES) $(META_INF_DIR)

clean clobber :: classes.clean resources.clean

resources.clean ::
	@rm -rf $(META_INF_DIR)
