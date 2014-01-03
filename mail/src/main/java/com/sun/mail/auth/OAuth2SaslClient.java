/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.security.Provider;
import java.security.Security;
import javax.security.sasl.*;
import javax.security.auth.callback.*;

import com.sun.mail.util.ASCIIUtility;

/**
 * JavaMail SASL client for OAUTH2.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6749">
 *	RFC 6749 - OAuth 2.0 Authorization Framework</a>
 * @see <a href="http://tools.ietf.org/html/rfc6750">
 *	RFC 6750 - OAuth 2.0 Authorization Framework: Bearer Token Usage</a>
 * @author Bill Shannon
 */
public class OAuth2SaslClient implements SaslClient {
    private CallbackHandler cbh;
    private Map<String,?> props;	// XXX - not currently used
    private boolean complete = false;

    public OAuth2SaslClient(Map<String,?> props, CallbackHandler cbh) {
	this.props = props;
	this.cbh = cbh;
    }

    //@Override
    public String getMechanismName() {
	return "XOAUTH2";
    }

    //@Override
    public boolean hasInitialResponse() {
	return true;
    }

    //@Override
    public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
	if (complete)
	    return new byte[0];

	NameCallback ncb = new NameCallback("User name:");
	PasswordCallback pcb = new PasswordCallback("OAuth token:", false);
	try {
	    cbh.handle(new Callback[] { ncb, pcb });
	} catch (UnsupportedCallbackException ex) {
	    throw new SaslException("Unsupported callback", ex);
	} catch (IOException ex) {
	    throw new SaslException("Callback handler failed", ex);
	}

	/*
	 * The OAuth token isn't really a password, and JavaMail doesn't
	 * use char[] for passwords, so we don't worry about storing the
	 * token in strings.
	 */
	String user = ncb.getName();
	String token = new String(pcb.getPassword());
	pcb.clearPassword();
	String resp = "user=" + user + "\001auth=Bearer " + token + "\001\001";
	byte[] response;
	try {
	    response = resp.getBytes("utf-8");
	} catch (UnsupportedEncodingException ex) {
	    // fall back to ASCII
	    response = ASCIIUtility.getBytes(resp);
	}
	complete = true;
	return response;
    }

    //@Override
    public boolean isComplete() {
	return complete;
    }

    //@Override
    public byte[] unwrap(byte[] incoming, int offset, int len)
				throws SaslException {
	throw new IllegalStateException("OAUTH2 unwrap not supported");
    }

    //@Override
    public byte[] wrap(byte[] outgoing, int offset, int len)
				throws SaslException {
	throw new IllegalStateException("OAUTH2 wrap not supported");
    }

    //@Override
    public Object getNegotiatedProperty(String propName) {
	if (!complete)
	    throw new IllegalStateException("OAUTH2 getNegotiatedProperty");
	return null;
    }

    //@Override
    public void dispose() throws SaslException {
    }
}
