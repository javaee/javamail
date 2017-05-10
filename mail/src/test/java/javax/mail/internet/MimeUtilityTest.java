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

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import javax.activation.*;
import javax.mail.util.ByteArrayDataSource;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test MimeUtility methods.
 *
 * XXX - just a beginning...
 *
 * @author Bill Shannon
 */
public class MimeUtilityTest {
    private static final byte[] utf16beBytes = {
	(byte)0xfe, (byte)0x5b, (byte)0xdc, (byte)0x5f,
	(byte)0x92, (byte)0x30, (byte)0x88, (byte)0x30,
	(byte)0x8d, (byte)0x30, (byte)0x57, (byte)0x30,
	(byte)0x4f, (byte)0x30, (byte)0x4a, (byte)0x30,
	(byte)0x6d, (byte)0x30, (byte)0x4c, (byte)0x30,
	(byte)0x44, (byte)0x30, (byte)0x57, (byte)0x30,
	(byte)0x7e, (byte)0x30, (byte)0x59, (byte)0x30,
	(byte)0x0d, (byte)0x00, (byte)0x0a, (byte)0x00
    };

    @SuppressWarnings("serial")
    private static final Set<String> encodings = new HashSet<String>() {{
	add("7bit"); add("8bit"); add("quoted-printable"); add("base64"); }};

    /**
     * Test that utf-16be data is encoded with base64 and not quoted-printable.
     */
    @Test
    public void testNonAsciiEncoding() throws Exception {
	DataSource ds = new ByteArrayDataSource(utf16beBytes,
						"text/plain; charset=utf-16be");
	String en = MimeUtility.getEncoding(ds);
	assertEquals("non-ASCII encoding", "base64", en);
    }

    /**
     * Test that getEncoding returns a valid value even if the file
     * doesn't exist.  The return value should be a valid
     * Content-Transfer-Encoding, but mostly we care that it doesn't
     * throw NullPointerException.
     */
    @Test
    public void getEncodingMissingFile() throws Exception {
	File missing = new File(getClass().getName());
	assertFalse(missing.toString(), missing.exists());
	FileDataSource fds = new FileDataSource(missing);
	assertEquals(fds.getName(), missing.getName());
	assertTrue("getEncoding(DataSource)",
	    encodings.contains(MimeUtility.getEncoding(fds)));
	assertTrue("getEncoding(DataHandler)",
	    encodings.contains(MimeUtility.getEncoding(new DataHandler(fds))));
    }

    /**
     * Test that getEncoding returns a valid value even if the content
     * type is bad.  The return value should be a valid
     * Content-Transfer-Encoding, but mostly we care that it doesn't
     * throw NullPointerException.
     */
    @Test
    public void getEncodingBadContent() throws Exception {
	String content = "bad-content-type";
	ContentType type = null;
	try {
	    type = new ContentType(content);
	    fail(type.toString());
	} catch (ParseException expect) {
	    if (type != null) {
	       throw expect; 
	    }
	}

	ByteArrayDataSource bads = new ByteArrayDataSource("", content);
	bads.setName(null);
	assertTrue(encodings.contains(MimeUtility.getEncoding(bads)));
	assertTrue(encodings.contains(
			MimeUtility.getEncoding(new DataHandler(bads))));

	bads.setName("");
	assertTrue(encodings.contains(MimeUtility.getEncoding(bads)));
	assertTrue(encodings.contains(
			MimeUtility.getEncoding(new DataHandler(bads))));

	bads.setName(getClass().getName());
	assertTrue(encodings.contains(MimeUtility.getEncoding(bads)));
	assertTrue(encodings.contains(
			MimeUtility.getEncoding(new DataHandler(bads))));
    }

    /**
     * Test that encoding a Unicode string with surrogate pairs
     * doesn't split the encoding between the pairs.
     */
    @Test
    public void testSurrogatePairs() throws Exception {
	// test a specific case
	String sp = "a" +
		    "\ud801\udc00\ud801\udc00\ud801\udc00\ud801\udc00" +
		    "\ud801\udc00\ud801\udc00\ud801\udc00\ud801\udc00" +
		    "\ud801\udc00\ud801\udc00\ud801\udc00\ud801\udc00" +
		    "\ud801\udc00\ud801\udc00\ud801\udc00\ud801\udc00";
	String en = MimeUtility.encodeText(sp, "utf-8", "B");
	String dt = MimeUtility.decodeText(en);
	// encoding it and decoding it shouldn't change it
	assertEquals(dt, sp);
	String[] w = en.split(" ");
	// the first word should end with the second half of a pair
	String dw = MimeUtility.decodeWord(w[0]);
	assertTrue(dw.charAt(dw.length()-1) == '\udc00');
	// and the second word should start with the first half of a pair
	dw = MimeUtility.decodeWord(w[1]);
	assertTrue(dw.charAt(0) == '\ud801');

	// test various string lengths
	int ch = 0xFE000;
	String test = "";
	for (int i = 0; i < 50; i++) {
	    test += new String(Character.toChars(ch));
	    String encoded = MimeUtility.encodeText(test, "UTF-8", "B");
	    String decoded = MimeUtility.decodeText(encoded);
	    assertEquals(decoded, test);
	}
    }

    /**
     * Test that encoded words with the wrong Chinese charset still
     * decode correctly.
     */
    @Test
    public void testBadChineseCharsets() throws Exception {
	String badgb2312 = "=?gb2312?B?xbfUqqLjIChFVVIpttK7u4EwhDYgKENOWSk=?=";
	String badgbk = "=?gbk?B?xbfUqqLjIChFVVIpttK7u4EwhDYgKENOWSk=?=";
	String badms936 = "=?ms936?B?xbfUqqLjIChFVVIpttK7u4EwhDYgKENOWSk=?=";
	String badcp936 = "=?cp936?B?xbfUqqLjIChFVVIpttK7u4EwhDYgKENOWSk=?=";
	String good = "=?gb18030?B?xbfUqqLjIChFVVIpttK7u4EwhDYgKENOWSk=?=";
	String goodDecoded = MimeUtility.decodeWord(good);

	assertEquals("gb2312", goodDecoded, MimeUtility.decodeWord(badgb2312));
	assertEquals("gbk", goodDecoded, MimeUtility.decodeWord(badgbk));
	assertEquals("ms936", goodDecoded, MimeUtility.decodeWord(badms936));
	assertEquals("cp936", goodDecoded, MimeUtility.decodeWord(badcp936));
    }
}
