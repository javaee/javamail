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

package com.sun.mail.pop3;

import javax.mail.*;

/**
 * The POP3 DefaultFolder.  Only contains the "INBOX" folder.
 *
 * @author Christopher Cotton
 */
public class DefaultFolder extends Folder {

    DefaultFolder(POP3Store store) {
	super(store);
    }

    @Override
    public String getName() {
	return "";
    }

    @Override
    public String getFullName() {
	return "";
    }

    @Override
    public Folder getParent() {
	return null;
    }

    @Override
    public boolean exists() {
	return true;
    }

    @Override
    public Folder[] list(String pattern) throws MessagingException {
	Folder[] f = { getInbox() };
	return f;
    }

    @Override
    public char getSeparator() {
	return '/';
    }

    @Override
    public int getType() {
	return HOLDS_FOLDERS;
    }

    @Override
    public boolean create(int type) throws MessagingException {
	return false;
    }

    @Override
    public boolean hasNewMessages() throws MessagingException {
	return false;
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
	if (!name.equalsIgnoreCase("INBOX")) {
	    throw new MessagingException("only INBOX supported");
	} else {
	    return getInbox();
	}
    }

    protected Folder getInbox() throws MessagingException {
	return getStore().getFolder("INBOX");
    }
    

    @Override
    public boolean delete(boolean recurse) throws MessagingException {
	throw new MethodNotSupportedException("delete");
    }

    @Override
    public boolean renameTo(Folder f) throws MessagingException {
	throw new MethodNotSupportedException("renameTo");
    }

    @Override
    public void open(int mode) throws MessagingException {
	throw new MethodNotSupportedException("open");
    }

    @Override
    public void close(boolean expunge) throws MessagingException {
	throw new MethodNotSupportedException("close");
    }

    @Override
    public boolean isOpen() {
	return false;
    }

    @Override
    public Flags getPermanentFlags() {
	return new Flags(); // empty flags object
    }

    @Override
    public int getMessageCount() throws MessagingException {
	return 0;
    }

    @Override
    public Message getMessage(int msgno) throws MessagingException {
	throw new MethodNotSupportedException("getMessage");
    }

    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
	throw new MethodNotSupportedException("Append not supported");	
    }

    @Override
    public Message[] expunge() throws MessagingException {
	throw new MethodNotSupportedException("expunge");	
    }
}
