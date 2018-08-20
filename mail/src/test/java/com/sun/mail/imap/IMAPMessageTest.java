/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.imap;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test IMAPMessage methods.
 */
public final class IMAPMessageTest {

    // timeout the test in case of deadlock
    @Rule
    public Timeout deadlockTimeout = Timeout.seconds(20);

    private static final String RDATE = "23-Jun-2004 06:26:26 -0700";
    private static final String ENV_DATE =
	"\"Wed, 23 Jun 2004 18:56:42 +0530\"";
    private static final String ENV_SUBJECT = "\"test\"";
    private static final String ENV_UTF8_ENCODED_SUBJECT =
      "=?UTF-8?B?VVRGOCB0ZXN0OiDgsqzgsr4g4LKH4LKy4LON4LKy4LK/IOCyuOCygg==?= " +
      "=?UTF-8?B?4LKt4LK14LK/4LK44LOBIOCyh+CyguCypuCzhuCyqA==?= " +
      "=?UTF-8?B?4LON4LKoIOCyueCzg+CypuCyr+CypuCysuCyvyA=?=";
    private static final String ENV_ADDRS =
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((NIL NIL \"testuser\" \"example.com\")) NIL NIL NIL " +
	"\"<40D98512.9040803@example.com>\"";
    private static final String ENVELOPE =
	"(" + ENV_DATE + " " + ENV_SUBJECT + " " + ENV_ADDRS + ")";

    public static abstract class IMAPTest {
	public void init(Properties props) { };
	public abstract void test(Folder folder, IMAPHandlerMessage handler)
				    throws Exception;
    }

    /**
     * Test that a small message size is returned correctly.
     */
    @Test
    public void testSizeSmall() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Folder folder, IMAPHandlerMessage handler)
				    throws MessagingException {
		    Message m = folder.getMessage(1);
		    assertEquals(123, m.getSize());
		}
	    },
	    new IMAPHandlerMessage() {
		{{ size = 123; }}
	    });
    }

    /**
     * Test that a large message size is returned as Integer.MAX_VALUE
     * from MimeMessage.getSize and returned as the actual value from
     * IMAPMessage.getSizeLong.
     */
    @Test
    public void testSizeLarge() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Folder folder, IMAPHandlerMessage handler)
				    throws MessagingException {
		    Message m = folder.getMessage(1);
		    assertEquals(Integer.MAX_VALUE, m.getSize());
		    assertEquals((long)Integer.MAX_VALUE + 1,
				    ((IMAPMessage)m).getSizeLong());
		}
	    },
	    new IMAPHandlerMessage() {
		{{ size = (long)Integer.MAX_VALUE + 1; }}
	    });
    }

    /**
     * Test that returning NIL instead of an empty string for the content
     * of the message works correctly.
     */
    @Test
    public void testEmptyBody() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void init(Properties props) {
		    props.setProperty("mail.imap.partialfetch","false");
		}

		@Override
		public void test(Folder folder, IMAPHandlerMessage handler)
				    throws MessagingException, IOException {
		    Message m = folder.getMessage(1);
		    String t = (String)m.getContent();
		    assertEquals("", t);
		}
	    },
	    new IMAPHandlerMessage() {
		@Override
		public void fetch(String line) throws IOException {
		    if (line.indexOf("BODYSTRUCTURE") >= 0)
			untagged("1 FETCH (BODYSTRUCTURE " +
			    "(\"text\" \"plain\" (\"charset\" \"us-ascii\") " +
				"NIL NIL \"7bit\" 0 0 NIL NIL NIL NIL)" +
			    ")");
		    else if (line.indexOf("BODY[TEXT]") >= 0)
			untagged("1 FETCH (BODY[TEXT] NIL " +
				    "FLAGS (\\Seen \\Recent))");
		    ok();
		}
	    });
    }

    @Test
    public void testAttachementFileName() {
        testWithHandler(
                new IMAPTest() {
                    @Override
                    public void test(Folder folder, IMAPHandlerMessage handler) throws MessagingException, IOException {
                        Message m = folder.getMessage(1);
                        Multipart mp = (Multipart)m.getContent();
                        BodyPart bp = mp.getBodyPart(1);
                        assertEquals("filename.csv", MimeUtility.decodeText(bp.getFileName()));
                    }
                },
                new IMAPHandlerMessage() {
                    @Override
                    public void fetch(String line) throws IOException {
                        untagged("1 FETCH (BODYSTRUCTURE (" +
                                "(\"text\" \"html\" (\"charset\" \"utf-8\") NIL NIL \"base64\" 402 6 NIL NIL NIL NIL)" +
                                "(\"application\" \"octet-stream\" (\"name\" \"=?utf-8?B?ZmlsZW5hbWU=?= =?utf-8?B?LmNzdg==?=\") NIL NIL \"base64\" 658 NIL " +
                                "(\"attachment\" (\"filename\" \"\")) NIL NIL) \"mixed\" " +
                                "(\"boundary\" \"--boundary_539_27806e16-2599-4612-b98a-69335bedd206\") NIL NIL NIL))"
                        );
                        ok();
                    }
                }
        );
    }

    /**
     * Test that returning NIL instead of an empty string for the content
     * of an empty body part works correctly.
     */
    @Test
    public void testEmptyBodyAttachment() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void init(Properties props) {
		    props.setProperty("mail.imap.partialfetch","false");
		}

		@Override
		public void test(Folder folder, IMAPHandlerMessage handler)
				    throws MessagingException, IOException {
		    Message m = folder.getMessage(1);
		    Multipart mp = (Multipart)m.getContent();
		    BodyPart bp = mp.getBodyPart(1);
		    String t = (String)bp.getContent();
		    assertEquals("", t);
		}
	    },
	    new IMAPHandlerMessage() {
		@Override
		public void fetch(String line) throws IOException {
		    if (line.indexOf("BODYSTRUCTURE") >= 0)
			untagged("1 FETCH (BODYSTRUCTURE (" +
			    "(\"text\" \"plain\" (\"charset\" \"us-ascii\") " +
				"NIL NIL \"7bit\" 4 0 NIL NIL NIL NIL)" +
			    "(\"text\" \"plain\" (\"charset\" \"us-ascii\") " +
				"NIL NIL \"7bit\" 0 0 NIL NIL NIL NIL)" +
			    " \"mixed\" (\"boundary\" \"----=_x\") NIL NIL))");
		    else if (line.indexOf("BODY[2]") >= 0)
			untagged("1 FETCH (BODY[2] NIL " +
				    "FLAGS (\\Seen \\Recent))");
		    ok();
		}
	    });
    }

    /**
     * Test that returning NIL instead of an empty string for the content
     * of an empty body part works correctly.
     * This is a bug in office365.com.  Note the space in "base64 ".
     */
    @Test
    public void testBadEncoding() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void init(Properties props) {
		    props.setProperty("mail.imap.partialfetch","false");
		}

		@Override
		public void test(Folder folder, IMAPHandlerMessage handler)
				    throws MessagingException, IOException {
		    Message m = folder.getMessage(1);
		    Multipart mp = (Multipart)m.getContent();
		    BodyPart bp = mp.getBodyPart(1);
		    StringBuilder sb = new StringBuilder();
		    try (InputStream is = bp.getInputStream()) {
			int c;
			while ((c = is.read()) != -1)
			    sb.append((char)c);
		    }
		    assertEquals("test", sb.toString());
		}
	    },
	    new IMAPHandlerMessage() {
		@Override
		public void fetch(String line) throws IOException {
		    if (line.indexOf("BODYSTRUCTURE") >= 0)
			untagged("1 FETCH (BODYSTRUCTURE (" +
			    "(\"text\" \"plain\" (\"charset\" \"us-ascii\") " +
				"NIL NIL \"7bit\" 0 0 NIL NIL NIL NIL)" +
			    "(\"application\" \"octet-stream\" " +
				"(\"name\" \"test.txt\") NIL NIL \"base64 \" " +
				"8 NIL NIL NIL NIL) " +
			    "\"mixed\" (\"boundary\" \"=_x\") NIL NIL))");
		    else if (line.indexOf("BODY[2]") >= 0)
			untagged("1 FETCH (BODY[2] \"dGVzdA==\" " +
				    "FLAGS (\\Seen \\Recent))");
		    ok();
		}
	    });
    }


    /**
     * Test that a UTF-8 encoded Subject is decoded properly.
     */
    @Test
    public void testUtf8SubjectEncoded() {
	String s = null;
	try {
	    s = MimeUtility.decodeText(ENV_UTF8_ENCODED_SUBJECT);
	} catch (UnsupportedEncodingException ex) {
	}
	final String subject = s;

	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Folder folder, IMAPHandlerMessage handler)
				    throws MessagingException {
		    Message m = folder.getMessage(1);
		    assertEquals(subject, m.getSubject());
		}
	    },
	    new IMAPHandlerMessage() {
		{{
		    envelope = "(" + ENV_DATE + " \"" +
				ENV_UTF8_ENCODED_SUBJECT + "\" " +
				ENV_ADDRS + ")";
		}}
	    });
    }

    /**
     * Test that a UTF-8 Subject is decoded properly.
     */
    @Test
    public void testUtf8Subject() {
	String s = null;
	try {
	    s = MimeUtility.decodeText(ENV_UTF8_ENCODED_SUBJECT);
	} catch (UnsupportedEncodingException ex) {
	}
	final String subject = s;

	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Folder folder, IMAPHandlerMessage handler)
				    throws MessagingException {
		    Message m = folder.getMessage(1);
		    assertEquals(subject, m.getSubject());
		}
	    },
	    new IMAPHandlerMessage() {
		{{
		    envelope = "(" + ENV_DATE + " \"" + subject + "\" " +
				    ENV_ADDRS + ")";
		    capabilities += " ENABLE UTF8=ACCEPT";
		}}

		@Override
		public void enable(String line) throws IOException {
		    ok();
		}
	    });
    }

    private void testWithHandler(IMAPTest test, IMAPHandlerMessage handler) {
        TestServer server = null;
        try {
            server = new TestServer(handler);
            server.start();

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "" + server.getPort());
	    test.init(properties);
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

            final Store store = session.getStore("imap");
	    Folder folder = null;
            try {
                store.connect("test", "test");
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
		test.test(folder, handler);
	    } catch (Exception ex) {
		System.out.println(ex);
		//ex.printStackTrace();
		fail(ex.toString());
            } finally {
		if (folder != null)
		    folder.close(false);
                store.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
            }
        }
    }

    /**
     * Custom handler.
     */
    private static class IMAPHandlerMessage extends IMAPHandler {

	String rdate = RDATE;
	String envelope = ENVELOPE;
	long size = 0;

	@Override
        public void examine(String line) throws IOException {
	    numberOfMessages = 1;
	    super.examine(line);
	}

	@Override
	public void fetch(String line) throws IOException {
	    untagged("1 FETCH (ENVELOPE " + envelope +
		" INTERNALDATE \"" + rdate + "\" " +
		"RFC822.SIZE " + size + ")");
	    ok();
	}
    }
}
