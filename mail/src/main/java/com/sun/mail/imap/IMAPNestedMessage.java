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

package com.sun.mail.imap;

import java.io.*;
import javax.mail.*;
import com.sun.mail.imap.protocol.*;
import com.sun.mail.iap.ProtocolException;

/**
 * This class implements a nested IMAP message
 *
 * @author  John Mani
 */

public class IMAPNestedMessage extends IMAPMessage {
    private IMAPMessage msg; // the enclosure of this nested message

    /**
     * Package private constructor. <p>
     *
     * Note that nested messages have no containing folder, nor 
     * a message number.
     */
    IMAPNestedMessage(IMAPMessage m, BODYSTRUCTURE b, ENVELOPE e, String sid) {
	super(m._getSession());
	msg = m;
	bs = b;
	envelope = e;
	sectionId = sid;
	setPeek(m.getPeek());
    }

    /*
     * Get the enclosing message's Protocol object. Overrides
     * IMAPMessage.getProtocol().
     */
    @Override
    protected IMAPProtocol getProtocol()
			throws ProtocolException, FolderClosedException {
	return msg.getProtocol();
    }

    /*
     * Is this an IMAP4 REV1 server?
     */
    @Override
    protected boolean isREV1() throws FolderClosedException {
	return msg.isREV1();
    }

    /*
     * Get the enclosing message's messageCacheLock. Overrides
     * IMAPMessage.getMessageCacheLock().
     */
    @Override
    protected Object getMessageCacheLock() {
	return msg.getMessageCacheLock();
    }

    /*
     * Get the enclosing message's sequence number. Overrides
     * IMAPMessage.getSequenceNumber().
     */
    @Override
    protected int getSequenceNumber() {
	return msg.getSequenceNumber();
    }

    /*
     * Check whether the enclosing message is expunged. Overrides 
     * IMAPMessage.checkExpunged().
     */
    @Override
    protected void checkExpunged() throws MessageRemovedException {
	msg.checkExpunged();
    }

    /*
     * Check whether the enclosing message is expunged. Overrides
     * Message.isExpunged().
     */
    @Override
    public boolean isExpunged() {
	return msg.isExpunged();
    }

    /*
     * Get the enclosing message's fetchBlockSize. 
     */
    @Override
    protected int getFetchBlockSize() {
	return msg.getFetchBlockSize();
    }

    /*
     * Get the enclosing message's ignoreBodyStructureSize. 
     */
    @Override
    protected boolean ignoreBodyStructureSize() {
	return msg.ignoreBodyStructureSize();
    }

    /*
     * IMAPMessage uses RFC822.SIZE. We use the "size" field from
     * our BODYSTRUCTURE.
     */
    @Override
    public int getSize() throws MessagingException {
	return bs.size;
    }

    /*
     * Disallow setting flags on nested messages
     */
    @Override
    public synchronized void setFlags(Flags flag, boolean set) 
			throws MessagingException {
	// Cannot set FLAGS on a nested IMAP message	
	throw new MethodNotSupportedException(
		"Cannot set flags on this nested message");
    }
}
