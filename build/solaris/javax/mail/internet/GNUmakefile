#
#
# Makefile for building all of javax.mail.internet.*
#

BUILDDIR = ../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = javax.mail.internet

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

default: all

include $(MINCLUDES)javax_mail_internet.jmk
include $(BUILDDIR)/makefiles/Classes.gmk

all: classes

clean clobber :: classes.clean


