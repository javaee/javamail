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
import java.util.concurrent.CountDownLatch;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message;
import javax.mail.FetchProfile;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test untagged responses before IDLE continuation.
 */
public final class IMAPIdleUntaggedResponseTest {

    // timeout the test in case of deadlock
    @Rule
    public Timeout deadlockTimeout = Timeout.seconds(20);

    @Test
    public void test() {
        TestServer server = null;
        try {
            final IMAPHandlerIdleExists handler = new IMAPHandlerIdleExists();
            server = new TestServer(handler);
            server.start();

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "" + server.getPort());
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

            final Store store = session.getStore("imap");
	    Folder folder0 = null;
            try {
                store.connect("test", "test");
                final Folder folder = store.getFolder("INBOX");
		folder0 = folder;
                folder.open(Folder.READ_ONLY);

		// create a thread to make sure we're kicked out of idle
		Thread t = new Thread() {
		    @Override
		    public void run() {
			try {
			    handler.waitForIdle();
			    // now do something that is sure to touch the server
			    FetchProfile fp = new FetchProfile();
			    fp.add(FetchProfile.Item.ENVELOPE);
			    folder.fetch(folder.getMessages(), fp);
			} catch (Exception ex) {
			}
		    }
		};
		t.start();

		((com.sun.mail.imap.IMAPFolder)folder).idle();

		assertEquals("message count", 1, folder.getMessageCount());

	    } catch (Exception ex) {
		System.out.println(ex);
		//ex.printStackTrace();
		fail(ex.toString());
            } finally {
		if (folder0 != null)
		    folder0.close(false);
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
     * Custom handler.  Returns untagged responses before continuation,
     * followed by a flag change for one of the new messages, to make
     * sure the notification of the new message is seen.
     */
    private static final class IMAPHandlerIdleExists extends IMAPHandler {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void examine(String line) throws IOException {
	    numberOfMessages = 1;
	    super.examine(line);
	}

	@Override
        public void idle() throws IOException {
            untagged("1 EXISTS");
            untagged("1 RECENT");
	    cont();
            untagged("1 FETCH (FLAGS (\\Recent \\Seen))");
	    latch.countDown();
	    idleWait();
	    ok();
        }

	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }
}
