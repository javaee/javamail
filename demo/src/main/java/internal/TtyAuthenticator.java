/*
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.*;
import java.net.*;
import javax.mail.*;
import javax.mail.PasswordAuthentication;
import javax.mail.Authenticator;

/**
 * A simple Authenticator that prompts for the user name and password on stdin.
 * Puts up a dialog something like:
 * <p> <pre>
 * Connecting to &lt;protocol&gt; mail service on host &lt;addr&gt;, port &lt;port&gt;.
 * &lt;prompt&gt;
 *
 * User Name: [defaultUserName]
 * Password:
 * </pre> <p>
 *
 * @author Bill Shannon
 */

public class TtyAuthenticator extends Authenticator {

    /**
     * @return The PasswordAuthentication collected from the
     *		user, or null if none is provided.
     */
    protected PasswordAuthentication getPasswordAuthentication() {
	BufferedReader in = new BufferedReader(
				new InputStreamReader((System.in)));
	StringBuffer sb = new StringBuffer();
	sb.append("Connecting to ");
	sb.append(getRequestingProtocol());
	sb.append(" mail service on host ");
	sb.append(getRequestingSite().getHostName());
	int port = getRequestingPort();
	if (port > 0) {
	    sb.append(", port ");
	    sb.append(port);
	}
	sb.append(".");
	System.out.println(sb.toString());
	String prompt = getRequestingPrompt();
	if (prompt != null)
	    System.out.println(prompt);
	System.out.println();
	String userName = get(in, "User Name", getDefaultUserName());
	String password = getpw("Password");
	if (userName == null)
	    return null;
	else
	    return new PasswordAuthentication(userName, password);
    }

    private static final String get(BufferedReader in,
				String name, String value) {
	PrintStream p = System.out;

	p.print(name + ": ");
	if (value != null)
	    p.print("[" + value + "] ");
	p.flush();

	try {
	    String s = in.readLine();
	    if (s.length() == 0)
		return value;
	    else
		return s;
	} catch (IOException e) {
	    return value;
	}
    }

    private static final String getpw(String name) {
	Console cons;
	char[] passwd;
	if ((cons = System.console()) != null &&
	    (passwd = cons.readPassword("[%s] ", name)) != null)
	    return new String(passwd);
	return "";
    }

    // main program, for debugging.
    // Usage: java TtyAuthenticator host port protocol prompt defaultUser
    public static void main(String argv[]) throws Exception {
	Session sess = Session.getInstance(System.getProperties(),
					new TtyAuthenticator());
	PasswordAuthentication pw = sess.requestPasswordAuthentication(
		InetAddress.getByName(argv[0]),
		Integer.parseInt(argv[1]), argv[2], z(argv[3]), z(argv[4]));
	System.out.println("User: " + n(pw.getUserName()));
	System.out.println("Password: " + n(pw.getPassword()));
    }

    private static final String n(String s) {
	return s == null ? "<null>" : s;
    }

    private static final String z(String s) {
	return s.length() > 0 ? s : null;
    }
}
