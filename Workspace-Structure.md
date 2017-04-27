JavaMail Workspace Structure
============================

Here's the structure of the JavaMail workspace, showing the different
maven modules I needed to create to allow JavaMail to be built by
maven.

First, the main maven module, including the assembly descriptor used to
build the distribution zip file.

    assembly.xml
    pom.xml

Then, some files left over from the ant build.

    ant-common.xml
    build.properties
    build.xml
    mbox.xml
    project.properties
    project.xml

Finally, some files for checking that the API signatures match the spec
(not yet integrated into the build).

    mail.sig
    siggen
    sigtest

The main JavaMail source code module, containing all the code that goes
into mail.jar.

    mail/pom.xml
    mail/src/main/java/com/sun/mail/handlers/image_gif.java
    ...
    mail/src/main/java/com/sun/mail/util/UUEncoderStream.java
    mail/src/main/java/javax/mail/Address.java
    ...
    mail/src/main/java/javax/mail/util/package.html
    mail/src/main/java/overview.html
    mail/src/main/resources/META-INF/MANIFEST.MF
    mail/src/main/resources/META-INF/javamail.charset.map
    mail/src/main/resources/META-INF/javamail.default.address.map
    mail/src/main/resources/META-INF/javamail.default.providers
    mail/src/main/resources/META-INF/mailcap
    mail/src/oldtest/java/javax/mail/internet/addrlist
    ...
    mail/src/oldtest/java/javax/mail/internet/tokenlist

Several modules containing demo source code. They're buildable to make
sure they do build before shipping them, but they're shipped only as
source code. Note the embedded README.txt files. Where should they
really come from? I can put them in another location, but that would
just complicate the assembly descriptor.

    demo/pom.xml
    demo/src/main/java/CRLFOutputStream.java
    demo/src/main/java/NewlineOutputStream.java
    demo/src/main/java/README.txt
    demo/src/main/java/copier.java
    demo/src/main/java/folderlist.java
    demo/src/main/java/internal/...
    demo/src/main/java/monitor.java
    demo/src/main/java/mover.java
    demo/src/main/java/msgmultisendsample.java
    demo/src/main/java/msgsend.java
    demo/src/main/java/msgsendsample.java
    demo/src/main/java/msgshow.java
    demo/src/main/java/namespace.java
    demo/src/main/java/populate.java
    demo/src/main/java/registry.java
    demo/src/main/java/search.java
    demo/src/main/java/sendfile.java
    demo/src/main/java/sendhtml.java
    demo/src/main/java/smtpsend.java
    demo/src/main/java/transport.java
    demo/src/main/java/uidmsgshow.java
    client/pom.xml
    client/src/main/java/ComponentFrame.java
    client/src/main/java/FolderModel.java
    client/src/main/java/FolderTreeNode.java
    client/src/main/java/FolderViewer.java
    client/src/main/java/MessageViewer.java
    client/src/main/java/MultipartViewer.java
    client/src/main/java/README.txt
    client/src/main/java/SimpleAuthenticator.java
    client/src/main/java/SimpleClient.java
    client/src/main/java/StoreTreeNode.java
    client/src/main/java/TextViewer.java
    client/src/main/java/simple.mailcap
    servlet/pom.xml
    servlet/src/main/java/JavaMail.html
    servlet/src/main/java/JavaMailServlet.java
    servlet/src/main/java/README.txt
    webapp/build.bat
    webapp/build.sh
    webapp/pom.xml
    webapp/src/main/java/demo/AttachmentServlet.java
    webapp/src/main/java/demo/FilterServlet.java
    webapp/src/main/java/demo/MailUserBean.java
    webapp/src/main/webapp/WEB-INF/web.xml
    webapp/src/main/webapp/compose.jsp
    webapp/src/main/webapp/errordetails.jsp
    webapp/src/main/webapp/errorpage.jsp
    webapp/src/main/webapp/folders.jsp
    webapp/src/main/webapp/index.html
    webapp/src/main/webapp/login.jsp
    webapp/src/main/webapp/logout.jsp
    webapp/src/main/webapp/messagecontent.jsp
    webapp/src/main/webapp/messageheaders.jsp
    webapp/src/main/webapp/send.jsp
    webapp/webapp.README.txt
    taglib/pom.xml
    taglib/src/main/java/demo/AttachmentInfo.java
    taglib/src/main/java/demo/ListAttachmentsTEI.java
    taglib/src/main/java/demo/ListAttachmentsTag.java
    taglib/src/main/java/demo/ListMessagesTEI.java
    taglib/src/main/java/demo/ListMessagesTag.java
    taglib/src/main/java/demo/MessageInfo.java
    taglib/src/main/java/demo/MessageTEI.java
    taglib/src/main/java/demo/MessageTag.java
    taglib/src/main/java/demo/SendTag.java
    taglib/src/main/resources/META-INF/taglib.tld

