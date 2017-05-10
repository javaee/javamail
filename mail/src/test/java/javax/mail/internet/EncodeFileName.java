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

package javax.mail.internet;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.junit.*;
import static org.junit.Assert.assertTrue;

/**
 * Test "mail.mime.encodefilename" System property set.
 */
public class EncodeFileName extends NoEncodeFileName {
 
    // depends on exactly how MimeUtility.encodeText splits long words
    private static String expected1 =
	"=?utf-8?B?w4DDgcOFw4bDgMOBw4XDhsOHw4jDicOKw4vDjMONw47Dj8OQw4DDgcOF?=";
    private static String expected2 =
	"=?utf-8?B?w4bDh8OIw4nDisOLw4zDjcOOw4/DkMORw5LDk8OUw5XDlsOYw5nDmsObw5w=?=";
    private static String expected3 =
	"=?utf-8?B?w53DnsOfw6DDocOiw6PDpMOlw6bDp8Oow6nDqsOrw6zDrcOuw6/DsMOx?=";
    private static String expected4 =
	"=?utf-8?B?w7LDs8O0w7XDtsO4w7nDusO7w7zDvcO+w7/DgMOBw4XDhsOHLmRvYw==?=";

    @BeforeClass
    public static void before() {
	System.out.println("EncodeFileName");
	System.setProperty("mail.mime.charset", "utf-8");
	System.setProperty("mail.mime.encodefilename", "true");
	// assume mail.mime.encodeparamters defaults to true
	System.clearProperty("mail.mime.encodeparamters");
    }

    @Test
    @Override
    public void test() throws Exception {
	MimeBodyPart mbp = new MimeBodyPart();
	mbp.setText("test");
	mbp.setFileName(fileName);
	mbp.updateHeaders();
	String h = mbp.getHeader("Content-Type", "");
	assertTrue(h.contains("name="));
	assertTrue(h.contains(expected1));
	assertTrue(h.contains(expected2));
	assertTrue(h.contains(expected3));
	assertTrue(h.contains(expected4));
	h = mbp.getHeader("Content-Disposition", "");
	assertTrue(h.contains("filename="));
	assertTrue(h.contains(expected1));
	assertTrue(h.contains(expected2));
	assertTrue(h.contains(expected3));
	assertTrue(h.contains(expected4));
    }
}
