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

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Internet address parsing.
 *
 * @author Bill Shannon
 */

@RunWith(Parameterized.class)
public class InternetAddressTest {
    private String headerName;
    private String headerValue;
    private String[] expected;
    private boolean doStrict;
    private boolean doParseHeader;

    static boolean strict = false;		// enforce strict RFC822 syntax
    static boolean gen_test_input = false;	// output good for input to -p
    static boolean parse_mail = false;		// parse input in mail format
    static boolean parse_header = false;	// use parseHeader method?
    static boolean verbose;			// print progress?
    static int errors = 0;			// number of errors detected

    static boolean junit;
    static List<Object[]> testData;

    public InternetAddressTest(String headerName, String headerValue,
	    String[] expected, boolean doStrict, boolean doParseHeader) {
	this.headerName = headerName;
	this.headerValue = headerValue;
	this.expected = expected;
	this.doStrict = doStrict;
	this.doParseHeader = doParseHeader;
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException {
	junit = true;
	testData = new ArrayList<>();
	parse(new BufferedReader(new InputStreamReader(
	    InternetAddressTest.class.getResourceAsStream("addrlist"))));
	return testData;
    }

    public static void main(String argv[]) throws Exception {
	verbose = true;		// default for standalone
	int optind;
	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-")) {
		// ignore
	    } else if (argv[optind].equals("-g")) {
		gen_test_input = true;
	    } else if (argv[optind].equals("-h")) {
		parse_header = true;
	    } else if (argv[optind].equals("-p")) {
		parse_mail = true;
	    } else if (argv[optind].equals("-s")) {
		strict = true;
	    } else if (argv[optind].equals("-q")) {
		verbose = false;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
		"Usage: addrtest [-g] [-h] [-p] [-s] [-q] [-] [address ...]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	/*
	 * If there's any args left on the command line,
	 * concatenate them into a string and test that.
	 */
	if (optind < argv.length) {
	    StringBuffer sb = new StringBuffer();
	    for (int i = optind; i < argv.length; i++) {
		sb.append(argv[i]);
		sb.append(" ");
	    }
	    test("To", sb.toString(), null, strict, parse_header);
	} else {
	    // read from stdin
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(System.in));
	    String s;

	    if (parse_mail)
		parse(in);
	    else {
		while ((s = in.readLine()) != null)
		    test("To", s, null, strict, parse_header);
	    }
	}
	System.exit(errors);

    }

    /*
     * Parse the input in "mail" format, extracting the From, To, and Cc
     * headers and testing them.  The parse is rather crude, but sufficient
     * to test against most existing UNIX mailboxes.
     */
    public static void parse(BufferedReader in) throws IOException {
	String header = "";
	boolean doStrict = strict;
	boolean doParseHeader = parse_header;

	for (;;) {
	    String s = in.readLine();
	    if (s != null && s.length() > 0) {
		char c = s.charAt(0);
		if (c == ' ' || c == '\t') {
		    // a continuation line, add it to the current header
		    header += '\n' + s;
		    continue;
		}
	    }
	    // "s" is the next header, "header" is the last complete header
	    if (header.startsWith("Strict: ")) {
		doStrict = Boolean.parseBoolean(value(header));
	    } else if (header.startsWith("Header: ")) {
		doParseHeader = Boolean.parseBoolean(value(header));
	    } else if (header.startsWith("From: ") ||
		    header.startsWith("To: ") ||
		    header.startsWith("Cc: ")) {
		int i;
		String[] expect = null;
		if (s != null && s.startsWith("Expect: ")) {
		    try {
			int nexpect = Integer.parseInt(s.substring(8));
			expect = new String[nexpect];
			for (i = 0; i < nexpect; i++)
			    expect[i] = readLine(in).trim();
		    } catch (NumberFormatException e) {
			try {
			    if (s.substring(8, 17).equals("Exception")) {
				expect = new String[1];
				expect[0] = "Exception";
			    }
			} catch (StringIndexOutOfBoundsException se) {
			    // ignore it
			}
		    }
		}
		i = header.indexOf(':');
		try {
		    if (junit)
			testData.add(new Object[] {
			    header.substring(0, i), header.substring(i + 2),
			    expect, doStrict, doParseHeader });
		    else
			test(header.substring(0, i), header.substring(i + 2),
			    expect, doStrict, doParseHeader);
		} catch (StringIndexOutOfBoundsException e) {
		    e.printStackTrace(System.out);
		}
	    }
	    if (s == null)
		return;		// EOF
	    if (s.length() == 0) {
		while ((s = in.readLine()) != null) {
		    if (s.startsWith("From "))
			break;
		}
		if (s == null)
		    return;
	    }
	    header = s;
	}
    }

    private static String value(String header) {
	return header.substring(header.indexOf(':') + 1).trim();
    }

