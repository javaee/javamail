/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.handlers;

import java.io.*;
import java.util.Properties;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;


/**
 * @author	Christopher Cotton
 */


public class message_rfc822 extends handler_base {

    private static ActivationDataFlavor[] ourDataFlavor = {
	new ActivationDataFlavor(Message.class, "message/rfc822", "Message")
    };

    @Override
    protected ActivationDataFlavor[] getDataFlavors() {
	return ourDataFlavor;
    }

    /**
     * Return the content.
     */
    @Override
    public Object getContent(DataSource ds) throws IOException {
	// create a new MimeMessage
	try {
	    Session session;
	    if (ds instanceof MessageAware) {
		MessageContext mc = ((MessageAware)ds).getMessageContext();
		session = mc.getSession();
	    } else {
		// Hopefully a rare case.  Also hopefully the application
		// has created a default Session that can just be returned
		// here.  If not, the one we create here is better than
		// nothing, but overall not a really good answer.
		session = Session.getDefaultInstance(new Properties(), null);
	    }
	    return new MimeMessage(session, ds.getInputStream());
	} catch (MessagingException me) {
	    IOException ioex =
		new IOException("Exception creating MimeMessage in " +
		    "message/rfc822 DataContentHandler");
	    ioex.initCause(me);
	    throw ioex;
	}
    }
    
    /**
     * Write the object as a byte stream.
     */
    @Override
    public void writeTo(Object obj, String mimeType, OutputStream os) 
			throws IOException {
	// if the object is a message, we know how to write that out
	if (obj instanceof Message) {
	    Message m = (Message)obj;
	    try {
		m.writeTo(os);
	    } catch (MessagingException me) {
		IOException ioex = new IOException("Exception writing message");
		ioex.initCause(me);
		throw ioex;
	    }
	} else {
	    throw new IOException("unsupported object");
	}
    }
}
