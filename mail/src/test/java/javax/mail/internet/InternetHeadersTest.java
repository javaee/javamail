/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

package javax.mail.internet;

import com.sun.mail.test.AsciiStringInputStream;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;

import javax.mail.*;

import org.junit.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Test the InternetHeaders class.
 */
public class InternetHeadersTest {
 
    private static final String initialWhitespaceHeader =
						" \r\nSubject: test\r\n\r\n";
    private static final String initialContinuationHeader =
						" Subject: test\r\n\r\n";

    /**
     * Test that a continuation line is handled properly.
     */
    @Test
    public void testContinuationLine() throws Exception {
	String header = "Subject: a\r\n b\r\n\r\n";
	InternetHeaders ih = new InternetHeaders(
		new AsciiStringInputStream(header));
	assertEquals(1, ih.getHeader("Subject").length);
	assertEquals("a\r\n b", ih.getHeader("Subject")[0]);
    }

    /**
     * Test that a whitespace line at the beginning is ignored.
     */
    @Test
    public void testInitialWhitespaceLineConstructor() throws Exception {
	InternetHeaders ih = new InternetHeaders(
		new AsciiStringInputStream(initialWhitespaceHeader));
	testInitialWhitespaceLine(ih);
    }

    /**
     * Test that a whitespace line at the beginning is ignored.
     */
    @Test
    public void testInitialWhitespaceLineLoad() throws Exception {
	InternetHeaders ih = new InternetHeaders();
	ih.load(new AsciiStringInputStream(initialWhitespaceHeader));
	testInitialWhitespaceLine(ih);
    }

    private void testInitialWhitespaceLine(InternetHeaders ih)
				throws Exception {
	assertEquals(1, ih.getHeader("Subject").length);
	assertEquals("test", ih.getHeader("Subject")[0]);
	Enumeration<Header> e = ih.getAllHeaders();
	while (e.hasMoreElements()) {
	    Header h = e.nextElement();
	    assertEquals("Subject", h.getName());
	    assertEquals("test", h.getValue());
	}
    }

    /**
     * Test that a continuation line at the beginning is handled.
     */
    @Test
    public void testInitialContinuationLineConstructor() throws Exception {
	InternetHeaders ih = new InternetHeaders(
		new AsciiStringInputStream(initialContinuationHeader));
	testInitialContinuationLine(ih);
    }

    /**
     * Test that a continuation line at the beginning is handled.
     */
    @Test
    public void testInitialContinuationLineLoad() throws Exception {
	InternetHeaders ih = new InternetHeaders();
	ih.load(new AsciiStringInputStream(initialContinuationHeader));
	testInitialContinuationLine(ih);
    }

    private void testInitialContinuationLine(InternetHeaders ih)
				throws Exception {
	assertEquals(1, ih.getHeader("Subject").length);
	assertEquals("test", ih.getHeader("Subject")[0]);
	Enumeration<Header> e = ih.getAllHeaders();
	while (e.hasMoreElements()) {
	    Header h = e.nextElement();
	    assertEquals("Subject", h.getName());
	    assertEquals("test", h.getValue());
	}
    }
}
