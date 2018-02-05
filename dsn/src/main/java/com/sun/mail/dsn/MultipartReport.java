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
import java.util.Vector;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * A multipart/report message content, as defined in
 * <A HREF="http://www.ietf.org/rfc/rfc3462.txt" TARGET="_top">RFC 3462</A>.
 * A multipart/report content is a container for mail reports
 * of any kind, and is most often used to return a delivery
 * status report or a disposition notification report. <p>
 *
 * A MultipartReport object is a special type of MimeMultipart
 * object with a restricted set of body parts.  A MultipartReport
 * object contains:
 * <ul>
 * <li>[Required] A human readable text message describing the
 * reason the report was generated.</li>
 * <li>[Required] A {@link Report} object containing the
 * details for why the report was generated.</li>
 * <li>[Optional] A returned copy of the entire message, or just
 * its headers, which caused the generation of this report.
 * </ul>
 * Many of the normal MimeMultipart operations are restricted to
 * ensure that the MultipartReport object always follows this
 * structure.
 *
 * @since	JavaMail 1.4
 */
public class MultipartReport extends MimeMultipart {
    protected boolean constructed; // true when done with constructor

    /**
     * Construct a multipart/report object with no content.
     *
     * @exception	MessagingException for failures
     */
    public MultipartReport() throws MessagingException {
	super("report");
	// always at least two body parts
	MimeBodyPart mbp = new MimeBodyPart();
	setBodyPart(mbp, 0);
	mbp = new MimeBodyPart();
	setBodyPart(mbp, 1);
	constructed = true;
    }

    /**
     * Construct a multipart/report object with the specified plain
     * text and report type (DeliveryStatus or DispositionNotification)
     * to be returned to the user.
     *
     * @param	text	the plain text
     * @param	report	the Report object
     * @exception	MessagingException for failures
     */
    public MultipartReport(String text, Report report)
				throws MessagingException {
	super("report");
	ContentType ct = new ContentType(contentType);
	String reportType = report.getType();
	ct.setParameter("report-type", reportType);
	contentType = ct.toString();
	MimeBodyPart mbp = new MimeBodyPart();
	mbp.setText(text);
	setBodyPart(mbp, 0);
	mbp = new MimeBodyPart();
	ct = new ContentType("message", reportType, null);
	mbp.setContent(report, ct.toString());
	setBodyPart(mbp, 1);
	constructed = true;
    }

    /**
     * Construct a multipart/report object with the specified plain
     * text, report, and original message to be returned to the user.
     *
     * @param	text	the plain text
     * @param	report	the Report object
     * @param	msg	the message this report is about
     * @exception	MessagingException for failures
     */
    public MultipartReport(String text, Report report, MimeMessage msg)
				throws MessagingException {
	this(text, report);
	if (msg != null) {
	    MimeBodyPart mbp = new MimeBodyPart();
	    mbp.setContent(msg, "message/rfc822");
	    setBodyPart(mbp, 2);
	}
    }

    /**
     * Construct a multipart/report object with the specified plain
     * text, report, and headers from the original message
     * to be returned to the user.
     *
     * @param	text	the plain text
     * @param	report	the Report object
     * @param	hdr	the headers of the message this report is about
     * @exception	MessagingException for failures
     */
    public MultipartReport(String text, Report report, InternetHeaders hdr)
				throws MessagingException {
	this(text, report);
	if (hdr != null) {
	    MimeBodyPart mbp = new MimeBodyPart();
	    mbp.setContent(new MessageHeaders(hdr), "text/rfc822-headers");
	    setBodyPart(mbp, 2);
	}
    }

    /**
     * Constructs a MultipartReport object and its bodyparts from the 
     * given DataSource. <p>
     *
     * @param	ds	DataSource, can be a MultipartDataSource
     * @exception	MessagingException for failures
     */
    public MultipartReport(DataSource ds) throws MessagingException {
	super(ds);
	parse();
	constructed = true;
	/*
	 * Can't fail to construct object because some programs just
	 * want to treat this as a Multipart and examine the parts.
	 *
	if (getCount() < 2 || getCount() > 3)	// XXX allow extra parts
	    throw new MessagingException(
		"Wrong number of parts in multipart/report: " + getCount());
	 */
    }

