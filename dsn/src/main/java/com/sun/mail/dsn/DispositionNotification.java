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

package com.sun.mail.dsn;

import java.io.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.util.LineOutputStream;	// XXX
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.MailLogger;

/**
 * A message/disposition-notification message content, as defined in
 * <A HREF="http://www.ietf.org/rfc/rfc3798.txt" TARGET="_top">RFC 3798</A>.
 *
 * @since	JavaMail 1.4.2
 */
public class DispositionNotification extends Report {

    private static MailLogger logger = new MailLogger(
	DeliveryStatus.class,
	"DEBUG DSN",
	PropUtil.getBooleanSystemProperty("mail.dsn.debug", false),
	System.out);

    /**
     * The disposition notification content fields.
     */
    protected InternetHeaders notifications;

    /**
     * Construct a disposition notification with no content.
     *
     * @exception	MessagingException for failures
     */
    public DispositionNotification() throws MessagingException {
	super("disposition-notification");
	notifications = new InternetHeaders();
    }

    /**
     * Construct a disposition notification by parsing the
     * supplied input stream.
     *
     * @param	is	the input stream
     * @exception	IOException for I/O errors reading the stream
     * @exception	MessagingException for other failures
     */
    public DispositionNotification(InputStream is)
				throws MessagingException, IOException {
	super("disposition-notification");
	notifications = new InternetHeaders(is);
	logger.fine("got MDN notification content");
    }

    /**
     * Return all the disposition notification fields in the
     * disposition notification.
     * The fields are defined as:
     *
     * <pre>
     *    disposition-notification-content =
     *		[ reporting-ua-field CRLF ]
     *		[ mdn-gateway-field CRLF ]
     *		[ original-recipient-field CRLF ]
     *		final-recipient-field CRLF
     *		[ original-message-id-field CRLF ]
     *		disposition-field CRLF
     *		*( failure-field CRLF )
     *		*( error-field CRLF )
     *		*( warning-field CRLF )
     *		*( extension-field CRLF )
     * </pre>
     *
     * @return	the DSN fields
     */
    // XXX - could parse each of these fields
    public InternetHeaders getNotifications() {
	return notifications;
    }

    /**
     * Set the disposition notification fields in the
     * disposition notification.
     *
     * @param	notifications	the DSN fields
     */
    public void setNotifications(InternetHeaders notifications) {
	this.notifications = notifications;
    }

    public void writeTo(OutputStream os) throws IOException {
	// see if we already have a LOS
	LineOutputStream los = null;
	if (os instanceof LineOutputStream) {
	    los = (LineOutputStream) os;
	} else {
	    los = new LineOutputStream(os);
	}

	writeInternetHeaders(notifications, los);
	los.writeln();
    }

    private static void writeInternetHeaders(InternetHeaders h,
				LineOutputStream los) throws IOException {
	Enumeration e = h.getAllHeaderLines();
	while (e.hasMoreElements())
	    los.writeln((String)e.nextElement());
    }

    public String toString() {
	return "DispositionNotification: Reporting-UA=" +
	    notifications.getHeader("Reporting-UA", null);
    }
}
