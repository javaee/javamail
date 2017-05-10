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

package com.sun.mail.smtp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.ASCIIUtility;

/**
 * Handle connection with LOGIN or PLAIN authentication.
 *
 * @author Bill Shannon
 */
public class SMTPLoginHandler extends SMTPHandler {
    protected String username = "test";
    protected String password = "test";

    /**
     * EHLO command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    @Override
    public void ehlo() throws IOException {
        println("250-hello");
        println("250-SMTPUTF8");
        println("250-8BITMIME");
        println("250 AUTH PLAIN LOGIN");
    }

    /**
     * AUTH command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    @Override
    public void auth(String line) throws IOException {
        StringTokenizer ct = new StringTokenizer(line, " ");
        String commandName = ct.nextToken().toUpperCase();
	String mech = ct.nextToken().toUpperCase();
	String ir = "";
	if (ct.hasMoreTokens())
	    ir = ct.nextToken();

	if (LOGGER.isLoggable(Level.FINE))
	    LOGGER.fine(line);
	if (mech.equalsIgnoreCase("PLAIN"))
	    plain(ir);
	else if (mech.equalsIgnoreCase("LOGIN"))
	    login(ir);
	else
	    println("501 bad AUTH mechanism");
    }

    /**
     * AUTH LOGIN
     */
    private void login(String ir) throws IOException {
	println("334");
	// read user name
	String resp = readLine();
	if (!isBase64(resp)) {
	    println("501 response not base64");
	    return;
	}
	byte[] response = resp.getBytes(StandardCharsets.US_ASCII);
	response = BASE64DecoderStream.decode(response);
	String u = new String(response, StandardCharsets.UTF_8);
	if (LOGGER.isLoggable(Level.FINE))
	    LOGGER.fine("USER: " + u);
	println("334");

	// read password
	resp = readLine();
	if (!isBase64(resp)) {
	    println("501 response not base64");
	    return;
	}
	response = resp.getBytes(StandardCharsets.US_ASCII);
	response = BASE64DecoderStream.decode(response);
	String p = new String(response, StandardCharsets.UTF_8);
	if (LOGGER.isLoggable(Level.FINE))
	    LOGGER.fine("PASSWORD: " + p);

	//System.out.printf("USER: %s, PASSWORD: %s%n", u, p);
	if (!u.equals(username) || !p.equals(password)) {
	    println("535 authentication failed");
	    return;
	}

	println("235 Authenticated");
    }

    /**
     * AUTH PLAIN
     */
    private void plain(String ir) throws IOException {
	String auth = new String(BASE64DecoderStream.decode(
				    ir.getBytes(StandardCharsets.US_ASCII)),
				StandardCharsets.UTF_8);
	String[] ap = auth.split("\000");
	String u = ap[1];
	String p = ap[2];
	//System.out.printf("USER: %s, PASSWORD: %s%n", u, p);
	if (!u.equals(username) || !p.equals(password)) {
	    println("535 authentication failed");
	    return;
	}
	println("235 Authenticated");
    }

    /**
     * Is every character in the string a base64 character?
     */
    private boolean isBase64(String s) {
	int len = s.length();
	if (s.endsWith("=="))
	    len -= 2;
	else if (s.endsWith("="))
	    len--;
	for (int i = 0; i < len; i++) {
	    char c = s.charAt(i);
	    if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
		    (c >= '0' && c <= '9') || c == '+' || c == '/'))
		return false;
	}
	return true;
    }
}
