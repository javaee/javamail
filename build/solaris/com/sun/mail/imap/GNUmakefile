#
#
# Makefile for building all of com.sun.mail.imap
#

BUILDDIR = ../../../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = com.sun.mail.imap

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)com_sun_mail_imap.jmk

SSCLASS	= $(BUILDDIR)/classes/$(PKGDIR)/protocol/IMAPSaslAuthenticator.class

all :: $(CLASSBINDIR) classes $(SSCLASS)

clean clobber :: classes.clean

include $(BUILDDIR)/makefiles/Classes.gmk

JAVAC15_CMD = /java/re/jdk/1.5.0/archive/fcs/binaries/solaris-sparc/bin/javac -source 1.4

#
$(SSCLASS): $(TOPDIR)/src/share/classes/$(PKGDIR)/protocol/IMAPSaslAuthenticator.java
	$(JAVAC15_CMD) -classpath $(CLASSPATH) -d $(CLASSBINDIR) $(JAVACFLAGS) $?
