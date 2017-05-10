/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test the Status class.
 */
public class StatusTest {
    /**
     * Test that the returned mailbox name is decoded.
     */
    @Test
    public void testMailboxDecode() throws Exception {
	String mbox = "Entw\u00fcrfe";
	IMAPResponse response = new IMAPResponse(
	    "* STATUS " +
	    BASE64MailboxEncoder.encode(mbox) +
	    " (MESSAGES 231 UIDNEXT 44292)", false);
	Status s = new Status(response);
	assertEquals(mbox, s.mbox);
	assertEquals(231, s.total);
	assertEquals(44292, s.uidnext);
    }

    /**
     * Test that the returned mailbox name is correct when using UTF-8.
     */
    @Test
    public void testMailboxUtf8() throws Exception {
	String mbox = "Entw\u00fcrfe";
	IMAPResponse response = new IMAPResponse(
	    "* STATUS " +
	    mbox +
	    " (MESSAGES 231 UIDNEXT 44292)", true);
	Status s = new Status(response);
	assertEquals(mbox, s.mbox);
	assertEquals(231, s.total);
	assertEquals(44292, s.uidnext);
    }

    /**
     * Test that spaces in the response don't confuse it.
     */
    @Test
    public void testSpaces() throws Exception {
	IMAPResponse response = new IMAPResponse(
	    "* STATUS  test  ( MESSAGES  231  UIDNEXT  44292 )");
	Status s = new Status(response);
	assertEquals("test", s.mbox);
	assertEquals(231, s.total);
	assertEquals(44292, s.uidnext);
    }

    /**
     * Test that a bad response throws a ParsingException
     */
    @Test(expected = ParsingException.class)
    public void testBadResponseNoAttrList() throws Exception {
	String mbox = "test";
	IMAPResponse response = new IMAPResponse("* STATUS test ");
	Status s = new Status(response);
    }

    /**
     * Test that a bad response throws a ParsingException
     */
    @Test(expected = ParsingException.class)
    public void testBadResponseNoAttrs() throws Exception {
	String mbox = "test";
	IMAPResponse response = new IMAPResponse("* STATUS test (");
	Status s = new Status(response);
    }
}
