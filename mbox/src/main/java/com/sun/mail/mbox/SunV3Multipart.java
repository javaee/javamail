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

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.sun.mail.util.LineInputStream;

/**
 * The SunV3Multipart class is an implementation of the abstract Multipart
 * class that uses SunV3 conventions for the multipart data. <p>
 *
 * @author  Bill Shannon
 */

public class SunV3Multipart extends MimeMultipart {
    private boolean parsing;

    /**
     * Constructs a SunV3Multipart object and its bodyparts from the 
     * given DataSource. <p>
     *
     * @param	ds	DataSource, can be a MultipartDataSource
     */
    public SunV3Multipart(DataSource ds) throws MessagingException {
	super(ds);
    }

    /**
     * Set the subtype.  Throws MethodNotSupportedException.
     *
     * @param	subtype		Subtype
     */
    public void setSubType(String subtype) throws MessagingException {
	throw new MethodNotSupportedException(
		"can't change SunV3Multipart subtype");
    }

    /**
     * Get the BodyPart referred to by the given ContentID (CID). 
     * Throws MethodNotSupportException.
     */
    public synchronized BodyPart getBodyPart(String CID) 
			throws MessagingException {
	throw new MethodNotSupportedException(
		"SunV3Multipart doesn't support Content-ID");
    }

    /**
     * Update headers.  Throws MethodNotSupportException.
     */
    protected void updateHeaders() throws MessagingException {
	throw new MethodNotSupportedException("SunV3Multipart not writable");
    }

    /**
     * Iterates through all the parts and outputs each SunV3 part
     * separated by a boundary.
     */
    public void writeTo(OutputStream os)
				throws IOException, MessagingException {
	throw new MethodNotSupportedException(
		"SunV3Multipart writeTo not supported");
    }

    private static final String boundary = "----------";

    /*
     * Parse the contents of this multipart message and create the
     * child body parts.
     */
    protected synchronized void parse() throws MessagingException {
	/*
	 * If the data has already been parsed, or we're in the middle of
	 * parsing it, there's nothing to do.  The latter will occur when
	 * we call addBodyPart, which will call parse again.  We really
	 * want to be able to call super.super.addBodyPart.
	 */
	if (parsed || parsing)
	    return;

	InputStream in = null;

	try {
	    in = ds.getInputStream();
	    if (!(in instanceof ByteArrayInputStream) &&
		!(in instanceof BufferedInputStream))
		in = new BufferedInputStream(in);
	} catch (IOException ex) {
	    throw new MessagingException("No inputstream from datasource");
	} catch (RuntimeException ex) {
	    throw new MessagingException("No inputstream from datasource");
	}

	byte[] bndbytes = boundary.getBytes(StandardCharsets.ISO_8859_1);
	int bl = bndbytes.length;

	String line;
	parsing = true;
	try {
	    /*
	     * Skip any kind of junk until we get to the first
	     * boundary line.
	     */
	    LineInputStream lin = new LineInputStream(in);
	    while ((line = lin.readLine()) != null) {
		if (line.trim().equals(boundary))
		    break;
	    }
	    if (line == null)
		throw new MessagingException("Missing start boundary");

	    /*
	     * Read and process body parts until we see the
	     * terminating boundary line (or EOF).
	     */
	    for (;;) {
		/*
		 * Collect the headers for this body part.
		 */
		InternetHeaders headers = new InternetHeaders(in);

		if (!in.markSupported())
		    throw new MessagingException("Stream doesn't support mark");

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int b;

		/*
		 * Read and save the content bytes in buf.
		 */
		while ((b = in.read()) >= 0) {
		    if (b == '\r' || b == '\n') {
			/*
			 * Found the end of a line, check whether the
			 * next line is a boundary.
			 */
			int i;
			in.mark(bl + 4 + 1);	// "4" for possible "--\r\n"
			if (b == '\r' && in.read() != '\n') {
			    in.reset();
			    in.mark(bl + 4);
			}
			// read bytes, matching against the boundary
			for (i = 0; i < bl; i++)
			    if (in.read() != bndbytes[i])
				break;
			if (i == bl) {
			    int b2 = in.read();
			    // check for end of line
			    if (b2 == '\n')
				break;	// got it!  break out of the while loop
			    if (b2 == '\r') {
				in.mark(1);
				if (in.read() != '\n')
				    in.reset();
				break;	// got it!  break out of the while loop
			    }
			}
			// failed to match, reset and proceed normally
			in.reset();
		    }
		    buf.write(b);
		}

		/*
		 * Create a SunV3BodyPart to represent this body part.
		 */
		SunV3BodyPart body =
			new SunV3BodyPart(headers, buf.toByteArray());
		addBodyPart(body);
		if (b < 0)
		    break;
	    }
	} catch (IOException e) {
	    throw new MessagingException("IO Error");	// XXX
	} finally {
	    parsing = false;
	}

	parsed = true;
    }
}
