/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2013 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;

import com.sun.mail.imap.IMAPStore;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.fail;

/**
 * Test that IMAP idle state is handled properly.
 */
public final class IMAPIdleStateTest {

    // timeout the test in case of deadlock
    @Rule
    public Timeout deadlockTimeout = new Timeout(20000);

    @Test
    public void test() {
        IMAPServer server = null;
        try {
            final IMAPHandler handler = new IMAPHandlerIdleBye();
            server = new IMAPServer(handler, 26422);
            server.start();
            Thread.sleep(1000);

            final Properties properties = new Properties();
            properties.setProperty("mail.imap.host", "localhost");
            properties.setProperty("mail.imap.port", "26422");
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

            final IMAPStore store = (IMAPStore)session.getStore("imap");
            try {
                store.connect("test", "test");

		// create a thread to run the IDLE command on the Store
		Thread t = new Thread() {
		    public void run() {
			try {
			    store.idle();
			} catch (Exception ex) {
			}
		    }
		};
		t.start();
		Thread.sleep(20);	// give it time to IDLE

		// Now break it out of idle.
		// Need to use a method that doesn't check that the Store
		// is connected first.
		store.hasCapability("XXX");
		// no NullPointerException means the bug is fixed!

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
     * Custom handler.  Simulates the server sending a BYE response
     * to abort an IDLE.
     */
    private static final class IMAPHandlerIdleBye extends IMAPHandler {
	@Override
        public void idle() throws IOException {
	    cont();
	    // don't wait for DONE, just close the connection now
	    bye("closing");
        }
    }
}
