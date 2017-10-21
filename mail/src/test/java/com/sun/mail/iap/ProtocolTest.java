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

package com.sun.mail.iap;

import java.io.*;
import java.util.Properties;

import com.sun.mail.test.NullOutputStream;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test the Protocol class.
 */
public final class ProtocolTest {

    private static final byte[] noBytes = new byte[0];
    private static final PrintStream nullps =
				    new PrintStream(new NullOutputStream());
    private static final ByteArrayInputStream nullis =
				    new ByteArrayInputStream(noBytes);

    /**
     * Test that the tag prefix is computed properly.
     */
    @Test
    public void testTagPrefix() throws IOException, ProtocolException {
	Protocol.tagNum.set(0);		// reset for testing
	String tag = newProtocolTag();
	assertEquals("A0", tag);
	for (int i = 1; i < 26; i++)
	    tag = newProtocolTag();
	assertEquals("Z0", tag);
	tag = newProtocolTag();
	assertEquals("AA0", tag);
	for (int i = 26 + 1; i < (26*26 + 26); i++)
	    tag = newProtocolTag();
	assertEquals("ZZ0", tag);
	tag = newProtocolTag();
	assertEquals("AAA0", tag);
	for (int i = 26*26 + 26 + 1; i < (26*26*26 + 26*26 + 26); i++)
	    tag = newProtocolTag();
	assertEquals("ZZZ0", tag);
	tag = newProtocolTag();
	// did it wrap around?
	assertEquals("A0", tag);
    }

    private String newProtocolTag() throws IOException, ProtocolException {
	Properties props = new Properties();
	Protocol p = new Protocol(nullis, nullps, props, false);
	String tag = p.writeCommand("CMD", null);
	return tag;
    }

    /**
     * Test that the tag prefix is reused.
     */
    @Test
    public void testTagPrefixReuse() throws IOException, ProtocolException {
	Properties props = new Properties();
	props.setProperty("mail.imap.reusetagprefix", "true");
	Protocol p = new Protocol(nullis, nullps, props, false);
	String tag = p.writeCommand("CMD", null);
	assertEquals("A0", tag);
	p = new Protocol(nullis, nullps, props, false);
	tag = p.writeCommand("CMD", null);
	assertEquals("A0", tag);
    }
}
