/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.imap.protocol;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;
import com.sun.mail.test.AsciiStringInputStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test the IMAPProtocol class.
 */
public class IMAPProtocolTest {
    private static final boolean debug = false;
    private static final String content = "aXQncyBteSB0ZXN0IG1haWwNCg0K\r\n";
    private static final String response =
	    "* 1 FETCH (UID 127 BODY[1.1.MIME] {82}\r\n" +
	    "Content-Type: text/plain;\r\n" +
	    "\tcharset=\"utf-8\"\r\n" +
	    "Content-Transfer-Encoding: base64\r\n" +
	    "\r\n" +
	    " ENVELOPE (\"Mon, 17 Mar 2014 14:03:08 +0100\" \"test invoice\"" +
	    " ((\"Joe User\" NIL \"joe.user\" \"example.com\"))" +
	    " ((\"Joe User\" NIL \"joe.user\" \"example.com\"))" +
	    " ((\"Joe User\" NIL \"joe.user\" \"example.com\"))" +
	    " ((\"Joe User\" NIL \"joe.user\" \"example.com\"))" + 
	    " NIL NIL NIL \"<1234@example.com>\") BODY[1.1]<0> " +
	    "{" + content.length() + "}\r\n" + content + 
	    ")\r\n" +
	    "A0 OK FETCH completed.\r\n";

    /**
     * Test that a response containing multiple BODY elements
     * returns the correct one.  Derived from a customer bug
     * with Exchange 2003.  Normally this would never happen,
     * but it's a valid IMAP response and JavaMail needs to
     * handle it properly.
     */
    @Test
    public void testMultipleBodyResponses() throws Exception {
	Properties props = new Properties();
	props.setProperty("mail.imap.reusetagprefix", "true");
	IMAPProtocol p = new IMAPProtocol(
	    new AsciiStringInputStream(response),
	    new PrintStream(new ByteArrayOutputStream()),
	    props,
	    debug);
	BODY b = p.fetchBody(1, "1.1");
	assertEquals("section number", "1.1", b.getSection());
	//System.out.println(b);
	//System.out.write(b.getByteArray().getNewBytes());
	String result = new String(b.getByteArray().getNewBytes(), "us-ascii");
	assertEquals("getByteArray.getNewBytes", content, result);
	InputStream is = b.getByteArrayInputStream();
	byte[] ba = new byte[is.available()];
	is.read(ba);
	result = new String(ba, "us-ascii");
	assertEquals("getByteArrayInputStream", content, result);
    }

    /**
     * Same test as above, but using a different fetchBody method.
     */
    @Test
    public void testMultipleBodyResponses2() throws Exception {
	Properties props = new Properties();
	props.setProperty("mail.imap.reusetagprefix", "true");
	IMAPProtocol p = new IMAPProtocol(
	    new AsciiStringInputStream(response),
	    new PrintStream(new ByteArrayOutputStream()),
	    props,
	    debug);
	BODY b = p.fetchBody(1, "1.1", 0, content.length(), null);
	assertEquals("section number", "1.1", b.getSection());
	//System.out.println(b);
	//System.out.write(b.getByteArray().getNewBytes());
	String result = new String(b.getByteArray().getNewBytes(), "us-ascii");
	assertEquals("getByteArray.getNewBytes", content, result);
	InputStream is = b.getByteArrayInputStream();
	byte[] ba = new byte[is.available()];
	is.read(ba);
	result = new String(ba, "us-ascii");
	assertEquals("getByteArrayInputStream", content, result);
    }
}
