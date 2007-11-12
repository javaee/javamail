#
#
# Makefile for building all of javax
#
# Targets and their description:
#
#	all
#		builds everything, javax, com, demo
#
#	jar
#		creates the mail.jar file for internal user
#
#	mailjar
#		creates the mail.jar file for release
#
#	jar_all
#		first makes "all", then makes "jar" of *all* class files
#
#	install
#		creates a jar file that contains only
#		the packages we ship.
#
#	project
#		creates all the JavaWorkShop project files
#
#	release
#		removes everything (realclean) then
#		creates the release zip file that includes everything
#		that is shipped in the javamail.zip file
#		IMPORTANT: javamail.pdf, and
#		Provider.pdf should be copied to the 
#		$SPEC_DIR directory prior to building 
#		this target
#	
#	docs
#		builds javadocs
#
#	clean
#		removes all the class files and binaries
#	
#	realclean
#		clean + removes mail.jar & javamail.zip
#
# 	mbox
#		makes mbox

BUILDDIR = .
include $(BUILDDIR)/Platform.gmk
include $(BUILDDIR)/Version.gmk

PRODUCT = sun

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Defs.gmk

ZIP = /usr/local/bin/zip

LIBSRC = $(TOPDIR)/src/share/lib

JAVAMAIL_RELEASE = $(VERSION)
JAVAMAIL_RELEASEB = $(shell echo $(JAVAMAIL_RELEASE) | tr '.' '_')
#JAVAMAIL_RELEASEDIR  = $(BUILDDIR)/javamail-$(JAVAMAIL_RELEASE)
JAVAMAIL_RELEASEDIR  = javamail-$(JAVAMAIL_RELEASE)
JAVAMAIL_RELEASEFILE = $(RELEASEDIR)/javamail$(JAVAMAIL_RELEASEB).zip

# for mbox
MBOX_RELEASEDIR = $(BUILDDIR)/mbox
MBOX_RELEASEFILE = $(RELEASEDIR)/mbox.zip

# specs
SPEC_DIR = /net/anybodys.sfbay/export6/javamail/spec
RELEASE_SPECS = $(SPEC_DIR)/JavaMail-1.4.pdf $(SPEC_DIR)/Providers.pdf
11_TXT = $(TOPDIR)/src/share/doc/spec/JavaMail-1.1-changes.txt
12_TXT = $(TOPDIR)/src/share/doc/spec/JavaMail-1.2-changes.txt
13_TXT = $(TOPDIR)/src/share/doc/spec/JavaMail-1.3-changes.txt
14_TXT = $(TOPDIR)/src/share/doc/spec/JavaMail-1.4-changes.txt


SUBDIRS = javax com demo META-INF

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Subdirs.gmk

include $(TOPDIR)/build/$(PLATFORM)/makefiles/Project.gmk

jar ::
	@echo Jarring ...
	@sed -e 's"%RELEASE%"$(JAVAMAIL_RELEASE)"' < $(LIBSRC)/javamail.mf \
					> classes/javamail.mf
	@cd classes; $(JAR) cfm ../mail.jar javamail.mf \
			META-INF \
			javax \
			com

jar_all :: all jar sigtest

install :: mailjar imapjar smtpjar pop3jar dsnjar sigtest

# generate a new signature file
siggen ::
	sh siggen -in mail.jar -package javax -out mail.sig

# test that API hasn't changed
sigtest ::
	sh sigtest -base mail.sig -test mail.jar -package javax \
		-out report.out -check bincomp

mailjar ::
	@rm -f classes/META-INF/javamail.providers
	@rm -f classes/META-INF/javamail.address.map
	@sed -e 's"%RELEASE%"$(JAVAMAIL_RELEASE)"' < $(LIBSRC)/javamail.mf \
					> classes/javamail.mf
	@cd classes; $(JAR) cfm ../mail.jar javamail.mf \
			META-INF \
			javax \
			com/sun/mail/util \
			com/sun/mail/smtp \
			com/sun/mail/iap \
			com/sun/mail/imap \
			`find com/sun/mail/pop3 -type f -print | \
				grep -v POP3RemoteStore` \
			com/sun/mail/handlers
	@cd classes; $(JAR) cfm ../mailapi.jar javamail.mf \
			`find META-INF -type f -print | \
				grep -v javamail.default.providers | \
				grep -v javamail.default.address.map` \
			javax \
			com/sun/mail/util \
			com/sun/mail/handlers

