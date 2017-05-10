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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.FetchProfile;
import javax.mail.MessagingException;
import javax.mail.event.ConnectionAdapter;
import javax.mail.event.ConnectionEvent;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test IdleManager.
 */
public final class IMAPIdleManagerTest {

    private static final int TIMEOUT = 1000;	// 1 second

    // timeout the test in case of deadlock
    @Rule
    public Timeout deadlockTimeout = Timeout.millis(10 * TIMEOUT);

    /**
     * Test that IdleManager handles multiple responses in a single packet.
     */
    @Test
    public void testDone() {
	testSuccess(new IMAPHandlerIdleDone());
    }

    @Test
    public void testExists() {
	testSuccess(new IMAPHandlerIdleExists());
    }

    private void testSuccess(IMAPHandlerIdle handler) {
        TestServer server = null;
	IdleManager idleManager = null;
        try {
            server = new TestServer(handler);
            server.start();

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "" + server.getPort());
            properties.setProperty("mail.imap.usesocketchannels", "true");
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

	    ExecutorService executor = Executors.newCachedThreadPool();
	    idleManager = new IdleManager(session, executor);

            final IMAPStore store = (IMAPStore)session.getStore("imap");
	    Folder folder = null;
            try {
                store.connect("test", "test");
		folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		idleManager.watch(folder);
		handler.waitForIdle();

		// now do something that is sure to touch the server
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		folder.fetch(folder.getMessages(), fp);

		// check that the new message was seen
		int count = folder.getMessageCount();
		folder.close(true);

		assertEquals(3, count);
	    } catch (Exception ex) {
		System.out.println(ex);
		ex.printStackTrace();
		fail(ex.toString());
            } finally {
		try {
		    folder.close(false);
		} catch (Exception ex2) { }
                store.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
	    if (idleManager != null)
		idleManager.stop();
            if (server != null) {
                server.quit();
            }
        }
    }

    /**
     * Test that IdleManager handles timeouts.
     */
    @Test
    public void testBeforeIdleTimeout() {
	testFailure(new IMAPHandlerBeforeIdleTimeout(), true);
    }

    @Test
    public void testIdleTimeout() {
	testFailure(new IMAPHandlerIdleTimeout(), true);
    }

    @Test
    public void testDoneTimeout() {
	testFailure(new IMAPHandlerDoneTimeout(), true);
    }

    /**
     * Test that IdleManager handles connection failures.
     */
    @Test
    public void testBeforeIdleDrop() {
	testFailure(new IMAPHandlerBeforeIdleDrop(), false);
    }

    @Test
    public void testIdleDrop() {
	testFailure(new IMAPHandlerIdleDrop(), false);
    }

    @Test
    public void testDoneDrop() {
	testFailure(new IMAPHandlerDoneDrop(), false);
    }

    private void testFailure(IMAPHandlerIdle handler, boolean setTimeout) {
        TestServer server = null;
	IdleManager idleManager = null;
        try {
            server = new TestServer(handler);
            server.start();

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "" + server.getPort());
	    if (setTimeout)
		properties.setProperty("mail.imap.timeout", "" + TIMEOUT);
            properties.setProperty("mail.imap.usesocketchannels", "true");
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

	    ExecutorService executor = Executors.newCachedThreadPool();
	    idleManager = new IdleManager(session, executor);

            final IMAPStore store = (IMAPStore)session.getStore("imap");
	    Folder folder = null;
            try {
                store.connect("test", "test");
		folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		idleManager.watch(folder);
		handler.waitForIdle();

		// now do something that is sure to touch the server
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		folder.fetch(folder.getMessages(), fp);

		fail("No exception");
	    } catch (MessagingException mex) {
		// success!
	    } catch (Exception ex) {
		System.out.println("Failed with exception: " + ex);
		ex.printStackTrace();
		fail(ex.toString());
            } finally {
		try {
		    folder.close(false);
		} catch (Exception ex2) { }
                store.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
	    if (idleManager != null)
		idleManager.stop();
            if (server != null) {
                server.quit();
            }
        }
    }

    @Test
    public void testNotOpened() {
        TestServer server = null;
	IdleManager idleManager = null;
        try {
            server = new TestServer(new IMAPHandler());
            server.start();

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "" + server.getPort());
            properties.setProperty("mail.imap.usesocketchannels", "true");
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

	    ExecutorService executor = Executors.newCachedThreadPool();
	    idleManager = new IdleManager(session, executor);

            final IMAPStore store = (IMAPStore)session.getStore("imap");
	    Folder folder = null;
            try {
                store.connect("test", "test");
		folder = store.getFolder("INBOX");
		idleManager.watch(folder);

		fail("No exception");
	    } catch (MessagingException mex) {
		// make sure we get the expected exception
		assertTrue(mex.getMessage().contains("open"));
		// success!
	    } catch (Exception ex) {
		System.out.println("Failed with exception: " + ex);
		ex.printStackTrace();
		fail(ex.toString());
            } finally {
		try {
		    folder.close(false);
		} catch (Exception ex2) { }
                store.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
	    if (idleManager != null)
		idleManager.stop();
            if (server != null) {
                server.quit();
            }
        }
    }

