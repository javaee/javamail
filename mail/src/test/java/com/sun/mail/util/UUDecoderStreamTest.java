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

package com.sun.mail.util;

import java.io.*;
import java.util.*;
import com.sun.mail.util.UUDecoderStream;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test uudecoder.
 *
 * @author Bill Shannon
 */

@RunWith(Parameterized.class)
public class UUDecoderStreamTest {
    private TestData data;

    private static boolean gen_test_input = false;	// output good
    private static int errors = 0;		// number of errors detected

    private static boolean junit;

    static class TestData {
	public String name;
	public boolean ignoreErrors;
	public boolean ignoreMissingBeginEnd;
	public byte[] input;
	public byte[] expectedOutput;
	public String expectedException;
    }

    public UUDecoderStreamTest(TestData t) {
	data = t;
    }

    @Test
    public void testData() {
	test(data);
    }

    @Parameters
    public static Collection<TestData[]> data() throws Exception {
	junit = true;
	// XXX - gratuitous array requirement
	List<TestData[]> testData = new ArrayList<>();
	BufferedReader in = new BufferedReader(new InputStreamReader(
	    UUDecoderStreamTest.class.getResourceAsStream("uudata")));
	TestData t;
	while ((t = parse(in)) != null)
	    testData.add(new TestData[] { t });
	return testData;
    }

    public static void main(String argv[]) throws Exception {
	int optind;
	// XXX - all options currently ignored
	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-")) {
		// ignore
	    } else if (argv[optind].equals("-g")) {
		gen_test_input = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
		    "Usage: uutest [-g] [-]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	// read from stdin
	BufferedReader in =
	    new BufferedReader(new InputStreamReader(System.in));

	TestData t;
	while ((t = parse(in)) != null)
	    test(t);
	System.exit(errors);
    }

    /*
     * Parse the input, returning a test case.
     */
    public static TestData parse(BufferedReader in) throws Exception {

	String line = null;
	for (;;) {
	    line = in.readLine();
	    if (line == null)
		return null;
	    if (line.length() == 0 || line.startsWith("#"))
		continue;

	    if (!line.startsWith("TEST"))
		throw new Exception("Bad test data format");
	    break;
	}

	TestData t = new TestData();
	int i = line.indexOf(' ');	// XXX - crude
	t.name = line.substring(i + 1);

	line = in.readLine();
	StringTokenizer st = new StringTokenizer(line);
	String tok = st.nextToken();
	if (!tok.equals("DATA"))
	    throw new Exception("Bad test data format: " + line);
	while (st.hasMoreTokens()) {
	    tok = st.nextToken();
	    if (tok.equals("ignoreErrors"))
		t.ignoreErrors = true;
	    else if (tok.equals("ignoreMissingBeginEnd"))
		t.ignoreMissingBeginEnd = true;
	    else
		throw new Exception("Bad DATA option in line: " + line);
	}

	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	Writer os = new OutputStreamWriter(bos, "us-ascii");
	for (;;) {
	    line = in.readLine();
	    if (line.equals("EXPECT"))
		break;
	    os.write(line);
	    os.write("\n");
	}
	os.close();
	t.input = bos.toByteArray();

	bos = new ByteArrayOutputStream();
	os = new OutputStreamWriter(bos, "us-ascii");
	for (;;) {
	    line = in.readLine();
	    if (line.startsWith("EXCEPTION")) {
		i = line.indexOf(' ');	// XXX - crude
		t.expectedException = line.substring(i + 1);
	    } else if (line.equals("END"))
		break;
	    os.write(line);
	    os.write("\n");
	}
	os.close();
	if (t.expectedException == null)
	    t.expectedOutput = bos.toByteArray();

	return t;
    }

    /**
     * Test the data in the test case.
     */
    public static void test(TestData t) {
	InputStream in =
	    new UUDecoderStream(new ByteArrayInputStream(t.input),
				t.ignoreErrors, t.ignoreMissingBeginEnd);

	// two cases - either we're expecting an exception or we're not
	if (t.expectedException != null) {
	    try {
		int c;
		while ((c = in.read()) >= 0)
		    ;	// throw it away
		// read all the data with no exception - fail
		if (junit)
		    Assert.fail("Didn't get expected exception: " +
				    t.expectedException);
		System.out.println("Test: " + t.name);
		System.out.println("Got no Exception");
		System.out.println("Expected Exception: " +
				    t.expectedException);
		errors++;
	    } catch (Exception ex) {
		if (junit)
		    Assert.assertEquals("For expected exception",
			ex.getClass().getName(), t.expectedException);
		if (!ex.getClass().getName().equals(t.expectedException)) {
		    System.out.println("Test: " + t.name);
		    System.out.println("Got Exception: " + ex);
		    System.out.println("Expected Exception: " +
					t.expectedException);
		    errors++;
		}
	    } finally {
		try {
		    in.close();
		} catch (IOException ioex) { }
	    }
	} else {
	    InputStream ein = new ByteArrayInputStream(t.expectedOutput);
	    try {
		int c, ec;
		boolean gotError = false;
		while ((c = in.read()) >= 0) {
		    ec = ein.read();
		    if (junit)
			Assert.assertFalse("For expected EOF, got char " + c,
					    ec < 0);
		    if (ec < 0) {
			System.out.println("Test: " + t.name);
			System.out.println("Got char: " + c);
			System.out.println("Expected EOF");
			errors++;
			gotError = true;
			break;
		    }
		    if (junit)
			Assert.assertEquals("For expected char " + ec +
					    ", got char " + c, ec, c);
		    if (c != ec) {
			System.out.println("Test: " + t.name);
			System.out.println("Got char: " + c);
			System.out.println("Expected char: " + ec);
			errors++;
			gotError = true;
			break;
		    }
		}
		if (!gotError) {
		    ec = ein.read();
		    if (junit)
			Assert.assertFalse("For expected char " + ec +
					    ", got EOF", ec >= 0);
		    if (ec >= 0) {
			System.out.println("Test: " + t.name);
			System.out.println("Got EOF");
			System.out.println("Expected char: " + ec);
			errors++;
		    }
		}
	    } catch (Exception ex) {
		if (junit)
		    Assert.fail("Got exception: " + ex);
		System.out.println("Test: " + t.name);
		System.out.println("Got Exception: " + ex);
		System.out.println("Expected no Exception");
		errors++;
	    } finally {
		try {
		    in.close();
		} catch (IOException ioex) { }
		try {
		    ein.close();
		} catch (IOException ioex) { }
	    }
	}
    }
}
