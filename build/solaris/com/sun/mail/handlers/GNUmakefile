#
#
# Makefile for building all of com.sun.mail.pop3
#

BUILDDIR = ../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = com.sun.mail.handlers

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

default: all

include $(MINCLUDES)com_sun_mail_handlers.jmk
include $(BUILDDIR)/makefiles/Classes.gmk

all: classes

clean clobber :: classes.clean


