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

package com.sun.mail.imap;

import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test IMAPFolder methods.
 */
public final class IMAPFolderTest {

    // timeout the test in case of deadlock
    @Rule
    public Timeout deadlockTimeout = Timeout.seconds(20);

    private static final String utf8Folder = "test\u03b1";
    private static final String utf7Folder = "test&A7E-";

    public static abstract class IMAPTest {
	public void init(Properties props) { };
	public abstract void test(Store store, IMAPHandler handler)
				    throws Exception;
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-7
     * encoded name for the CREATE command.
     */
    @Test
    public void testUtf7FolderNameCreate() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    assertTrue(test.create(Folder.HOLDS_MESSAGES));
		}
	    },
	    new IMAPHandler() {
		@Override
		public void create(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "CREATE"
		    String name = st.nextToken();
		    if (name.equals(utf7Folder))
			ok();
		    else
			no("wrong name");
		}

		@Override
		public void list(String line) throws IOException {
		    untagged("LIST (\\HasNoChildren) \"/\" " + utf7Folder);
		    ok();
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-8
     * unencoded name for the CREATE command.
     */
    @Test
    public void testUtf8FolderNameCreate() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    assertTrue(test.create(Folder.HOLDS_MESSAGES));
		}
	    },
	    new IMAPUtf8Handler() {
		@Override
		public void create(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "CREATE"
		    String name = unquote(st.nextToken());
		    if (name.equals(utf8Folder))
			ok();
		    else
			no("wrong name");
		}

		@Override
		public void list(String line) throws IOException {
		    untagged("LIST (\\HasNoChildren) \"/\" \"" +
							utf8Folder + "\"");
		    ok();
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-7
     * encoded name for the DELETE command.
     */
    @Test
    public void testUtf7FolderNameDelete() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    assertTrue(test.delete(false));
		}
	    },
	    new IMAPHandler() {
		@Override
		public void delete(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "DELETE"
		    String name = st.nextToken();
		    if (name.equals(utf7Folder))
			ok();
		    else
			no("wrong name");
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-8
     * unencoded name for the DELETE command.
     */
    @Test
    public void testUtf8FolderNameDelete() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    assertTrue(test.delete(false));
		}
	    },
	    new IMAPUtf8Handler() {
		@Override
		public void delete(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "DELETE"
		    String name = unquote(st.nextToken());
		    if (name.equals(utf8Folder))
			ok();
		    else
			no("wrong name");
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-7
     * encoded name for the SELECT command.
     */
    @Test
    public void testUtf7FolderNameSelect() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    test.open(Folder.READ_WRITE);
		    test.close(true);
		    // no exception means success
		}
	    },
	    new IMAPHandler() {
		@Override
		public void select(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "SELECT"
		    String name = st.nextToken();
		    if (name.equals(utf7Folder))
			ok();
		    else
			no("wrong name");
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-8
     * unencoded name for the SELECT command.
     */
    @Test
    public void testUtf8FolderNameSelect() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    test.open(Folder.READ_WRITE);
		    test.close(true);
		    // no exception means success
		}
	    },
	    new IMAPUtf8Handler() {
		@Override
		public void select(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "SELECT"
		    String name = unquote(st.nextToken());
		    if (name.equals(utf8Folder))
			ok();
		    else
			no("wrong name");
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-7
     * encoded name for the EXAMINE command.
     */
    @Test
    public void testUtf7FolderNameExamine() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    test.open(Folder.READ_ONLY);
		    test.close(true);
		    // no exception means success
		}
	    },
	    new IMAPHandler() {
		@Override
		public void examine(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "EXAMINE"
		    String name = st.nextToken();
		    if (name.equals(utf7Folder))
			ok();
		    else
			no("wrong name");
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-8
     * unencoded name for the EXAMINE command.
     */
    @Test
    public void testUtf8FolderNameExamine() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    test.open(Folder.READ_ONLY);
		    test.close(true);
		    // no exception means success
		}
	    },
	    new IMAPUtf8Handler() {
		@Override
		public void examine(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "EXAMINE"
		    String name = unquote(st.nextToken());
		    if (name.equals(utf8Folder))
			ok();
		    else
			no("wrong name");
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-7
     * encoded name for the STATUS command.
     */
    @Test
    public void testUtf7FolderNameStatus() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    assertEquals(123, ((UIDFolder)test).getUIDValidity());
		}
	    },
	    new IMAPHandler() {
		@Override
		public void status(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "STATUS"
		    String name = st.nextToken();
		    if (name.equals(utf7Folder)) {
			untagged("STATUS " + utf7Folder +
				    " (UIDVALIDITY 123)");
			ok();
		    } else
			no("wrong name");
		}
	    });
    }

    /**
     * Test that using a UTF-8 folder name results in the proper UTF-8
     * unencoded name for the STATUS command.
     */
    @Test
    public void testUtf8FolderNameStatus() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder(utf8Folder);
		    assertEquals(123, ((UIDFolder)test).getUIDValidity());
		}
	    },
	    new IMAPUtf8Handler() {
		@Override
		public void status(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip tag
		    st.nextToken();	// skip "STATUS"
		    String name = unquote(st.nextToken());
		    if (name.equals(utf8Folder)) {
			untagged("STATUS \"" + utf8Folder +
				    "\" (UIDVALIDITY 123)");
			ok();
		    } else
			no("wrong name");
		}
	    });
    }

    /**
     * Test that UIDNOTSTICKY is false in the formal case.
     */
    @Test
    public void testUidNotStickyFalse() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder("test");
		    try {
			test.open(Folder.READ_WRITE);
			assertFalse(((IMAPFolder)test).getUIDNotSticky());
		    } finally {
			test.close();
		    }
		}
	    },
	    new IMAPHandler());
    }

    /**
     * Test that UIDNOTSTICKY is true when the untagged response is included.
     */
    @Test
    public void testUidNotStickyTrue() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder("test");
		    try {
			test.open(Folder.READ_WRITE);
			assertTrue(((IMAPFolder)test).getUIDNotSticky());
		    } finally {
			test.close();
		    }
		}
	    },
	    new IMAPHandler() {
		@Override
		public void select(String line) throws IOException {
		    untagged("NO [UIDNOTSTICKY]");
		    super.select(line);
		}
	    });
    }

    /**
     * Test that EXPUNGE responses with out-of-range message numbers
     * are ignored.
     */
    @Test
    public void testExpungeOutOfRange() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Store store, IMAPHandler handler)
				    throws MessagingException, IOException {
		    Folder test = store.getFolder("test");
		    try {
			test.open(Folder.READ_WRITE);
			// no way to force a noop without waiting so do this
			assertEquals(0, test.getUnreadMessageCount());
			assertEquals(0, test.getMessageCount());
		    } finally {
			test.close();
		    }
		}
	    },
	    new IMAPHandler() {
		@Override
		public void search(String line) throws IOException {
		    untagged("1 EXPUNGE");
		    untagged("0 EXISTS");
		    super.search(line);
		}
	    });
    }

    private void testWithHandler(IMAPTest test, IMAPHandler handler) {
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
            try {
                store.connect("test", "test");
		test.test(store, handler);
	    } catch (Exception ex) {
		System.out.println(ex);
		//ex.printStackTrace();
		fail(ex.toString());
            } finally {
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

    private static String unquote(String s) {
	if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 1) {
	    s = s.substring(1, s.length() - 1);
	    // check for any escaped characters
	    if (s.indexOf('\\') >= 0) {
		StringBuilder sb = new StringBuilder(s.length());	// approx
		for (int i = 0; i < s.length(); i++) {
		    char c = s.charAt(i);
		    if (c == '\\' && i < s.length() - 1)
			c = s.charAt(++i);
		    sb.append(c);
		}
		s = sb.toString();
	    }
	}
	return s;
    }

    /**
     * An IMAPHandler that enables UTF-8 support.
     */
    private static class IMAPUtf8Handler extends IMAPHandler {
	{{ capabilities += " ENABLE UTF8=ACCEPT"; }}

	@Override
	public void enable(String line) throws IOException {
	    ok();
	}
    }
}
