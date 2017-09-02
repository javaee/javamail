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

package com.sun.mail.imap.protocol;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;
import com.sun.mail.iap.*;
import com.sun.mail.util.PropUtil;

/**
 * The ENEVELOPE item of an IMAP FETCH response.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class ENVELOPE implements Item {
    
    // IMAP item name
    static final char[] name = {'E','N','V','E','L','O','P','E'};
    public int msgno;

    public Date date = null;
    public String subject;
    public InternetAddress[] from;
    public InternetAddress[] sender;
    public InternetAddress[] replyTo;
    public InternetAddress[] to;
    public InternetAddress[] cc;
    public InternetAddress[] bcc;
    public String inReplyTo;
    public String messageId;

    // Used to parse dates
    private static final MailDateFormat mailDateFormat = new MailDateFormat();

    // special debugging output to debug parsing errors
    private static final boolean parseDebug =
	PropUtil.getBooleanSystemProperty("mail.imap.parse.debug", false);
    
    public ENVELOPE(FetchResponse r) throws ParsingException {
	if (parseDebug)
	    System.out.println("parse ENVELOPE");
	msgno = r.getNumber();

	r.skipSpaces();

	if (r.readByte() != '(')
	    throw new ParsingException("ENVELOPE parse error");
	
	String s = r.readString();
	if (s != null) {
	    try {
            synchronized (mailDateFormat) {
                date = mailDateFormat.parse(s);
            }
	    } catch (ParseException pex) {
	    }
	}
	if (parseDebug)
	    System.out.println("  Date: " + date);

	subject = r.readString();
	if (parseDebug)
	    System.out.println("  Subject: " + subject);
	if (parseDebug)
	    System.out.println("  From addresses:");
	from = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  Sender addresses:");
	sender = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  Reply-To addresses:");
	replyTo = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  To addresses:");
	to = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  Cc addresses:");
	cc = parseAddressList(r);
	if (parseDebug)
	    System.out.println("  Bcc addresses:");
	bcc = parseAddressList(r);
	inReplyTo = r.readString();
	if (parseDebug)
	    System.out.println("  In-Reply-To: " + inReplyTo);
	messageId = r.readString();
	if (parseDebug)
	    System.out.println("  Message-ID: " + messageId);

	if (!r.isNextNonSpace(')'))
	    throw new ParsingException("ENVELOPE parse error");
    }

    private InternetAddress[] parseAddressList(Response r) 
		throws ParsingException {
	r.skipSpaces(); // skip leading spaces

	byte b = r.readByte();
	if (b == '(') {
	    /*
	     * Some broken servers (e.g., Yahoo Mail) return an empty
	     * list instead of NIL.  Handle that here even though it
	     * doesn't conform to the IMAP spec.
	     */
	    if (r.isNextNonSpace(')'))
		return null;

	    List<InternetAddress> v = new ArrayList<>();

	    do {
		IMAPAddress a = new IMAPAddress(r);
		if (parseDebug)
		    System.out.println("    Address: " + a);
		// if we see an end-of-group address at the top, ignore it
		if (!a.isEndOfGroup())
		    v.add(a);
	    } while (!r.isNextNonSpace(')'));

	    return v.toArray(new InternetAddress[v.size()]);
	} else if (b == 'N' || b == 'n') { // NIL
	    r.skip(2); // skip 'NIL'
	    return null;
	} else
	    throw new ParsingException("ADDRESS parse error");
    }
}

class IMAPAddress extends InternetAddress {
    private boolean group = false;
    private InternetAddress[] grouplist;
    private String groupname;

    private static final long serialVersionUID = -3835822029483122232L;

    IMAPAddress(Response r) throws ParsingException {
        r.skipSpaces(); // skip leading spaces

        if (r.readByte() != '(')
            throw new ParsingException("ADDRESS parse error");

        encodedPersonal = r.readString();

        r.readString(); // throw away address_list
	String mb = r.readString();
	String host = r.readString();
	// skip bogus spaces inserted by Yahoo IMAP server if
	// "undisclosed-recipients" is a recipient
	r.skipSpaces();
	if (!r.isNextNonSpace(')')) // skip past terminating ')'
            throw new ParsingException("ADDRESS parse error");

	if (host == null) {
	    // it's a group list, start or end
	    group = true;
	    groupname = mb;
	    if (groupname == null)	// end of group list
		return;
	    // Accumulate a group list.  The members of the group
	    // are accumulated in a List and the corresponding string
	    // representation of the group is accumulated in a StringBuilder.
	    StringBuilder sb = new StringBuilder();
	    sb.append(groupname).append(':');
	    List<InternetAddress> v = new ArrayList<>();
	    while (r.peekByte() != ')') {
		IMAPAddress a = new IMAPAddress(r);
		if (a.isEndOfGroup())	// reached end of group
		    break;
		if (v.size() != 0)	// if not first element, need a comma
		    sb.append(',');
		sb.append(a.toString());
		v.add(a);
	    }
	    sb.append(';');
	    address = sb.toString();
	    grouplist = v.toArray(new IMAPAddress[v.size()]);
	} else {
	    if (mb == null || mb.length() == 0)
		address = host;
	    else if (host.length() == 0)
		address = mb;
	    else
		address = mb + "@" + host;
	}

    }

    boolean isEndOfGroup() {
	return group && groupname == null;
    }

    @Override
    public boolean isGroup() {
	return group;
    }

    @Override
    public InternetAddress[] getGroup(boolean strict) throws AddressException {
	if (grouplist == null)
	    return null;
	return grouplist.clone();
    }
}
