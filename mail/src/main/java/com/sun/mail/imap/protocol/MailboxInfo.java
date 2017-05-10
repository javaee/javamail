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

import javax.mail.Flags;

import com.sun.mail.iap.*;

/**
 * Information collected when opening a mailbox.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class MailboxInfo { 
    /** The available flags. */
    public Flags availableFlags = null;
    /** The permanent flags. */
    public Flags permanentFlags = null;
    /** The total number of messages. */
    public int total = -1;
    /** The number of recent messages. */
    public int recent = -1;
    /** The first unseen message. */
    public int first = -1;
    /** The UIDVALIDITY. */
    public long uidvalidity = -1;
    /** The next UID value to be assigned. */
    public long uidnext = -1;
    /** UIDs are not sticky. */
    public boolean uidNotSticky = false;	// RFC 4315
    /** The highest MODSEQ value. */
    public long highestmodseq = -1;	// RFC 4551 - CONDSTORE
    /** Folder.READ_WRITE or Folder.READ_ONLY, set by IMAPProtocol. */
    public int mode;
    /** VANISHED or FETCH responses received while opening the mailbox. */
    public List<IMAPResponse> responses;

    /**
     * Collect the information about this mailbox from the
     * responses to a SELECT or EXAMINE.
     *
     * @param	r	the responses
     * @exception	ParsingException	for errors parsing the responses
     */
    public MailboxInfo(Response[] r) throws ParsingException {
	for (int i = 0; i < r.length; i++) {
	    if (r[i] == null || !(r[i] instanceof IMAPResponse))
		continue;

	    IMAPResponse ir = (IMAPResponse)r[i];

	    if (ir.keyEquals("EXISTS")) {
		total = ir.getNumber();
		r[i] = null; // remove this response
	    } else if (ir.keyEquals("RECENT")) {
		recent = ir.getNumber();
		r[i] = null; // remove this response
	    } else if (ir.keyEquals("FLAGS")) {
		availableFlags = new FLAGS(ir);
		r[i] = null; // remove this response
	    } else if (ir.keyEquals("VANISHED")) {
		if (responses == null)
		    responses = new ArrayList<>();
		responses.add(ir);
		r[i] = null; // remove this response
	    } else if (ir.keyEquals("FETCH")) {
		if (responses == null)
		    responses = new ArrayList<>();
		responses.add(ir);
		r[i] = null; // remove this response
	    } else if (ir.isUnTagged() && ir.isOK()) {
		/*
		 * should be one of:
		 * 	* OK [UNSEEN 12]
		 * 	* OK [UIDVALIDITY 3857529045]
		 * 	* OK [PERMANENTFLAGS (\Deleted)]
		 * 	* OK [UIDNEXT 44]
		 * 	* OK [HIGHESTMODSEQ 103]
		 */
		ir.skipSpaces();

		if (ir.readByte() != '[') {	// huh ???
		    ir.reset();
		    continue;
		}

		boolean handled = true;
		String s = ir.readAtom();
		if (s.equalsIgnoreCase("UNSEEN"))
		    first = ir.readNumber();
		else if (s.equalsIgnoreCase("UIDVALIDITY"))
		    uidvalidity = ir.readLong();
		else if (s.equalsIgnoreCase("PERMANENTFLAGS"))
		    permanentFlags = new FLAGS(ir);
		else if (s.equalsIgnoreCase("UIDNEXT"))
		    uidnext = ir.readLong();
		else if (s.equalsIgnoreCase("HIGHESTMODSEQ"))
		    highestmodseq = ir.readLong();
		else
		    handled = false;	// possibly an ALERT

		if (handled)
		    r[i] = null; // remove this response
		else
		    ir.reset();	// so ALERT can be read
	    } else if (ir.isUnTagged() && ir.isNO()) {
		/*
		 * should be one of:
		 * 	* NO [UIDNOTSTICKY]
		 */
		ir.skipSpaces();

		if (ir.readByte() != '[') {	// huh ???
		    ir.reset();
		    continue;
		}

		boolean handled = true;
		String s = ir.readAtom();
		if (s.equalsIgnoreCase("UIDNOTSTICKY"))
		    uidNotSticky = true;
		else
		    handled = false;	// possibly an ALERT

		if (handled)
		    r[i] = null; // remove this response
		else
		    ir.reset();	// so ALERT can be read
	    }
	}

	/*
	 * The PERMANENTFLAGS response code is optional, and if
	 * not present implies that all flags in the required FLAGS
	 * response can be changed permanently.
	 */
	if (permanentFlags == null) {
	    if (availableFlags != null)
		permanentFlags = new Flags(availableFlags);
	    else
		permanentFlags = new Flags();
	}
    }
}