    /**
     * Get the plain text to be presented to the user, if there is any.
     * Rarely, the message may contain only HTML text, or no text at
     * all.  If the text body part of this multipart/report object is
     * of type text/plain, or if it is of type multipart/alternative
     * and contains a text/plain part, the text from that part is
     * returned.  Otherwise, null is return and the {@link #getTextBodyPart
     * getTextBodyPart} method may be used to extract the data.
     *
     * @return	the text
     * @exception	MessagingException for failures
     */
    public synchronized String getText() throws MessagingException {
	try {
	    BodyPart bp = getBodyPart(0);
	    if (bp.isMimeType("text/plain"))
		return (String)bp.getContent();
	    if (bp.isMimeType("multipart/alternative")) {
		Multipart mp = (Multipart)bp.getContent();
		for (int i = 0; i < mp.getCount(); i++) {
		    bp = mp.getBodyPart(i);
		    if (bp.isMimeType("text/plain"))
			return (String)bp.getContent();
		}
	    }
	} catch (IOException ex) {
	    throw new MessagingException("Exception getting text content", ex);
	}
	return null;
    }

    /**
     * Set the message to be presented to the user as just a text/plain
     * part containing the specified text.
     *
     * @param	text	the text
     * @exception	MessagingException for failures
     */
    public synchronized void setText(String text) throws MessagingException {
	MimeBodyPart mbp = new MimeBodyPart();
	mbp.setText(text);
	setBodyPart(mbp, 0);
    }

    /**
     * Return the body part containing the message to be presented to
     * the user, usually just a text/plain part.
     *
     * @return	the body part containing the text
     * @exception	MessagingException for failures
     */
    public synchronized MimeBodyPart getTextBodyPart()
				throws MessagingException {
	return (MimeBodyPart)getBodyPart(0);
    }

    /**
     * Set the body part containing the text to be presented to the
     * user.  Usually this a text/plain part, but it might also be
     * a text/html part or a multipart/alternative part containing
     * text/plain and text/html parts.  Any type is allowed here
     * but these types are most common.
     *
     * @param	mbp	the body part containing the text
     * @exception	MessagingException for failures
     */
    public synchronized void setTextBodyPart(MimeBodyPart mbp)
				throws MessagingException {
	setBodyPart(mbp, 0);
    }

    /**
     * Get the report associated with this multipart/report.
     *
     * @return	the Report object
     * @exception	MessagingException for failures
     * @since	JavaMail 1.4.2
     */
    public synchronized Report getReport() throws MessagingException {
	if (getCount() < 2)
	    return null;
	BodyPart bp = getBodyPart(1);
	try {
	    Object content = bp.getContent();
	    if (!(content instanceof Report))
		return null;
	    return (Report)content;
	} catch (IOException ex) {
	    throw new MessagingException("IOException getting Report", ex);
	}
    }

    /**
     * Set the report associated with this multipart/report.
     *
     * @param	report	the Report object
     * @exception	MessagingException for failures
     * @since	JavaMail 1.4.2
     */
    public synchronized void setReport(Report report)
				throws MessagingException {
	MimeBodyPart mbp = new MimeBodyPart();
	ContentType ct = new ContentType(contentType);
	String reportType = report.getType();
	ct.setParameter("report-type", reportType);
	contentType = ct.toString();
	ct = new ContentType("message", reportType, null);
	mbp.setContent(report, ct.toString());
	setBodyPart(mbp, 1);
    }

    /**
     * Get the delivery status associated with this multipart/report.
     *
     * @return	the delivery status
     * @exception	MessagingException for failures
     * @deprecated	use getReport instead
     */
    @Deprecated
    public synchronized DeliveryStatus getDeliveryStatus()
				throws MessagingException {
	if (getCount() < 2)
	    return null;
	BodyPart bp = getBodyPart(1);
	if (!bp.isMimeType("message/delivery-status"))
	    return null;
	try {
	    return (DeliveryStatus)bp.getContent();
	} catch (IOException ex) {
	    throw new MessagingException("IOException getting DeliveryStatus",
					ex);
	}
    }

