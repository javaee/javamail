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

package com.sun.mail.pop3;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message;
import javax.mail.FolderClosedException;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test that FolderClosedException is thrown when server times out connection.
 * This test is derived from real failures seen with Hotmail.
 *
 * @author sbo
 * @author Bill Shannon
 */
public final class POP3FolderClosedExceptionTest {

    /**
     * Test that FolderClosedException is thrown when the timeout occurs
     * when reading the message body.
     */
    @Test
    public void testFolderClosedExceptionBody() {
	TestServer server = null;
	try {
	    final POP3Handler handler = new POP3HandlerTimeoutBody();
	    server = new TestServer(handler);
	    server.start();
	    Thread.sleep(1000);

	    final Properties properties = new Properties();
	    properties.setProperty("mail.pop3.host", "localhost");
	    properties.setProperty("mail.pop3.port", "" + server.getPort());
	    final Session session = Session.getInstance(properties);
	    //session.setDebug(true);

	    final Store store = session.getStore("pop3");
	    try {
		store.connect("test", "test");
		final Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);
		Message msg = folder.getMessage(1);
		try {
		    msg.getContent();
		} catch (IOException ioex) {
		    // expected
		    // first attempt detects error return from server
		}
		// second attempt detects closed connection from server
		msg.getContent();

		// Check
		assertFalse(folder.isOpen());
	    } catch (FolderClosedException ex) {
		// success!
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

    /**
     * Custom handler.  Returns ERR for RETR the first time,
     * then closes the connection the second time.
     */
    private static final class POP3HandlerTimeoutBody extends POP3Handler {

	private boolean first = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void retr(String arg) throws IOException {
	    if (first) {
		println("-ERR Server timeout");
		first = false;
	    } else
		exit();
	}
    }

    /**
     * Test that FolderClosedException is thrown when the timeout occurs
     * when reading the headers.
     */
    @Test
    public void testFolderClosedExceptionHeaders() {
	TestServer server = null;
	try {
	    final POP3Handler handler = new POP3HandlerTimeoutHeader();
	    server = new TestServer(handler);
	    server.start();
	    Thread.sleep(1000);

	    final Properties properties = new Properties();
	    properties.setProperty("mail.pop3.host", "localhost");
	    properties.setProperty("mail.pop3.port", "" + server.getPort());
	    final Session session = Session.getInstance(properties);
	    //session.setDebug(true);

	    final Store store = session.getStore("pop3");
	    try {
		store.connect("test", "test");
		final Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);
		Message msg = folder.getMessage(1);
		msg.getSubject();

		// Check
		assertFalse(folder.isOpen());
	    } catch (FolderClosedException ex) {
		// success!
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

    /**
     * Custom handler.  Returns ERR for TOP, then closes connection.
     */
    private static final class POP3HandlerTimeoutHeader extends POP3Handler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void top(String arg) throws IOException {
	    println("-ERR Server timeout");
	    exit();
	}
    }
}
