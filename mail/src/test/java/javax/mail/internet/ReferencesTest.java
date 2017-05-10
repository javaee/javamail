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

package javax.mail.internet;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 * Test setting of the References header.
 *
 * @author Bill Shannon
 */
public class ReferencesTest {
    private static Session session = Session.getInstance(new Properties());

    /*
     * Test cases:
     * 
     * Message-Id	References	In-Reply-To	Expected Result
     */

    @Test
    public void test1() throws MessagingException {
	test(null,	null,		null,		null);
    }

    @Test
    public void test2() throws MessagingException {
	test(null,	null,		"<1@a>",	"<1@a>");
    }

    @Test
    public void test3() throws MessagingException {
	test(null,	"<2@b>",	null,		"<2@b>");
    }

    @Test
    public void test4() throws MessagingException {
	test(null,	"<2@b>",	"<1@a>",	"<2@b>");
    }

    @Test
    public void test5() throws MessagingException {
	test("<3@c>",	null,		null,		"<3@c>");
    }

    @Test
    public void test6() throws MessagingException {
	test("<3@c>",	null,		"<1@a>",	"<1@a> <3@c>");
    }

    @Test
    public void test7() throws MessagingException {
	test("<3@c>",	"<2@b>",	null,		"<2@b> <3@c>");
    }

    @Test
    public void test8() throws MessagingException {
	test("<3@c>",	"<2@b>",	"<1@a>",	"<2@b> <3@c>");
    }

    private static void test(String msgid, String ref, String irt, String res)
				throws MessagingException {
	MimeMessage msg = new MimeMessage(session);
	msg.setFrom();
	msg.setRecipients(Message.RecipientType.TO, "you@example.com");
	msg.setSubject("test");
	if (msgid != null)
	    msg.setHeader("Message-Id", msgid);
	if (ref != null)
	    msg.setHeader("References", ref);
	if (irt != null)
	    msg.setHeader("In-Reply-To", irt);
	msg.setText("text");

	MimeMessage reply = (MimeMessage)msg.reply(false);
	String rref = reply.getHeader("References", " ");

	assertEquals(res, rref);
    }
}
