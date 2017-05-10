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

package com.sun.mail.util;

import java.lang.reflect.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.activation.DataHandler;
import javax.net.ssl.*;

import com.sun.mail.imap.IMAPHandler;
import com.sun.mail.test.TestServer;
import com.sun.mail.test.TestSocketFactory;
import com.sun.mail.test.TestSSLSocketFactory;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

/**
 * Test that write timeouts work.
 */
public final class WriteTimeoutSocketTest {

    // timeout the test in case of deadlock
    @Rule
    public Timeout deadlockTimeout = Timeout.seconds(20);

    private static final int TIMEOUT = 200;	// ms
    private static final String data =
	"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Test write timeouts with plain sockets.
     */
    @Test
    public void test() {
	final Properties properties = new Properties();
	properties.setProperty("mail.imap.host", "localhost");
	properties.setProperty("mail.imap.writetimeout", "" + TIMEOUT);
	test(properties, false);
    }

    /**
     * Test write timeouts with custom socket factory.
     */
    @Test
    public void testSocketFactory() {
	final Properties properties = new Properties();
	properties.setProperty("mail.imap.host", "localhost");
	properties.setProperty("mail.imap.writetimeout", "" + TIMEOUT);
	TestSocketFactory sf = new TestSocketFactory();
	properties.put("mail.imap.socketFactory", sf);
	properties.setProperty("mail.imap.socketFactory.fallback", "false");
	test(properties, false);
	// make sure our socket factory was actually used
	assertTrue(sf.getSocketCreated());
    }

    /**
     * Test write timeouts with SSL sockets.
     */
    @Test
    public void testSSL() {
	final Properties properties = new Properties();
	properties.setProperty("mail.imap.host", "localhost");
	properties.setProperty("mail.imap.writetimeout", "" + TIMEOUT);
	properties.setProperty("mail.imap.ssl.enable", "true");
	// enable only the anonymous cipher suites since there's no
	// server certificate
	properties.setProperty("mail.imap.ssl.ciphersuites",
						    getAnonCipherSuites());
	test(properties, true);
    }

    /**
     * Test write timeouts with a custom SSL socket factory.
     */
    @Test
    public void testSSLSocketFactory() throws Exception {
	final Properties properties = new Properties();
	properties.setProperty("mail.imap.host", "localhost");
	properties.setProperty("mail.imap.writetimeout", "" + TIMEOUT);
	properties.setProperty("mail.imap.ssl.enable", "true");
	TestSSLSocketFactory sf = new TestSSLSocketFactory();
	sf.setDefaultCipherSuites(getAnonCipherSuitesArray());
	properties.put("mail.imap.ssl.socketFactory", sf);
	// don't fall back to non-SSL
	properties.setProperty("mail.imap.socketFactory.fallback", "false");
	// enable only the anonymous cipher suites since there's no
	// server certificate
	properties.setProperty("mail.imap.ssl.ciphersuites",
						    getAnonCipherSuites());
	test(properties, true);
	// make sure our socket factory was actually used
	assertTrue(sf.getSocketWrapped() || sf.getSocketCreated());
    }

    /**
     * Test that WriteTimeoutSocket overrides all methods from Socket.
     * XXX - this is kind of hacky since it depends on Method.toString
     */
    @Test
    public void testOverrides() throws Exception {
	Set<String> socketMethods = new HashSet<>();
	Method[] m = java.net.Socket.class.getDeclaredMethods();
	String className = java.net.Socket.class.getName() + ".";
	for (int i = 0; i < m.length; i++) {
	    if (Modifier.isPublic(m[i].getModifiers()) &&
		!Modifier.isStatic(m[i].getModifiers())) {
		String name = m[i].toString().
				    replace("synchronized ", "").
				    replace(className, "");
		socketMethods.add(name);
	    }
	}
	Set<String> wtsocketMethods = new HashSet<>();
	m = WriteTimeoutSocket.class.getDeclaredMethods();
	className = WriteTimeoutSocket.class.getName() + ".";
	for (int i = 0; i < m.length; i++) {
	    if (Modifier.isPublic(m[i].getModifiers())) {
		String name = m[i].toString().
				    replace("synchronized ", "").
				    replace(className, "");
		socketMethods.remove(name);
	    }
	}
	for (String s : socketMethods)
	    System.out.println("WriteTimeoutSocket did not override: " + s);
	assertTrue(socketMethods.isEmpty());
    }

    private static String[] getAnonCipherSuitesArray() {
	SSLSocketFactory sf = (SSLSocketFactory)SSLSocketFactory.getDefault();
	List<String> anon = new ArrayList<>();
	String[] suites = sf.getSupportedCipherSuites();
	for (int i = 0; i < suites.length; i++) {
	    if (suites[i].indexOf("_anon_") >= 0) {
		anon.add(suites[i]);
	    }
	}
	return anon.toArray(new String[anon.size()]);
    }

    private static String getAnonCipherSuites() {
	SSLSocketFactory sf = (SSLSocketFactory)SSLSocketFactory.getDefault();
	StringBuilder anon = new StringBuilder();
	String[] suites = sf.getSupportedCipherSuites();
	for (int i = 0; i < suites.length; i++) {
	    if (suites[i].indexOf("_anon_") >= 0) {
		if (anon.length() > 0)
		    anon.append(" ");
		anon.append(suites[i]);
	    }
	}
	return anon.toString();
    }

    private void test(Properties properties, boolean isSSL) {
        TestServer server = null;
        try {
            final TimeoutHandler handler = new TimeoutHandler();
            server = new TestServer(handler, isSSL);
            server.start();

	    properties.setProperty("mail.imap.port", "" + server.getPort());
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

	    MimeMessage msg = new MimeMessage(session);
	    msg.setFrom("test@example.com");
	    msg.setSubject("test");
	    final int size = 8192000;	// enough data to fill network buffers
	    byte[] part = new byte[size];
	    for (int i = 0; i < size; i++) {
		int j = i % 64;
		if (j == 62)
		    part[i] = (byte)'\r';
		else if (j == 63)
		    part[i] = (byte)'\n';
		else
		    part[i] = (byte)data.charAt((j + i / 64) % 62);
	    }
	    msg.setDataHandler(new DataHandler(
		new ByteArrayDataSource(part, "text/plain")));
	    msg.saveChanges();

            final Store store = session.getStore("imap");
            try {
                store.connect("test", "test");
		final Folder f = store.getFolder("test");
		f.appendMessages(new Message[] { msg });
		fail("No timeout");
	    } catch (StoreClosedException scex) {
		// success!
	    } catch (Exception ex) {
		System.out.println(ex);
		//ex.printStackTrace();
		fail(ex.toString());
            } finally {
                store.close();
            }
        } catch (final Exception e) {
            //e.printStackTrace();
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
    private static final class TimeoutHandler extends IMAPHandler {
	@Override
        protected void collectMessage(int bytes) throws IOException {
	    try {
		// allow plenty of time for even slow machines to time out
		Thread.sleep(TIMEOUT*20);
	    } catch (InterruptedException ex) { }
	    super.collectMessage(bytes);
        }

	@Override
	public void list(String line) throws IOException {
	    untagged("LIST () \"/\" test");
	    ok();
	}
    }
}
