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

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * A special MultipartDataSource used with MSMessage.
 *
 * @author John Mani
 * @author Bill Shannon
 */
public class MSMultipartDataSource extends MimePartDataSource
				implements MultipartDataSource {
    //private List<MSBodyPart> parts;
    private List parts;

    public MSMultipartDataSource(MimePart part, byte[] content)
				throws MessagingException {
	super(part);
	//parts = new ArrayList<MSBodyPart>();
	parts = new ArrayList();

	/*
	 * Parse the text of the message to find the attachments.
	 *
	 * Currently we just look for the lines that mark the
	 * begin and end of uuencoded data, but this can be
	 * fooled by similar text in the message body.  Instead,
	 * we could use the Encoding header, which indicates how
	 * many lines are in each body part.  For example:
	 *
	 * Encoding: 41 TEXT, 38 UUENCODE, 3155 UUENCODE, 1096 UUENCODE
	 *
	 * Similarly, we could get the filenames of the attachments
	 * from the X-MS-Attachment headers.  For example:
	 *
	 * X-MS-Attachment: ATT00000.htx 0 00-00-1980 00:00
	 * X-MS-Attachment: Serengeti 2GG.mpp 0 00-00-1980 00:00
	 * X-MS-Attachment: project team update 031298.doc 0 00-00-1980 00:00
	 *
	 * (Note that there might be unquoted spaces in the filename.)
	 */
	int pos = startsWith(content, 0, "begin");
	if (pos == -1)
	    throw new MessagingException("invalid multipart");
	
	if (pos > 0)	// we have an unencoded main body part
	    parts.add(new MSBodyPart(content, 0, pos, "inline", "7bit"));
	else		// no main body part
	    pos = 0;

	// now collect all the uuencoded individual body parts
	int start;
	for (;;) {
	    start = startsWith(content, pos, "begin");
	    if (start == -1)
		break;
	    pos = startsWith(content, start, "end");
	    if (pos == -1)
		break;
	    pos += 3;	// skip to the end of "end"
	    parts.add(new MSBodyPart(content, start, pos,
					"attachment", "uuencode"));
	}
    }

    public int getCount() {
	return parts.size();
    }

    public BodyPart getBodyPart(int index) throws MessagingException {
	return (BodyPart)parts.get(index);
    }

    /**
     * This method scans the given byte[], beginning at "start", for
     * lines that begin with the sequence "seq".  If found, the start
     * position of the sequence within the byte[] is returned.
     */
    private int startsWith(byte[] content, int start, String seq) {
	int slen = seq.length();
	boolean bol = true;
	for (int i = start; i < content.length; i++) {
	    if (bol) {
		if ((i + slen) < content.length) {
		    String s = MSMessage.toString(content, i, i + slen);
		    if (s.equalsIgnoreCase(seq))
			return i;
		}
	    }
	    int b = content[i] & 0xff;
	    bol = b == '\r' || b == '\n';
	}
	return -1;
    }
}
