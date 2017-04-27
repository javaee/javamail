mbox Store
==========

The mbox Store supports reading messages from files in the UNIX
[mbox](http://en.wikipedia.org/wiki/Mbox) format. The mbox Store is
included in the JavaMail source code, but is not currently distributed
with JavaMail. To use the mbox Store, you'll need to build it
yourself.

### File Locking

The mbox Store supports several file locking choices. To properly
interact with other native programs accessing UNIX mailboxes, native
file locking code is required. This native file locking code has only
been tested on Solaris, and requires a native compiler to build. Native
code is used to access the Solaris mailbox locking functions in the
libmail library. Unfortunately, the mailbox locking protocol depends on
creating hard links, which is not supported before JDK 7.

The mbox Store can also use Java file locking support, which will allow
coordination between multiple applications using the mbox Store, but
not with native applications accessing the same mailboxes.

Finally, the mbox Store can operate with no locking at all. This is
only appropriate if you're sure that only a single instance of your
Java application is accessing the mailbox, and no native applications
are accessing the mailbox. You'll need to coordinate access to the
mailbox within your application to ensure that each mailbox is accessed
by only a single thread at a time.

The file locking options are selected by setting the mail.mbox.locktype
System property:

|Lock Type|Description|
|---------|-----------|
|native|This is the default, which requires native code as described above.|
|java|This uses java.nio.channels.FileLock.|
|none|No file locking is done|

### Mailbox Names

Mailbox names are of the form **mbox:*name***. If ***name*** is a
relative path name, it is normally relative to the current directory.
If the System property mail.mbox.homerelative is set to true, relative
names are relative to the current user's home directory.

The mailbox name can also be of the form **mbox:\~/*name***, which is
always relative to the current user's home directory, or
**mbox:\~*user/name***, which is relative to the given user's home
directory. (The latter only works on Solaris.)

The mailbox name **mbox:INBOX** is the current user's Inbox, e.g., in
/var/mail on Solaris.

### Build Instructions

To build the mbox Store provider, assuming the "c89" compiler is in
your PATH and the JDK is in /usr/java:

    export MACH=`uname -p`
    export JAVA_HOME=/usr/java
    cd mbox
    mvn
    cd native
    mvn

You can override the default options for the compiler and linker for
the native component by specifying Maven properties. The defaults
correspond to this:

    mvn -Dcompiler.name=c89 \
        -Dcompiler.start.options='-Xa -xO2 -v -D_REENTRANT -I${env.JAVA_HOME}/include -I${env.JAVA_HOME}/include/solaris' \
        -Dlinker.name=c89 \
        -Dlinker.start.options='-G' \
        -Dlinker.end.options='-L${env.JAVA_HOME}/jre/lib/${env.MACH} -lmail -ljava -lc'

XXX - Still need to provide more information.
