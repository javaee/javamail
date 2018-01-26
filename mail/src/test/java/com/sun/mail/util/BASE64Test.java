/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.util;

import java.io.*;
import java.util.*;
import javax.mail.*;

import org.junit.Test;
import org.junit.Assert;

/**
 * Test base64 encoding/decoding.
 *
 * @author Bill Shannon
 */

public class BASE64Test {

    @Test
    public void test() throws IOException {
	// test a range of buffer sizes
	for (int bufsize = 1; bufsize < 100; bufsize++) {
	    //System.out.println("Buffer size: " + bufsize);
	    byte[] buf = new byte[bufsize];

	    // test a set of patterns

	    // first, all zeroes
	    Arrays.fill(buf, (byte)0);
	    test("Zeroes", buf);

	    // now, all ones
	    Arrays.fill(buf, (byte)0xff);
	    test("Ones", buf);

	    // now, small integers
	    for (int i = 0; i < bufsize; i++)
		buf[i] = (byte)i;
	    test("Ints", buf);

	    // finally, random numbers
	    Random rnd = new Random();
	    rnd.nextBytes(buf);
	    test("Random", buf);
	}
    }

    /**
     * Encode and decode the buffer and check that we get back the
     * same data.  Encoding is done both with the static encode
     * method and using the encoding stream.  Likewise, decoding
     * is done both with the static decode method and using the
     * decoding stream.  Check all combinations.
     */
    private static void test(String name, byte[] buf) throws IOException {
	// first encode and decode with method
	byte[] encoded = BASE64EncoderStream.encode(buf);
	byte[] nbuf = BASE64DecoderStream.decode(encoded);
	compare(name, "method", buf, nbuf);

	// encode with stream, compare with method encoded version
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	BASE64EncoderStream os =
	    new BASE64EncoderStream(bos, Integer.MAX_VALUE);
	os.write(buf);
	os.flush();
	os.close();
	byte[] sbuf = bos.toByteArray();
	compare(name, "encoded", encoded, sbuf);

	// encode with stream, decode with method
	nbuf = BASE64DecoderStream.decode(sbuf);
	compare(name, "stream->method", buf, nbuf);

	// encode with stream, decode with stream
	ByteArrayInputStream bin = new ByteArrayInputStream(sbuf);
	BASE64DecoderStream in = new BASE64DecoderStream(bin);
	readAll(in, nbuf, nbuf.length);
	compare(name, "stream", buf, nbuf);

	// encode with method, decode with stream
	for (int i = 1; i <= nbuf.length; i++) {
	    bin = new ByteArrayInputStream(encoded);
	    in = new BASE64DecoderStream(bin);
	    readAll(in, nbuf, i);
	    compare(name, "method->stream " + i, buf, nbuf);
	}

	// encode with stream, decode with stream, many buffers

	// first, fill the output with multiple buffers, up to the limit
	int limit = 10000;		// more than 8K
	bos = new ByteArrayOutputStream();
	os = new BASE64EncoderStream(bos);
	for (int size = 0, blen = buf.length; size < limit; size += blen) {
	    if (size + blen > limit) {
		blen = limit - size;
		// write out partial buffer, starting at non-zero offset
		os.write(buf, buf.length - blen, blen);
	    } else
		os.write(buf);
	}
	os.flush();
	os.close();

	// read the encoded output and check the line length
	String type = "big stream";		// for error messages below
	sbuf = bos.toByteArray();
	bin = new ByteArrayInputStream(sbuf);
	byte[] inbuf = new byte[78];
	for (int size = 0, blen = 76; size < limit; size += blen) {
	    if (size + blen > limit) {
		blen = limit - size;
		int n = bin.read(inbuf, 0, blen);
		Assert.assertEquals(name + ": " + type +
		    " read wrong size at offset " + (size + blen), blen, n);
	    } else {
		int n = bin.read(inbuf, 0, blen + 2);
		Assert.assertEquals(name + ": " + type +
		    " read wrong size at offset " + (size + blen), blen + 2, n);
		Assert.assertTrue(name + ": " + type +
		    " no CRLF: at offset " + (size + blen),
		    inbuf[blen] == (byte)'\r' && inbuf[blen+1] == (byte)'\n');
	    }
	}

	// decode the output and check the data
	bin = new ByteArrayInputStream(sbuf);
	in = new BASE64DecoderStream(bin);
	inbuf = new byte[buf.length];
	for (int size = 0, blen = buf.length; size < limit; size += blen) {
	    if (size + blen > limit)
		blen = limit - size;
	    int n = in.read(nbuf, 0, blen);
	    Assert.assertEquals(name + ": " + type +
		" read decoded wrong size at offset " + (size + blen), blen, n);
	    if (blen != buf.length) {
		// have to compare with end of original buffer
		byte[] cbuf = new byte[blen];
		System.arraycopy(buf, buf.length - blen, cbuf, 0, blen);
		// need a version of the read buffer that's the right size
		byte[] cnbuf = new byte[blen];
		System.arraycopy(nbuf, 0, cnbuf, 0, blen);
		compare(name, type, cbuf, cnbuf);
	    } else {
		compare(name, type, buf, nbuf);
	    }
	}
    }