    /**
     * Read an "expected" line, handling continuations
     * (backslash at end of line).  If line ends with
     * two backslashes, it's not a continuation, just a
     * line that ends with a single backslash.
     */
    private static String readLine(BufferedReader in) throws IOException {
	String line = in.readLine();
	if (!line.endsWith("\\"))
	    return line;
	if (line.endsWith("\\\\"))
	    return line.substring(0, line.length() - 1);
	StringBuilder sb = new StringBuilder(line);
	sb.setCharAt(sb.length() - 1, '\n');
	for (;;) {
	    line = in.readLine();
	    sb.append(line);
	    if (!line.endsWith("\\"))
		break;
	    if (line.endsWith("\\\\")) {
		sb.setLength(sb.length() - 1);
		break;
	    }
	    sb.setCharAt(sb.length() - 1, '\n');
	}
	return sb.toString();
    }

    @Test
    public void testAddress() {
	test(headerName, headerValue, expected, doStrict, doParseHeader);
    }

    /**
     * Test the header's value to see if we can parse it as expected.
     */
    public static void test(String header, String value, String expect[],
		boolean doStrict, boolean doParseHeader) {
	PrintStream out = System.out;
	if (gen_test_input)
	    pr(header + ": " + value);
	else
	    pr("Test: " + value);

	try {
	    InternetAddress[] al;
	    if (doParseHeader)
		al = InternetAddress.parseHeader(value, doStrict);
	    else
		al = InternetAddress.parse(value, doStrict);
	    if (gen_test_input)
		pr("Expect: " + al.length);
	    else {
		pr("Got " + al.length + " addresses:");
		if (expect != null && al.length != expect.length) {
		    pr("Expected " + expect.length + " addresses");
		    if (junit)
			Assert.assertEquals("For " + value +
			    " number of addresses",
			    al.length, expect.length);
		    errors++;
		}
	    }
	    for (int i = 0; i < al.length; i++) {
		if (gen_test_input)
		    pr("\t" + al[i].getAddress());	// XXX - escape newlines
		else {
		    pr("\t[" + (i+1) + "] " + al[i].getAddress() +
			"\t\tPersonal: " + n(al[i].getPersonal()));
		    if (expect != null && i < expect.length &&
				!expect[i].equals(al[i].getAddress())) {
			pr("\tExpected:\t" + expect[i]);
			if (junit)
			    Assert.assertEquals("For " + value +
				" address[" + i + "]",
				expect[i], al[i].getAddress());
			errors++;
		    }
		}
	    }

	    if (al.length == 0)
		return;

	    /*
	     * Some of the really bad addresses fail the toString
	     * tests, but we don't want them to cause build failures.
	     */
	    if (junit)
		return;

	    /*
	     * As a sanity test, convert the address array to a string and
	     * then parse it again, to see if we get the same thing back.
	     */
	    try {
		InternetAddress[] al2;
		String ta = InternetAddress.toString(al);
		if (doParseHeader)
		    al2 = InternetAddress.parseHeader(ta, doStrict);
		else
		    al2 = InternetAddress.parse(ta, doStrict);
		if (al.length != al2.length) {
		    pr("toString FAILED!!!");
		    pr("Expected length " + al.length +
				", got " + al2.length);
		    if (junit)
			Assert.assertEquals("For " + value +
			    " toString number of addresses",
			    al.length, al2.length);
		    errors++;
		} else {
		    for (int i = 0; i < al.length; i++) {
			if (!al[i].getAddress().equals(al2[i].getAddress())) {
			    pr("toString FAILED!!!");
			    pr("Expected address " +
					al[i].getAddress() +
					", got " + al2[i].getAddress());
			    if (junit)
				Assert.assertEquals("For " + value +
				    " toString " + ta + " address[" + i + "]",
				    al[i].getAddress(), al2[i].getAddress());
			    errors++;
			}
			String p1 = al[i].getPersonal();
			String p2 = al2[i].getPersonal();
			if (!(p1 == p2 || (p1 != null && p1.equals(p2)))) {
			    pr("toString FAILED!!!");
			    pr("Expected personal " + n(p1) +
					", got " + n(p2));
			    if (junit)
				Assert.assertEquals("For " + value +
				    " toString " + ta + " personal[" + i + "]",
				    p1, p2);
			    errors++;
			}
		    }
		}
	    } catch (AddressException e2) {
		pr("toString FAILED!!!");
		pr("Got Exception: " + e2);
		if (junit)
		    Assert.fail("For " + value +
				" toString got Exception: " + e2);
		errors++;
	    }
	} catch (AddressException e) {
	    if (gen_test_input)
		pr("Expect: Exception " + e);
	    else {
		pr("Got Exception: " + e);
		if (expect != null &&
		   (expect.length != 1 || !expect[0].equals("Exception"))) {
		    pr("Expected " + expect.length + " addresses");
		    for (int i = 0; i < expect.length; i++)
			pr("\tExpected:\t" + expect[i]);
		    if (junit)
			Assert.fail("For " + value + " expected " +
			    expect.length + "addresses, got Exception: " + e);
		    errors++;
		}
	    }
	}
    }

    private static final void pr(String s) {
	if (verbose)
	    System.out.println(s);
    }

    private static final String n(String s) {
	return s == null ? "<null>" : s;
    }
}
