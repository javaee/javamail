#
#
# Makefile for building all of com.sun.mail.client.*
#

BUILDDIR = ../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = com.sun.mail.client

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

default: all

include $(MINCLUDES)com_sun_mail_client.jmk
include $(BUILDDIR)/makefiles/Classes.gmk


all: classes

clean clobber :: classes.clean


