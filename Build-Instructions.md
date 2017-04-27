JavaMail Build Instructions
===========================

To download the most recent JavaMail source code you'll need
[git](https://git-scm.com/downloads).

Once you've installed git, the following command will check out a copy
of the source code:

    % git clone git@github.com:javaee/javamail.git

Or, to check out the version corresponding to a particular release, use
a tag. For example, to check out the 1.4.7 version:

    % git clone -b JAVAMAIL-1_4_7 git@github.com:javaee/javamail.git

To build JavaMail you'll need [Maven](http://maven.apache.org/).

To simply build everything, use:

    % cd javamail
    % mvn install

You'll find the javax.mail.jar file in mail/target/javax.mail.jar.

See [Workspace Structure](Workspace Structure) for a description of the
workspace.
