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
 * Test that the Content-Transfer-Encoding header is ignored
 * for composite parts.
 *
 * XXX - We don't test any of the properties that control this behavior.
 */
public class RestrictEncodingTest {
 
    private static Session s = Session.getInstance(new Properties());

    @Test
    public void testMultipart() throws Exception {
        MimeMessage m = createMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();
	assertEquals(2, mp.getCount());

	BodyPart bp = mp.getBodyPart(0);
	assertEquals("first part=\n", bp.getContent());
    }

    @Test
    public void testMessage() throws Exception {
        MimeMessage m = createMessage();
	MimeMultipart mp = (MimeMultipart)m.getContent();

	BodyPart bp = mp.getBodyPart(1);
	MimeMessage m2 = (MimeMessage)bp.getContent();
	assertEquals("message=\n", m2.getContent());
    }

    @Test
    public void testWrite() throws Exception {
        MimeMessage m = new MimeMessage(s);
	MimeMultipart mp = new MimeMultipart();
	MimeBodyPart mbp = new MimeBodyPart();
	mbp.setText("first part");
	mp.addBodyPart(mbp);
	MimeMessage m2 = new MimeMessage(s);
	m2.setSubject("example");
	m2.setText("message=\n");
	mbp = new MimeBodyPart();
	mbp.setContent(m2, "message/rfc822");
	mbp.setHeader("Content-Transfer-Encoding", "quoted-printable");
	mp.addBodyPart(mbp);
	m.setContent(mp);
	m.setHeader("Content-Transfer-Encoding", "quoted-printable");

	m = new MimeMessage(m);		// copy it
	mp = (MimeMultipart)m.getContent();

	BodyPart bp = mp.getBodyPart(1);
	m2 = (MimeMessage)bp.getContent();
	assertEquals("message=\n", m2.getContent());
    }

    private static MimeMessage createMessage() throws MessagingException {
        String content =
	    "Mime-Version: 1.0\n" +
	    "Content-Type: multipart/mixed; boundary=\"=3D\"\n" +
	    "Content-Transfer-Encoding: quoted-printable\n" +
	    "\n" +
	    "--=3D\n" +
	    "\n" +
	    "first part=\n" +
	    "\n" +
	    "--=3D\n" +
	    "Content-Type: message/rfc822\n" +
	    "Content-Transfer-Encoding: quoted-printable\n" +
	    "\n" +
	    "Subject: example\n" +
	    "\n" +
	    "message=\n" +
	    "\n" +
	    "--=3D--\n";

	return new MimeMessage(s, new AsciiStringInputStream(content));
    }
}
