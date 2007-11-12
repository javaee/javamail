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
import javax.mail.internet.MimeUtility;

/**
 * Test header folding.
 *
 * NOTE: Requires hacked version of MimeUtility to make
 *	 fold and unfold methods public.
 *
 * @author Bill Shannon
 */

public class foldtest {
    private static boolean verbose;
    private static int errors;

    public static void main(String argv[]) throws Exception {
	int optind;
	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-")) {
		// ignore
	    } else if (argv[optind].equals("-v")) {
		verbose = true;
	    /*
	    } else if (argv[optind].equals("-p")) {
		parse_mail = true;
	    } else if (argv[optind].equals("-s")) {
		strict = true;
	    */
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
		    "Usage: foldtest [-v] [-] [address ...]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	/*
	 * If there's any args left on the command line,
	 * test each of them.
	 */
	if (optind < argv.length) {
	    for (int i = optind; i < argv.length; i++) {
		test(argv[i]);
	    }
	} else {
	    // read from stdin
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(System.in));
	    test(in);
	}
	System.exit(errors);

    }

    /**
     * Read the data from the test file.  Format is multiple of any of
     * the following:
     *
     * FOLD\nString$\nEXPECT\nString$\n
     * UNFOLD\nString$\nEXPECT\nString$\n
     * BOTH\nString$\n
     */
    private static void test(BufferedReader in) throws Exception {
	String line;
	while ((line = in.readLine()) != null) {
	    if (line.startsWith("#"))
		continue;
	    String orig = readString(in);
	    if (line.equals("BOTH")) {
		test(orig);
	    } else {
		String e = in.readLine();
		if (!e.equals("EXPECT"))
		    System.out.println("TEST DATA FORMAT ERROR");
		String expect = readString(in);
		if (line.equals("FOLD")) {
		    String t = MimeUtility.fold(0, orig);
		    if (!t.equals(expect) || verbose) {
			if (!t.equals(expect)) {
			    System.out.println("ERROR:");
			    errors++;
			}
			System.out.println("Orig:     " + orig);
			System.out.println("Folded:   " + t);
			System.out.println("Expected: " + expect);
			//diff(t, expect);
		    }
		} else {
		    String t = MimeUtility.unfold(orig);
		    if (!t.equals(expect) || verbose) {
			if (!t.equals(expect)) {
			    System.out.println("ERROR:");
			    errors++;
			}
			System.out.println("Orig:     " + orig);
			System.out.println("Unfolded: " + t);
			System.out.println("Expected: " + expect);
			//diff(t, expect);
		    }
		}
	    }
	}
    }

    /**
     * Read a string that ends with '$', preserving all characters,
     * especially including CR and LF.
     */
    private static String readString(BufferedReader in) throws Exception {
	StringBuffer sb = new StringBuffer();
	int c;
	while ((c = in.read()) != '$')
	    sb.append((char)c);
	in.readLine();	// throw away rest of line
	return sb.toString();
    }

    private static void test(String s) throws Exception {
	String fs = MimeUtility.fold(0, s);
	String us = MimeUtility.unfold(fs);
	if (!s.equals(us) || verbose) {
	    if (!s.equals(us)) {
		System.out.println("ERROR:");
		errors++;
	    }
	    System.out.println("Orig:     " + s);
	    System.out.println("Folded:   " + fs);
	    System.out.println("Unfolded: " + us);
	}
    }

    private static void diff(String s, String t) {
	int len = Math.max(s.length(), t.length());
	for (int i = 0; i < len; i++) {
	    System.out.println(i + " " +
		(i < s.length() ? (s.charAt(i)&0xff) : -1) + " " +
		(i < t.length() ? (t.charAt(i)&0xff) : -1));
	}
    }
}
