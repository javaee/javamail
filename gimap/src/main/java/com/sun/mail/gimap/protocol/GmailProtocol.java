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

package com.sun.mail.gimap.protocol;

import java.io.*;
import java.util.*;

import com.sun.mail.iap.*;
import com.sun.mail.imap.protocol.*;
import com.sun.mail.gimap.GmailFolder.FetchProfileItem;

import com.sun.mail.util.MailLogger;

/**
 * Extend IMAP support to handle Gmail-specific protocol extensions.
 *
 * @since JavaMail 1.4.6
 * @author Bill Shannon
 */

public class GmailProtocol extends IMAPProtocol {
    
    /*
     * Define the Gmail-specific FETCH items.
     */
    public static final FetchItem MSGID_ITEM =
	new FetchItem("X-GM-MSGID", FetchProfileItem.MSGID) {
	    public Object parseItem(FetchResponse r) {
		return new Long(r.readLong());
	    }
	};
    public static final FetchItem THRID_ITEM =
	new FetchItem("X-GM-THRID", FetchProfileItem.THRID) {
	    public Object parseItem(FetchResponse r) {
		return new Long(r.readLong());
	    }
	};
    public static final FetchItem LABELS_ITEM =
	new FetchItem("X-GM-LABELS", FetchProfileItem.LABELS) {
	    public Object parseItem(FetchResponse r) {
		return r.readAtomStringList();
	    }
	};

    private static final FetchItem[] myFetchItems = {
	MSGID_ITEM,
	THRID_ITEM,
	LABELS_ITEM
    };

    private FetchItem[] fetchItems = null;

    /**
     * Connect to Gmail.
     *
     * @param host	host to connect to
     * @param port	portnumber to connect to
     * @param props	Properties object used by this protocol
     * @param isSSL	use SSL?
     * @param logger	for log messages
     */
    public GmailProtocol(String name, String host, int port, 
			Properties props, boolean isSSL, MailLogger logger)
			throws IOException, ProtocolException {
	super(name, host, port, props, isSSL, logger);

	// check to see if this is really Gmail
	if (!hasCapability("X-GM-EXT-1")) {
	    logger.fine("WARNING! Not connected to Gmail!");
	    // XXX - could call "disconnect()" here and make this a fatal error
	} else {
	    logger.fine("connected to Gmail");
	}
    }

    /**
     * Return the additional fetch items supported by the Gmail protocol.
     * Combines our fetch items with those supported by the superclass.
     */
    public FetchItem[] getFetchItems() {
	if (fetchItems != null)
	    return fetchItems;
	FetchItem[] sfi = super.getFetchItems();
	if (sfi == null || sfi.length == 0)
	    fetchItems = myFetchItems;
	else {
	    fetchItems = new FetchItem[sfi.length + myFetchItems.length];
	    System.arraycopy(sfi, 0, fetchItems, 0, sfi.length);
	    System.arraycopy(myFetchItems, 0, fetchItems, sfi.length,
							myFetchItems.length);
	}
	return fetchItems;
    }

    /**
     * Return a GmailSearchSequence.
     */
    protected SearchSequence getSearchSequence() {
	if (searchSequence == null)
	    searchSequence = new GmailSearchSequence();
	return searchSequence;
    }
}