# Javadoc defines
DOCDIR = $(TOPDIR)/src/share/doc/api/javadocs
DOCPACKAGES =	javax.mail javax.mail.internet javax.mail.search \
		javax.mail.event javax.mail.util \
		com.sun.mail.imap com.sun.mail.pop3 com.sun.mail.smtp \
		com.sun.mail.dsn
DOCFILES = \
	src/share/classes/overview.html \
	src/share/classes/javax/mail \
	src/share/classes/com/sun/mail/imap/package.html \
	src/share/classes/com/sun/mail/imap/IMAPFolder.java \
	src/share/classes/com/sun/mail/imap/IMAPStore.java \
	src/share/classes/com/sun/mail/imap/IMAPSSLStore.java \
	src/share/classes/com/sun/mail/imap/ACL.java \
	src/share/classes/com/sun/mail/imap/Rights.java \
	src/share/classes/com/sun/mail/imap/Quota.java \
	src/share/classes/com/sun/mail/pop3/package.html \
	src/share/classes/com/sun/mail/pop3/POP3Store.java \
	src/share/classes/com/sun/mail/pop3/POP3SSLStore.java \
	src/share/classes/com/sun/mail/pop3/POP3Folder.java \
	src/share/classes/com/sun/mail/pop3/POP3Message.java \
	src/share/classes/com/sun/mail/smtp/package.html \
	src/share/classes/com/sun/mail/smtp/SMTPMessage.java \
	src/share/classes/com/sun/mail/smtp/SMTPAddressFailedException.java \
	src/share/classes/com/sun/mail/smtp/SMTPAddressSucceededException.java \
	src/share/classes/com/sun/mail/smtp/SMTPSendFailedException.java \
	src/share/classes/com/sun/mail/smtp/SMTPTransport.java \
	src/share/classes/com/sun/mail/smtp/SMTPSSLTransport.java \
	src/share/classes/com/sun/mail/dsn/package.html \
	src/share/classes/com/sun/mail/dsn/DeliveryStatus.java \
	src/share/classes/com/sun/mail/dsn/MessageHeaders.java \
	src/share/classes/com/sun/mail/dsn/MultipartReport.java
DOCFILESEX = \
	src/share/classes/javax/mail/SecuritySupport.java

# for "docs" target (set to 1.5.0 so that newer javadoc is used)
JAVADOC_JDK = /java/re/jdk/1.5.0/archive/fcs/binaries/solaris-sparc
JAVADOC_OPTS=	-J-Xmx32m
TITLE="JavaMail API documentation"

# XXX - Would like to use standard "docs" rule but we need to do
#	some special things here.  In order to generate javadocs
#	for only the limited set of classes listed in DOCFILES,
#	we have to copy all those files to a temporary directory
#	and run javadoc against that directory.

JAVADOC_OPTS += -use -splitIndex -windowtitle $(TITLE) -doctitle $(TITLE)

docs:
	rm -rf $(DOCDIR)
	mkdir -p $(DOCDIR)
	mkdir $(DOCDIR)/tmp
	(cd $(TOPDIR); tar cFFf - $(DOCFILES)) | (cd $(DOCDIR)/tmp; tar xf -)
	(cd $(DOCDIR)/tmp; rm -f $(DOCFILESEX))
	$(JAVADOC_JDK)/bin/javadoc $(JAVADOC_OPTS) -d $(DOCDIR) \
		-group "JavaMail API Packages" "javax.*" \
		-group "Sun-specific Packages" "com.sun.*" \
		-overview $(DOCDIR)/tmp/src/share/classes/overview.html \
		-sourcepath $(DOCDIR)/tmp/src/share/classes \
		-classpath $(CLASSBINDIR):$(JAF_JAR):$(DEPENDS) $(DOCPACKAGES)
	rm -rf $(DOCDIR)/tmp

realclean :: clean
	@rm -f mail.jar $(JAVAMAIL_RELEASEFILE) \
		pop3.jar imap.jar smtp.jar dsn.jar mailapi.jar \
		mbox.jar $(MBOX_RELEASEFILE)

imapjar ::
	@cp $(LIBSRC)/javamail.imap.provider classes/META-INF/javamail.providers
	@sed -e 's"%RELEASE%"$(JAVAMAIL_RELEASE)"' < $(LIBSRC)/imap.mf \
					> classes/imap.mf
	@cd classes; $(JAR) cfm ../imap.jar imap.mf \
		META-INF/javamail.providers \
		com/sun/mail/iap \
		com/sun/mail/imap
	@rm -f classes/META-INF/javamail.providers

