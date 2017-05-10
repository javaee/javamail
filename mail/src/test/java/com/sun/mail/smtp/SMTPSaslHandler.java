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
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.security.sasl.*;
import javax.security.auth.callback.*;

import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.ASCIIUtility;

/**
 * Handle connection with SASL authentication.
 *
 * @author Bill Shannon
 */
public class SMTPSaslHandler extends SMTPHandler {

    /**
     * EHLO command.
     *
     * @throws IOException
     *             unable to read/write to socket
     */
    @Override
    public void ehlo() throws IOException {
        println("250-hello");
        println("250 AUTH DIGEST-MD5");
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

	final String u = "test";
	final String p = "test";
	final String realm = "test";

	CallbackHandler cbh = new CallbackHandler() {
	    @Override
	    public void handle(Callback[] callbacks) {
		if (LOGGER.isLoggable(Level.FINE))
		    LOGGER.fine("SASL callback length: " + callbacks.length);
		for (int i = 0; i < callbacks.length; i++) {
		    if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("SASL callback " + i + ": " + callbacks[i]);
		    if (callbacks[i] instanceof NameCallback) {
			NameCallback ncb = (NameCallback)callbacks[i];
			ncb.setName(u);
		    } else if (callbacks[i] instanceof PasswordCallback) {
			PasswordCallback pcb = (PasswordCallback)callbacks[i];
			pcb.setPassword(p.toCharArray());
		    } else if (callbacks[i] instanceof AuthorizeCallback) {
			AuthorizeCallback ac = (AuthorizeCallback)callbacks[i];
			if (LOGGER.isLoggable(Level.FINE))
			    LOGGER.fine("SASL authorize: " +
				"authn: " + ac.getAuthenticationID() + ", " +
				"authz: " + ac.getAuthorizationID() + ", " +
				"authorized: " + ac.getAuthorizedID());
			ac.setAuthorized(true);
		    } else if (callbacks[i] instanceof RealmCallback) {
			RealmCallback rcb = (RealmCallback)callbacks[i];
			rcb.setText(realm != null ?
				    realm : rcb.getDefaultText());
		    } else if (callbacks[i] instanceof RealmChoiceCallback) {
			RealmChoiceCallback rcb =
			    (RealmChoiceCallback)callbacks[i];
			if (realm == null)
			    rcb.setSelectedIndex(rcb.getDefaultChoice());
			else {
			    // need to find specified realm in list
			    String[] choices = rcb.getChoices();
			    for (int k = 0; k < choices.length; k++) {
				if (choices[k].equals(realm)) {
				    rcb.setSelectedIndex(k);
				    break;
				}
			    }
			}
		    }
		}
	    }
	};

	SaslServer ss;
	try {
	    ss = Sasl.createSaslServer(mech, "smtp", "localhost", null, cbh);
	} catch (SaslException sex) {
	    LOGGER.log(Level.FINE, "Failed to create SASL server", sex);
	    println("501 Failed to create SASL server");
	    return;
	}
	if (ss == null) {
	    LOGGER.fine("No SASL support");
	    println("501 No SASL support");
	    return;
	}
	if (LOGGER.isLoggable(Level.FINE))
	    LOGGER.fine("SASL server " + ss.getMechanismName());

	byte[] response = ir.getBytes();
	while (!ss.isComplete()) {
	    try {
		byte[] chal = ss.evaluateResponse(response);
		// send challenge
		if (LOGGER.isLoggable(Level.FINE))
		    LOGGER.fine("SASL challenge: " +
			ASCIIUtility.toString(chal, 0, chal.length));
		byte[] ba = BASE64EncoderStream.encode(chal);
		if (ba.length > 0)
		    println("334 " +
			    ASCIIUtility.toString(ba, 0, ba.length));
		else
		    println("334");
		// read response
		String resp = readLine();
		if (!isBase64(resp)) {
		    println("501 response not base64");
		break;
		}
		response = resp.getBytes();
		response = BASE64DecoderStream.decode(response);
	    } catch (SaslException ex) {
		println("501 " + ex.toString());
		break;
	    }
	}

	if (ss.isComplete() /*&& status == SUCCESS*/) {
	    String qop = (String)ss.getNegotiatedProperty(Sasl.QOP);
	    if (qop != null && (qop.equalsIgnoreCase("auth-int") ||
				qop.equalsIgnoreCase("auth-conf"))) {
		// XXX - NOT SUPPORTED!!!
		LOGGER.fine(
			"SASL Mechanism requires integrity or confidentiality");
		println("501 " +
			"SASL Mechanism requires integrity or confidentiality");
		return;
	    }
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
