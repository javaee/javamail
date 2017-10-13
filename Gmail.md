Gmail
=====

This page describes hints and tips for using JavaMail with Gmail. Basic
Gmail usage information can be found in the
[JavaMail FAQ](FAQ.html#gmail).

Gmail users might want to read the
[Gmail Help page](https://support.google.com/mail/topic/7280141?hl=en).
The IMAP features page in Gmail Help no longer exists.
Previous versions (as viewable on the
[Wayback Machine](https://web.archive.org/web/20140918045327/https://support.google.com/mail/answer/78761?hl=en&ref_topic=3397501))
indicated that Gmail does not support the following IMAP features.
(This list may no longer be accurate.)

* \Recent flags on messages.
* untagged FETCH responses.
* Substring search. All searches are assumed to be words.
* There is no SIEVE interface to Gmail filters.
* Only AUTH=XOAUTH and plain-text LOGIN over SSL tunneled connections are supported.
* ENVELOPE responses for email addresses in group syntax (RFC 5322 3.4)
* The \Answered flag is not preserved when a message is moved or a label is added to the message.

To send email using Gmail with a different address than the one you
signed in with, see this
[Gmail help page](https://support.google.com/mail/bin/answer.py?hl=en&answer=22370).

You may need to enable
[less secure apps](https://www.google.com/settings/security/lesssecureapps)
to use JavaMail with Gmail.

If the Gmail Auto-Expunge setting is turned on (the default),
when setting the \Deleted flag on a message, Gmail will expunge
the message, causing it to disappear from the mailbox.

Gmail also includes a setting to control what happens to deleted
and expunged messages (including auto-expunged messages).  They
can be archived (left in the [Gmail]/All Mail folder), moved to
the [Gmail]/Trash folder, or removed completely.  The default is
to archive the message.

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
