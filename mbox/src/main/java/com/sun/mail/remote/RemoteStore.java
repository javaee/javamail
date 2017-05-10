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

package com.sun.mail.remote;

import java.io.*;
import javax.mail.*;
import com.sun.mail.mbox.*;

/**
 * A wrapper around a local <code>MboxStore</code> that fetches data
 * from the Inbox in a remote store and adds it to our local Inbox.
 */
public abstract class RemoteStore extends MboxStore {

    protected Store remoteStore;
    protected Folder remoteInbox;
    protected Folder inbox;
    protected String host, user, password;
    protected int port;
    protected long lastUpdate = 0;

    public RemoteStore(Session session, URLName url) {
	super(session, url);
	remoteStore = getRemoteStore(session, url);
    }

    /**
     * Subclasses override this method to return the appropriate
     * <code>Store</code> object.  This method will be called by
     * the <code>RemoteStore</code> constructor.
     */
    protected abstract Store getRemoteStore(Session session, URLName url);

    /**
     * Connect to the store.
     */
    public void connect(String host, int port, String user, String password)
			throws MessagingException {
	this.host = host;
	this.port = port;
	this.user = user;
	this.password = password;
	updateInbox();
    }

    /**
     * Fetch any new mail in the remote INBOX and add it to the local INBOX.
     */
    protected void updateInbox() throws MessagingException {
	// is it time to do an update yet?
	// XXX - polling frequency, rules, etc. should be in properties
	if (System.currentTimeMillis() < lastUpdate + (5 * 1000))
	    return;
	try {
	    /*
	     * Connect to the remote store, using the saved
	     * authentication information.
	     */
	    remoteStore.connect(host, port, user, password);

	    /*
	     * If this store isn't connected yet, do it now, because
	     * it needs to be connected to get the INBOX folder.
	     */
	    if (!isConnected())
		super.connect(host, port, user, password);
	    if (remoteInbox == null)
		remoteInbox = remoteStore.getFolder("INBOX");
	    if (inbox == null)
		inbox = getFolder("INBOX");
	    remoteInbox.open(Folder.READ_WRITE);
	    Message[] msgs = remoteInbox.getMessages();
	    inbox.appendMessages(msgs);
	    remoteInbox.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
	    remoteInbox.close(true);
	    remoteStore.close();
	} catch (MessagingException ex) {
	    try {
		if (remoteInbox != null && remoteInbox.isOpen())
		    remoteInbox.close(false);
	    } finally {
		if (remoteStore != null && remoteStore.isConnected())
		    remoteStore.close();
	    }
	    throw ex;
	}
    }

    public Folder getDefaultFolder() throws MessagingException {
	checkConnected();

	return new RemoteDefaultFolder(this, null);
    }

    public Folder getFolder(String name) throws MessagingException {
	checkConnected();

	if (name.equalsIgnoreCase("INBOX"))
	    return new RemoteInbox(this, name);
	else
	    return super.getFolder(name);
    }

    public Folder getFolder(URLName url) throws MessagingException {
	checkConnected();
	return getFolder(url.getFile());
    }

    private void checkConnected() throws MessagingException {
	if (!isConnected())
	    throw new MessagingException("Not connected");
    }
}
