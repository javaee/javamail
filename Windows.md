Windows Hints and Tips
======================

TCP TIME\_WAIT
--------------

When making a large number of connections (e.g., to a mail server) in a
short amount of time, you might run out of sockets and not be able to
create any more connections. The existing connections will be in a
TIME\_WAIT state as shown by netstat. The following articles explain
this situation in more detail and include tips for how to deal with
it:

-   [Microsoft TechNet](http://technet.microsoft.com/en-us/library/cc757512%28WS.10%29.aspx)
-   [Microsoft MSDN](http://msdn.microsoft.com/en-us/library/ms819739.aspx)
-   [Microsoft Support](http://support.microsoft.com/kb/328476) (talks about SQL Server, but applies here as well)

Note that it seems the
[number of available ports has increased in Windows Server 2008](http://support.microsoft.com/kb/929851).

.msg and .eml Files
-------------------

Windows applications typically store single messages in MIME format in
".eml" files. JavaMail can read these files using the MimeMessage
constructor that takes an InputStream.

Windows applications typically store messages in the Windows
proprietary Outlook message format in ".msg" files. JavaMail can't
process these files directly, but the
[Apache POI project](http://poi.apache.org/) might help.
