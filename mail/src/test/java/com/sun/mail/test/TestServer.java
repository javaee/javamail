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

package com.sun.mail.test;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import javax.net.ssl.*;

/**
 * A simple server for testing.
 *
 * Inspired by, and derived from, POP3Server by sbo.
 *
 * @author sbo
 * @author Bill Shannon
 */
public final class TestServer extends Thread {

    /** Server socket. */
    private ServerSocket serverSocket;

    /** Keep on? */
    private volatile boolean keepOn;

    /** Protocol handler. */
    private final ProtocolHandler handler;

    private List<Thread> clients = new ArrayList<Thread>();

    /**
     * Test server.
     *
     * @param handler	the protocol handler
     */
    public TestServer(final ProtocolHandler handler) throws IOException {
	this(handler, false);
    }

    /**
     * Test server.
     *
     * @param handler	the protocol handler
     * @param isSSL	create SSL sockets?
     */
    public TestServer(final ProtocolHandler handler, final boolean isSSL)
				throws IOException {
        this.handler = handler;

	/*
	 * Allowing the JDK to pick a port number sometimes results in it
	 * picking a number that's already in use by another process, but
	 * no error is returned.  Picking it ourself allows us to make sure
	 * that it's not used before we pick it.  Hopefully the socket
	 * creation will fail if the port is already in use.
	 *
	 * XXX - perhaps we should use Random to choose a port number in
	 * the emphemeral range, in case a lot of low port numbers are
	 * already in use.
	 */
	for (int port = 49152; port < 50000 /*65535*/; port++) {
	    /*
	    if (isListening(port))
		continue;
	    */
	    try {
		serverSocket = createServerSocket(port, isSSL);
		return;
	    } catch (IOException ex) {
		// ignore
	    }
	}
	throw new RuntimeException("Can't find unused port");
    }

    private static ServerSocket createServerSocket(int port, boolean isSSL)
				throws IOException {
	ServerSocket ss;
	if (isSSL) {
	    SSLServerSocketFactory sf =
		(SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
	    ss = sf.createServerSocket(port);
	    // enable only the anonymous cipher suites so we don't have to
	    // create a server certificate
	    List<String> anon = new ArrayList<>();
	    String[] suites = sf.getSupportedCipherSuites();
	    for (int i = 0; i < suites.length; i++) {
		if (suites[i].indexOf("_anon_") >= 0) {
		    anon.add(suites[i]);
		}
	    }
	    ((SSLServerSocket)ss).setEnabledCipherSuites(
				    anon.toArray(new String[anon.size()]));
	} else
	    ss = new ServerSocket(port);
	return ss;
    }

    /**
     * Return the port the server is listening on.
     */
    public int getPort() {
	return serverSocket.getLocalPort();
    }

    /**
     * Exit server.
     */
    public void quit() {
        try {
            keepOn = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
	super.start();
	// don't return until server is really listening
	// XXX - this might not be necessary
	for (int tries = 0; tries < 10; tries++) {
	    if (isListening(getPort())) {
		return;
	    }
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException ex) { }
	}
	throw new RuntimeException("Server isn't listening");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            keepOn = true;

            while (keepOn) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    final ProtocolHandler pHandler =
			(ProtocolHandler)handler.clone();
                    pHandler.setClientSocket(clientSocket);
                    Thread t = new Thread(pHandler);
		    synchronized (clients) {
			clients.add(t);
		    }
		    t.start();
                } catch (final IOException e) {
                    //e.printStackTrace();
		} catch (NullPointerException nex) {
		    // serverSocket can be set to null before we could check
                }
            }
        } finally {
            quit();
        }
    }

    /**
     * Return number of clients ever created.
     */
    public int clientCount() {
	synchronized (clients) {
	    // isListening creates a client that we don't count
	    return clients.size() - 1;
	}
    }

    /**
     * Wait for at least n clients to terminate.
     */
    public void waitForClients(int n) {
	if (n > clientCount())
	    throw new RuntimeException("not that many clients");
	for (;;) {
	    int num = -1;	// ignore isListening client
	    synchronized (clients) {
		for (Thread t : clients) {
		    if (!t.isAlive()) {
			if (++num >= n)
			    return;
		    }
		}
	    }
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException ex) { }
	}
    }

    private boolean isListening(int port) {
	try {
	    Socket s = new Socket();
	    s.connect(new InetSocketAddress("localhost", port), 100);
	    // it's listening!
	    s.close();
	    return true;
	} catch (Exception ex) {
	    //System.out.println(ex);
	}
	return false;
    }
}
