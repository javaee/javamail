/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.mail.mbox;

import java.io.*;

/**
 * Update the Content-Length header in the message written to the stream.
 */
class ContentLengthUpdater extends FilterOutputStream {
    private String contentLength;
    private boolean inHeader = true;
    private boolean sawContentLength = false;
    private int lastb1 = -1, lastb2 = -1;
    private StringBuilder line = new StringBuilder();

    public ContentLengthUpdater(OutputStream os, long contentLength) {
	super(os);
	this.contentLength = "Content-Length: " + contentLength;
    }

    public void write(int b) throws IOException {
	if (inHeader) {
	    String eol = "\n";
	    // First, determine if we're still in the header.
	    if (b == '\r') {
		// if line terminator is CR
		if (lastb1 == '\r') {
		    inHeader = false;
		    eol = "\r";
		// else, if line terminator is CRLF
		} else if (lastb1 == '\n' && lastb2 == '\r') {
		    inHeader = false;
		    eol = "\r\n";
		}
	    // else, if line terminator is \n
	    } else if (b == '\n') {
		if (lastb1 == '\n') {
		    inHeader = false;
		    eol = "\n";
		}
	    }

	    // If we're no longer in the header, and we haven't seen
	    // a Content-Length header yet, it's time to put one out.
	    if (!inHeader && !sawContentLength) {
		out.write(contentLength.getBytes("iso-8859-1"));
		out.write(eol.getBytes("iso-8859-1"));
	    }

	    // If we have a full line, see if it's a Content-Length header.
	    if (b == '\r' || (b == '\n' && lastb1 != '\r')) {
		if (line.toString().regionMatches(true, 0,
					"content-length:", 0, 15)) {
		    // yup, got it
		    sawContentLength = true;
		    // put out the new version
		    out.write(contentLength.getBytes("iso-8859-1"));
		} else {
		    // not a Content-Length header, just write it out
		    out.write(line.toString().getBytes("iso-8859-1"));
		}
		line.setLength(0);	// clear buffer for next line
	    }
	    if (b == '\r' || b == '\n')
		out.write(b);	// write out line terminator immediately
	    else
		line.append((char)b);	// accumulate characters of the line

	    // rotate saved characters for next time through loop
	    lastb2 = lastb1;
	    lastb1 = b;
	} else
	    out.write(b);		// not in the header, just write it out
    }

    public void write(byte[] b) throws IOException {
	if (inHeader)
	    write(b, 0, b.length);
	else
	    out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
	if (inHeader) {
	    for (int i = 0 ; i < len ; i++) {
		write(b[off + i]);
	    }
	} else
	    out.write(b, off, len);
    }

    // for testing
    public static void main(String argv[]) throws Exception {
	int b;
	ContentLengthUpdater os =
	    new ContentLengthUpdater(System.out, Long.parseLong(argv[0]));
	while ((b = System.in.read()) >= 0)
	    os.write(b);
	os.flush();
    }
}
