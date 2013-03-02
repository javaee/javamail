/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Handle connection.
 *
 * Inspired by, and derived from, POP3Handler by sbo.
 *
 * @author Bill Shannon
 * @author sbo
 */
public class SMTPHandler implements Runnable, Cloneable {

    /** Logger for this class. */
    protected static final Logger LOGGER =
	Logger.getLogger(SMTPHandler.class.getName());

    /** Client socket. */
    private Socket clientSocket;

    /** Quit? */
    private boolean quit;

    /** Writer to socket. */
    private PrintWriter writer;

    /** Reader from socket. */
    private BufferedReader reader;

    /** Current line. */
    private String currentLine;

    /**
     * Sets the client socket.
     *
     * @param clientSocket
     *            client socket
     */
    public final void setClientSocket(final Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * {@inheritDoc}
     */
    public final void run() {
        try {
            writer = new PrintWriter(clientSocket.getOutputStream());
            reader = new BufferedReader(
		new InputStreamReader(clientSocket.getInputStream()));

            sendGreetings();

            while (!quit) {
                handleCommand();
            }

            //clientSocket.close();
        } catch (final SocketException sex) {
            // ignore it, often get "connection reset" when client closes
        } catch (final Exception e) {
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
     * Send greetings.
     *
     * @throws IOException
     *             unable to write to socket
     */
    public void sendGreetings() throws IOException {
        println("220 localhost dummy server ready");
    }

    /**
     * Send String to socket.
     *
     * @param str
     *            String to send
     * @throws IOException
     *             unable to write to socket
     */
    public void println(final String str) throws IOException {
        writer.print(str);
	writer.print("\r\n");
        writer.flush();
    }

    /**
     * Handle command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void handleCommand() throws IOException {
        currentLine = reader.readLine();

        if (currentLine == null) {
            LOGGER.severe("Current line is null!");
            exit();
            return;
        }

        final StringTokenizer st = new StringTokenizer(currentLine, " ");
        final String commandName = st.nextToken().toUpperCase();
        final String arg = st.hasMoreTokens() ? st.nextToken() : null;
        if (commandName == null) {
            LOGGER.severe("Command name is empty!");
            exit();
            return;
        }

        if (commandName.equals("HELO")) {
            helo();
        } else if (commandName.equals("EHLO")) {
            ehlo();
        } else if (commandName.equals("MAIL")) {
            mail(arg);
        } else if (commandName.equals("RCPT")) {
            rcpt();
        } else if (commandName.equals("DATA")) {
            data();
        } else if (commandName.equals("NOOP")) {
            noop();
        } else if (commandName.equals("RSET")) {
            rset();
        } else if (commandName.equals("QUIT")) {
            quit();
        } else if (commandName.equals("AUTH")) {
            auth(currentLine);
        } else {
            LOGGER.log(Level.SEVERE, "ERROR command unknown: {0}", commandName);
            println("-ERR unknown command");
        }
    }

    protected String readLine() throws IOException {
        currentLine = reader.readLine();

        if (currentLine == null) {
            LOGGER.severe("Current line is null!");
            exit();
        }
	return currentLine;
    }

    /**
     * HELO command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void helo() throws IOException {
        println("220 Ok");
    }

    /**
     * EHLO command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void ehlo() throws IOException {
        println("250-hello");
        println("250 AUTH PLAIN");	// PLAIN is simplest to fake
    }

    /**
     * MAIL command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void mail(String arg) throws IOException {
	ok();
    }

    /**
     * RCPT command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void rcpt() throws IOException {
	ok();
    }

    /**
     * DATA command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void data() throws IOException {
	String line;
	while ((line = reader.readLine()) != null) {
	    if (line.equals("."))
		break;
	}
	ok();
    }

    /**
     * NOOP command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void noop() throws IOException {
        ok();
    }

    /**
     * RSET command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void rset() throws IOException {
        ok();
    }

    /**
     * QUIT command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void quit() throws IOException {
        println("221 BYE");
        exit();
    }

    /**
     * AUTH command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void auth(String line) throws IOException {
        println("235 Authorized");
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

    private void ok() throws IOException {
	println("250 OK");
    }
}
