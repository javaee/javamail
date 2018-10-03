Microsoft Exchange and Office 365
=================================

*This page should have a lot more information about Exchange. For now
it's just a collection of notes.*

My understanding is that Office 365 (and Exchange Online) is largely
just a version of Exchange hosted by Microsoft. Many of the issues
listed here that apply to newer versions of Exchange may also apply to
Office 365 and vice versa.

Exchange
--------

Exchange 2016 returns incorrect BODYSTRUCTURE data for an S/MIME
encrypted message.  Use the workaround described in the
[JavaMail FAQ](FAQ.html#imapserverbug).

Exchange fails to find an email address using FromTerm, FromStringTerm,
RecipientTerm, or RecipientStringTerm.  As described in
[this blog entry](https://blogs.technet.microsoft.com/dkhrebin/2013/10/04/how-exchange-imap-search-in-message-header/),
Exchange will only find a personal name using these search terms.
Instead, use a HeaderTerm to search for the email address.

Exchange 2010 has a bug where it returns NIL instead of "" for empty
parameter values, causing a NullPointerException.  A
[workaround](https://github.com/javaee/javamail/issues/203)
was added to JavaMail 1.5.5.

Searching for an address in Exchange 2010 matches only the "personal
name" field of the address, not the email address itself.  (Reported
12/8/2015)

Exchange 365 returns an incorrect BODYSTRUCTURE response for single
part messages, failing to include the message disposition value in
parentheses as required by the IMAP spec. See
[this bug report](https://github.com/javaee/javamail/issues/31).
This causes a MessagingException with the message "Unable to load
BODYSTRUCTURE". Use the workaround described in the
[JavaMail FAQ](FAQ.html#imapserverbug).

Exchange 2010 has a bug where it fails to quote the encoding value in a
BODYSTRUCTURE response. This causes a MessagingException with the
message "Unable to load BODYSTRUCTURE". As of 1/10/2011, a customer
reported that Microsoft expects to fix this bug in "Roll Up 3" for
Exchange 2010. JavaMail 1.4.4 includes a workaround for this Exchange
bug.

Exchange 2007 has a bug where it returns "-1" as the size of a
multipart/signed body part (at least) in the BODYSTRUCTURE response..
This causes a MessagingException with the message "Unable to load
BODYSTRUCTURE". Use the workaround described in the
[JavaMail FAQ](FAQ.html#imapserverbug).

Exchange 2007 has a bug where it advertises that it supports
AUTH=PLAIN, even though
[this Exchange documentation](http://technet.microsoft.com/en-us/library/cc540463.aspx)
claims that it's not supported. This causes JavaMail to choose PLAIN
authentication, which will always fail. To work around this Exchange
bug, set the session property "mail.imap.auth.plain.disable" to "true".
(Change "imap" to "imaps" if you're using the "imaps" protocol.)

On Aug 6, 2012, a customer reported that Exchange 2010 has a similar
problem where both PLAIN and NTLM authentication fail for shared
mailboxes (with user names of the form user1@domain.com/sharedMB), even
though they work for regular user mailboxes. Disabling all
authentication types and falling back to IMAP LOGIN support seemed to
work.

Exchange 2007 through SP3 has a
[bug](https://github.com/javaee/javamail/issues/86)
where, at least in some circumstances, it will report a message as a
result of an IMAP SEARCH command that it had not previously notified
the client of via an EXISTS response, causing an exception such as
"java.lang.ArrayIndexOutOfBoundsException: message number (1) out of
bounds (0)" from the Folder.search() method. A workaround for this bug
was included in JavaMail 1.5.1. (Reported by a user on 6/18/2012)

To access a shared mailbox in Exchange, you need to login using the
"alias" name and password for the shared mailbox, which you can get
from your Exchange server administrator.
[This article](http://social.technet.microsoft.com/Forums/bg-BG/exchangesvrgeneral/thread/8c8b4605-efae-49eb-a118-54aa418de6c2)
has more information.

Here's another article that discusses
[the use of shared mailboxes with Exchange 2013/2016](https://ingogegenwarth.wordpress.com/2016/04/11/exchange-20132016-imapews-and-service-accounts/).

In Exchange 2007 Microsoft removed the ability to access public
folders, and they have no plans to restore it.

Office 365
----------

To access a shared mailbox using Office 365, see
[this article](https://social.technet.microsoft.com/Forums/msonline/en-US/6369118f-7dee-4728-ac1c-a0c706b3d290/office-365-exchange-online-how-to-access-a-shared-mailbox-using-thunderbird-or-other-imap-client?forum=onlineservicesexchange).

In some cases,
[Office 365 will accept a bad password for a correct user name](http://unix.stackexchange.com/questions/164823/user-is-authenticated-but-not-connected-after-changing-my-exchange-password)
and then later return the error "BAD User is authenticated but not
connected" for subsequent IMAP commands, resulting in a
MessagingException.
