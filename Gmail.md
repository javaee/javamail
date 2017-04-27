Gmail
=====

This page describes hints and tips for using JavaMail with Gmail. Basic
Gmail usage information can be found in the
[JavaMail FAQ](FAQ.html#gmail).

Gmail users might want to read the
[IMAP Issues page in Gmail Help](http://support.google.com/mail/bin/topic.py?hl=en&topic=1668982&parent=1668981&ctx=topic).

To send email using Gmail with a different address than the one you
signed in with, see this
[Gmail help page](https://support.google.com/mail/bin/answer.py?hl=en&answer=22370).

You may need to enable
[less secure apps](https://www.google.com/settings/security/lesssecureapps)
to use JavaMail with Gmail.

As of Mar 21, 2011, an old Gmail bug has returned. When reading an
attachment, Gmail fails to return all of the data. This occurs because
Gmail reports the wrong size for the attachment in the BODYSTRUCTURE
response. It reports the size using Unix line terminators instead of
the required MIME line terminators. The bug has been reported to
Google.

As of Dec 2011, several users have reported an
ArrayIndexOutOfBoundsException when accessing some messages in Gmail.
This is a bug in Gmail involving Subject headers with embedded
newlines. Google expects a fix to be available in mid March 2012.

As of Jan 2012, Gmail has a bug that causes it to fail to return a
valid IMAP BODYSTRUCTURE response for messages that include another
message as an attachment. Applications may see an exception with a
message of "ENVELOPE parse error". The
[workaround described in the JavaMail FAQ](FAQ.html#imapserverbug)
can be helpful for dealing with such messages. JavaMail 1.4.5 includes
a workaround for this Gmail bug.

As of Dec 2012, Gmail has a bug that causes it to return a different
value for the getMessageID method than is in the Message-Id header of a
message. The returned Message-Id is "canonicalized". If you need the
actual value in the header, use the getHeader method instead. Google
has no current plans to fix this bug.

As of Jan 2014, Gmail still only notifies clients of new messages and
expunged messages when using IMAP IDLE. Other flag changes are not
notified.

As of Aug 2014, Gmail moves expunged messages to the Trash folder.
They're only really removed if expunged from Trash.
