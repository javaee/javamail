Logging Demo
------------

Notes:
======

This should not be taken as a demo of how to use the logging API, but
rather how to use the features of the MailHandler.

To run the demo:
================

    1.  The demo requires Java version 1.4 or newer.
	We *strongly* encourage you to use the latest version of J2SE,
	which you can download from http://java.sun.com/j2se/.

    2.  Set your CLASSPATH to include the "mail.jar" and "activation.jar".

	For JDK 1.1 on UNIX:

	export CLASSPATH=/u/me/download/mail.jar:/u/me/download/activation.jar.

	For JDK 1.2 and newer on UNIX:

	export CLASSPATH=/u/me/download/mail.jar:/u/me/download/activation.jar:.

    3.  Go to the demo/logging directory

    4.  Compile all the files using your Java compiler.  For example:

	  javac *.java
	  
    5.  Not required but, you should edit the maildemo.properties and change the 
        mail.to address and mail.host to your mail server ip or host name.

    6.  Run the demo. For example:

	  java -Dmail.debug=false -Djava.util.logging.config.file=/u/me/download/javamail/demo/maildemo.properties MailHandlerDemo



Overview of the Classes
=======================

Main Classes:

	MailHandlerDemo       = The main method creates log messages
				for the MailHander to capture.  The
				initXXX methods describe some of the
				common setup code for different types
				of email messages.
	
	FileErrorManager      = Used to store email messages to the
				local file system when mail transport
				fails.  This is installed as a
				fallback in case the logging config is
				not specified.

	SummaryFormatter      = An example compact formatter for the
				body of an email message.

	SummaryNameFormatter  = An example formatter used to generate
				subject lines and attachment file
				names based on the contents of the
				email message.

Support files:

	maildemo.properties   = A sample LogManager properties file for
				the MailHandlerDemo.
