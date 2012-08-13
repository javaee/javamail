/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.gimap;

import java.io.*;

import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.iap.*;
import com.sun.mail.imap.*;
import com.sun.mail.imap.protocol.*;

/**
 * A Gmail folder.  Defines new FetchProfile items and
 * uses GmailMessage to store additional Gmail message attributes.
 *
 * @since JavaMail 1.4.6
 * @author Bill Shannon
 */

public class GmailFolder extends IMAPFolder {
    /**
     * A fetch profile item for fetching headers.
     * This inner class extends the <code>FetchProfile.Item</code>
     * class to add new FetchProfile item types, specific to Gmail.
     *
     * @see FetchProfile
     */
    public static class FetchProfileItem extends FetchProfile.Item {
	protected FetchProfileItem(String name) {
	    super(name);
	}

	/**
	 * MSGID is a fetch profile item that can be included in a
	 * <code>FetchProfile</code> during a fetch request to a Folder.
	 * This item indicates that the Gmail unique message ID for messages
	 * in the specified range are desired to be prefetched. <p>
	 * 
	 * An example of how a client uses this is below: <p>
	 * <blockquote><pre>
	 *
	 * 	FetchProfile fp = new FetchProfile();
	 *	fp.add(GmailFolder.FetchProfileItem.MSGID);
	 *	folder.fetch(msgs, fp);
	 *
	 * </pre></blockquote><p>
	 */ 
	public static final FetchProfileItem MSGID = 
		new FetchProfileItem("X-GM-MSGID");

	/**
	 * THRID is a fetch profile item that can be included in a
	 * <code>FetchProfile</code> during a fetch request to a Folder.
	 * This item indicates that the Gmail unique thread ID for messages
	 * in the specified range are desired to be prefetched. <p>
	 * 
	 * An example of how a client uses this is below: <p>
	 * <blockquote><pre>
	 *
	 * 	FetchProfile fp = new FetchProfile();
	 *	fp.add(GmailFolder.FetchProfileItem.THRID);
	 *	folder.fetch(msgs, fp);
	 *
	 * </pre></blockquote><p>
	 */ 
	public static final FetchProfileItem THRID = 
		new FetchProfileItem("X-GM-THRID");

	/**
	 * LABELS is a fetch profile item that can be included in a
	 * <code>FetchProfile</code> during a fetch request to a Folder.
	 * This item indicates that the Gmail labels for messages
	 * in the specified range are desired to be prefetched. <p>
	 * 
	 * An example of how a client uses this is below: <p>
	 * <blockquote><pre>
	 *
	 * 	FetchProfile fp = new FetchProfile();
	 *	fp.add(GmailFolder.FetchProfileItem.LABELS);
	 *	folder.fetch(msgs, fp);
	 *
	 * </pre></blockquote><p>
	 */ 
	public static final FetchProfileItem LABELS = 
		new FetchProfileItem("X-GM-LABELS");
    }

    /**
     * Constructor used to create a possibly non-existent folder.
     *
     * @param fullName	fullname of this folder
     * @param separator the default separator character for this 
     *			folder's namespace
     * @param store	the Store
     */
    protected GmailFolder(String fullName, char separator, IMAPStore store,
				Boolean isNamespace) {
	super(fullName, separator, store, isNamespace);
    }

    /**
     * Constructor used to create an existing folder.
     */
    protected GmailFolder(ListInfo li, IMAPStore store) {
	super(li, store);
    }

    /**
     * Create a new IMAPMessage object to represent the given message number.
     */
    protected IMAPMessage newIMAPMessage(int msgnum) {
	return new GmailMessage(this, msgnum);
    }
}
