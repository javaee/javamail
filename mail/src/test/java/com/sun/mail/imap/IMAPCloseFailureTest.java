/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.imap;

import java.io.*;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;

import org.junit.Test;
import static org.junit.Assert.fail;

/**
 * Test that failures while closing a folder are handled properly.
 */
public final class IMAPCloseFailureTest {

    private static final String HOST = "localhost";
    private static final int PORT = 26422;

    static class NoIMAPHandler extends IMAPHandler {
	static boolean first = true;

	public void examine() throws IOException {
	    if (first)
		no("mailbox gone");
	    else
		super.examine();
	    first = false;
	}
    }

    static class BadIMAPHandler extends IMAPHandler {
	static boolean first = true;

	public void examine() throws IOException {
	    if (first)
		bad("mailbox gone");
	    else
		super.examine();
	    first = false;
	}
    }

    @Test
    public void testCloseNo() {
	testClose(new NoIMAPHandler());
    }

    @Test
    public void testCloseBad() {
	testClose(new BadIMAPHandler());
    }

    public void testClose(IMAPHandler handler) {
	IMAPServer server = null;
	try {
	    server = new IMAPServer(handler, PORT);
	    server.start();
	    Thread.sleep(1000);

	    Properties properties = new Properties();
            properties.setProperty("mail.imap.host", HOST);
            properties.setProperty("mail.imap.port", "" + PORT);
	    Session session = Session.getInstance(properties);
	    //session.setDebug(true);

	    Store store = session.getStore("imap");
	    try {
		store.connect("test", "test");
		Folder f = store.getFolder("INBOX");
		f.open(Folder.READ_WRITE);
		f.close(false);
		// Make sure that failure while closing doesn't leave us
		// with a connection that can't be used to open a folder.
		f.open(Folder.READ_WRITE);
		f.close(false);
	    } catch (Exception ex) {
		System.out.println(ex);
		//ex.printStackTrace();
		fail(ex.toString());
	    } finally {
		if (store.isConnected())
		    store.close();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	} finally {
	    if (server != null) {
		server.quit();
	    }
	}
    }
}
