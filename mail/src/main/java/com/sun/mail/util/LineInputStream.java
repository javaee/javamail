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

package com.sun.mail.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class is to support reading CRLF terminated lines that
 * contain only US-ASCII characters from an input stream. Provides
 * functionality that is similar to the deprecated 
 * <code>DataInputStream.readLine()</code>. Expected use is to read
 * lines as String objects from a RFC822 stream.
 *
 * It is implemented as a FilterInputStream, so one can just wrap 
 * this class around any input stream and read bytes from this filter.
 * 
 * @author John Mani
 * @author Bill Shannon
 */

public class LineInputStream extends FilterInputStream {

    private boolean allowutf8;
    private byte[] lineBuffer = null; // reusable byte buffer
    private static int MAX_INCR = 1024*1024;	// 1MB

    public LineInputStream(InputStream in) {
	this(in, false);
    }

    /**
     * @param	in	the InputStream
     * @param	allowutf8	allow UTF-8 characters?
     * @since	JavaMail 1.6
     */
    public LineInputStream(InputStream in, boolean allowutf8) {
	super(in);
	this.allowutf8 = allowutf8;
    }

    /**
     * Read a line containing only ASCII characters from the input 
     * stream. A line is terminated by a CR or NL or CR-NL sequence.
     * A common error is a CR-CR-NL sequence, which will also terminate
     * a line.
     * The line terminator is not returned as part of the returned 
     * String. Returns null if no data is available. <p>
     *
     * This class is similar to the deprecated 
     * <code>DataInputStream.readLine()</code>
     *
     * @return		the line
     * @exception	IOException	for I/O errors
     */
    @SuppressWarnings("deprecation")	// for old String constructor
    public String readLine() throws IOException {
	//InputStream in = this.in;
	byte[] buf = lineBuffer;

	if (buf == null)
	    buf = lineBuffer = new byte[128];

	int c1;
	int room = buf.length;
	int offset = 0;

	while ((c1 = in.read()) != -1) {
	    if (c1 == '\n') // Got NL, outa here.
		break;
	    else if (c1 == '\r') {
		// Got CR, is the next char NL ?
		boolean twoCRs = false;
		if (in.markSupported())
		    in.mark(2);
		int c2 = in.read();
		if (c2 == '\r') {		// discard extraneous CR
		    twoCRs = true;
		    c2 = in.read();
		}
		if (c2 != '\n') {
		    /*
		     * If the stream supports it (which we hope will always
		     * be the case), reset to after the first CR.  Otherwise,
		     * we wrap a PushbackInputStream around the stream so we
		     * can unread the characters we don't need.  The only
		     * problem with that is that the caller might stop
		     * reading from this LineInputStream, throw it away,
		     * and then start reading from the underlying stream.
		     * If that happens, the pushed back characters will be
		     * lost forever.
		     */
		    if (in.markSupported())
			in.reset();
		    else {
			if (!(in instanceof PushbackInputStream))
			    in /*= this.in*/ = new PushbackInputStream(in, 2);
			if (c2 != -1)
			    ((PushbackInputStream)in).unread(c2);
			if (twoCRs)
			    ((PushbackInputStream)in).unread('\r');
		    }
		}
		break; // outa here.
	    }

	    // Not CR, NL or CR-NL ...
	    // .. Insert the byte into our byte buffer
	    if (--room < 0) { // No room, need to grow.
		if (buf.length < MAX_INCR)
		    buf = new byte[buf.length * 2];
		else
		    buf = new byte[buf.length + MAX_INCR];
		room = buf.length - offset - 1;
		System.arraycopy(lineBuffer, 0, buf, 0, offset);
		lineBuffer = buf;
	    }
	    buf[offset++] = (byte)c1;
	}

	if ((c1 == -1) && (offset == 0))
	    return null;

	if (allowutf8)
	    return new String(buf, 0, offset, StandardCharsets.UTF_8);
	else
	    return new String(buf, 0, 0, offset);
    }
}
