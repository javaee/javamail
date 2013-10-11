/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.imap.protocol;

import java.util.*;
import com.sun.mail.iap.*;

/**
 * This class represents the response to the ID command. <p>
 *
 * See <A HREF="http://www.ietf.org/rfc/rfc2971.txt">RFC 2971</A>.
 *
 * @since JavaMail 1.5.1
 * @author Bill Shannon
 */

public class ID {

    private Map<String, String> serverParams = null;

    /**
     * Parse the server parameter list out of the response.
     */
    public ID(Response r) throws ProtocolException {
	// id_response ::= "ID" SPACE id_params_list
	// id_params_list ::= "(" #(string SPACE nstring) ")" / nil
	//       ;; list of field value pairs

	r.skipSpaces();
	int c = r.peekByte();
	if (c == 'N' || c == 'n')	// assume NIL
	    return;

	if (c != '(')
	    throw new ProtocolException("Missing '(' at start of ID");

	serverParams = new HashMap<String, String>();

	String[] v = r.readStringList();
	if (v != null) {
	    for (int i = 0; i < v.length; i += 2) {
		String name = v[i];
		if (name == null)
		    throw new ProtocolException("ID field name null");
		if (i + 1 >= v.length)
		    throw new ProtocolException("ID field without value: " +
									name);
		String value = v[i + 1];
		serverParams.put(name, value);
	    }
	}
	serverParams = Collections.unmodifiableMap(serverParams);
    }

    /**
     * Return the parsed server params.
     */
    Map<String, String> getServerParams() {
	return serverParams;
    }

    /**
     * Convert the client parameters into an argument list for the ID command.
     */
    static Argument getArgumentList(Map<String,String> clientParams) {
	Argument arg = new Argument();
	if (clientParams == null) {
	    arg.writeString("NIL");
	    return arg;
	}
	Argument list = new Argument();
	// add params to list
	for (Map.Entry<String, String> e : clientParams.entrySet()) {
	    list.writeNString(e.getKey());
	    list.writeNString(e.getValue());
	}
	arg.writeArgument(list);
	return arg;
    }
}
