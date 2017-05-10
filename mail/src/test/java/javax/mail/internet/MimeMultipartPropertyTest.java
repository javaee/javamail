/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.mail.test.NullOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.MessagingException;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the properties that control the MimeMultipart class.
 * Since the properties are now read in the parse method, all
 * these tests can be run in the same JVM.
 */
public class MimeMultipartPropertyTest {
 
    private static Session s = Session.getInstance(new Properties());

    /**
     * Clear all properties before each test.
     */
    @Before
    public void beforeTest() {
	clearAll();
    }

    @Test
    public void testBoundary() throws Exception {
	MimeMessage m = createMessage("x", "x", true);
	MimeMultipart mp = (MimeMultipart)m.getContent();
	assertEquals(mp.getCount(), 2);
    }

    @Test
    public void testBoundaryIgnore() throws Exception {
        System.setProperty(
	    "mail.mime.multipart.ignoreexistingboundaryparameter", "true");
	MimeMessage m = createMessage("x", "-", true);
	MimeMultipart mp = (MimeMultipart)m.getContent();
	assertEquals(mp.getCount(), 2);
    }

    @Test
    public void testBoundaryMissing() throws Exception {
	MimeMessage m = createMessage(null, "x", true);
	MimeMultipart mp = (MimeMultipart)m.getContent();
	assertEquals(mp.getCount(), 2);
    }

    @Test(expected=MessagingException.class)
    public void testBoundaryMissingEx() throws Exception {
        System.setProperty(
	    "mail.mime.multipart.ignoremissingboundaryparameter", "false");
	MimeMessage m = createMessage(null, "x", true);
	MimeMultipart mp = (MimeMultipart)m.getContent();
	mp.getCount();		// throw exception
	assertTrue(false);	// never get here
    }

    @Test
    public void testEndBoundaryMissing() throws Exception {
	MimeMessage m = createMessage("x", "x", false);
	MimeMultipart mp = (MimeMultipart)m.getContent();
	assertEquals(mp.getCount(), 2);
    }

    @Test(expected=MessagingException.class)
    public void testEndBoundaryMissingEx() throws Exception {
        System.setProperty(
	    "mail.mime.multipart.ignoremissingendboundary", "false");
	MimeMessage m = createMessage("x", "x", false);
	MimeMultipart mp = (MimeMultipart)m.getContent();
	mp.getCount();		// throw exception
	assertTrue(false);	// never get here
    }

    @Test
    public void testAllowEmpty() throws Exception {
        System.setProperty( "mail.mime.multipart.allowempty", "true");
	MimeMessage m = createEmptyMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();
	assertEquals(mp.getCount(), 0);
    }

    @Test(expected=MessagingException.class)
    public void testAllowEmptyEx() throws Exception {
	MimeMessage m = createEmptyMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();
	mp.getCount();		// throw exception
	assertTrue(false);	// never get here
    }

    @Test
    public void testAllowEmptyOutput() throws Exception {
        System.setProperty( "mail.mime.multipart.allowempty", "true");
	MimeMessage m = new MimeMessage(s);
	MimeMultipart mp = new MimeMultipart();
	m.setContent(mp);
	m.writeTo(new NullOutputStream());
	assertEquals(mp.getCount(), 0);
    }

    @Test(expected=IOException.class)
    public void testAllowEmptyOutputEx() throws Exception {
	MimeMessage m = new MimeMessage(s);
	MimeMultipart mp = new MimeMultipart();
	m.setContent(mp);
	m.writeTo(new NullOutputStream());	// throw exception
	assertTrue(false);	// never get here
    }

    /**
     * Clear all properties after all tests.
     */
    @AfterClass
    public static void after() {
        clearAll();
    }

    private static void clearAll() {
        System.clearProperty(
	    "mail.mime.multipart.ignoreexistingboundaryparameter");
        System.clearProperty(
	    "mail.mime.multipart.ignoremissingboundaryparameter");
        System.clearProperty(
	    "mail.mime.multipart.ignoremissingendboundary");
        System.clearProperty(
	    "mail.mime.multipart.allowempty");
    }

    /**
     * Create a test message.
     * If param is not null, it specifies the boundary parameter.
     * The actual boundary is specified by "actual".
     * If "end" is true, include the end boundary.
     */
    private static MimeMessage createMessage(String param, String actual,
				boolean end) throws MessagingException {
        String content =
	    "Mime-Version: 1.0\n" +
	    "Subject: Example\n" +
	    "Content-Type: multipart/mixed; " +
		(param != null ? "boundary=\"" + param + "\"" : "") + "\n" +
	    "\n" +
	    "preamble\n" +
	    "--" + actual + "\n" +
	    "\n" +
	    "first part\n" +
	    "\n" +
	    "--" + actual + "\n" +
	    "\n" +
	    "second part\n" +
	    "\n" +
	    (end ? "--" + actual + "--\n" : "");
 
	return new MimeMessage(s, new AsciiStringInputStream(content));
    }

    /**
     * Create a test message with no parts.
     */
    private static MimeMessage createEmptyMessage() throws MessagingException {
        String content =
	    "Mime-Version: 1.0\n" +
	    "Subject: Example\n" +
	    "Content-Type: multipart/mixed; boundary=\"x\"\n\n";
 
	return new MimeMessage(s, new AsciiStringInputStream(content));
    }
}
