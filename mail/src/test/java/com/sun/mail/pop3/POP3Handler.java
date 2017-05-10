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
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.mail.test.ProtocolHandler;

/**
 * Handle connection.
 *
 * @author sbo
 */
public class POP3Handler extends ProtocolHandler {

    /** Current line. */
    private String currentLine;

    /** First test message. */
    private String top1 =
	    "Mime-Version: 1.0\r\n" +
	    "From: joe@example.com\r\n" +
	    "To: bob@example.com\r\n" +
	    "Subject: Example\r\n" +
	    "Content-Type: text/plain\r\n" +
	    "\r\n";
    private String msg1 = top1 +
	    "plain text\r\n";

    /** Second test message. */
    private String top2 =
	    "Mime-Version: 1.0\r\n" +
	    "From: joe@example.com\r\n" +
	    "To: bob@example.com\r\n" +
	    "Subject: Multipart Example\r\n" +
	    "Content-Type: multipart/mixed; boundary=\"xxx\"\r\n" +
	    "\r\n";
    private String msg2 = top2 +
	    "preamble\r\n" +
	    "--xxx\r\n" +
	    "\r\n" +
	    "first part\r\n" +
	    "\r\n" +
	    "--xxx\r\n" +
	    "\r\n" +
	    "second part\r\n" +
	    "\r\n" +
	    "--xxx--\r\n";

    /**
     * Send greetings.
     *
     * @throws IOException
     *             unable to write to socket
     */
    @Override
    public void sendGreetings() throws IOException {
        this.println("+OK POP3 CUSTOM");
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
        this.writer.print(str);
	this.writer.print("\r\n");
        this.writer.flush();
    }

    /**
     * Handle command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    @Override
    public void handleCommand() throws IOException {
        this.currentLine = readLine();

        if (this.currentLine == null) {
	    // probably just EOF because the socket was closed
            //LOGGER.severe("Current line is null!");
            this.exit();
            return;
        }

        final StringTokenizer st = new StringTokenizer(this.currentLine, " ");
        final String commandName = st.nextToken().toUpperCase();
        final String arg = st.hasMoreTokens() ? st.nextToken() : null;
        if (commandName == null) {
            LOGGER.severe("Command name is empty!");
            this.exit();
            return;
        }

        if (commandName.equals("STAT")) {
            this.stat();
        } else if (commandName.equals("LIST")) {
            this.list();
        } else if (commandName.equals("RETR")) {
            this.retr(arg);
        } else if (commandName.equals("DELE")) {
            this.dele();
        } else if (commandName.equals("NOOP")) {
            this.noop();
        } else if (commandName.equals("RSET")) {
            this.rset();
        } else if (commandName.equals("QUIT")) {
            this.quit();
        } else if (commandName.equals("TOP")) {
            this.top(arg);
        } else if (commandName.equals("UIDL")) {
            this.uidl();
        } else if (commandName.equals("USER")) {
            this.user();
        } else if (commandName.equals("PASS")) {
            this.pass();
        } else if (commandName.equals("CAPA")) {
            this.println("-ERR CAPA not supported");
        } else {
            LOGGER.log(Level.SEVERE, "ERROR command unknown: {0}", commandName);
            this.println("-ERR unknown command");
        }
    }

    /**
     * STAT command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void stat() throws IOException {
        this.println("+OK 2 " + (msg1.length() + msg2.length()));
    }

    /**
     * LIST command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void list() throws IOException {
        this.writer.println("+OK");
        this.writer.println("1 " + msg1.length());
        this.writer.println("2 " + msg2.length());
        this.println(".");
    }

    /**
     * RETR command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void retr(String arg) throws IOException {
	String msg;
	if (arg.equals("1"))
	    msg = msg1;
	else
	    msg = msg2;
        this.println("+OK " + msg.length() + " octets");
	this.writer.write(msg);
	this.println(".");
    }

    /**
     * DELE command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void dele() throws IOException {
	this.println("-ERR DELE not supported");
    }

    /**
     * NOOP command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void noop() throws IOException {
        this.println("+OK");
    }

    /**
     * RSET command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void rset() throws IOException {
        this.println("+OK");
    }

    /**
     * QUIT command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void quit() throws IOException {
        this.println("+OK");
        this.exit();
    }

    /**
     * TOP command.
     * XXX - ignores number of lines argument
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void top(String arg) throws IOException {
	String top;
	if (arg.equals("1"))
	    top = top1;
	else
	    top = top2;
        this.println("+OK " + top.length() + " octets");
	this.writer.write(top);
	this.println(".");
    }

    /**
     * UIDL command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void uidl() throws IOException {
        this.writer.println("+OK");
        this.writer.println("1 1");
        this.writer.println("2 2");
        this.println(".");
    }

    /**
     * USER command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void user() throws IOException {
        this.println("+OK");
    }

    /**
     * PASS command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    public void pass() throws IOException {
        this.println("+OK");
    }
}