smtpjar ::
	@cp $(LIBSRC)/javamail.smtp.provider classes/META-INF/javamail.providers
	@cp $(LIBSRC)/javamail.smtp.address.map classes/META-INF/javamail.address.map
	@sed -e 's"%RELEASE%"$(JAVAMAIL_RELEASE)"' < $(LIBSRC)/smtp.mf \
					> classes/smtp.mf
	@cd classes; $(JAR) cfm ../smtp.jar smtp.mf \
		META-INF/javamail.providers \
		META-INF/javamail.address.map \
		com/sun/mail/smtp
	@rm -f classes/META-INF/javamail.providers
	@rm -f classes/META-INF/javamail.address.map

pop3jar ::
	@cp $(LIBSRC)/javamail.pop3.provider classes/META-INF/javamail.providers
	@sed -e 's"%RELEASE%"$(JAVAMAIL_RELEASE)"' < $(LIBSRC)/pop3.mf \
					> classes/pop3.mf
	@cd classes; $(JAR) cfm ../pop3.jar pop3.mf \
		META-INF/javamail.providers \
		`find com/sun/mail/pop3 -type f -print | grep -v POP3RemoteStore`
	@rm -f classes/META-INF/javamail.providers

dsnjar ::
	@sed -e 's"%RELEASE%"$(JAVAMAIL_RELEASE)"' < $(LIBSRC)/dsn.mf \
					> dsn_classes/dsn.mf
	@cd dsn_classes; $(JAR) cfm ../dsn.jar dsn.mf \
		META-INF/mailcap \
		com/sun/mail/dsn

mbox :: all mboxzip

mboxjar ::
	@cp $(LIBSRC)/javamail.mbox.provider classes/META-INF/javamail.providers
	@cd classes; $(JAR) cf ../mbox.jar \
			META-INF/javamail.providers \
			com/sun/mail/mbox
	@rm -rf classes/META-INF/javamail.providers

mboxzip :: mboxjar
	@echo Making mbox release ...
	@rm -rf $(MBOX_RELEASEFILE) $(MBOX_RELEASEDIR)
	@mkdir $(MBOX_RELEASEDIR)
	@cp mbox.jar $(MBOX_RELEASEDIR)
	@cp $(TOPDIR)/src/share/doc/release/LICENSE.txt \
			$(MBOX_RELEASEDIR)/LICENSE.txt
	#@cp $(TOPDIR)/src/share/doc/release/POP3.README.txt \
	#		$(MBOX_RELEASEDIR)/README.txt
	@cp -r lib $(MBOX_RELEASEDIR)
	@$(ZIP) -r $(MBOX_RELEASEFILE) $(MBOX_RELEASEDIR)
	@rm -rf $(MBOX_RELEASEDIR)
	@echo Done Making release

# The "release" target is ugly-ish. Should clean up later.
release :: realclean all install docs releasezip