Several modules that extract subsets of the mail.jar file to build
other jar files. The source code for each of these jar files could be
moved into these modules, and then mail.jar could be constructed by
combining all these jar files. I did it this way because I like having
all the source code in one place, but it's probably less maven-like.

    mailapi/pom.xml
    imap/pom.xml
    imap/src/main/resources/META-INF/MANIFEST.MF
    imap/src/main/resources/META-INF/javamail.providers
    pop3/pom.xml
    pop3/src/main/resources/META-INF/MANIFEST.MF
    pop3/src/main/resources/META-INF/javamail.providers
    smtp/pom.xml
    smtp/src/main/resources/META-INF/MANIFEST.MF
    smtp/src/main/resources/META-INF/javamail.address.map
    smtp/src/main/resources/META-INF/javamail.providers

A module to act as parent of the imap, pop3, and smtp modules to allow
sharing of some common rules.

    parent-distrib/pom.xml

A module that contains only the Delivery Status Notification support. I
moved all the source code here because none of this appears in
mail.jar.

    dsn/pom.xml
    dsn/src/main/java/com/sun/mail/dsn/DeliveryStatus.java
    dsn/src/main/java/com/sun/mail/dsn/MessageHeaders.java
    dsn/src/main/java/com/sun/mail/dsn/MultipartReport.java
    dsn/src/main/java/com/sun/mail/dsn/message_deliverystatus.java
    dsn/src/main/java/com/sun/mail/dsn/multipart_report.java
    dsn/src/main/java/com/sun/mail/dsn/package.html
    dsn/src/main/java/com/sun/mail/dsn/text_rfc822headers.java
    dsn/src/main/resources/META-INF/MANIFEST.MF
    dsn/src/main/resources/META-INF/mailcap

The mbox protocol provider module. Again, source code moved here
because none of this appears in mail.jar. Also includes a submodule to
build the native code (even though the native source code is in the
upper module; is that too weird?), and a submodule to build a
distribution zip file containing all of the mbox provider. Possibly
this should be inverted so that the distribution module is the top
level module and the java and native modules are submodules.

    mbox/pom.xml
    mbox/src/main/cpp/com/sun/mail/mbox/UNIXFile.c
    mbox/src/main/cpp/com/sun/mail/mbox/UNIXInbox.c
    mbox/src/main/java/com/sun/mail/mbox/ContentLengthCounter.java
    ...
    mbox/src/main/java/com/sun/mail/mbox/UNIXInbox.java
    mbox/src/main/java/com/sun/mail/remote/POP3RemoteStore.java
    mbox/src/main/java/com/sun/mail/remote/RemoteDefaultFolder.java
    mbox/src/main/java/com/sun/mail/remote/RemoteInbox.java
    mbox/src/main/java/com/sun/mail/remote/RemoteStore.java
    mbox/src/main/resources/META-INF/MANIFEST.MF
    mbox/src/main/resources/META-INF/javamail.providers
    mbox/native/pom.xml
    mbox/dist/assembly.xml
    mbox/dist/pom.xml

A module just for building the javadocs. Putting these rules in the
parent pom.xml just didn't work so I moved them here.

    javadoc/pom.xml

Finally, the documentation. Not a module, but most of it is included in
the distribution zip file.

    doc/release/ApacheJServ.html
    doc/release/BLURB
    doc/release/CHANGES.txt
    doc/release/COMPAT.txt
    doc/release/JavaWebServer.html
    doc/release/LICENSE.txt
    doc/release/NOTES.txt
    doc/release/README.txt
    doc/release/SSLNOTES.txt
    doc/release/Tomcat.html
    doc/release/classpath-NT.html
    doc/release/distributionREADME.txt
    doc/release/iPlanet.html
    doc/release/images/direct-classpath.jpg
    doc/release/images/indirect-classpath.jpg
    doc/spec/JavaMail-1.1-changes.txt
    doc/spec/JavaMail-1.2-changes.txt
    doc/spec/JavaMail-1.3-changes.txt
    doc/spec/JavaMail-1.4-changes.txt
