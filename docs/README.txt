			README
			======

	    JavaMail(TM) API 1.6.2 release
	    ------------------------------

Welcome to the JavaMail API 1.6.2 release!  This release includes
versions of the JavaMail API implementation, IMAP, SMTP, and POP3
service providers, some examples, and documentation for the JavaMail
API.

Please see the FAQ at https://javaee.github.io/javamail/FAQ

JDK Version notes
-----------------

The JavaMail API supports JDK 1.5 or higher.  Note that we have
currently tested this implementation with JDK 1.5, 1.6, 1.7, and 1.8.

While JavaMail will work with JAF 1.0.2, we recommend the use of JAF 1.1
or newer.  JAF 1.1.1 is currently the newest version.  Note that JAF 1.1
is included in JDK 1.6 and JAF 1.1.1 is included in JDK 1.6.0_10 and
later.


Protocols supported
-------------------

This release supports the following Internet standard mail protocols:

    IMAP - a message Store protocol, for reading messages from a server
    POP3 - a message Store protocol, for reading messages from a server
    SMTP - a message Transport protocol, for sending messages to a server

The following table lists the names of the supported protocols (as used
in the JavaMail API) and their capabilities:

	Protocol	Store or	Uses	Supports
	Name		Transport?	SSL?	STARTTLS?
	-------------------------------------------------
	imap		Store		No	Yes
	imaps		Store		Yes	N/A
	gimap		Store		Yes	N/A
	pop3		Store		No	Yes
	pop3s		Store		Yes	N/A
	smtp		Transport	No	Yes
	smtps		Transport	Yes	N/A

See our web page at http://www.oracle.com/technetwork/java/javamail/
for the latest information on third party protocol providers.


Download
--------

See the JavaMail project page to download this release.

	https://javaee.github.io/javamail/


Requirements
------------

Note that the JavaMail API requires the JavaBeans(TM) Activation
Framework package to be installed as well if you're using JDK 1.5.
Download the latest version of the JavaBeans Activation Framework from

	http://www.oracle.com/technetwork/java/javase/index-jsp-136939.html

and install it in a suitable location.


Installation
------------

  UNIX/Linux
  ----------

  1. Download the javax.mail.jar file from the JavaMail project website.
     https://github.com/javaee/javamail/releases

  2. Set your CLASSPATH to include the "javax.mail.jar" file obtained from
     the download, as well as the current directory.

     Assuming you have downloaded javax.mail.jar to the /u/me/download/
     directory, the following would work:

      export CLASSPATH=$CLASSPATH:/u/me/download/javax.mail.jar:.

    (Don't forget the trailing "." for the current directory.)
    Also, if you're using JDK 1.5, include the "activation.jar" file that you
    obtained from downloading the JavaBeans Activation Framework.  For example:

      export CLASSPATH=$CLASSPATH:/u/me/download/activation/activation.jar

  3. Download the javamail-samples.zip file from the project website.
     https://github.com/javaee/javamail/releases

  4. Compile any sample program using your Java compiler. For example:

      javac msgshow.java

  5. Run the sample program.  The '-' option lists the required and optional
     command-line options to successfully run any sample.  For example:

      java msgshow -

    lists the available options.  And

      java msgshow -T imap -H <mailserver> -U <username> -P <passwd> -f INBOX 5

    uses the IMAP protocol to display message number 5 from your INBOX.

  (Additional instructions on how to run the simple mail reader sample
  and servlet sample are provided in client/README.txt and servlet/README.txt,
  respectively.)


  Windows
  -------

  1. Download the javax.mail.jar file from the JavaMail project website.
     https://github.com/javaee/javamail/releases

  2. Set your CLASSPATH to include the "javax.mail.jar" file obtained from
     the download, as well as the current directory.

     Assuming you have downloaded javax.mail.jar to the /u/me/download/
     directory, the following would work:

      set CLASSPATH=%CLASSPATH%;c:\download\javax.mail.jar;.

    (Don't forget the trailing "." for the current directory.)
    Also, if you're using JDK 1.5, include the "activation.jar" file that you
    obtained from downloading the JavaBeans Activation Framework.  For example:

      set CLASSPATH=%CLASSPATH%;c:\download\activation\activation.jar

  3. Download the javamail-samples.zip file from the project website.
     https://github.com/javaee/javamail/releases

  4. Compile any sample program using your Java compiler. For example:

      javac msgshow.java

  5. Run the sample program.  The '-' option lists the required and optional
     command-line options to successfully run any sample.  For example:

      java msgshow -

    lists the available options.  And

      java msgshow -T imap -H <mailserver> -U <username> -P <passwd> -f INBOX 5

    uses the IMAP protocol to display message number 5 from your INBOX.

  (Additional instructions on how to run the simple mail reader sample
  and servlet sample are provided in client/README.txt and servlet/README.txt,
  respectively.)


Problems?
---------

The JavaMail FAQ at https://javaee.github.io/javamail/FAQ
includes information on protocols supported, installation problems,
debugging tips, etc.

See the NOTES.txt file for information on how to report bugs.

Enjoy!

The JavaMail API Team
