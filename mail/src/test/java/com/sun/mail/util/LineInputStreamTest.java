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
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test handling of line terminators.
 * LineInputStream handles these different line terminators:
 *
 *	NL		- Unix
 *	CR LF		- Windows, MIME
 *	CR		- old MacOS
 *	CR CR LF	- broken internet servers
 *
 * @author Bill Shannon
 */

public class LineInputStreamTest {
    private static final String[] lines = {
	"line1\nline2\nline3\n",
	"line1\r\nline2\r\nline3\r\n",
	"line1\rline2\rline3\r",
	"line1\r\r\nline2\r\r\nline3\r\r\n"
    };

    private static final String[] empty = {
	"\n\n\n",
	"\r\n\r\n\r\n",
	"\r\r\r",
	"\r\r\n\r\r\n\r\r\n"
    };

    private static final String[] mixed = {
	"line1\n\nline3\n",
	"line1\r\n\r\nline3\r\n",
	"line1\r\rline3\r",
	"line1\r\r\n\r\r\nline3\r\r\n"
    };

    @Test
    public void testLines() throws IOException {
	for (String s : lines) {
	    LineInputStream is = createStream(s);
	    assertEquals("line1", is.readLine());
	    assertEquals("line2", is.readLine());
	    assertEquals("line3", is.readLine());
	    assertEquals(null, is.readLine());
	}
    }

    @Test
    public void testEmpty() throws IOException {
	for (String s : empty) {
	    LineInputStream is = createStream(s);
	    assertEquals("", is.readLine());
	    assertEquals("", is.readLine());
	    assertEquals("", is.readLine());
	    assertEquals(null, is.readLine());
	}
    }

    @Test
    public void testMixed() throws IOException {
	for (String s : mixed) {
	    LineInputStream is = createStream(s);
	    assertEquals("line1", is.readLine());
	    assertEquals("", is.readLine());
	    assertEquals("line3", is.readLine());
	    assertEquals(null, is.readLine());
	}
    }

    private LineInputStream createStream(String s) {
	try {
	return new LineInputStream(
	    new ByteArrayInputStream(s.getBytes("us-ascii")));
	} catch (UnsupportedEncodingException ex) {
	    return null;	// should never happen
	}
    }
}
