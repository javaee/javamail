Yahoo! Mail
===========

This page is currently a placeholder for describing hints and tips for
using JavaMail with Yahoo! Mail. Basic Yahoo! Mail usage information
can be found in the
[JavaMail FAQ](FAQ.html#yahoomail).

See also the
[Wikipedia page on Yahoo! Mail](http://en.wikipedia.org/wiki/Yahoo!_Mail#Free_IMAP_and_SMTPs_access)
for information about accessing the Yahoo! Mail IMAP server. I've just
added a feature to JavaMail to enable Yahoo! Mail IMAP server support -
set the property "mail.imap.yahoo.guid" to "1". As of 3/19/2015, this
no longer seems to be necessary.

On 3/12/2015 a user reported
that using the JavaMail equivalent of UID FETCH X:Y (UID) where X is
greater than any existing UID in the folder never returns.

As of 8/19/2015, Yahoo Mail doesn't handle some IMAP SEARCH terms
correctly. For example, the NOT term seems to be completely ignored.
