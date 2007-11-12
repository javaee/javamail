/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

/**
 * Test Internet address parsing.
 *
 * @author Bill Shannon
 */

public class addrtest {
    static boolean strict = false;		// enforce strict RFC822 syntax
    static boolean gen_test_input = false;	// output good for input to -p
    static boolean parse_mail = false;		// parse input in mail format
    static int errors = 0;			// number of errors detected

    public static void main(String argv[]) throws Exception {
	int optind;
	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-")) {
		// ignore
	    } else if (argv[optind].equals("-g")) {
		gen_test_input = true;
	    } else if (argv[optind].equals("-p")) {
		parse_mail = true;
	    } else if (argv[optind].equals("-s")) {
		strict = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
		    "Usage: addrtest [-g] [-p] [-s] [-] [address ...]");
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
	    test("To", sb.toString(), null);
	} else {
	    // read from stdin
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(System.in));
	    String s;

	    if (parse_mail)
		parse(in);
	    else {
		while ((s = in.readLine()) != null)
		    test("To", s, null);
	    }
	}
	System.exit(errors);

    }

    /*
     * Parse the input in "mail" format, extracting the From, To, and Cc
     * headers and testing them.  The parse is rather crude, but sufficient
     * to test against most existing UNIX mailboxes.
     */
    public static void parse(BufferedReader in) throws Exception {
	String header = "";

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
	    if (header.startsWith("From: ") ||
		    header.startsWith("To: ") ||
		    header.startsWith("Cc: ")) {
		int i;
		String[] expect = null;
		if (s != null && s.startsWith("Expect: ")) {
		    try {
			int nexpect = Integer.parseInt(s.substring(8));
			expect = new String[nexpect];
			for (i = 0; i < nexpect; i++)
			    expect[i] = in.readLine().trim();
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
		    test(header.substring(0, i), header.substring(i + 2),
			expect);
		} catch (StringIndexOutOfBoundsException e) {
		    // ignore
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

    /**
     * Test the header's value to see if we can parse it as expected.
     */
    public static void test(String header, String value, String expect[])
		throws Exception {
	PrintStream out = System.out;
	if (gen_test_input)
	    out.println(header + ": " + value);
	else
	    out.println("Test: " + value);

	try {
	    InternetAddress[] al = InternetAddress.parse(value, strict);
	    if (gen_test_input)
		out.println("Expect: " + al.length);
	    else {
		out.println("Got " + al.length + " addresses:");
		if (expect != null && al.length != expect.length) {
		    out.println("Expected " + expect.length + " addresses");
		    errors++;
		}
	    }
	    for (int i = 0; i < al.length; i++) {
		if (gen_test_input)
		    out.println("\t" + al[i].getAddress());
		else {
		    out.println("\t[" + (i+1) + "] " + al[i].getAddress() +
			"\t\tPersonal: " + n(al[i].getPersonal()));
		    if (expect != null && i < expect.length &&
				!expect[i].equals(al[i].getAddress())) {
			out.println("\tExpected:\t" + expect[i]);
			errors++;
		    }
		}
	    }
	    if (al.length == 0)
		return;
	    /*
	     * As a sanity test, convert the address array to a string and
	     * then parse it again, to see if we get the same thing back.
	     */
	    try {
		InternetAddress[] al2 = InternetAddress.parse(
					InternetAddress.toString(al), strict);
		if (al.length != al2.length) {
		    out.println("toString FAILED!!!");
		    out.println("Expected length " + al.length +
				", got " + al2.length);
		    errors++;
		} else {
		    for (int i = 0; i < al.length; i++) {
			if (!al[i].getAddress().equals(al2[i].getAddress())) {
			    out.println("toString FAILED!!!");
			    out.println("Expected address " +
					al[i].getAddress() +
					", got " + al2[i].getAddress());
			    errors++;
			}
			String p1 = al[i].getPersonal();
			String p2 = al2[i].getPersonal();
			if (!(p1 == p2 || (p1 != null && p1.equals(p2)))) {
			    out.println("toString FAILED!!!");
			    out.println("Expected personal " + n(p1) +
					", got " + n(p2));
			    errors++;
			}
		    }
		}
	    } catch (AddressException e2) {
		out.println("toString FAILED!!!");
		out.println("Got Exception: " + e2);
		errors++;
	    }
	} catch (AddressException e) {
	    if (gen_test_input)
		out.println("Expect: Exception " + e);
	    else {
		out.println("Got Exception: " + e);
		if (expect != null &&
		   (expect.length != 1 || !expect[0].equals("Exception"))) {
		    out.println("Expected " + expect.length + " addresses");
		    for (int i = 0; i < expect.length; i++)
			out.println("\tExpected:\t" + expect[i]);
		    errors++;
		}
	    }
	}
    }

    private static final String n(String s) {
	return s == null ? "<null>" : s;
    }
}
