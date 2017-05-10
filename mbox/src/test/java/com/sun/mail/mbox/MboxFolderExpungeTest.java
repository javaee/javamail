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

package com.sun.mail.mbox;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Date;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test expunge of mbox folders.
 */
public final class MboxFolderExpungeTest {

    @BeforeClass
    public static void before() {
	System.setProperty("mail.mbox.locktype", "none");
    }

    @AfterClass
    public static void after() {
	System.getProperties().remove("mail.mbox.locktype");
    }

    @Test
    public void testRemoveFirst() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message m = f.getMessage(1);
	    m.setFlag(Flags.Flag.DELETED, true);
	    f.expunge();
	    m = f.getMessage(1);
	    assertEquals("2", ((String)m.getContent()).trim());
	    m.setFlag(Flags.Flag.DELETED, true);
	    m = f.getMessage(2);
	    assertEquals("3", ((String)m.getContent()).trim());
	    f.expunge();
	    m = f.getMessage(1);
	    assertEquals("3", ((String)m.getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testRemoveMiddle() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message m = f.getMessage(2);
	    m.setFlag(Flags.Flag.DELETED, true);
	    f.expunge();
	    m = f.getMessage(1);
	    assertEquals("1", ((String)m.getContent()).trim());
	    m = f.getMessage(2);
	    assertEquals("3", ((String)m.getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testRemoveLast() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message m = f.getMessage(3);
	    m.setFlag(Flags.Flag.DELETED, true);
	    f.expunge();
	    m = f.getMessage(1);
	    assertEquals("1", ((String)m.getContent()).trim());
	    m = f.getMessage(2);
	    assertEquals("2", ((String)m.getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testRemoveFirstClose() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message m = f.getMessage(1);
	    m.setFlag(Flags.Flag.DELETED, true);
	    f.close(true);
	    f.open(Folder.READ_WRITE);
	    m = f.getMessage(1);
	    assertEquals("2", ((String)m.getContent()).trim());
	    m.setFlag(Flags.Flag.DELETED, true);
	    m = f.getMessage(2);
	    assertEquals("3", ((String)m.getContent()).trim());
	    f.close(true);
	    f.open(Folder.READ_WRITE);
	    m = f.getMessage(1);
	    assertEquals("3", ((String)m.getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testRemoveMiddleClose() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message m = f.getMessage(2);
	    m.setFlag(Flags.Flag.DELETED, true);
	    f.close(true);
	    f.open(Folder.READ_WRITE);
	    m = f.getMessage(1);
	    assertEquals("1", ((String)m.getContent()).trim());
	    m = f.getMessage(2);
	    assertEquals("3", ((String)m.getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testRemoveLastClose() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message m = f.getMessage(3);
	    m.setFlag(Flags.Flag.DELETED, true);
	    f.close(true);
	    f.open(Folder.READ_WRITE);
	    m = f.getMessage(1);
	    assertEquals("1", ((String)m.getContent()).trim());
	    m = f.getMessage(2);
	    assertEquals("2", ((String)m.getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testRemoveFirstMessages() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message[] msgs = f.getMessages(1, 3);
	    msgs[0].setFlag(Flags.Flag.DELETED, true);
	    f.expunge();
	    assertEquals("2", ((String)msgs[1].getContent()).trim());
	    assertEquals("3", ((String)msgs[2].getContent()).trim());
	    msgs[1].setFlag(Flags.Flag.DELETED, true);
	    f.expunge();
	    assertEquals("3", ((String)msgs[2].getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testRemoveMiddleMessages() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message[] msgs = f.getMessages(1, 3);
	    msgs[1].setFlag(Flags.Flag.DELETED, true);
	    f.expunge();
	    assertEquals("1", ((String)msgs[0].getContent()).trim());
	    assertEquals("3", ((String)msgs[2].getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testRemoveLastMessages() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message[] msgs = f.getMessages(1, 3);
	    msgs[2].setFlag(Flags.Flag.DELETED, true);
	    f.expunge();
	    assertEquals("1", ((String)msgs[0].getContent()).trim());
	    assertEquals("2", ((String)msgs[1].getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testNewMessagesAfterExpunge() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message[] msgs = f.getMessages(1, 3);
	    msgs[0].setFlag(Flags.Flag.DELETED, true);
	    f.expunge();
	    f.appendMessages(new Message[] { createMessage(null, 4) });
	    assertEquals(3, f.getMessageCount());
	    Message m = f.getMessage(3);
	    assertEquals("4", ((String)m.getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    @Test
    public void testNewMessagesAfterClose() throws Exception {
	Folder f = createTestFolder();
	try {
	    f.open(Folder.READ_WRITE);
	    Message[] msgs = f.getMessages(1, 3);
	    msgs[0].setFlag(Flags.Flag.DELETED, true);
	    f.close(true);
	    f.appendMessages(new Message[] { createMessage(null, 4) });
	    f.open(Folder.READ_WRITE);
	    assertEquals(3, f.getMessageCount());
	    Message m = f.getMessage(3);
	    assertEquals("4", ((String)m.getContent()).trim());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    f.close(false);
	    f.delete(false);
	    f.getStore().close();
	}
    }

    /**
     * Create a temp file to use as a test folder and populate it
     * with 3 messages.
     */
    private Folder createTestFolder() {
	Properties properties = new Properties();
	Session session = Session.getInstance(properties);
	//session.setDebug(true);

	Folder folder = null;
	try {
	    Store store = session.getStore("mbox");
	    File temp = File.createTempFile("mbox", ".mbx");
	    temp.deleteOnExit();
	    store.connect();
	    folder = store.getFolder(temp.getAbsolutePath());
	    folder.create(Folder.HOLDS_MESSAGES);
	    Message[] msgs = new Message[3];
	    for (int i = 0; i < 3; i++)
		msgs[i] = createMessage(session, i + 1);
	    folder.appendMessages(msgs);
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	}
	return folder;
    }

    /**
     * Create a test message.
     */
    private Message createMessage(Session session, int msgno)
				throws MessagingException {
	MimeMessage msg = new MimeMessage(session);
	msg.setFrom("test@example.com");
	msg.setSentDate(new Date());
	String subject = "test ";
	// ensure each message is a different length
	for (int i = 0; i < msgno; i++)
	    subject += "test ";
	msg.setSubject(subject + msgno);
	msg.setText(msgno + "\n");
	msg.saveChanges();
	return msg;
    }
}
