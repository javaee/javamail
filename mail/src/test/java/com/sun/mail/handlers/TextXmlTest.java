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

package com.sun.mail.handlers;

import java.io.*;
import java.util.*;
import java.awt.datatransfer.DataFlavor;
import javax.activation.*;
import javax.mail.*;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.transform.stream.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the text/xml DataContentHandler.
 *
 * XXX - should test other Source objects in addition to StreamSource.
 *
 * @author Bill Shannon
 */

public class TextXmlTest {

    private static String xml = "<test><foo>bar</foo></test>\n";
    private static byte[] xmlBytes = xml.getBytes();

    // test InputStream to String
    @Test
    public void testStreamToStringTextXml() throws Exception {
	testStreamToString("text/xml");
    }

    // test InputStream to String
    @Test
    public void testStreamToStringApplicationXml() throws Exception {
	testStreamToString("application/xml");
    }

    private static void testStreamToString(String mimeType) throws Exception {
	DataContentHandler dch = new text_xml();
	DataFlavor df = new ActivationDataFlavor(String.class, mimeType, "XML");
	DataSource ds = new ByteArrayDataSource(xmlBytes, mimeType);
	Object content = dch.getContent(ds);
	assertEquals(String.class, content.getClass());
	assertEquals(xml, (String)content);
	content = dch.getTransferData(df, ds);
	assertEquals(String.class, content.getClass());
	assertEquals(xml, (String)content);
    }

    // test InputStream to StreamSource
    @Test
    public void testStreamToSource() throws Exception {
	DataContentHandler dch = new text_xml();
	DataFlavor df = new ActivationDataFlavor(StreamSource.class,
						    "text/xml", "XML stream");
	DataSource ds = new ByteArrayDataSource(xmlBytes, "text/xml");
	Object content = dch.getTransferData(df, ds);
	assertEquals(StreamSource.class, content.getClass());
	String sc = streamToString(((StreamSource)content).getInputStream());
	assertEquals(xml, sc);
    }

    // test String to OutputStream
    @Test
    public void testStringToStream() throws Exception {
	DataContentHandler dch = new text_xml();
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	dch.writeTo(xml, "text/xml", bos);
	String sc = new String(bos.toByteArray(), "us-ascii");
	assertEquals(xml, sc);
    }

    // test StreamSource to OutputStream
    @Test
    public void testSourceToStream() throws Exception {
	DataContentHandler dch = new text_xml();
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	StreamSource ss = new StreamSource(new ByteArrayInputStream(xmlBytes));
	dch.writeTo(ss, "text/xml", bos);
	String sc = new String(bos.toByteArray(), "us-ascii");
	// transformer adds an <?xml> header, so can't check for exact match
	assertTrue(sc.indexOf(xml.trim()) >= 0);
    }

    /**
     * Read a stream into a String.
     */
    private static String streamToString(InputStream is) {
	try {
	    StringBuilder sb = new StringBuilder();
	    int c;
	    while ((c = is.read()) > 0)
		sb.append((char)c);
	    return sb.toString();
	} catch (IOException ex) {
	    return "";
	} finally {
	    try {
		is.close();
	    } catch (IOException cex) { }
	}
    }
}
