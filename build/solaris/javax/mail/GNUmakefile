#
#
# Makefile for building all of javax.mail.*
#

BUILDDIR = ../..
include $(BUILDDIR)/Platform.gmk

PRODUCT = sun
PACKAGE = javax.mail

SUBDIRS = internet

include $(BUILDDIR)/Version.gmk

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

include $(MINCLUDES)javax_mail.jmk

VERSIONCLASS=$(SHARE_SRC)/classes/javax/mail/Version.java

all :: $(VERSIONCLASS) $(CLASSBINDIR) classes

$(VERSIONCLASS): $(VERSIONCLASS).template $(BUILDDIR)/Version.gmk
	sed -e 's/@VERSION@/$(VERSION)/' < $(VERSIONCLASS).template > $@

# I don't know why the implicit rule doesn't work for this, but it doesn't...
$(CLASSBINDIR)/javax/mail/Version.class: $(VERSIONCLASS)
	@echo $? >>.classes.list

clean clobber :: classes.clean

include $(BUILDDIR)/makefiles/Subdirs.gmk

include $(BUILDDIR)/makefiles/Classes.gmk
