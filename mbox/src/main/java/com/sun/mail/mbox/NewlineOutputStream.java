/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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
import java.nio.charset.StandardCharsets;

/**
 * Convert the various newline conventions to the local platform's
 * newline convention.  Optionally, make sure the output ends with
 * a blank line.
 */
public class NewlineOutputStream extends FilterOutputStream {
    private int lastb = -1;
    private int bol = 1; // number of times in a row we're at beginning of line
    private final boolean endWithBlankLine;
    private static final byte[] newline;

    static {
	String s = null;
	try {
	    s = System.lineSeparator();
	} catch (SecurityException sex) {
	    // ignore, should never happen
	}
	if (s == null || s.length() <= 0)
	    s = "\n";
	newline = s.getBytes(StandardCharsets.ISO_8859_1);
    }

    public NewlineOutputStream(OutputStream os) {
	this(os, false);
    }

    public NewlineOutputStream(OutputStream os, boolean endWithBlankLine) {
	super(os);
	this.endWithBlankLine = endWithBlankLine;
    }

    public void write(int b) throws IOException {
	if (b == '\r') {
	    out.write(newline);
	    bol++;
	} else if (b == '\n') {
	    if (lastb != '\r') {
		out.write(newline);
		bol++;
	    }
	} else {
	    out.write(b);
	    bol = 0;	// no longer at beginning of line
	}
	lastb = b;
    }

    public void write(byte b[]) throws IOException {
	write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
	for (int i = 0 ; i < len ; i++) {
	    write(b[off + i]);
	}
    }

    public void flush() throws IOException {
	if (endWithBlankLine) {
	    if (bol == 0) {
		// not at bol, return to bol and add a blank line
		out.write(newline);
		out.write(newline);
	    } else if (bol == 1) {
		// at bol, add a blank line
		out.write(newline);
	    }
	}
	bol = 2;
	out.flush();
    }
}
