#
# @(#)GNUmakefile	1.1 97/09/24
#
# Makefile for building all of com.sun.mail.smtp.*
#

BUILDDIR = ../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = com.sun.mail.smtp

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

default: all

include $(MINCLUDES)com_sun_mail_smtp.jmk
include $(BUILDDIR)/makefiles/Classes.gmk

all: classes

clean clobber :: classes.clean


