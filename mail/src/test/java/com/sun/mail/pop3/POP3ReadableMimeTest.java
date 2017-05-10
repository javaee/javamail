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
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.MessagingException;

import com.sun.mail.util.ReadableMime;
import com.sun.mail.test.TestServer;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test ReadableMime support for POP3.
 *
 * @author Bill Shannon
 */
public final class POP3ReadableMimeTest {

    private static TestServer server = null;
    private static Store store;
    private static Folder folder;

    private static void startServer(boolean cached) {
        try {
            final POP3Handler handler = new POP3Handler();
            server = new TestServer(handler);
            server.start();
            Thread.sleep(1000);

            final Properties properties = new Properties();
            properties.setProperty("mail.pop3.host", "localhost");
            properties.setProperty("mail.pop3.port", "" + server.getPort());
	    if (cached)
		properties.setProperty("mail.pop3.filecache.enable", "true");
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

            store = session.getStore("pop3");
	    store.connect("test", "test");
	    folder = store.getFolder("INBOX");
	    folder.open(Folder.READ_ONLY);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static void stopServer() {
	try {
	    if (folder != null)
		folder.close(false);
	    if (store != null)
		store.close();
	} catch (MessagingException ex) {
	    // ignore it
	} finally {
	    if (server != null)
		server.quit();
	}
    }

    /**
     * Test that the data returned by the getMimeStream method
     * is exactly the same data as produced by the writeTo method.
     */
    @Test
    public void testReadableMime() throws Exception {
	test(false);
    }

    /**
     * Now test it using the file cache.
     */
    @Test
    public void testReadableMimeCached() throws Exception {
	test(true);
    }

    private void test(boolean cached) throws Exception {
	startServer(cached);
	try {
	    Message[] msgs = folder.getMessages();
	    for (int i = 0; i < msgs.length; i++)
		verifyData(msgs[i]);
	} finally {
	    stopServer();
	}
	// no exception is success!
    }

    private void verifyData(Part p) throws MessagingException, IOException {
	assertTrue("ReadableMime", p instanceof ReadableMime);
	InputStream is = null;
	try {
	    ReadableMime rp = (ReadableMime)p;
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    p.writeTo(bos);
	    bos.close();
	    byte[] buf = bos.toByteArray();
	    is = rp.getMimeStream();
	    int i, b;
	    for (i = 0; (b = is.read()) != -1; i++)
		assertTrue("message data", b == (buf[i] & 0xff));
	    assertTrue("data size", i == buf.length);
	} finally {
	    try {
		is.close();
	    } catch (IOException ex) { }
	}
    }
}
