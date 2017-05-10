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
import java.util.Set;
import java.util.HashSet;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.FetchProfile;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test IMAP FetchProfile items.
 */
public final class IMAPFetchProfileTest {

    // timeout the test in case of deadlock
    @Rule
    public Timeout deadlockTimeout = Timeout.seconds(20);

    private static final String RDATE = "23-Jun-2004 06:26:26 -0700";
    private static final String ENVELOPE =
	"(\"Wed, 23 Jun 2004 18:56:42 +0530\" \"test\" " +
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((NIL NIL \"testuser\" \"example.com\")) NIL NIL NIL " +
	"\"<40D98512.9040803@example.com>\")";

    public static interface IMAPTest {
	public void test(Folder folder, IMAPHandlerFetch handler)
				    throws MessagingException;
    }

    @Test
    public void testINTERNALDATEFetch() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Folder folder, IMAPHandlerFetch handler)
				    throws MessagingException {
		    FetchProfile fp = new FetchProfile();
		    fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
		    Message m = folder.getMessage(1);
		    folder.fetch(new Message[] { m }, fp);
		    assertTrue(handler.saw("INTERNALDATE"));
		    handler.reset();
		    assertTrue(m.getReceivedDate() != null);
		    assertFalse(handler.saw("INTERNALDATE"));
		}
	    },
	    new IMAPHandlerFetch() {
		@Override
		public void fetch(String line) throws IOException {
		    if (line.indexOf("INTERNALDATE") >= 0)
			saw.add("INTERNALDATE");
		    untagged("1 FETCH (INTERNALDATE \"" + RDATE + "\")");
		    ok();
		}
	    });
    }

    @Test
    public void testINTERNALDATEFetchEnvelope() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Folder folder, IMAPHandlerFetch handler)
				    throws MessagingException {
		    FetchProfile fp = new FetchProfile();
		    fp.add(FetchProfile.Item.ENVELOPE);
		    Message m = folder.getMessage(1);
		    folder.fetch(new Message[] { m }, fp);
		    assertTrue(handler.saw("INTERNALDATE"));
		    handler.reset();
		    assertTrue(m.getReceivedDate() != null);
		    assertFalse(handler.saw("INTERNALDATE"));
		}
	    },
	    new IMAPHandlerFetch() {
		@Override
		public void fetch(String line) throws IOException {
		    if (line.indexOf("INTERNALDATE") >= 0)
			saw.add("INTERNALDATE");
		    untagged("1 FETCH (INTERNALDATE \"" + RDATE + "\")");
		    ok();
		}
	    });
    }

    @Test
    public void testINTERNALDATENoFetch() {
	testWithHandler(
	    new IMAPTest() {
		@Override
		public void test(Folder folder, IMAPHandlerFetch handler)
				    throws MessagingException {
		    Message m = folder.getMessage(1);
		    assertTrue(m.getReceivedDate() != null);
		    assertTrue(handler.saw("INTERNALDATE"));
		}
	    },
	    new IMAPHandlerFetch() {
		@Override
		public void fetch(String line) throws IOException {
		    if (line.indexOf("INTERNALDATE") >= 0)
			saw.add("INTERNALDATE");
		    untagged("1 FETCH (ENVELOPE " + ENVELOPE +
			" INTERNALDATE \"" + RDATE + "\" RFC822.SIZE 0)");
		    ok();
		}
	    });
    }

    public void testWithHandler(IMAPTest test, IMAPHandlerFetch handler) {
        TestServer server = null;
        try {
            server = new TestServer(handler);
            server.start();

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "" + server.getPort());
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

            final Store store = session.getStore("imap");
	    Folder folder = null;
            try {
                store.connect("test", "test");
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
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
    private static class IMAPHandlerFetch extends IMAPHandler {
	// must be static because handler is cloned for each connection
	protected static Set<String> saw = new HashSet<>();

	@Override
        public void select(String line) throws IOException {
	    numberOfMessages = 1;
	    super.select(line);
	}

	public boolean saw(String item) {
	    return saw.contains(item);
	}

	public void reset() {
	    saw.clear();
	}
    }
}
