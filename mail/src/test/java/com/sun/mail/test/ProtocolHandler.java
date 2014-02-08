/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Handle protocol connection.
 *
 * Inspired by, and derived from, POP3Handler by sbo.
 *
 * @author sbo
 * @author Bill Shannon
 */
public abstract class ProtocolHandler implements Runnable, Cloneable {

    /** Logger for this class. */
    protected final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    /** Client socket. */
    protected Socket clientSocket;

    /** Quit? */
    protected boolean quit;

    /** Writer to socket. */
    protected PrintWriter writer;

    /** Reader from socket. */
    protected BufferedReader reader;

    /**
     * Sets the client socket.
     *
     * @param clientSocket	the client socket
     */
    public final void setClientSocket(final Socket clientSocket)
				throws IOException {
        this.clientSocket = clientSocket;
	writer = new PrintWriter(clientSocket.getOutputStream());
	reader = new BufferedReader(
	    new InputStreamReader(clientSocket.getInputStream()));
    }

    /**
     * Optionally send a greeting when first connected.
     */
    public void sendGreetings() throws IOException {
    }

    /**
     * Read and process a single command.
     */
    public abstract void handleCommand() throws IOException;

    /**
     * {@inheritDoc}
     */
    public final void run() {
        try {

            sendGreetings();

            while (!quit) {
                handleCommand();
            }

            //clientSocket.close();
	} catch (SocketException sex) {
	    // ignore it, often get "connection reset" when client closes
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error", e);
        } finally {
            try {
		if (clientSocket != null)
		    clientSocket.close();
            } catch (final IOException ioe) {
                LOGGER.log(Level.SEVERE, "Error", ioe);
            }
        }
    }

    /**
     * Quit.
     */
    public void exit() {
        quit = true;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
		clientSocket = null;
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
