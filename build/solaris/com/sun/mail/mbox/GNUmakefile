#
#
# Makefile for building all of com.sun.mail.mbox
#

BUILDDIR = ../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = com.sun.mail.mbox
LIBRARY = mbox

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

default: all

include $(MINCLUDES)com_sun_mail_mbox.jmk
include $(MINCLUDES)com_sun_mail_mbox.cmk

FILES_export = \
	com/sun/mail/mbox/UNIXFile.java \
	com/sun/mail/mbox/UNIXInbox.java

# include $(BUILDDIR)/makefiles/Classes.gmk

all: classes library

clean clobber :: classes.clean library.clean


#
# Use JNI for generating header files
#
JAVAHFLAGS += -jni

#
# include rules
#

include $(BUILDDIR)/makefiles/Library.gmk

OTHER_LDLIBS = -lmail