releasezip:
	@echo Making release ...
	@rm -rf $(JAVAMAIL_RELEASEFILE) $(JAVAMAIL_RELEASEDIR)
	@[ -d $(RELEASEDIR) ] || mkdir $(RELEASEDIR)
	@mkdir $(JAVAMAIL_RELEASEDIR)
	@mkdir $(JAVAMAIL_RELEASEDIR)/lib
	@mkdir $(JAVAMAIL_RELEASEDIR)/docs
	@mkdir $(JAVAMAIL_RELEASEDIR)/docs/javadocs
	@mkdir -p $(JAVAMAIL_RELEASEDIR)/demo/client
	@mkdir -p $(JAVAMAIL_RELEASEDIR)/demo/servlet
	@mkdir -p $(JAVAMAIL_RELEASEDIR)/demo/webapp
	@cp mail.jar $(JAVAMAIL_RELEASEDIR)
	@cp mailapi.jar imap.jar smtp.jar pop3.jar dsn.jar $(JAVAMAIL_RELEASEDIR)/lib
	@rm -f $(RELEASEDIR)/CHANGES.txt
	@cp $(TOPDIR)/src/share/doc/release/CHANGES.txt $(RELEASEDIR)
	@rm -f $(RELEASEDIR)/COMPAT.txt
	@cp $(TOPDIR)/src/share/doc/release/COMPAT.txt $(RELEASEDIR)
	@rm -f $(RELEASEDIR)/README.txt
	@cp $(TOPDIR)/src/share/doc/release/README.txt $(RELEASEDIR)
	@rm -f $(RELEASEDIR)/LICENSE.txt
	@cp $(TOPDIR)/src/share/doc/release/LICENSE.txt $(RELEASEDIR)
	@rm -f $(RELEASEDIR)/distributionREADME.txt
	@cp $(TOPDIR)/src/share/doc/release/distributionREADME.txt $(RELEASEDIR)
	@rm -f $(RELEASEDIR)/NOTES.txt
	@cp $(TOPDIR)/src/share/doc/release/NOTES.txt $(RELEASEDIR)
	@rm -f $(RELEASEDIR)/SSLNOTES.txt
	@cp $(TOPDIR)/src/share/doc/release/SSLNOTES.txt $(RELEASEDIR)
	@rm -rf $(RELEASEDIR)/javadocs
	@cp -r $(TOPDIR)/src/share/doc/api/javadocs $(RELEASEDIR)
	@cp $(TOPDIR)/src/share/doc/release/CHANGES.txt $(JAVAMAIL_RELEASEDIR)
	@cp $(TOPDIR)/src/share/doc/release/COMPAT.txt $(JAVAMAIL_RELEASEDIR)
	@cp $(TOPDIR)/src/share/doc/release/README.txt $(JAVAMAIL_RELEASEDIR)
	@cp $(TOPDIR)/src/share/doc/release/LICENSE.txt $(JAVAMAIL_RELEASEDIR)
	@cp $(TOPDIR)/src/share/doc/release/distributionREADME.txt $(JAVAMAIL_RELEASEDIR)
	@cp $(TOPDIR)/src/share/doc/release/NOTES.txt $(JAVAMAIL_RELEASEDIR)
	@cp $(TOPDIR)/src/share/doc/release/SSLNOTES.txt $(JAVAMAIL_RELEASEDIR)
	@cp $(TOPDIR)/src/share/classes/demo/*.java $(JAVAMAIL_RELEASEDIR)/demo
	@cp $(TOPDIR)/src/share/classes/demo/README.txt $(JAVAMAIL_RELEASEDIR)/demo
	@cp $(TOPDIR)/src/share/classes/demo/client/*.java $(JAVAMAIL_RELEASEDIR)/demo/client
	@cp $(TOPDIR)/src/share/classes/demo/client/simple.mailcap $(JAVAMAIL_RELEASEDIR)/demo/client
	@cp $(TOPDIR)/src/share/classes/demo/client/README.txt $(JAVAMAIL_RELEASEDIR)/demo/client
	@cp $(TOPDIR)/src/share/classes/demo/servlet/*.java $(JAVAMAIL_RELEASEDIR)/demo/servlet
	@cp $(TOPDIR)/src/share/classes/demo/servlet/*.html $(JAVAMAIL_RELEASEDIR)/demo/servlet
	@cp $(TOPDIR)/src/share/classes/demo/servlet/README.txt $(JAVAMAIL_RELEASEDIR)/demo/servlet
	@(cd $(TOPDIR)/src/share/classes/demo/webapp; tar cfFF - .) | \
		(cd $(JAVAMAIL_RELEASEDIR)/demo/webapp; tar xf -)
	@cp -r $(TOPDIR)/src/share/doc/api/javadocs $(JAVAMAIL_RELEASEDIR)/docs/
	@for i in $(RELEASE_SPECS); do \
		if [ -f $$i ]; then \
			cp $$i $(RELEASEDIR); \
			cp $$i $(JAVAMAIL_RELEASEDIR)/docs; \
		fi \
	done
	@if [ -f $(11_TXT) ] ;  then cp $(11_TXT) $(JAVAMAIL_RELEASEDIR)/docs; fi
	@if [ -f $(12_TXT) ] ;  then cp $(12_TXT) $(JAVAMAIL_RELEASEDIR)/docs; fi
	@if [ -f $(13_TXT) ] ;  then cp $(13_TXT) $(JAVAMAIL_RELEASEDIR)/docs; fi
	@if [ -f $(14_TXT) ] ;  then cp $(14_TXT) $(JAVAMAIL_RELEASEDIR)/docs; fi
	@$(ZIP) -r $(JAVAMAIL_RELEASEFILE) $(JAVAMAIL_RELEASEDIR)
	@rm -rf $(JAVAMAIL_RELEASEDIR)
	@echo Done Making release
