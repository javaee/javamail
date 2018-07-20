/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.mail;

import java.net.URL;

import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 * Test the URLName class.
 *
 * XXX - for now, just some simple regression tests for reported bugs.
 */
public class URLNameTest {
 
    @Test
    public void testReflexiveEquality() throws Exception {
	URLName u = new URLName("test");
	assertEquals(u, u);	// bug 6365
	u = new URLName("imap://test.com/INBOX");
	assertEquals(u, u);
    }

    /**
     * Test that the getFile method returns the file part *without*
     * the separator character.  This behavior is different than the
     * URL or URI classes but needs to be preserved for compatibility.
     */
    @Test
    public void testFile() throws Exception {
	URLName u = new URLName("http://host/file");
	assertEquals("file", u.getFile());
	u = new URLName("http://host:123/file");
	assertEquals("file", u.getFile());
	u = new URLName("http://host/");
	assertEquals("", u.getFile());
	u = new URLName("http://host");
	assertEquals(null, u.getFile());
	u = new URLName("http://host:123");
	assertEquals(null, u.getFile());
    }

    /**
     * Test that the getURL method returns a URL with the same value
     * as the URLName.
     */
    @Test
    public void testURL() throws Exception {
	// Note: must use a protocol supported by the URL class
	URLName u = new URLName("http://host/file");
	assertEquals("file", u.getFile());
	URL url = u.getURL();
	assertEquals(u.toString(), url.toString());
	u = new URLName("http://host:123/file");
	url = u.getURL();
	assertEquals(u.toString(), url.toString());
	u = new URLName("http://host:123/");
	url = u.getURL();
	assertEquals(u.toString(), url.toString());
	u = new URLName("http://host:123");
	url = u.getURL();
	assertEquals(u.toString(), url.toString());
    }
}
