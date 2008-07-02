/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

//package javax.mail.internet.tests;

import java.io.*;
import com.sun.mail.util.UUDecoderStream;

/**
 * Test uudecoder.
 *
 * @author Bill Shannon
 */

public class uutest {
    static boolean gen_test_input = false;	// output good
    static int errors = 0;			// number of errors detected

    static class TestCase {
	public String name;
	public boolean ignoreErrors;
	public byte[] input;
	public byte[] expectedOutput;
	public String expectedException;
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

	TestCase t;
	while ((t = parse(in)) != null)
	    test(t);
	System.exit(errors);
    }

    /*
     * Parse the input, returning a test case.
     */
    public static TestCase parse(BufferedReader in) throws Exception {

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

	TestCase t = new TestCase();
	int i = line.indexOf(' ');	// XXX - crude
	t.name = line.substring(i + 1);

	line = in.readLine();
	if (!line.startsWith("DATA"))
	    throw new Exception("Bad test data format");
	if (line.length() > 4)		// XXX - crude
	    t.ignoreErrors = true;

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
     * Test that data in the test case.
     */
    public static void test(TestCase t) throws Exception {
	InputStream in =
	    new UUDecoderStream(new ByteArrayInputStream(t.input),
				t.ignoreErrors);

	// two cases - either we're expecting an exception or we're not
	if (t.expectedException != null) {
	    try {
		int c;
		while ((c = in.read()) >= 0)
		    ;	// throw it away
		// read all the data with no exception - fail
		System.out.println("Test: " + t.name);
		System.out.println("Got no Exception");
		System.out.println("Expected Exception: " +
				    t.expectedException);
		errors++;
	    } catch (Exception ex) {
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
		    if (ec < 0) {
			System.out.println("Test: " + t.name);
			System.out.println("Got char: " + c);
			System.out.println("Expected EOF");
			errors++;
			gotError = true;
			break;
		    }
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
		    if (ec >= 0) {
			System.out.println("Test: " + t.name);
			System.out.println("Got EOF");
			System.out.println("Expected char: " + ec);
			errors++;
		    }
		}
	    } catch (Exception ex) {
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
