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

package com.sun.mail.test;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.PushbackInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.net.ssl.SSLException;

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

    /** Input from socket. */
    protected InputStream in;

    /**
     * Sets the client socket.
     *
     * @param clientSocket	the client socket
     */
    public final void setClientSocket(final Socket clientSocket)
				throws IOException {
        this.clientSocket = clientSocket;
	writer = new PrintWriter(new OutputStreamWriter(
		    clientSocket.getOutputStream(), StandardCharsets.UTF_8));
	in = new BufferedInputStream(clientSocket.getInputStream());
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
     * Read a single line terminated by newline or CRLF.
     * Convert the UTF-8 bytes in the line (minus the line terminator)
     * to a String.
     */
    protected String readLine() throws IOException {
        byte[] buf = new byte[128];

        int room = buf.length;
        int offset = 0;
        int c;

	while ((c = in.read()) != -1) {
	    if (c == '\n') {
		break;
	    } else if (c == '\r') {
		int c2 = in.read();
		if ((c2 != '\n') && (c2 != -1)) {
		    if (!(in instanceof PushbackInputStream))
			this.in = new PushbackInputStream(in);
		    ((PushbackInputStream)in).unread(c2);
		}
		break;
	    } else {
		if (--room < 0) {
		    byte[] nbuf = new byte[offset + 128];
		    room = nbuf.length - offset - 1;
		    System.arraycopy(buf, 0, nbuf, 0, offset);
		    buf = nbuf;
		}
		buf[offset++] = (byte)c;
	    }
	}
	if ((c == -1) && (offset == 0))
	    return null;
	return new String(buf, 0, offset, StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run() {
        try {

            sendGreetings();

            while (!quit) {
                handleCommand();
            }

            //clientSocket.close();
	} catch (SocketException sex) {
	    // ignore it, often get "connection reset" when client closes
	} catch (SSLException sex) {
	    // ignore it, often occurs when testing SSL
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
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
