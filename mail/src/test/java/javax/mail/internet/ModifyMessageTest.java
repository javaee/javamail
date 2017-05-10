/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.BodyPart;

import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 * Test some of the ways you might modify a message that has been
 * read from an input stream.
 */
public class ModifyMessageTest {
 
    private static Session s = Session.getInstance(new Properties());

    @Test
    public void testAddHeader() throws Exception {
        MimeMessage m = createMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();
        m.setHeader("a", "b");
        m.saveChanges();

	MimeMessage m2 = new MimeMessage(m);
	assertEquals("b", m2.getHeader("a", null));
    }

    @Test
    public void testChangeHeader() throws Exception {
        MimeMessage m = createMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();
        m.setHeader("Subject", "test");
        m.saveChanges();

	MimeMessage m2 = new MimeMessage(m);
	assertEquals("test", m2.getHeader("Subject", null));
    }

    @Test
    public void testAddContent() throws Exception {
        MimeMessage m = createMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();
	MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText("test");
	mp.addBodyPart(mbp);
        m.saveChanges();

	MimeMessage m2 = new MimeMessage(m);
	mp = (MimeMultipart)m2.getContent();
	BodyPart bp = mp.getBodyPart(2);
	assertEquals("test", bp.getContent());
	// make sure nothing else changed
	bp = mp.getBodyPart(0);
	assertEquals("first part\n", bp.getContent());
	bp = mp.getBodyPart(1);
	assertEquals("second part\n", bp.getContent());
    }

    @Test
    public void testChangeContent() throws Exception {
        MimeMessage m = createMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();
	BodyPart bp = mp.getBodyPart(0);
        bp.setText("test");
        m.saveChanges();

	MimeMessage m2 = new MimeMessage(m);
	mp = (MimeMultipart)m2.getContent();
	bp = mp.getBodyPart(0);
	assertEquals("test", bp.getContent());
    }

    @Test
    public void testChangeNestedContent() throws Exception {
        MimeMessage m = createNestedMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();
	mp = (MimeMultipart)mp.getBodyPart(0).getContent();
	BodyPart bp = mp.getBodyPart(0);
        bp.setText("test");
        m.saveChanges();

	MimeMessage m2 = new MimeMessage(m);
	mp = (MimeMultipart)m2.getContent();
	mp = (MimeMultipart)mp.getBodyPart(0).getContent();
	bp = mp.getBodyPart(0);
	assertEquals("test", bp.getContent());
	// make sure other content is not changed or re-encoded
	MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(1);
	assertEquals("second part\n", mbp.getContent());
	assertEquals("quoted-printable", mbp.getEncoding());
	mbp = (MimeBodyPart)mp.getBodyPart(2);
	assertEquals("third part\n", mbp.getContent());
	assertEquals("base64", mbp.getEncoding());
    }

    private static MimeMessage createMessage() throws MessagingException {
        String content =
	    "Mime-Version: 1.0\n" +
	    "Subject: Example\n" +
	    "Content-Type: multipart/mixed; boundary=\"-\"\n" +
	    "\n" +
	    "preamble\n" +
	    "---\n" +
	    "\n" +
	    "first part\n" +
	    "\n" +
	    "---\n" +
	    "\n" +
	    "second part\n" +
	    "\n" +
	    "-----\n";

	return new MimeMessage(s, new AsciiStringInputStream(content));
    }

    private static MimeMessage createNestedMessage() throws MessagingException {
        String content =
	    "Mime-Version: 1.0\n" +
	    "Subject: Example\n" +
	    "Content-Type: multipart/mixed; boundary=\"-\"\n" +
	    "\n" +
	    "preamble\n" +
	    "---\n" +
	    "Content-Type: multipart/mixed; boundary=\"x\"\n" +
	    "\n" +
	    "--x\n" +
	    "\n" +
	    "first part\n" +
	    "\n" +
	    "--x\n" +
	    "Content-Transfer-Encoding: quoted-printable\n" +
	    "\n" +
	    "second part\n" +
	    "\n" +
	    "--x\n" +
	    "Content-Transfer-Encoding: base64\n" +
	    "\n" +
	    "dGhpcmQgcGFydAo=\n" +	// "third part\n", base64 encoded
	    "\n" +
	    "--x--\n" +
	    "-----\n";

	return new MimeMessage(s, new AsciiStringInputStream(content));
    }
}