    /**
     * Set the delivery status associated with this multipart/report.
     *
     * @param	status the deliver status
     * @exception	MessagingException for failures
     * @deprecated	use setReport instead
     */
    public synchronized void setDeliveryStatus(DeliveryStatus status)
				throws MessagingException {
	MimeBodyPart mbp = new MimeBodyPart();
	mbp.setContent(status, "message/delivery-status");
	setBodyPart(mbp, 1);
	ContentType ct = new ContentType(contentType);
	ct.setParameter("report-type", "delivery-status");
	contentType = ct.toString();
    }

    /**
     * Get the original message that is being returned along with this
     * multipart/report.  If no original message is included, null is
     * returned.  In some cases only the headers of the original
     * message will be returned as an object of type MessageHeaders.
     *
     * @return	the returned message
     * @exception	MessagingException for failures
     */
    public synchronized MimeMessage getReturnedMessage()
				throws MessagingException {
	if (getCount() < 3)
	    return null;
	BodyPart bp = getBodyPart(2);
	if (!bp.isMimeType("message/rfc822") &&
		!bp.isMimeType("text/rfc822-headers"))
	    return null;
	try {
	    return (MimeMessage)bp.getContent();
	} catch (IOException ex) {
	    throw new MessagingException("IOException getting ReturnedMessage",
					ex);
	}
    }

    /**
     * Set the original message to be returned as part of the
     * multipart/report.  If msg is null, any previously set
     * returned message or headers is removed.
     *
     * @param	msg	the returned message
     * @exception	MessagingException for failures
     */
    public synchronized void setReturnedMessage(MimeMessage msg)
				throws MessagingException {
	if (msg == null) {
	    super.removeBodyPart(2);
	    return;
	}
	MimeBodyPart mbp = new MimeBodyPart();
	if (msg instanceof MessageHeaders)
	    mbp.setContent(msg, "text/rfc822-headers");
	else
	    mbp.setContent(msg, "message/rfc822");
	setBodyPart(mbp, 2);
    }

    private synchronized void setBodyPart(BodyPart part, int index) 
				throws MessagingException {
	if (parts == null)	// XXX - can never happen?
	    parts = new Vector<BodyPart>();

	if (index < parts.size())
	    super.removeBodyPart(index);
	super.addBodyPart(part, index);
    }


    // Override Multipart methods to preserve integrity of multipart/report.

    /**
     * Set the subtype.  Throws MessagingException.
     *
     * @param	subtype		Subtype
     * @exception	MessagingException	always; can't change subtype
     */
    public synchronized void setSubType(String subtype) 
			throws MessagingException {
	throw new MessagingException("Can't change subtype of MultipartReport");
    }

    /**
     * Remove the specified part from the multipart message.
     * Not allowed on a multipart/report object.
     *
     * @param   part	The part to remove
     * @exception	MessagingException always
     */
    public boolean removeBodyPart(BodyPart part) throws MessagingException {
	throw new MessagingException(
	    "Can't remove body parts from multipart/report");
    }

    /**
     * Remove the part at specified location (starting from 0).
     * Not allowed on a multipart/report object.
     *
     * @param   index	Index of the part to remove
     * @exception	MessagingException	always
     */
    public void removeBodyPart(int index) throws MessagingException {
	throw new MessagingException(
	    "Can't remove body parts from multipart/report");
    }

    /**
     * Adds a Part to the multipart.
     * Not allowed on a multipart/report object.
     *
     * @param  part  The Part to be appended
     * @exception       MessagingException	always
     */
    public synchronized void addBodyPart(BodyPart part) 
		throws MessagingException {
	// Once constructor is done, don't allow this anymore.
	if (!constructed)
	    super.addBodyPart(part);
	else
	    throw new MessagingException(
		"Can't add body parts to multipart/report 1");
    }

    /**
     * Adds a BodyPart at position <code>index</code>.
     * Not allowed on a multipart/report object.
     *
     * @param  part  The BodyPart to be inserted
     * @param  index Location where to insert the part
     * @exception       MessagingException	always
     */
    public synchronized void addBodyPart(BodyPart part, int index) 
				throws MessagingException {
	throw new MessagingException(
	    "Can't add body parts to multipart/report 2");
    }
}
