/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.logging.Level;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.util.LineOutputStream;	// XXX
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.MailLogger;

/**
 * A message/delivery-status message content, as defined in
 * <A HREF="http://www.ietf.org/rfc/rfc3464.txt" TARGET="_top">RFC 3464</A>.
 *
 * @since	JavaMail 1.4
 */
public class DeliveryStatus extends Report {

    private static MailLogger logger = new MailLogger(
	DeliveryStatus.class,
	"DEBUG DSN",
	PropUtil.getBooleanSystemProperty("mail.dsn.debug", false),
	System.out);

    /**
     * The DSN fields for the message.
     */
    protected InternetHeaders messageDSN;

    /**
     * The DSN fields for each recipient.
     */
    protected InternetHeaders[] recipientDSN;

    /**
     * Construct a delivery status notification with no content.
     *
     * @exception	MessagingException for failures
     */
    public DeliveryStatus() throws MessagingException {
	super("delivery-status");
	messageDSN = new InternetHeaders();
	recipientDSN = new InternetHeaders[0];
    }

    /**
     * Construct a delivery status notification by parsing the
     * supplied input stream.
     *
     * @param	is	the input stream
     * @exception	IOException for I/O errors reading the stream
     * @exception	MessagingException for other failures
     */
    public DeliveryStatus(InputStream is)
				throws MessagingException, IOException {
	super("delivery-status");
	messageDSN = new InternetHeaders(is);
	logger.fine("got messageDSN");
	Vector<InternetHeaders> v = new Vector<>();
	try {
	    while (is.available() > 0) {
		InternetHeaders h = new InternetHeaders(is);
		logger.fine("got recipientDSN");
		v.addElement(h);
	    }
	} catch (EOFException ex) {
	    logger.log(Level.FINE, "got EOFException", ex);
	}
	if (logger.isLoggable(Level.FINE))
	    logger.fine("recipientDSN size " + v.size());
	recipientDSN = new InternetHeaders[v.size()];
	v.copyInto(recipientDSN);
    }

    /**
     * Return all the per-message fields in the delivery status notification.
     * The fields are defined as:
     *
     * <pre>
     *    per-message-fields =
     *          [ original-envelope-id-field CRLF ]
     *          reporting-mta-field CRLF
     *          [ dsn-gateway-field CRLF ]
     *          [ received-from-mta-field CRLF ]
     *          [ arrival-date-field CRLF ]
     *          *( extension-field CRLF )
     * </pre>
     *
     * @return	the per-message DSN fields
     */
    // XXX - could parse each of these fields
    public InternetHeaders getMessageDSN() {
	return messageDSN;
    }

    /**
     * Set the per-message fields in the delivery status notification.
     *
     * @param	messageDSN	the per-message DSN fields
     */
    public void setMessageDSN(InternetHeaders messageDSN) {
	this.messageDSN = messageDSN;
    }

    /**
     * Return the number of recipients for which we have
     * per-recipient delivery status notification information.
     *
     * @return	the number of recipients
     */
    public int getRecipientDSNCount() {
	return recipientDSN.length;
    }

    /**
     * Return the delivery status notification information for
     * the specified recipient.
     *
     * @param	n	the recipient number
     * @return	the DSN fields for the recipient
     */
    public InternetHeaders getRecipientDSN(int n) {
	return recipientDSN[n];
    }

    /**
     * Add deliver status notification information for another
     * recipient.
     *
     * @param	h	the DSN fields for the recipient
     */
    public void addRecipientDSN(InternetHeaders h) {
	InternetHeaders[] rh = new InternetHeaders[recipientDSN.length + 1];
	System.arraycopy(recipientDSN, 0, rh, 0, recipientDSN.length);
	recipientDSN = rh;
	recipientDSN[recipientDSN.length - 1] = h;
    }

    public void writeTo(OutputStream os) throws IOException {
	// see if we already have a LOS
	LineOutputStream los = null;
	if (os instanceof LineOutputStream) {
	    los = (LineOutputStream) os;
	} else {
	    los = new LineOutputStream(os);
	}

	writeInternetHeaders(messageDSN, los);
	los.writeln();
	for (int i = 0; i < recipientDSN.length; i++) {
	    writeInternetHeaders(recipientDSN[i], los);
	    los.writeln();
	}
    }

    private static void writeInternetHeaders(InternetHeaders h,
				LineOutputStream los) throws IOException {
	Enumeration e = h.getAllHeaderLines();
	while (e.hasMoreElements())
	    los.writeln((String)e.nextElement());
    }

    public String toString() {
	return "DeliveryStatus: Reporting-MTA=" +
	    messageDSN.getHeader("Reporting-MTA", null) + ", #Recipients=" +
	    recipientDSN.length;
    }
}
