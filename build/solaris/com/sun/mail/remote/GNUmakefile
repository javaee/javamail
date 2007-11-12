#
#
# Makefile for building all of com.sun.mail.remote
#

BUILDDIR = ../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = com.sun.mail.remote

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

default: all

include $(MINCLUDES)com_sun_mail_remote.jmk

all: classes

clean clobber :: classes.clean

#
# include rules
#

include $(BUILDDIR)/makefiles/Classes.gmk
