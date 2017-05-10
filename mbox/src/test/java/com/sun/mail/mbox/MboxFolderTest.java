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
import java.io.PrintWriter;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test of mbox folders.
 */
public final class MboxFolderTest {

    @BeforeClass
    public static void before() {
	System.setProperty("mail.mbox.locktype", "none");
    }

    @AfterClass
    public static void after() {
	System.getProperties().remove("mail.mbox.locktype");
    }

    /**
     * Test that a mailbox that has garbage at the beginning
     * (such as a gratuitous blank line) is handled without
     * crashing and without corrupting the mailbox.
     */
    @Test
    public void testGarbageAtStartOfFolder() throws Exception {
	Folder f = null;
	try {
	    File temp = File.createTempFile("mbox", ".mbx");
	    temp.deleteOnExit();
	    PrintWriter pw = new PrintWriter(temp);
	    pw.println();
	    pw.println("From - Tue Aug 23 11:56:51 2011");
	    pw.println();
	    pw.println("test");
	    pw.println();
	    pw.close();
	    long size = temp.length();

	    Properties properties = new Properties();
	    Session session = Session.getInstance(properties);
	    Store store = session.getStore("mbox");
	    store.connect();
	    f = store.getFolder(temp.getAbsolutePath());
	    f.open(Folder.READ_WRITE);
	    assertEquals(0, f.getMessageCount());
	    f.close(true);
	    assertEquals(size, temp.length());
	} catch (Exception ex) {
	    System.out.println(ex);
	    //ex.printStackTrace();
	    fail(ex.toString());
	} finally {
	    if (f != null) {
		f.delete(false);
		f.getStore().close();
	    }
	}
    }
}
