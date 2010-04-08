The classes in this directory allow processing old style non-MIME
messages created by Microsoft Outlook.  Use them like this:

	if (MSMessage.isMSMessage(msg))
	    msg = new MSMessage(session, msg);

Note that these classes are not particularly efficient or optimized,
but they show how to process these non-MIME messages and make them
look like MIME messages.
