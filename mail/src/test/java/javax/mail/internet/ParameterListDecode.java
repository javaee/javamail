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
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.*;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test parameter list parsing.
 *
 * XXX - this should be a JUnit parameterized test,
 *	 but I can't figure out how to run parameterized
 *	 tests under my ClassLoaderSuite.
 *
 * @author Bill Shannon
 */

public class ParameterListDecode {
    static boolean gen_test_input = false;	// output good for input to -p
    static boolean parse_mail = false;		// parse input in mail format
    static boolean test_mail = false;		// test using a mail server
    static int errors = 0;			// number of errors detected

    static String protocol;
    static String host = null;
    static String user = null;
    static String password = null;
    static String mbox = null;
    static String url = null;
    static int port = -1;
    static boolean debug = false;

    static Session session;
    static Store store;
    static Folder folder;

    static boolean junit;

    protected static void testDecode(String paramData) throws Exception {
	junit = true;
	parse(new BufferedReader(new InputStreamReader(
	    ParameterListDecode.class.getResourceAsStream(paramData))));
    }

    public static void main(String argv[]) throws Exception {
	System.getProperties().put("mail.mime.decodeparameters", "true");
	int optind;
	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-")) {
		// ignore
	    } else if (argv[optind].equals("-g")) {
		gen_test_input = true;
	    } else if (argv[optind].equals("-p")) {
		parse_mail = true;
	    } else if (argv[optind].equals("-m")) {
		test_mail = true;
	    } else if (argv[optind].equals("-T")) {
		protocol = argv[++optind];
	    } else if (argv[optind].equals("-H")) {
		host = argv[++optind];
	    } else if (argv[optind].equals("-U")) {
		user = argv[++optind];
	    } else if (argv[optind].equals("-P")) {
		password = argv[++optind];
	    } else if (argv[optind].equals("-D")) {
		debug = true;
	    } else if (argv[optind].equals("-f")) {
		mbox = argv[++optind];
	    } else if (argv[optind].equals("-L")) {
		url = argv[++optind];
	    } else if (argv[optind].equals("-p")) {
		port = Integer.parseInt(argv[++optind]);
	    } else if (argv[optind].equals("-d")) {
		debug = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
		    "Usage: paramtest [-g] [-p] [-] [content-type ...]");
		System.out.println(
"or\tparamtest -m [-g] [-L url] [-T protocol] [-H host] [-p port] [-U user]");
		System.out.println(
"\t[-P password] [-f mailbox] [-d]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	if (test_mail)
	    initMail();

	/*
	 * If there's any args left on the command line,
	 * concatenate them into a string and test that.
	 */
	if (optind < argv.length) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = optind; i < argv.length; i++) {
		sb.append(argv[i]);
		sb.append(" ");
	    }
	    test("Content-Type", sb.toString(), null);
	} else {
	    // read from stdin
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(System.in));
	    String s;

	    if (parse_mail)
		parse(in);
	    else if (test_mail)
		testMail();
	    else {
		while ((s = in.readLine()) != null)
		    test("Content-Type", s, null);
	    }
	}

	if (test_mail)
	    doneMail();

	System.exit(errors);

    }

    /*
     * Parse the input in "mail" format, extracting the Content-Type
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
	    if (header.regionMatches(true, 0, "Content-Type: ", 0, 14)) {
		int i;
		String[] expect = null;
		if (s != null && s.startsWith("Expect: ")) {
		    try {
			int nexpect = Integer.parseInt(s.substring(8));
			expect = new String[nexpect];
			for (i = 0; i < nexpect; i++)
			    expect[i] = decode(trim(in.readLine()));
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
     * Like String.trim, but only the left side.
     */
    public static String trim(String s) {
	int i = 0;
	while (i < s.length() && s.charAt(i) <= ' ')
	    i++;
	return s.substring(i);
    }

    /**
     * Decode Unicode escapes.
     */
    public static String decode(String s) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if (c == '\\' && s.charAt(i + 1) == 'u') {
		c = (char)Integer.parseInt(s.substring(i + 2, i + 6), 16);
		i += 5;
	    }
	    sb.append(c);
	}
	return sb.toString();
    }

    /**
     * Test the header's value to see if we can parse it as expected.
     */
    public static void test(String header, String value, String expect[])
		throws Exception {
	PrintStream out = System.out;
	ByteArrayOutputStream bos = null;
	if (gen_test_input) {
	    if (test_mail) {
		bos = new ByteArrayOutputStream();
		out = new PrintStream(bos);
	    } else {
		out.println(header + ": " + value);
	    }
	} else if (!junit)
	    out.println("Test: " + value);

	try {
	    ContentType ct = new ContentType(value);
	    ParameterList pl = ct.getParameterList();
	    if (gen_test_input)
		out.println("Expect: " + pl.size());
	    else if (junit)
		assertEquals("Number of parameters",
		    expect.length, pl.size());
	    else {
		out.println("Got " + pl.size() + " parameters:");
		if (expect != null && pl.size() != expect.length) {
		    out.println("Expected " + expect.length + " parameters");
		    errors++;
		}
	    }
	    Enumeration<String> e = pl.getNames();
	    for (int i = 0; e.hasMoreElements(); i++) {
		String name = e.nextElement();
		String pvalue = pl.get(name);
		if (gen_test_input)
		    out.println("\t" + name + "=" + pvalue);	// XXX - newline
		else if (junit) {
		    if (i < expect.length)
			assertEquals("Parameter value",
			    expect[i], name + "=" + pvalue);
		} else {
		    out.println("\t[" + (i+1) + "] Name: " + name +
			"\t\tValue: " + pvalue);
		    if (expect != null && i < expect.length &&
				!expect[i].equals(name + "=" + pvalue)) {
			out.println("\tExpected:\t" + expect[i]);
			errors++;
		    }
		}
	    }
	} catch (ParseException e) {
	    if (gen_test_input)
		out.println("Expect: Exception " + e);
	    else if (junit)
		assertTrue("Expected exception",
		    expect.length == 1 && expect[0].equals("Exception"));
	    else {
		out.println("Got Exception: " + e);
		if (expect != null &&
		   (expect.length != 1 || !expect[0].equals("Exception"))) {
		    out.println("Expected " + expect.length + " parameters");
		    for (int i = 0; i < expect.length; i++)
			out.println("\tExpected:\t" + expect[i]);
		    errors++;
		}
	    }
	}
	if (gen_test_input && test_mail) {
	    MimeMessage msg = new MimeMessage(session);
	    byte[] buf = bos.toByteArray();
	    msg.setDataHandler(new DataHandler(
		new ByteArrayDataSource(buf, value)));
	    msg.saveChanges();
	    //msg.writeTo(System.out);
	    folder.appendMessages(new Message[] { msg });
	}
    }

    /**
     * Initialize the Session, Store, and Folder.
     */
    private static void initMail() {
        try {
	    // Get a Properties object
	    Properties props = System.getProperties();

	    // Get a Session object
	    session = Session.getInstance(props, null);
	    session.setDebug(debug);

	    // Get a Store object
	    if (url != null) {
		URLName urln = new URLName(url);
		store = session.getStore(urln);
		store.connect();
	    } else {
		if (protocol != null)		
		    store = session.getStore(protocol);
		else
		    store = session.getStore();

		// Connect
		if (host != null || user != null || password != null)
		    store.connect(host, port, user, password);
		else
		    store.connect();
	    }

	    // Open the Folder

	    folder = store.getDefaultFolder();
	    if (folder == null) {
	        System.out.println("No default folder");
	        System.exit(1);
	    }

	    if (mbox == null)
		mbox = "parameter-list-test";
	    folder = folder.getFolder(mbox);
	    if (folder == null) {
	        System.out.println("Invalid folder");
	        System.exit(1);
	    }

	    if (gen_test_input) {
		folder.delete(false);
		folder.create(Folder.HOLDS_MESSAGES);
		folder.open(Folder.READ_WRITE);
	    } else {
		folder.open(Folder.READ_ONLY);
	    }

	} catch (Exception ex) {
	    System.out.println("Oops, got exception! " + ex.getMessage());
	    ex.printStackTrace();
	    System.exit(1);
	}
    }

    /**
     * Close the Folder and Store.
     */
    private static void doneMail() throws Exception {
	folder.close(false);
	store.close();
    }

    /**
     * Use the messages in the Folder for testing.
     */
    private static void testMail() throws Exception {
	Message[] msgs = folder.getMessages();

	for (int i = 0; i < msgs.length; i++)
	    testMessage(msgs[i]);
    }

    /**
     * Test an individual message.
     */
    private static void testMessage(Message msg) throws Exception {
	String[] expect = null;

	BufferedReader in = new BufferedReader(
			    new InputStreamReader(msg.getInputStream()));

	String s = in.readLine();
	if (s != null && s.startsWith("Expect: ")) {
	    try {
		int nexpect = Integer.parseInt(s.substring(8));
		expect = new String[nexpect];
		for (int i = 0; i < nexpect; i++)
		    expect[i] = trim(in.readLine());
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

	String ct = msg.getContentType();
	test("Content-Type: ", ct, expect);
    }
}
