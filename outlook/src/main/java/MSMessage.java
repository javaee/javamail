/*
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * This class models a UUEncoded Message sent from MS Outlook etc. <p>
 *
 * The message structure looks like this :=
 *  [text body] [uuencoded attachment]*
 * <p>
 * i.e., an optional text/plain main-body, followed by zero or more
 * UUENCODE-ed attachments.
 *
 * @author John Mani
 * @author Bill Shannon
 * @see    javax.mail.internet.MimeMessage
 */

public class MSMessage extends MimeMessage {
    private String type;

    /**
     * Constructor that converts a MimeMessage object into a MSMessage.
     * 
     * @exception   MessagingException if the given MimeMessage
     *          is not a non-MIME MS message, or if an
     *          IOException occurs when accessing the given
     *          MimeMessage object
     */
    public MSMessage(Session session, MimeMessage msg) 
		throws MessagingException {
	super(session);

	if (!isMSMessage(msg))   // sanity check
	    throw new MessagingException("Not an MS message");

	class FastByteArrayOutputStream extends ByteArrayOutputStream {
	    ByteArrayInputStream toByteArrayInputStream() {
		return new ByteArrayInputStream(buf, 0, count);
	    }
	}

	// extract the bytes of the given message
	// ByteArrayOutputStream bos = new ByteArrayOutputStream();
	FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
	try {
	    msg.writeTo(bos);
	} catch (IOException ioex) {
	    throw new MessagingException("IOException", ioex);
	} catch (Exception ex) {
	    throw new MessagingException("Exception", ex);
	} catch (Throwable t) {
	    t.printStackTrace();
	    throw new MessagingException("Exception", (Exception)t);
	}
	//parse(new ByteArrayInputStream(bos.toByteArray()));
	parse(bos.toByteArrayInputStream());
    }

    /**
     * Constructor to create a MSMessage from the given InputStream.
     */
    public MSMessage(Session session, InputStream is) 
		throws MessagingException {
	super(session); // setup headerstore etc
	parse(is);
    }

    // parse input stream
    protected void parse(InputStream is) throws MessagingException {
	// Create a buffered input stream for efficiency
	if (!(is instanceof ByteArrayInputStream) &&
	    !(is instanceof BufferedInputStream))
	    is = new BufferedInputStream(is);

	// Load headerstore
	headers.load(is);

	/*
	 * Load the content into a byte[].
	 * This byte[] is shared among the bodyparts.
	 */
	try {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    int b;
	    // XXX - room for performance improvement
	    while ((b = is.read()) != -1)
		bos.write(b);
	    content = bos.toByteArray();
	} catch (IOException ioex) {
	    throw new MessagingException("IOException", ioex);
	}

	/*
	 * Check whether this is multipart.
	 */
	boolean isMulti = false;
	// check for presence of X-MS-Attachment header
	String[] att = getHeader("X-MS-Attachment");
	if (att != null && att.length > 0)
	    isMulti = true;
	else {
	    /*
	     * Fall back to scanning the content.
	     * We scan the content until we find a sequence that looks
	     * like the start of a uuencoded block, i.e., "<newline>begin".
	     * If found, we claim that this is a multipart message.
	     */
	    for (int i = 0; i < content.length; i++) {
		int b = content[i] & 0xff; // mask higher byte
		if (b == '\r' || b == '\n') {
		    // start of a new line
		    if ((i + 5) < content.length) {
			// can there be a "begin" now?
			String s = toString(content, i+1, i+6);
			if (s.equalsIgnoreCase("begin")) {
			    isMulti= true;
			    break;
			}
		    }
		}
	    }
	}

	if (isMulti) {
	    type = "multipart/mixed";
	    dh = new DataHandler(new MSMultipartDataSource(this, content));
	} else {
	    type = "text/plain"; // charset = ?
	    dh = new DataHandler(new MimePartDataSource(this));
	}

	modified = false;
    }

    /**
     * Return content-type
     */
    public String getContentType() throws MessagingException {
	return type;
    }

    /**
     * Return content-disposition
     */
    public String getDisposition() throws MessagingException {
	return "inline";
    }

    /**
     * Return content-transfer-encoding
     */
    public String getEncoding() throws MessagingException {
	return "7bit";
    }

    /**
     * Check whether the given MimeMessage object represents a
     * non-MIME message sent by Outlook.  Such a message will
     * have no MIME-Version header, may have an X-Mailer header
     * that includes the word "Microsoft", and will have at least
     * one X-MS-Attachment header.
     */
    public static boolean isMSMessage(MimeMessage msg) 
			throws MessagingException {
	// Check whether the MIME header is present
	if (msg.getHeader("MIME-Version") != null)
	    // MIME-Version header present, should be a MIME message
	    return false;

	/*
	 * XXX - disabled X-Mailer check because many sample messages
	 * I saw didn't have an X-Mailer header at all.
	 */
	if (false) {
	// Check X-Mailer
	String mailer = msg.getHeader("X-mailer", null);
	if (mailer == null) // No X-mailer ? 
	    return false; // Oh well !
	if (mailer.indexOf("Microsoft") == -1) // Not MS stuff ?
	    return false;
	}

	// Check X-MS-Attachment header
	// XXX - not all such messages have this header
	String[] att = msg.getHeader("X-MS-Attachment");
	if (att == null || att.length == 0)
	    return false;

	return true;
    }

    // convert given byte array of ASCII characters to string
    static String toString(byte[] b, int start, int end) {
	int size = end - start;
	char[] theChars = new char[size];

	for (int i = 0, j = start; i < size; )
	    theChars[i++] = (char)b[j++];
	return new String(theChars);
    }
}
