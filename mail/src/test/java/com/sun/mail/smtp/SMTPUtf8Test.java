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
import java.util.StringTokenizer;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.AuthenticationFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.test.TestServer;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test UTF-8 user names and recipients.
 */
public class SMTPUtf8Test {

    /**
     * Test using UTF-8 user name.
     */
    @Test
    public void testUtf8UserName() {
        TestServer server = null;
	final String user = "test\u03b1";
        try {
            server = new TestServer(new SMTPLoginHandler() {
		@Override
		public void auth(String line) throws IOException {
		    username = user;
		    password = user;
		    super.auth(line);
		}
	    });
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            properties.setProperty("mail.smtp.auth.mechanisms", "LOGIN");
	    properties.setProperty("mail.mime.allowutf8",  "true");
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect(user, user);
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
     * Test using UTF-8 user name but without mail.mime.allowutf8.
     */
    @Test
    public void testUtf8UserNameNoAllowUtf8() {
        TestServer server = null;
	final String user = "test\u03b1";
        try {
            server = new TestServer(new SMTPLoginHandler() {
		@Override
		public void auth(String line) throws IOException {
		    username = user;
		    password = user;
		    super.auth(line);
		}
	    });
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            properties.setProperty("mail.smtp.auth.mechanisms", "LOGIN");
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect(user, user);
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
     * Test using UTF-8 user name and PLAIN but without mail.mime.allowutf8.
     */
    @Test
    public void testUtf8UserNamePlain() {
        TestServer server = null;
	final String user = "test\u03b1";
        try {
            server = new TestServer(new SMTPLoginHandler() {
		@Override
		public void auth(String line) throws IOException {
		    username = user;
		    password = user;
		    super.auth(line);
		}
	    });
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
            properties.setProperty("mail.smtp.auth.mechanisms", "PLAIN");
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
                t.connect(user, user);
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

    private static class Envelope {
	public String from;
	public String to;
    }

    /**
     * Test using UTF-8 From and To address.
     */
    @Test
    public void testUtf8From() {
        TestServer server = null;
	final String test = "test\u03b1";
	final String saddr = test + "@" + test + ".com";
	final Envelope env = new Envelope();
        try {
            server = new TestServer(new SMTPHandler() {
		@Override
		public void ehlo() throws IOException {
		    println("250-hello");
		    println("250-SMTPUTF8");
		    println("250 AUTH PLAIN");
		}

		@Override
		public void mail(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip "MAIL"
		    env.from = st.nextToken().
				    replaceFirst("FROM:<(.*)>", "$1");
		    if (!st.hasMoreTokens() ||
			    !st.nextToken().equals("SMTPUTF8"))
			println("500 fail");
		    else
			ok();
		}

		@Override
		public void rcpt(String line) throws IOException {
		    StringTokenizer st = new StringTokenizer(line);
		    st.nextToken();	// skip "RCPT"
		    env.to = st.nextToken().
				    replaceFirst("TO:<(.*)>", "$1");
		    ok();
		}
	    });
            server.start();

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "localhost");
            properties.setProperty("mail.smtp.port", "" + server.getPort());
	    properties.setProperty("mail.mime.allowutf8",  "true");
            //properties.setProperty("mail.debug.auth", "true");
            Session session = Session.getInstance(properties);
            //session.setDebug(true);

            Transport t = session.getTransport("smtp");
            try {
		MimeMessage msg = new MimeMessage(session);
		InternetAddress addr = new InternetAddress(saddr, test);
		msg.setFrom(addr);
		msg.setRecipient(Message.RecipientType.TO, addr);
		msg.setSubject(test);
		msg.setText(test + "\n");
                t.connect("test", "test");
		t.sendMessage(msg, msg.getAllRecipients());
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
	// after we're sure the server is done
	assertEquals(saddr, env.from);
	assertEquals(saddr, env.to);
    }
}
