/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
    private static final String ENVELOPE =
	"(\"Wed, 23 Jun 2004 18:56:42 +0530\" \"test\" " +
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((\"JavaMail\" NIL \"testuser\" \"example.com\")) " +
	"((NIL NIL \"testuser\" \"example.com\")) NIL NIL NIL " +
	"\"<40D98512.9040803@example.com>\")";

    public static interface IMAPTest {
	public void test(Folder folder, IMAPHandlerMessage handler)
				    throws MessagingException;
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

    public void testWithHandler(IMAPTest test, IMAPHandlerMessage handler) {
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
        public void examine() throws IOException {
	    numberOfMessages = 1;
	    super.examine();
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
