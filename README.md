The JavaMail API provides a platform-independent and
protocol-independent framework to build mail and messaging
applications.
The JavaMail API is available as an optional package for use with the
[Java SE platform](http://www.oracle.com/technetwork/java/javase/index.html)
and is also included in the
[Java EE platform](http://www.oracle.com/technetwork/java/javaee/index.html).

# Table of Contents
* [Latest News](#Latest_News)
* [Download JavaMail Release](#Download_JavaMail_Release)
* [API Documentation](#API_Documentation)
* [Samples](#Samples)
* [Help](#Help)
* [Bugs](#Bugs)
* [Development Releases](#Development_Releases)
* [JavaMail for Android](#JavaMail_for_Android)
* [Project Documentation](#Project_Documentation)

# <a name="Latest_News"></a>Latest News

## April 28, 2017 - JavaMail moves to GitHub! ##

Welcome to the new home of the JavaMail API project on GitHub!
This project hosts the downloads and source code for the JavaMail API
reference implementation. The JavaMail reference implementation is
licensed under the
[Common Development and Distribution License (CDDL) v1.1 and GNU General Public
License (GPL) v2 with Classpath Exception](https://oss.oracle.com/licenses/CDDL+GPL-1.1).

By contributing to this project, you agree to these additional terms of
use, described in [CONTRIBUTING](CONTRIBUTING).

# <a name="Download_JavaMail_Release"></a>Download JavaMail Release

The latest release of JavaMail is 1.5.6.

The following table provides easy access to the latest release. Most
people will only need the main JavaMail reference implementation in the
javax.mail.jar file.

|Item|Description|
|:---|:----------|
|[javax.mail.jar](https://github.com/javaee/javamail/releases/download/JAVAMAIL-1_5_6/javax.mail.jar)|The JavaMail reference implementation, including the SMTP, IMAP, and POP3 protocol providers|
|[README.txt](docs/README.txt)|Overview of the release|
|[NOTES.txt](docs/NOTES.txt)|Additional notes about using JavaMail|
|[SSLNOTES.txt](docs/SSLNOTES.txt)|Notes on using SSL/TLS with JavaMail|
|[NTLMNOTES.txt](docs/NTLMNOTES.txt)|Notes on using NTLM authentication with JavaMail|
|[CHANGES.txt](docs/CHANGES.txt)|Changes since the previous release|
|[COMPAT.txt](docs/COMPAT.txt)|Important notes about compatibility|


In addition, the JavaMail jar files are published to the Maven repository. You can find them in both the [java.net Maven repository](http://maven.java.net), and in [Maven Central](http://search.maven.org).


|jar file|groupId|artifactId|Description|
|:-------|:------|:---------|:----------|
|[javax.mail.jar](https://maven.java.net/content/repositories/releases/com/sun/mail/javax.mail/1.5.6/javax.mail-1.5.6.jar)|com.sun.mail|javax.mail|The JavaMail reference implementation jar file, including the SMTP, IMAP, and POP3 protocol providers|
|[javax.mail-api.jar](https://maven.java.net/content/repositories/releases/javax/mail/javax.mail-api/1.5.6/javax.mail-api-1.5.6.jar)|javax.mail|javax.mail-api|The JavaMail API definitions only, suitable for compiling against|
|[mailapi.jar](https://maven.java.net/content/repositories/releases/com/sun/mail/mailapi/1.5.6/mailapi-1.5.6.jar)|com.sun.mail|mailapi|The JavaMail reference implementation with no protocol providers; use with one of the following providers|
|[smtp.jar](https://maven.java.net/content/repositories/releases/com/sun/mail/smtp/1.5.6/smtp-1.5.6.jar)|com.sun.mail|smtp|The SMTP protocol provider|
|[imap.jar](https://maven.java.net/content/repositories/releases/com/sun/mail/imap/1.5.6/imap-1.5.6.jar)|com.sun.mail|imap|The IMAP protocol provider|
|[pop3.jar](https://maven.java.net/content/repositories/releases/com/sun/mail/pop3/1.5.6/pop3-1.5.6.jar)|com.sun.mail|pop3|The POP3 protocol provider|
|[gimap.jar](https://maven.java.net/content/repositories/releases/com/sun/mail/gimap/1.5.6/gimap-1.5.6.jar)|com.sun.mail|gimap|An EXPERIMENTAL Gmail IMAP protocol provider that supports Gmail-specific features|
|[dsn.jar](https://maven.java.net/content/repositories/releases/com/sun/mail/dsn/1.5.6/dsn-1.5.6.jar)|com.sun.mail|dsn|Support for parsing and creating messages containing Delivery Status Notifications|
|[logging-mailhandler.jar](https://maven.java.net/content/repositories/releases/com/sun/mail/logging-mailhandler/1.5.6/logging-mailhandler-1.5.6.jar)|com.sun.mail|logging-mailhandler|A java.util.logging handler that uses JavaMail, suitable for use in Google App Engine.|

# <a name="API_Documentation"></a>API Documentation

The JavaMail API is defined through the Java Community Process as
[JSR 919](http://jcp.org/en/jsr/detail?id=919).

The JavaMail API documentation is available [here](docs/api/) and the
JavaMail specification is available [here](docs/JavaMail-1.5.pdf).

The following documents summarize the API changes in each release of
the JavaMail API specification:

-   [JavaMail 1.5](docs/JavaMail-1.5-changes.txt)
-   [JavaMail 1.4](docs/JavaMail-1.4-changes.txt)
-   [JavaMail 1.3](docs/JavaMail-1.3-changes.txt)
-   [JavaMail 1.2](docs/JavaMail-1.2-changes.txt)
-   [JavaMail 1.1](docs/JavaMail-1.1-changes.txt)

# <a name="Samples"></a>Samples

Some sample programs showing how to use the JavaMail APIs are available
[here](https://github.com/javaee/javamail/releases/download/JAVAMAIL-1_5_6/javamail-samples.zip).

# <a name="Help"></a>Help

Please read the
[JavaMail FAQ](FAQ.html)!
Read it again. Tell everyone you know to read it. Thank you!

You can search for and post questions to the
[JavaMail OTN forum](https://forums.oracle.com/forums/forum.jspa?forumID=975).
You can also subscribe to the
[mailing list](https://javaee.groups.io/g/javamail).

Or, post a question on [Stack Overflow](http://stackoverflow.com/) using the
[javamail](http://stackoverflow.com/questions/tagged/javamail) tag.

Finally, you can send mail directly to the JavaMail team at
<javamail_ww@oracle.com>.

# <a name="Bugs"></a>Bugs

JavaMail bugs are tracked in the
[GitHub JavaMail project issue tracker](https://github.com/javaee/javamail/issues).

# <a name="Development_Releases"></a>Development Releases

From time to time snapshot releases of the next version of JavaMail
under development are published to the
[java.net Maven repository](http://maven.java.net).
These snapshot releases have received only minimal testing, but may
provide previews of bug fixes or new features under development.

For example, you can download the javax.mail.jar file from the JavaMail
1.6.0-SNAPSHOT release
[here](https://maven.java.net/content/repositories/snapshots/com/sun/mail/javax.mail/1.6.0-SNAPSHOT/).
Be sure to scroll to the bottom and choose the jar file with the most
recent time stamp.

A [Release Candidate version of JavaMail 1.6.0](https://maven.java.net/content/repositories/releases/com/sun/mail/javax.mail/1.6.0-rc1/javax.mail-1.6.0-rc1.jar)
is now available. This is a non-final development release of JavaMail
1.6.0 suitable for testing. Your feedback on this release is
encouraged. The final release of JavaMail 1.6.0 is expected this
summer.

# <a name="JavaMail_for_Android"></a>JavaMail for Android

The latest release includes support for JavaMail on Android.
See the [Android](Android) page for details.

# <a name="Project_Documentation"></a>Project Documentation

You'll find more information about the protocol providers supported by
JavaMail on the following pages:

-   [ smtp ](SMTP-Transport)
-   [ imap ](IMAP-Store)
-   [ pop3 ](POP3-Store)
-   [ mbox ](Mbox-Store)
-   [ pop3remote ](POP3-Remote-Store)

If you're interested in writing your own protocol provider (most people
won't need to), you can find more documentation on protocol providers
[here](docs/Providers.pdf).

The following pages provide hints and tips for using particular mail servers:

-   [Gmail](Gmail)
-   [ Yahoo! Mail ](Yahoo)
-   [ Exchange and Office 365 ](Exchange)
-   [ Outlook.com ](Outlook)

The following pages provide hints and tips for using JavaMail on
particular operating systems or environments:

-   [Windows](Windows)
-   [Google App Engine](Google-App-Engine)

See [Build Instructions](Build-Instructions) for instructions on how to
download and build the most recent JavaMail source code. You can also
find a bundle of the source code for the most recent JavaMail release
in the [Releases](https://github.com/javaee/javamail/releases) area of
this project.

If you're interested in contributing to JavaMail, see the
[Contributions](Contributions) page.

You can find a list of products related to JavaMail on the
[Third Party Products](ThirdPartyProducts) page.

Please see our page of
[links to additional information about JavaMail and Internet email](Links)
and our list of
[books about JavaMail and Internet email](Books).

To understand the JavaMail license, see the [License](JavaMail-License) page.
