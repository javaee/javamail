OAuth2 Support
==============

JavaMail 1.5.5 and later
------------------------

Starting with JavaMail 1.5.5, support for
[OAuth2 authentication](https://developers.google.com/gmail/xoauth2_protocol)
is built-in and no longer requires SASL (although the SASL OAuth2
support continues to work).

Since OAuth2 uses an "access token" instead of a password, you'll want
to configure JavaMail to use only the XOAUTH2 mechanism. The access
token is passed as the password, which obviously won't work with any of
the other authentication mechanisms. For example, to access Gmail:

    Properties props = new Properties();
    props.put("mail.imap.ssl.enable", "true"); // required for Gmail
    props.put("mail.imap.auth.mechanisms", "XOAUTH2");
    Session session = Session.getInstance(props);
    Store store = session.getStore("imap");
    store.connect("imap.gmail.com", username, oauth2_access_token);

You'll need to acquire an OAuth2 access token to be used for the
session, e.g., using
[these Google tools](http://code.google.com/p/google-mail-oauth2-tools/wiki/JavaSampleCode)
and the procedure described
[here](http://code.google.com/apis/accounts/docs/OAuth2.html)
to obtain OAuth2 credentials from the
[Google Developers Console](https://console.developers.google.com/).
In particular, you'll need the Client ID and the Client secret from the
Google Developers Console for use with the oauth2.py program to
generate a Refresh Token, from which you can generate an Access Token.
(Note that the OAuth2 access token does **not** need to be base64
encoded first; the XOAUTH2 provider will do that. Note also that the
Google OAuth2 provider at the link above is **not** needed, only the
oauth2.py program to generate an access token.)

To connect to Outlook.com using OAuth2, see
[this page](http://technet.microsoft.com/en-ca/dn440163)
and the procedure described
[here](http://technet.microsoft.com/en-ca/hh243647)
for creating a Refresh Token and Access Token.
Get your Client ID and Client secret from the
[Microsoft account Developer Center](https://account.live.com/developers/applications).
The general procedure above should work to connect to
imap-mail.outlook.com, but the access token will need to be acquired as
described on the Outlook.com page.

OAuth2 is also supported with SMTP; change "imap" to "smtp" in the
property names.

Please send feedback to <javamail_ww@oracle.com>.


JavaMail 1.5.2 and later
------------------------

Starting with JavaMail 1.5.2, support for
[OAuth2 authentication](https://developers.google.com/gmail/xoauth2_protocol)
via the [SASL](http://www.ietf.org/rfc/rfc4422.txt) XOAUTH2 mechanism
is included. Please send feedback to <javamail_ww@oracle.com>.

The SASL XOAUTH2 provider will be
[added to the Java security configuration](http://docs.oracle.com/javase/7/docs/api/java/security/Security.html#addProvider(java.security.Provider))
when SASL support is first used. The application must have the
permission `SecurityPermission("insertProvider.JavaMail-OAuth2")`.

Since OAuth2 uses an "access token" instead of a password, you'll want
to configure JavaMail to use only the SASL XOAUTH2 mechanism. The
access token is passed as the password, which obviously won't work with
any of the other authentication mechanisms. For example, to access
Gmail:

    Properties props = new Properties();
    props.put("mail.imap.ssl.enable", "true"); // required for Gmail
    props.put("mail.imap.sasl.enable", "true");
    props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
    props.put("mail.imap.auth.login.disable", "true");
    props.put("mail.imap.auth.plain.disable", "true");
    Session session = Session.getInstance(props);
    Store store = session.getStore("imap");
    store.connect("imap.gmail.com", username, oauth2_access_token);

