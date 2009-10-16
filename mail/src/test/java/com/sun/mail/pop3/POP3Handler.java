/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Handle connection.
 * 
 * @author sbo
 */
public class POP3Handler extends Thread implements Cloneable {
    
    /** Logger for this class. */
    private static final Logger LOGGER =
	Logger.getLogger(POP3Handler.class.getName());
    
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
            this.writer = new PrintWriter(this.clientSocket.getOutputStream());
            this.reader = new BufferedReader(
		new InputStreamReader(this.clientSocket.getInputStream()));
            
            this.sendGreetings();
            
            while (!this.quit) {
                this.handleCommand();
            }
            
            //this.clientSocket.close();
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error", e);
        } finally {
            try {
		if (this.clientSocket != null)
		    this.clientSocket.close();
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
        this.writer.println(str);
        this.writer.flush();
    }
    
    /**
     * Handle command.
     * 
     * @throws IOException
     *             unable to read/write to socket
     */
    public void handleCommand() throws IOException {
        this.currentLine = this.reader.readLine();
        
        if (this.currentLine == null) {
            LOGGER.severe("Current line is null!");
            this.exit();
            return;
        }
        
        final String commandName = new StringTokenizer(
			    this.currentLine, " ").nextToken().toUpperCase();
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
            this.retr();
        } else if (commandName.equals("DELE")) {
            this.dele();
        } else if (commandName.equals("NOOP")) {
            this.noop();
        } else if (commandName.equals("RSET")) {
            this.rset();
        } else if (commandName.equals("QUIT")) {
            this.quit();
        } else if (commandName.equals("TOP")) {
            this.top();
        } else if (commandName.equals("UIDL")) {
            this.uidl();
        } else if (commandName.equals("USER")) {
            this.user();
        } else if (commandName.equals("PASS")) {
            this.pass();
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
        this.println("+OK 2 18");
    }
    
    /**
     * LIST command.
     * 
     * @throws IOException
     *             unable to read/write to socket
     */
    public void list() throws IOException {
        this.writer.println("+OK");
        this.writer.println("1 7");
        this.writer.println("2 7");
        this.println(".");
    }
    
    /**
     * RETR command.
     * 
     * @throws IOException
     *             unable to read/write to socket
     */
    public void retr() throws IOException {
        // Nothing
    }
    
    /**
     * DELE command.
     * 
     * @throws IOException
     *             unable to read/write to socket
     */
    public void dele() throws IOException {
        // Nothing
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
     * 
     * @throws IOException
     *             unable to read/write to socket
     */
    public void top() throws IOException {
        // Nothing
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
    
    /**
     * Quit.
     */
    public void exit() {
        this.quit = true;
        try {
            if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                this.clientSocket.close();
		this.clientSocket = null;
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
