/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.activation.DataHandler;

import javax.mail.*;

import com.sun.mail.test.AsciiStringInputStream;

import org.junit.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * Test the MimeBodyPart class.
 */
public class MimeBodyPartTest {
 
    private static String[] languages = new String[] {
	    "language1", "language2", "language3", "language4", "language5",
	    "language6", "language7", "language8", "language9", "language10",
	    "language11", "language12", "language13", "language14", "language15"
	};

    /**
     * Test that the Content-Language header is properly folded
     * if there are a lot of languages.
     */
    @Test
    public void testContentLanguageFold() throws Exception {
	MimeBodyPart mbp = new MimeBodyPart();
	mbp.setContentLanguage(languages);
	String header = mbp.getHeader("Content-Language", ",");
	assertTrue(header.indexOf("\r\n") > 0);

	String[] langs = mbp.getContentLanguage();
	assertArrayEquals(languages, langs);
    }

    /**
     * Test that copying a DataHandler from one message to another
     * has the desired effect.
     */
    @Test
    public void testCopyDataHandler() throws Exception {
	Session s = Session.getInstance(new Properties());
	// create a message and extract the DataHandler for a part
	MimeMessage orig = createMessage(s);
	MimeMultipart omp = (MimeMultipart)orig.getContent();
	MimeBodyPart obp = (MimeBodyPart)omp.getBodyPart(0);
	DataHandler dh = obp.getDataHandler();
	// create a new message and use the DataHandler
	MimeMessage msg = new MimeMessage(s);
	MimeMultipart mp = new MimeMultipart();
	MimeBodyPart mbp = new MimeBodyPart();
	mbp.setDataHandler(dh);
	mp.addBodyPart(mbp);
	msg.setContent(mp);
	// depend on copy constructor streaming the data
	msg = new MimeMessage(msg);
	mp = (MimeMultipart)msg.getContent();
	mbp = (MimeBodyPart)mp.getBodyPart(0);
	assertEquals("text/x-test", mbp.getContentType());
	assertEquals("quoted-printable", mbp.getEncoding());
	assertEquals("test part", getString(mbp.getInputStream()));
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
	MimeMultipart omp = (MimeMultipart)orig.getContent();
	MimeBodyPart obp = (MimeBodyPart)omp.getBodyPart(0);
	final DataHandler odh = obp.getDataHandler();
	// create a new message and use the DataHandler
	MimeMessage msg = new MimeMessage(s);
	MimeMultipart mp = new MimeMultipart();
	MimeBodyPart mbp = new MimeBodyPart() {
		{ dh = odh; }
	    };
	mp.addBodyPart(mbp);
	msg.setContent(mp);
	// depend on copy constructor streaming the data
	msg = new MimeMessage(msg);
	mp = (MimeMultipart)msg.getContent();
	mbp = (MimeBodyPart)mp.getBodyPart(0);
	assertEquals("text/x-test", mbp.getContentType());
	assertEquals("quoted-printable", mbp.getEncoding());
	assertEquals("test part", getString(mbp.getInputStream()));
    }

    /**
     * Test that a MimeBodyPart created from a stream with unencoded data
     * will have the data be encoded when the data is copied to another
     * MimeBodyPart by copying the DataHandler.
     */
    @Test
    public void testEncodingCopiedDataHandler() throws Exception {
	String part = 
	    "Content-Type: application/x-test\n" +
	    "\n" +
	    "\u0001\u0002\u0003" +
	    "\n";
	MimeBodyPart mbp = new MimeBodyPart(new AsciiStringInputStream(part));
	MimeBodyPart mbp2 = new MimeBodyPart() {
	    @Override
	    public void setDataHandler(DataHandler dh)
						throws MessagingException {
		super.setDataHandler(dh);
		updateHeaders();
	    }
	};
	mbp2.setDataHandler(mbp.getDataHandler());
	assertEquals("base64", mbp2.getEncoding());
	// ensure the data is correct by reading the first byte
	InputStream in = mbp2.getInputStream();
	assertEquals(1, in.read());
	in.close();
    }

    /**
     * Test that isMimeType does something reasonable even if the
     * Content-Type header can't be parsed because of a bad parameter.
     */
    @Test
    public void testIsMimeTypeBadParameter() throws Exception {
	String part = 
	    "Content-Type: application/x-test; type=a/b\n" +
	    "\n" +
	    "\n";
	MimeBodyPart mbp = new MimeBodyPart(new AsciiStringInputStream(part));
	assertTrue("complete MIME type", mbp.isMimeType("application/x-test"));
	assertTrue("pattern MIME type", mbp.isMimeType("application/*"));
	assertFalse("wrong MIME type", mbp.isMimeType("application/test"));
    }

    /**
     * Test that a Content-Transfer-Encoding header with no value doesn't
     * cause an IOException.
     */
    @Test
    public void testEmptyContentTransferEncoding() throws Exception {
	String part = 
	    "Content-Type: text/plain; charset=\"us-ascii\"\n" +
	    "Content-Transfer-Encoding: \n" +
	    "\n" +
	    "test" +
	    "\n";
	MimeBodyPart mbp = new MimeBodyPart(new AsciiStringInputStream(part));
	assertEquals("empty C-T-E value", null, mbp.getEncoding());
	assertEquals("empty C-T-E data", "test\n", mbp.getContent());
    }


    private static MimeMessage createMessage(Session s)
				throws MessagingException {
        String content =
	    "Mime-Version: 1.0\n" +
	    "Subject: Example\n" +
	    "Content-Type: multipart/mixed; boundary=\"-\"\n" +
	    "\n" +
	    "preamble\n" +
	    "---\n" +
	    "Content-Type: text/x-test\n" +
	    "Content-Transfer-Encoding: quoted-printable\n" +
	    "\n" +
	    "test part\n" +
	    "\n" +
	    "-----\n";

	return new MimeMessage(s, new AsciiStringInputStream(content));
    }

    private static String getString(InputStream is) throws IOException {
	BufferedReader r = new BufferedReader(new InputStreamReader(is));
	return r.readLine();
    }
}
