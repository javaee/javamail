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
 * Count the number of bytes in the body of the message written to the stream.
 */
class ContentLengthCounter extends OutputStream {
    private long size = 0;
    private boolean inHeader = true;
    private int lastb1 = -1, lastb2 = -1;

    public void write(int b) throws IOException {
	if (inHeader) {
	    // if line terminator is CR
	    if (b == '\r' && lastb1 == '\r')
		inHeader = false;
	    else if (b == '\n') {
		// if line terminator is \n
		if (lastb1 == '\n')
		    inHeader = false;
		// if line terminator is CRLF
		else if (lastb1 == '\r' && lastb2 == '\n')
		    inHeader = false;
	    }
	    lastb2 = lastb1;
	    lastb1 = b;
	} else
	    size++;
    }

    public void write(byte[] b) throws IOException {
	if (inHeader)
	    super.write(b);
	else
	    size += b.length;
    }

    public void write(byte[] b, int off, int len) throws IOException {
	if (inHeader)
	    super.write(b, off, len);
	else
	    size += len;
    }

    public long getSize() {
	return size;
    }

    /*
    public static void main(String argv[]) throws Exception {
	int b;
	ContentLengthCounter os = new ContentLengthCounter();
	while ((b = System.in.read()) >= 0)
	    os.write(b);
	System.out.println("size " + os.getSize());
    }
    */
}
