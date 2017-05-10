/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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
import java.net.Socket;
import java.net.InetAddress;
import javax.net.SocketFactory;

/**
 * A SocketFactory that saves the Socket it creates so that it can be
 * accessed later.  Useful for checking that sockets are closed properly.
 */
public class SavedSocketFactory extends SocketFactory {
    private SocketFactory factory;
    private Socket saved;

    public SavedSocketFactory() {
	super();
	try {
	    factory = SocketFactory.getDefault();
	} catch(Exception ex) {
	    // ignore
	}
    }

    @Override
    public Socket createSocket() throws IOException {
	return save(factory.createSocket());
    }

    @Override
    public Socket createSocket(InetAddress host, int port)
				throws IOException {
	return save(factory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port,
				InetAddress localAddress, int localPort)
				throws IOException {
	return save(factory.createSocket(
				    address, port, localAddress, localPort));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
	return save(factory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port,
				InetAddress localHost, int localPort)
				throws IOException {
	return save(factory.createSocket(host, port, localHost, localPort));
    }

    public Socket getSocket() {
	return saved;
    }

    private Socket save(Socket s) {
	saved = s;
	return saved;
    }
}
