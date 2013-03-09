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

package com.sun.mail.imap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Handle IMAP connection.
 *
 * Inspired by, and derived from, POP3Handler by sbo.
 *
 * @author sbo
 * @author Bill Shannon
 */
public class IMAPHandler implements Runnable, Cloneable {

    /** Logger for this class. */
    protected static final Logger LOGGER =
	Logger.getLogger(IMAPHandler.class.getName());

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

    /** Tag for current command */
    protected String tag;

    /** IMAP capabilities supported */
    protected String capabilities = "IMAP4REV1 IDLE";

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
     * @throws IOException unable to write to socket
     */
    public void sendGreetings() throws IOException {
        untagged("OK [CAPABILITY " + capabilities + "] IMAPHandler");
    }

    /**
     * Send String to socket.
     *
     * @param str String to send
     * @throws IOException unable to write to socket
     */
    public void println(final String str) throws IOException {
        writer.print(str);
	writer.print("\r\n");
        writer.flush();
    }

    /**
     * Send a tagged response.
     *
     * @param resp the response to send
     * @throws IOException unable to read/write to socket
     */
    public void tagged(final String resp) throws IOException {
	println(tag + " " + resp);
    }

    /**
     * Send an untagged response.
     *
     * @param resp the response to send
     * @throws IOException unable to read/write to socket
     */
    public void untagged(final String resp) throws IOException {
	println("* " + resp);
    }

    /**
     * Send a tagged OK response.
     *
     * @throws IOException unable to read/write to socket
     */
    public void ok() throws IOException {
	tagged("OK");
    }

    /**
     * Send a tagged OK response with a message.
     *
     * @param msg the message to send
     * @throws IOException unable to read/write to socket
     */
    public void ok(final String msg) throws IOException {
	tagged("OK " + (msg != null ? msg : ""));
    }

    /**
     * Send a tagged NO response with a message.
     *
     * @param msg the message to send
     * @throws IOException unable to read/write to socket
     */
    public void no(final String msg) throws IOException {
	tagged("NO " + (msg != null ? msg : ""));
    }

    /**
     * Send a tagged BAD response with a message.
     *
     * @param msg the message to send
     * @throws IOException unable to read/write to socket
     */
    public void bad(final String msg) throws IOException {
	tagged("BAD " + (msg != null ? msg : ""));
    }

    /**
     * Send an untagged BYE response with a message, then exit.
     *
     * @param msg the message to send
     * @throws IOException unable to read/write to socket
     */
    public void bye(final String msg) throws IOException {
	untagged("BYE " + (msg != null ? msg : ""));
	exit();
    }

    /**
     * Send a "continue" command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void cont() throws IOException {
	println("+ please continue");
    }

    /**
     * Send a "continue" command with a message.
     *
     * @throws IOException unable to read/write to socket
     */
    public void cont(String msg) throws IOException {
	println("+ " + (msg != null ? msg : ""));
    }

    /**
     * Handle command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void handleCommand() throws IOException {
        currentLine = reader.readLine();

        if (currentLine == null) {
	    // probably just EOF because the socket was closed
            //LOGGER.severe("Current line is null!");
            exit();
            return;
        }

        StringTokenizer ct = new StringTokenizer(currentLine, " ");
	tag = ct.nextToken();
        final String commandName = ct.nextToken().toUpperCase();
        if (commandName == null) {
            LOGGER.severe("Command name is empty!");
            exit();
            return;
        }

        if (commandName.equals("LOGIN")) {
            login();
        } else if (commandName.equals("AUTHENTICATE")) {
            authenticate(currentLine);
        } else if (commandName.equals("NOOP")) {
            noop();
        } else if (commandName.equals("SELECT")) {
            select();
        } else if (commandName.equals("EXAMINE")) {
            examine();
        } else if (commandName.equals("IDLE")) {
            idle();
        } else if (commandName.equals("FETCH")) {
            ok();	// XXX
        } else if (commandName.equals("CLOSE")) {
            close();
        } else if (commandName.equals("LOGOUT")) {
            logout();
        } else {
            LOGGER.log(Level.SEVERE, "ERROR command unknown: {0}", commandName);
            bad("unknown command");
        }
    }

    /**
     * LOGIN command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void login() throws IOException {
        ok("[CAPABILITY " + capabilities + "]");
    }

    /**
     * AUTHENTICATE command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void authenticate(String line) throws IOException {
        bad("AUTHENTICATE not supported");
    }

    /**
     * SELECT command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void select() throws IOException {
	untagged("0 EXISTS");
	untagged("0 RECENT");
        ok();
    }

    /**
     * EXAMINE command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void examine() throws IOException {
	untagged("0 EXISTS");
	untagged("0 RECENT");
        ok();
    }

    /**
     * IDLE command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void idle() throws IOException {
        cont();
	idleWait();
	ok();
    }

    protected String readLine() throws IOException {
        currentLine = reader.readLine();
        if (currentLine == null) {
            LOGGER.severe("Current line is null!");
            exit();
        }
	return currentLine;
    }

    protected void idleWait() throws IOException {
        String line = readLine();

        if (line != null && !line.equalsIgnoreCase("DONE")) {
            LOGGER.severe("Didn't get DONE response to IDLE");
            exit();
            return;
        }
    }

    /**
     * CLOSE command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void close() throws IOException {
        ok();
    }

    /**
     * NOOP command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void noop() throws IOException {
        ok();
    }

    /**
     * LOGOUT command.
     *
     * @throws IOException unable to read/write to socket
     */
    public void logout() throws IOException {
        ok();
        exit();
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