    private static byte[] origLine;
    private static byte[] encodedLine;
    static {
	try {
	    origLine =
		"000000000000000000000000000000000000000000000000000000000".
		    getBytes("us-ascii");
	    encodedLine =
		("MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAw" +
		"MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAw" + "\r\n").
		    getBytes("us-ascii");
	} catch (UnsupportedEncodingException uex) {
	    // should never happen;
	}
    }

    /**
     * Test that CRLF is inserted at the right place.
     * Test combinations of array writes of different sizes
     * and single byte writes.
     */
    @Test
    public void testLineLength() throws Exception {
	for (int i = 0; i < origLine.length; i++) {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    OutputStream os = new BASE64EncoderStream(bos);
	    os.write(origLine, 0, i);
	    os.write(origLine, i, origLine.length - i);
	    os.write((byte)'0');
	    os.flush();
	    os.close();

	    byte[] line = new byte[encodedLine.length];
	    System.arraycopy(bos.toByteArray(), 0, line, 0, line.length);
	    Assert.assertArrayEquals("encoded line " + i, encodedLine, line);
	}

	for (int i = 0; i < origLine.length; i++) {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    OutputStream os = new BASE64EncoderStream(bos);
	    os.write(origLine, 0, i);
	    os.write(origLine, i, origLine.length - i);
	    os.write(origLine);
	    os.flush();
	    os.close();

	    byte[] line = new byte[encodedLine.length];
	    System.arraycopy(bos.toByteArray(), 0, line, 0, line.length);
	    Assert.assertArrayEquals("all arrays, encoded line " + i,
					encodedLine, line);
	}

	for (int i = 1; i < 5; i++) {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    OutputStream os = new BASE64EncoderStream(bos);
	    for (int j = 0; j < i; j++)
		os.write((byte)'0');
	    os.write(origLine, i, origLine.length - i);
	    os.write((byte)'0');
	    os.flush();
	    os.close();

	    byte[] line = new byte[encodedLine.length];
	    System.arraycopy(bos.toByteArray(), 0, line, 0, line.length);
	    Assert.assertArrayEquals("single byte first encoded line " + i,
					encodedLine, line);
	}
	for (int i = origLine.length - 5; i < origLine.length; i++) {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    OutputStream os = new BASE64EncoderStream(bos);
	    os.write(origLine, 0, i);
	    for (int j = 0; j < origLine.length - i; j++)
		os.write((byte)'0');
	    os.write((byte)'0');
	    os.flush();
	    os.close();

	    byte[] line = new byte[encodedLine.length];
	    System.arraycopy(bos.toByteArray(), 0, line, 0, line.length);
	    Assert.assertArrayEquals("single byte last encoded line " + i,
					encodedLine, line);
	}
    }

    /**
     * Fill the buffer from the stream.
     */
    private static void readAll(InputStream in, byte[] buf, int readsize)
				throws IOException {
	int need = buf.length;
	int off = 0; 
	int got;
	while (need > 0) {
	    got = in.read(buf, off, need > readsize ? readsize : need);
	    if (got <= 0)
		break;
	    off += got;
	    need -= got;
	}
	if (need != 0)
	    System.out.println("couldn't read all bytes");
    }

    /**
     * Compare the two buffers.
     */
    private static void compare(String name, String type,
				byte[] buf, byte[] nbuf) {
	/*
	if (nbuf.length != buf.length) {
	    System.out.println(name + ": " + type +
		" decoded array size wrong: " +
		"got " + nbuf.length + ", expected " + buf.length);
	    dump(name + " buf", buf);
	    dump(name + " nbuf", nbuf);
	}
	*/
	Assert.assertEquals(name + ": " + type + " decoded array size wrong",
			    buf.length, nbuf.length);
	for (int i = 0; i < buf.length; i++) {
	    Assert.assertEquals(name + ": " + type + " data wrong: index " + i,
		buf[i], nbuf[i]);
	}
    }

    /**
     * Dump the contents of the buffer.
     */
    private static void dump(String name, byte[] buf) {
	System.out.println(name);
	for (int i = 0; i < buf.length; i++)
	    System.out.println(buf[i]);
    }
}
