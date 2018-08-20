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

package com.sun.mail.util;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.nio.charset.StandardCharsets;

import com.sun.mail.test.TestServer;
import com.sun.mail.test.ProtocolHandler;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test SocketFetcher.
 */
public final class SocketFetcherTest {

    // timeout the test in case of deadlock
    @Rule
    public Timeout deadlockTimeout = Timeout.seconds(20);

    /**
     * Test connecting with proxy host and port.
     */
    @Test
    public void testProxyHostPort() {
	assertTrue("proxy host, port", testProxy("proxy", "localhost", "PPPP"));
    }

    /**
     * Test connecting with proxy host and port and user name and password.
     */
    @Test
    public void testProxyHostPortUserPassword() {
	assertTrue("proxy host, port, user, password",
	    testProxyUserPassword("proxy", "localhost", "PPPP", "user", "pwd"));
    }

    /**
     * Test connecting with proxy host:port.
     */
    @Test
    public void testProxyHostColonPort() {
	assertTrue("proxy host:port", testProxy("proxy", "localhost:PPPP", null));
    }

    /**
     * Test connecting with socks host and port.
     */
    @Test
    public void testSocksHostPort() {
	assertTrue("socks host, port", testProxy("socks", "localhost", "PPPP"));
    }

    /**
     * Test connecting with socks host:port.
     */
    @Test
    public void testSocksHostColonPort() {
	assertTrue("socks host:port", testProxy("socks", "localhost:PPPP", null));
    }

    /**
     * Test connecting with no proxy.
     */
    @Test
    public void testNoProxy() {
	assertFalse("no proxy", testProxy("none", "localhost", null));
    }

    /**
     */
    public boolean testProxy(String type, String host, String port) {
	return testProxyUserPassword(type, host, port, null, null);
    }

    /**
     */
    public boolean testProxyUserPassword(String type, String host, String port,
					String user, String pwd) {
	TestServer server = null;
	try {
	    ProxyHandler handler = new ProxyHandler(type.equals("proxy"));
	    server = new TestServer(handler);
	    server.start();
	    String sport = "" + server.getPort();

	    //System.setProperty("mail.socket.debug", "true");
	    Properties properties = new Properties();
	    properties.setProperty("mail.test.host", "localhost");
	    properties.setProperty("mail.test.port", "2");
	    properties.setProperty("mail.test." + type + ".host",
				    host.replace("PPPP", sport));
	    if (port != null)
		properties.setProperty("mail.test." + type + ".port",
				    port.replace("PPPP", sport));
	    if (user != null)
		properties.setProperty("mail.test." + type + ".user", user);
	    if (pwd != null)
		properties.setProperty("mail.test." + type + ".password", pwd);

	    Socket s = null;
	    try {
		s = SocketFetcher.getSocket("localhost", 2,
					    properties, "mail.test", false);
	    } catch (Exception ex) {
		// ignore failure, which is expected
		//System.out.println(ex);
		//ex.printStackTrace();
	    } finally {
		if (s != null)
		    s.close();
	    }
	    if (!handler.getConnected())
		return false;
	    if (user != null && pwd != null)
		return (user + ":" + pwd).equals(handler.getUserPassword());
	    else
		return true;

	} catch (final Exception e) {
	    //e.printStackTrace();
	    fail(e.getMessage());
	    return false;	// XXX - doesn't matter
	} finally {
	    if (server != null) {
		server.quit();
	    }
	}
    }

    /**
     * Custom handler.  Remember whether any data was sent
     * and save user/password string;
     */
    private static class ProxyHandler extends ProtocolHandler {
	private boolean http;

	// must be static because handler is cloned for each connection
	private static volatile boolean connected;
	private static volatile String userPassword;

	public ProxyHandler(boolean http) {
	    this.http = http;
	    connected = false;
	}

	@Override
	public void handleCommand() throws IOException {
	    if (!http) {
		int c = in.read();
		if (c >= 0) {
		    // any data means a real client connected
		    connected = true;
		}
		exit();
	    }

	    // else, http...
	    String line;
	    while ((line = readLine()) != null) {
		// any data means a real client connected
		connected = true;
		if (line.length() == 0)
		    break;
		if (line.startsWith("Proxy-Authorization:")) {
		    int i = line.indexOf("Basic ") + 6;
		    String up = line.substring(i);
		    userPassword = new String(BASE64DecoderStream.decode(
				    up.getBytes(StandardCharsets.US_ASCII)),
				    StandardCharsets.UTF_8);
		}
	    }
	    exit();
	}

	public boolean getConnected() {
	    return connected;
	}

	public String getUserPassword() {
	    return userPassword;
	}
    }
}
