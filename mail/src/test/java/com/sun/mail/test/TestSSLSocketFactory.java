/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.net.*;
import java.security.GeneralSecurityException;

import javax.net.ssl.*;

/**
 * An SSL socket factory for testing that tracks whether it's being used.
 * <p>
 *
 * An instance of this factory can be set as the value of the
 * <code>mail.&lt;protocol&gt;.ssl.socketFactory</code> property.
 *
 * @since	JavaMail 1.5.3
 * @author	Stephan Sann
 * @author	Bill Shannon
 */
public class TestSSLSocketFactory extends SSLSocketFactory {

    /** Holds a SSLSocketFactory to pass all API-method-calls to */
    private SSLSocketFactory defaultFactory = null;

    /** Was a socket created? */
    private boolean socketCreated;

    /** Was a socket wrapped? */
    private boolean socketWrapped;

    private String[] suites;

    /**
     * Initializes a new TestSSLSocketFactory.
     * 
     * @throws  GeneralSecurityException for security errors
     */
    public TestSSLSocketFactory() throws GeneralSecurityException {
	this("TLS");
    }

    /**
     * Initializes a new TestSSLSocketFactory with a given protocol.
     * Normally the protocol will be specified as "TLS".
     * 
     * @param   protocol  The protocol to use
     * @throws  NoSuchAlgorithmException if given protocol is not supported
     * @throws  GeneralSecurityException for security errors
     */
    public TestSSLSocketFactory(String protocol)
				throws GeneralSecurityException {

	// Get the default SSLSocketFactory to delegate all API-calls to.
	defaultFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
    }

    /**
     * Was a socket created using one of the createSocket methods?
     */
    public boolean getSocketCreated() {
	return socketCreated;
    }

    /**
     * Was a socket wrapped using the createSocket method that takes a Socket?
     */
    public boolean getSocketWrapped() {
	return socketWrapped;
    }

    /**
     * Set the default cipher suites to be applied to future sockets.
     */
    public void setDefaultCipherSuites(String[] suites) {
	this.suites = suites;
    }

    /**
     * Configure the socket to be returned.
     */
    private Socket configure(Socket socket) {
	if (socket instanceof SSLSocket) {	// XXX - always true
	    SSLSocket s = (SSLSocket)socket;
	    if (suites != null)
		s.setEnabledCipherSuites(suites);
	}
	return socket;
    }


    // SocketFactory methods

    /* (non-Javadoc)
     * @see javax.net.ssl.SSLSocketFactory#createSocket(java.net.Socket,
     *						java.lang.String, int, boolean)
     */
    @Override
    public synchronized Socket createSocket(Socket socket, String s, int i,
				boolean flag) throws IOException {
	Socket wrappedSocket = defaultFactory.createSocket(socket, s, i, flag);
	socketWrapped = true;
	return configure(wrappedSocket);
    }

    /* (non-Javadoc)
     * @see javax.net.ssl.SSLSocketFactory#getDefaultCipherSuites()
     */
    @Override
    public synchronized String[] getDefaultCipherSuites() {
	if (suites != null)
	    return suites.clone();
	else
	    return defaultFactory.getDefaultCipherSuites();
    }

    /* (non-Javadoc)
     * @see javax.net.ssl.SSLSocketFactory#getSupportedCipherSuites()
     */
    @Override
    public synchronized String[] getSupportedCipherSuites() {
	return defaultFactory.getSupportedCipherSuites();
    }

    /* (non-Javadoc)
     * @see javax.net.SocketFactory#createSocket()
     */
    @Override
    public synchronized Socket createSocket() throws IOException {
	Socket socket = defaultFactory.createSocket();
	socketCreated = true;
	return configure(socket);
    }

    /* (non-Javadoc)
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
     *						java.net.InetAddress, int)
     */
    @Override
    public synchronized Socket createSocket(InetAddress inetaddress, int i,
			InetAddress inetaddress1, int j) throws IOException {
	Socket socket =
		defaultFactory.createSocket(inetaddress, i, inetaddress1, j);
	socketCreated = true;
	return configure(socket);
    }

    /* (non-Javadoc)
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
     */
    @Override
    public synchronized Socket createSocket(InetAddress inetaddress, int i)
				throws IOException {
	Socket socket = defaultFactory.createSocket(inetaddress, i);
	socketCreated = true;
	return configure(socket);
    }

    /* (non-Javadoc)
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
     *						java.net.InetAddress, int)
     */
    @Override
    public synchronized Socket createSocket(String s, int i,
				InetAddress inetaddress, int j)
				throws IOException, UnknownHostException {
	Socket socket = defaultFactory.createSocket(s, i, inetaddress, j);
	socketCreated = true;
	return configure(socket);
    }

    /* (non-Javadoc)
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
     */
    @Override
    public synchronized Socket createSocket(String s, int i)
				throws IOException, UnknownHostException {
	Socket socket = defaultFactory.createSocket(s, i);
	socketCreated = true;
	return configure(socket);
    }
}