    @Test
    public void testNoSocketChannel() {
        TestServer server = null;
	IdleManager idleManager = null;
        try {
            server = new TestServer(new IMAPHandler());
            server.start();

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "" + server.getPort());
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

	    ExecutorService executor = Executors.newCachedThreadPool();
	    idleManager = new IdleManager(session, executor);

            final IMAPStore store = (IMAPStore)session.getStore("imap");
	    Folder folder = null;
            try {
                store.connect("test", "test");
		folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		idleManager.watch(folder);

		fail("No exception");
	    } catch (MessagingException mex) {
		// make sure we get the expected exception
		assertTrue(!mex.getMessage().contains("open"));
		// success!
	    } catch (Exception ex) {
		System.out.println("Failed with exception: " + ex);
		ex.printStackTrace();
		fail(ex.toString());
            } finally {
		try {
		    folder.close(false);
		} catch (Exception ex2) { }
                store.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
	    if (idleManager != null)
		idleManager.stop();
            if (server != null) {
                server.quit();
            }
        }
    }

    /**
     * Base class for custom handler.
     */
    private static abstract class IMAPHandlerIdle extends IMAPHandler {
	@Override
        public void select(String line) throws IOException {
	    numberOfMessages = 1;
	    super.select(line);
	}

	public abstract void waitForIdle() throws InterruptedException;
    }

    /**
     * Custom handler.  Respond to DONE with a single packet containing
     * EXISTS and OK.
     */
    private static final class IMAPHandlerIdleDone extends IMAPHandlerIdle {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void idle() throws IOException {
	    cont();
	    latch.countDown();
	    idleWait();
	    println("* 3 EXISTS\r\n" + tag + " OK");
        }

	@Override
	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }

    /**
     * Custom handler.  Send two EXISTS responses in a single packet.
     */
    private static final class IMAPHandlerIdleExists extends IMAPHandlerIdle {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void idle() throws IOException {
	    cont();
	    latch.countDown();
	    idleWait();
	    println("* 2 EXISTS\r\n* 3 EXISTS");
	    ok();
        }

	@Override
	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }

    /**
     * Custom handler.  Delay long enough before IDLE starts to force a timeout.
     */
    private static final class IMAPHandlerBeforeIdleTimeout
						    extends IMAPHandlerIdle {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void idle() throws IOException {
	    try {
		Thread.sleep(2 * TIMEOUT);
	    } catch (InterruptedException ex) { }
	    cont();
	    latch.countDown();
	    idleWait();
	    ok();
        }

	@Override
	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }

    /**
     * Custom handler.  Delay long enough after IDLE starts to force a timeout.
     */
    private static final class IMAPHandlerIdleTimeout extends IMAPHandlerIdle {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void idle() throws IOException {
	    cont();
	    latch.countDown();
	    try {
		Thread.sleep(2 * TIMEOUT);
	    } catch (InterruptedException ex) { }
	    idleWait();
	    ok();
        }

	@Override
	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }

    /**
     * Custom handler.  Delay long enough after DONE received to force a
     * timeout.
     */
    private static final class IMAPHandlerDoneTimeout extends IMAPHandlerIdle {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void idle() throws IOException {
	    cont();
	    latch.countDown();
	    idleWait();
	    try {
		Thread.sleep(2 * TIMEOUT);
	    } catch (InterruptedException ex) { }
	    ok();
        }

	@Override
	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }

    /**
     * Custom handler.  Drop the connection before IDLE started.
     */
    private static final class IMAPHandlerBeforeIdleDrop extends
							    IMAPHandlerIdle {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void idle() throws IOException {
	    latch.countDown();
	    exit();
        }

	@Override
	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }

    /**
     * Custom handler.  Drop the connection after IDLE started.
     */
    private static final class IMAPHandlerIdleDrop extends IMAPHandlerIdle {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void idle() throws IOException {
	    cont();
	    latch.countDown();
	    exit();
        }

	@Override
	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }

    /**
     * Custom handler.  Drop the connection after DONE received.
     */
    private static final class IMAPHandlerDoneDrop extends IMAPHandlerIdle {
	// must be static because handler is cloned for each connection
	private static CountDownLatch latch = new CountDownLatch(1);

	@Override
        public void idle() throws IOException {
	    cont();
	    latch.countDown();
	    idleWait();
	    exit();
        }

	@Override
	public void waitForIdle() throws InterruptedException {
	    latch.await();
	}
    }
}
