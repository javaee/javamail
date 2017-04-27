JavaMail Mbox Provider
======================

The mbox provider is a JavaMail local store provider that manages files
in [ Unix mbox format ](http://en.wikipedia.org/wiki/Mbox)
(in particular, the *mboxcl2* variant).

The mbox provider has only been tested on Solaris and OpenSolaris. It
should be easy to make it work on Linux, but I haven't gotten around to
that yet. I tried it once on Windows, long ago, but there's no file
locking support for use on Windows. On Solaris and OpenSolaris it
depends on native file locking code.

To build the mbox provider:

    % (cd mbox; mvn)
    % (cd mbox/native; mvn)

This depends on having the **c89** command in your PATH.

To use the mbox provider you'll need to add mbox/target/mbox.jar to
your CLASSPATH and add mbox/native/target/libmbox.so to your
LD\_LIBRARY\_PATH.
