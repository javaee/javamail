/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.StoreListener;
import javax.mail.event.StoreEvent;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test IMAP response events.
 */
public final class IMAPResponseEventTest {

    private volatile boolean gotResponse;

    /**
     * Test that response events are sent for the LOGIN command.
     */
    @Test
    public void testLoginResponseEvent() {
	testLogin("");
    }

    /**
     * Test that response events are sent for the AUTHENTICATE LOGIN command.
     */
    @Test
    public void testAuthLoginResponseEvent() {
	testLogin("LOGINDISABLED AUTH=LOGIN");
    }

    /**
     * Test that response events are sent for the AUTHENTICATE PLAIN command.
     */
    @Test
    public void testAuthPlainResponseEvent() {
	testLogin("LOGINDISABLED AUTH=PLAIN");
    }

    private void testLogin(String type) {
        TestServer server = null;
        try {
            final IMAPHandler handler = new IMAPHandlerLogin(type);
            server = new TestServer(handler);
            server.start();

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "" + server.getPort());
            properties.setProperty("mail.imap.enableresponseevents", "true");
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);
	    final CountDownLatch latch = new CountDownLatch(1);

            final Store store = session.getStore("imap");
	    store.addStoreListener(new StoreListener() {
		@Override
		public void notification(StoreEvent e) {
		    String s;
		    if (e.getMessageType() == IMAPStore.RESPONSE) {
			s = "RESPONSE: ";
			// is this the expected AUTHENTICATE response?
			if (e.getMessage().indexOf("X-LOGIN-SUCCESS") >= 0)
			    gotResponse = true;
			latch.countDown();
		    } else
			s = "OTHER: ";
		    //System.out.println(s + e.getMessage());
		}
	    });
	    gotResponse = false;
            try {
                store.connect("test", "test");
		// time for event to be delivered
		latch.await(5, TimeUnit.SECONDS);
		assertTrue(gotResponse);

	    } catch (Exception ex) {
		System.out.println(ex);
		//ex.printStackTrace();
		fail(ex.toString());
            } finally {
                store.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
            }
        }
    }

    /**
     * Custom handler.  Forces use of specific login type and includes
     * a fake capability to be included in the OK response that we
     * will check for success.
     */
    private static final class IMAPHandlerLogin extends IMAPHandler {
	public IMAPHandlerLogin(String type) {
	    capabilities += " " + type + " X-LOGIN-SUCCESS";
	}
    }
}
