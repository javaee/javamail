/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.smtp;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.event.ConnectionAdapter;
import javax.mail.event.ConnectionEvent;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test that the connection is closed when an IOException is detected.
 */
public final class SMTPIOExceptionTest {

    // timeout the test in case of failure
    @Rule
    public Timeout deadlockTimeout = new Timeout(5000);

    private boolean closed = false;

    private static final int TIMEOUT = 200;	// I/O timeout, in millis

    @Test
    public void test() throws Exception {
        SMTPServer server = null;
        try {
	    SMTPHandler handler = new SMTPHandler() {
		public void rcpt() throws IOException {
		    try {
			// delay long enough to cause timeout
			Thread.sleep(2 * TIMEOUT);
		    } catch (Exception ex) { }
		    super.rcpt();
		}
	    };
            server = new SMTPServer(handler, 26423);
            server.start();
            Thread.sleep(1000);

            final Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "26423");
            properties.setProperty("mail.smtp.timeout", "" + TIMEOUT);
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);

            final Transport t = session.getTransport("smtp");
	    /*
	     * Use a listener to detect the connection being closed
	     * because if we called isConnected() and the connection
	     * wasn't already closed, it will issue a command that
	     * might detect that the connection was closed, even
	     * though it wasn't closed already.
	     */
	    t.addConnectionListener(new ConnectionAdapter() {
		@Override
		public void closed(ConnectionEvent e) {
		    setClosed(true);
		}
	    });
            try {
		MimeMessage msg = new MimeMessage(session);
		msg.setRecipients(Message.RecipientType.TO, "joe@example.com");
		msg.setSubject("test");
		msg.setText("test");
                t.connect();
		t.sendMessage(msg, msg.getAllRecipients());
	    } catch (MessagingException ex) {
		// expect an exception from sendMessage
		Thread.sleep(100);	// give event thread time to run
		assertTrue(getClosed());
            } finally {
                t.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
		server.interrupt();
		// wait long enough for handler to exit
		Thread.sleep(2 * TIMEOUT);
            }
        }
    }

    private synchronized void setClosed(boolean v) {
	closed = v;
    }

    private synchronized boolean getClosed() {
	return closed;
    }
}
