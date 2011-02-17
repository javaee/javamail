/*
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * A special MimeBodyPart used with MSMessage.
 *
 * @author John Mani
 * @author Bill Shannon
 */
public class MSBodyPart extends MimeBodyPart {
    private int start;
    private int end;
    private String type = UNKNOWN;
    private String disposition;
    private String encoding;
    private String filename = UNKNOWN;

    private static final String UNKNOWN = "UNKNOWN";

    public MSBodyPart(byte[] content, int start, int end,
		String disposition, String encoding) {
	this.content = content;
	this.start = start;
	this.end = end;
	this.disposition = disposition;
	this.encoding = encoding;
    }

    public String getContentType() throws MessagingException {
	// try to figure this out from the filename extension
	if (type == UNKNOWN)
	    processBegin();
	return type;
    }

    public String getEncoding() throws MessagingException {
	return encoding;
    }

    public String getDisposition() throws MessagingException {
	return disposition;
    }

    public String getFileName() throws MessagingException {
	// get filename from the "begin" line
	if (filename == UNKNOWN)
	    processBegin();
	return filename;
    }

    protected InputStream getContentStream() {
	return new ByteArrayInputStream(content, start, end - start);
    }

    /**
     * Process the "begin" line to extract the filename,
     * and from it determine the Content-Type.
     */
    private void processBegin() {
	InputStream in = getContentStream();
	try {
	    BufferedReader r = new BufferedReader(new InputStreamReader(in));
	    String begin = r.readLine();
	    // format is "begin 666 filename.txt"
	    if (begin != null && begin.regionMatches(true, 0, "begin ", 0, 6)) {
		int i = begin.indexOf(' ', 6);
		if (i > 0) {
		    filename = begin.substring(i + 1);
		    FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
		    type = map.getContentType(filename);
		    if (type == null)
			type = "application/octet-stream";
		}
	    }
	} catch (IOException ex) {
	    // ignore
	} finally {
	    try {
		in.close();
	    } catch (IOException ex) {
		// ignore it
	    }
	    if (filename == UNKNOWN)
		filename = null;
	    if (type == UNKNOWN || type == null)
		type = "text/plain";
	}
    }
}
