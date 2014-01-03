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

import java.util.*;
import java.security.Provider;
import java.security.Security;
import javax.security.sasl.*;
import javax.security.auth.callback.*;

/**
 * JavaMail SASL client factory for OAUTH2.
 *
 * @author Bill Shannon
 */
public class OAuth2SaslClientFactory implements SaslClientFactory {

    private static final String PROVIDER_NAME = "JavaMail-OAuth2";
    private static final String MECHANISM_NAME = "SaslClientFactory.XOAUTH2";

    static class OAuth2Provider extends Provider {
	private static final long serialVersionUID = -5371795551562287059L;

	public OAuth2Provider() {
	    super(PROVIDER_NAME, 1.0, "XOAUTH2 SASL Mechanism");
	    put(MECHANISM_NAME, OAuth2SaslClientFactory.class.getName());
	}
    }

    //@Override
    public SaslClient createSaslClient(String[] mechanisms,
				String authorizationId, String protocol,
				String serverName, Map<String,?> props,
				CallbackHandler cbh) throws SaslException {
	for (String m : mechanisms) {
	    if (m.equals("XOAUTH2"))
		return new OAuth2SaslClient(props, cbh);
	}
	return null;
    }

    //@Override
    public String[] getMechanismNames(Map<String,?> props) {
	return new String[] { "XOAUTH2" };
    }

    /**
     * Initialize this OAUTH2 provider, but only if there isn't one already.
     * If we're not allowed to add this provider, just give up silently.
     */
    public static void init() {
	try {
	    if (Security.getProvider(PROVIDER_NAME) == null)
		Security.addProvider(new OAuth2Provider());
	} catch (SecurityException ex) {
	    // oh well...
	}
    }
}
