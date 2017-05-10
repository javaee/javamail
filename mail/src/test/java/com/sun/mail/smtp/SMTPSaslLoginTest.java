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

package com.sun.mail.smtp;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.AuthenticationFailedException;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test login using a SASL mechanism on the server
 * with SASL and non-SASL on the client.
 */
public class SMTPSaslLoginTest {

    /**
     * Test using non-SASL DIGEST-MD5.
     */
    @Test
    public void testSuccess() {
        TestServer server = null;
        try {
            server = new TestServer(new SMTPSaslHandler());
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect("test", "test");
		// success!
	    } catch (Exception ex) {
		fail(ex.toString());
            } finally {
                t.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
		server.interrupt();
            }
        }
    }

    /**
     * Test using non-SASL DIGEST-MD5 with incorrect password.
     */
    @Test
    public void testFailure() {
        TestServer server = null;
        try {
            server = new TestServer(new SMTPSaslHandler());
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect("test", "xtest");
		// should have failed
		fail("wrong password succeeded");
	    } catch (AuthenticationFailedException ex) {
		// success!
	    } catch (Exception ex) {
		fail(ex.toString());
            } finally {
                t.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
		server.interrupt();
            }
        }
    }

    /**
     * Test using SASL DIGEST-MD5.
     */
    @Test
    public void testSaslSuccess() {
        TestServer server = null;
        try {
            server = new TestServer(new SMTPSaslHandler());
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            properties.setProperty("mail.smtp.sasl.enable", "true");
            properties.setProperty("mail.smtp.sasl.mechanisms", "DIGEST-MD5");
            properties.setProperty("mail.smtp.auth.digest-md5.disable", "true");
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect("test", "test");
		// success!
	    } catch (Exception ex) {
		fail(ex.toString());
            } finally {
                t.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
		server.interrupt();
            }
        }
    }

    /**
     * Test using SASL DIGEST-MD5 with incorrect password.
     */
    @Test
    public void testSaslFailure() {
        TestServer server = null;
        try {
            server = new TestServer(new SMTPSaslHandler());
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            properties.setProperty("mail.smtp.sasl.enable", "true");
            properties.setProperty("mail.smtp.sasl.mechanisms", "DIGEST-MD5");
            properties.setProperty("mail.smtp.auth.digest-md5.disable", "true");
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect("test", "xtest");
		// should have failed
		fail("wrong password succeeded");
	    } catch (AuthenticationFailedException ex) {
		// success!
	    } catch (Exception ex) {
		fail(ex.toString());
            } finally {
                t.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
		server.interrupt();
            }
        }
    }

    /**
     * Test that AUTH with no mechanisms fails.
     */
    @Test
    public void testAuthNoParam() {
        TestServer server = null;
        try {
            server = new TestServer(new SMTPSaslHandler() {
		@Override
		public void ehlo() throws IOException {
		    println("250-hello");
		    println("250 AUTH");
		}
	    });
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect("test", "test");
		fail("Connect didn't fail");
	    } catch (AuthenticationFailedException ex) {
		// success
	    } catch (Exception ex) {
		fail(ex.toString());
            } finally {
                t.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
		server.interrupt();
            }
        }
    }

    /**
     * Test that no AUTH succeeds by skipping authentication entirely.
     */
    @Test
    public void testNoAuth() {
        TestServer server = null;
        try {
            server = new TestServer(new SMTPSaslHandler() {
		@Override
		public void ehlo() throws IOException {
		    println("250-hello");
		    println("250 XXX");
		}
		@Override
		public void auth(String line) throws IOException {
		    println("501 Authentication failed");
		}
	    });
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect("test", "test");
		// success
	    } catch (Exception ex) {
		fail(ex.toString());
            } finally {
                t.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
		server.interrupt();
            }
        }
    }
}
