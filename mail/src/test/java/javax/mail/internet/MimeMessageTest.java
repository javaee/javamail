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

package javax.mail.internet;

import com.sun.mail.test.AsciiStringInputStream;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Enumeration;

import javax.activation.DataHandler;

import javax.mail.*;
import static javax.mail.Message.RecipientType.*;
import static javax.mail.internet.MimeMessage.RecipientType.*;

import org.junit.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test MimeMessage methods.
 *
 * XXX - just a beginning...
 *
 * @author Bill Shannon
 */
public class MimeMessageTest {

    private static final Session s = Session.getInstance(new Properties());

    /**
     * Test that setRecipients with a null string address removes the header.
     * (Bug 7021190)
     */
    @Test
    public void testSetRecipientsStringNull() throws Exception {
	String addr = "joe@example.com";
	MimeMessage m = new MimeMessage(s);
	m.setRecipients(TO, addr);
	assertEquals("To: is set", addr, m.getRecipients(TO)[0].toString());
	m.setRecipients(TO, (String)null);
	assertArrayEquals("To: is removed", null, m.getRecipients(TO));
    }

    /**
     * Test that setRecipient with a null string address removes the header.
     * (Bug 7536)
     */
    @Test
    public void testSetRecipientStringNull() throws Exception {
	String addr = "joe@example.com";
	MimeMessage m = new MimeMessage(s);
	m.setRecipient(TO, new InternetAddress(addr));
	assertEquals("To: is set", addr, m.getRecipients(TO)[0].toString());
	m.setRecipient(TO, (Address)null);
	assertArrayEquals("To: is removed", null, m.getRecipients(TO));
    }

    /**
     * Test that setFrom with an address containing a newline is folded
     * properly.
     * (Bug 7529)
     */
    @Test
    public void testSetFromFold() throws Exception {
	InternetAddress addr = new InternetAddress("joe@bad.com", "Joe\r\nBad");
	MimeMessage m = new MimeMessage(s);
	m.setFrom(addr);
	assertEquals("Joe\r\n Bad <joe@bad.com>", m.getHeader("From", null));
    }

    /**
     * Test that setSender with an address containing a newline is folded
     * properly.
     * (Bug 7529)
     */
    @Test
    public void testSetSenderFold() throws Exception {
	InternetAddress addr = new InternetAddress("joe@bad.com", "Joe\r\nBad");
	MimeMessage m = new MimeMessage(s);
	m.setSender(addr);
	assertEquals("Joe\r\n Bad <joe@bad.com>", m.getHeader("Sender", null));
    }

    /**
     * Test that setRecipient with a newsgroup address containing a newline is
     * handled properly.
     * (Bug 7529)
     */
    @Test
    public void testSetNewsgroupWhitespace() throws Exception {
	NewsAddress addr = new NewsAddress("alt.\r\nbad");
	MimeMessage m = new MimeMessage(s);
	m.setRecipient(NEWSGROUPS, addr);
	assertEquals("alt.bad", m.getHeader("Newsgroups", null));
    }

    /**
     * Test that setRecipients with many newsgroup addresses is folded properly.
     * (Bug 7529)
     */
    @Test
    public void testSetNewsgroupFold() throws Exception {
	NewsAddress[] longng = NewsAddress.parse(
	    "alt.loooooooooooooooooooooooooooooooooooooooooooooooooong," +
	    "alt.verylongggggggggggggggggggggggggggggggggggggggggggggg");
	MimeMessage m = new MimeMessage(s);
	m.setRecipients(NEWSGROUPS, longng);
	assertTrue(m.getHeader("Newsgroups", null).indexOf("\r\n\t") > 0);
    }

    /**
     * Test that newsgroups can be set and read back (even if folded).
     */
    @Test
    public void testSetGetNewsgroups() throws Exception {
	NewsAddress[] longng = NewsAddress.parse(
	    "alt.loooooooooooooooooooooooooooooooooooooooooooooooooong," +
	    "alt.verylongggggggggggggggggggggggggggggggggggggggggggggg");
	MimeMessage m = new MimeMessage(s);
	m.setRecipients(NEWSGROUPS, longng);
	assertArrayEquals(longng, m.getRecipients(NEWSGROUPS));
    }

    /**
     * Test that copying a DataHandler from one message to another
     * has the desired effect.
     */
    @Test
    public void testCopyDataHandler() throws Exception {
	Session s = Session.getInstance(new Properties());
	// create a message and extract the DataHandler
	MimeMessage orig = createMessage(s);
	DataHandler dh = orig.getDataHandler();
	// create a new message and use the DataHandler
	MimeMessage msg = new MimeMessage(s);
	msg.setDataHandler(dh);
	// depend on copy constructor streaming the data
	msg = new MimeMessage(msg);
	assertEquals("text/x-test", msg.getContentType());
	assertEquals("quoted-printable", msg.getEncoding());
	assertEquals("test message", getString(msg.getInputStream()));
    }

    /**
     * Test that copying a DataHandler from one message to another
     * by setting the "dh" field in a subclass has the desired effect.
     */
    @Test
    public void testSetDataHandler() throws Exception {
	Session s = Session.getInstance(new Properties());
	// create a message and extract the DataHandler for a part
	MimeMessage orig = createMessage(s);
	final DataHandler odh = orig.getDataHandler();
	// create a new message and use the DataHandler
	MimeMessage msg = new MimeMessage(s) {
		{ dh = odh; }
	    };
	// depend on copy constructor streaming the data
	msg = new MimeMessage(msg);
	assertEquals("text/x-test", msg.getContentType());
	assertEquals("quoted-printable", msg.getEncoding());
	assertEquals("test message", getString(msg.getInputStream()));
    }

    /**
     * Test that address headers account for the header length when folding.
     */
    @Test
    public void testAddressHeaderFolding() throws Exception {
	Session s = Session.getInstance(new Properties());
	MimeMessage msg = new MimeMessage(s);
	InternetAddress[] addrs = InternetAddress.parse(
	"long-address1@example.com, long-address2@example.com, joe@foobar.com");
	msg.setReplyTo(addrs);	// use Reply-To because it's a long header name
	Enumeration<String> e 
		= msg.getMatchingHeaderLines(new String[] { "Reply-To" });
	String line = e.nextElement();
	int npos = line.indexOf("\r");
	// was the line folded where we expected?
	assertTrue("Header folded",
	    npos > 9 && npos <= 77 && npos < line.length());
    }

    private static MimeMessage createMessage(Session s)
				throws MessagingException {
        String content =
	    "Mime-Version: 1.0\n" +
	    "Subject: Example\n" +
	    "Content-Type: text/x-test\n" +
	    "Content-Transfer-Encoding: quoted-printable\n" +
	    "\n" +
	    "test message\n";

	return new MimeMessage(s, new AsciiStringInputStream(content));
    }

    private static String getString(InputStream is) throws IOException {
	BufferedReader r = new BufferedReader(new InputStreamReader(is));
	return r.readLine();
    }
}
