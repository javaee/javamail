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

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import javax.activation.*;

/*
 * Test IMAP message cache.
 *
 * @author Bill Shannon
 */

public class messagecachetest {

    static String protocol;
    static String host = null;
    static String user = null;
    static String password = null;
    static String mbox = null;
    static String url = null;
    static int port = -1;
    static boolean verbose = false;
    static boolean debug = false;
    static Session session;

    public static void main(String argv[]) {
	int nummsg = 256;
	int optind;

	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-T")) {
		protocol = argv[++optind];
	    } else if (argv[optind].equals("-H")) {
		host = argv[++optind];
	    } else if (argv[optind].equals("-U")) {
		user = argv[++optind];
	    } else if (argv[optind].equals("-P")) {
		password = argv[++optind];
	    } else if (argv[optind].equals("-v")) {
		verbose = true;
	    } else if (argv[optind].equals("-D")) {
		debug = true;
	    } else if (argv[optind].equals("-f")) {
		mbox = argv[++optind];
	    } else if (argv[optind].equals("-L")) {
		url = argv[++optind];
	    } else if (argv[optind].equals("-p")) {
		port = Integer.parseInt(argv[++optind]);
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
"Usage: messagecachetest [-L url] [-T protocol] [-H host] [-p port] [-U user]");
		System.out.println(
"\t[-P password] [-f mailbox] [msgnum] [-v] [-D]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	try {
	    if (optind < argv.length)
		 nummsg = Integer.parseInt(argv[optind]);

	    // Get a Properties object
	    Properties props = System.getProperties();

	    // Get a Session object
	    session = Session.getInstance(props, null);
	    session.setDebug(debug);

	    // Get a Store object
	    Store store = null;
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

	    Folder folder = store.getDefaultFolder();
	    if (folder == null) {
		System.out.println("No default folder");
		System.exit(1);
	    }

	    if (mbox == null)
		mbox = "messagecachetest";
	    folder = folder.getFolder(mbox);
	    if (folder == null) {
		System.out.println("Invalid folder");
		System.exit(1);
	    }

	    Message[] msgs = createMessages(nummsg);
	    if (folder.exists())
		folder.delete(false);
	    folder.create(Folder.HOLDS_MESSAGES);

	    folder.open(Folder.READ_WRITE);
	    if (verbose)
		System.out.println("fill folder");
	    folder.appendMessages(msgs);
	    folder.close(false);

	    folder.open(Folder.READ_WRITE);
	    if (verbose)
		System.out.println("test message number");
	    testMessageNumber(folder);
	    folder.close(false);
	    folder.open(Folder.READ_WRITE);
	    if (verbose)
		System.out.println("test expunge forward");
	    testExpungeForward(folder);
	    folder.close(false);

	    folder.open(Folder.READ_WRITE);
	    folder.appendMessages(msgs);
	    folder.close(false);
	    folder.open(Folder.READ_WRITE);
	    if (verbose)
		System.out.println("test expunge reverse");
	    testExpungeReverse(folder);
	    folder.close(false);

	    folder.open(Folder.READ_WRITE);
	    folder.appendMessages(msgs);
	    folder.close(false);
	    folder.open(Folder.READ_WRITE);
	    if (verbose)
		System.out.println("test expunge random");
	    testExpungeRandom(folder);
	    folder.close(false);

	    folder.open(Folder.READ_WRITE);
	    folder.appendMessages(msgs);
	    folder.close(false);
	    folder.open(Folder.READ_WRITE);
	    if (verbose)
		System.out.println("test expunge other");
	    testExpungeOther(folder);
	    folder.close(false);
	    store.close();
	} catch (Exception ex) {
	    System.out.println("Oops, got exception! " + ex.getMessage());
	    ex.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }

    private static Message[] createMessages(int num) throws MessagingException {
	Message[] msgs = new Message[num];
	for (int i = 1; i <= num; i++) {
	    MimeMessage msg = new MimeMessage(session);
	    msg.setSentDate(new Date());
	    msg.setFrom();
	    msg.setRecipients(Message.RecipientType.TO, "nobody@nowhere.com");
	    msg.setSubject(Integer.toString(i));
	    msg.setText(i + "\n");
	    msg.saveChanges();
	    msgs[i-1] = msg;
	}
	return msgs;
    }

    private static void testMessageNumber(Folder folder)
				throws MessagingException {
	int nummsg = folder.getMessageCount();
	Message msgs[] = new Message[nummsg];
	for (int i = 1; i <= nummsg; i++) {
	    Message msg = folder.getMessage(i);
	    msgs[i-1] = msg;
	    if (Integer.valueOf(msg.getSubject()) != i) {
		System.out.println("FAIL: Wrong message! Got " +
				msg.getSubject() + ", Expected " + i);
	    }
	}
	for (int i = 1; i <= nummsg; i++) {
	    Message msg = folder.getMessage(i);
	    if (msgs[i-1] != msg || msg.getMessageNumber() != i) {
		System.out.println("FAIL: Wrong message! Got " +
				msg.getMessageNumber() + ", Expected " + i);
	    }
	}
    }

    private static void testExpungeForward(Folder folder)
				throws MessagingException {
	int nummsg = folder.getMessageCount();
	for (int i = 1; i <= nummsg; i++) {
	    Message msg = folder.getMessage(1);
	    msg.setFlag(Flags.Flag.DELETED, true);
	    folder.expunge();
	    for (int j = 1; j < nummsg - i; j++) {
		msg = folder.getMessage(j);
		if (msg.getMessageNumber() != j) {
		    System.out.println("FAIL: Wrong message! Got " +
				    msg.getMessageNumber() + ", Expected " + j);
		}
	    }
	}
    }

    private static void testExpungeReverse(Folder folder)
				throws MessagingException {
	int nummsg = folder.getMessageCount();
	for (int i = nummsg; i >= 1; i--) {
	    Message msg = folder.getMessage(i);
	    msg.setFlag(Flags.Flag.DELETED, true);
	    folder.expunge();
	    for (int j = 1; j < i; j++) {
		msg = folder.getMessage(j);
		if (msg.getMessageNumber() != j) {
		    System.out.println("FAIL: Wrong message! Got " +
				    msg.getMessageNumber() + ", Expected " + j);
		}
	    }
	}
    }

    private static void testExpungeRandom(Folder folder)
				throws MessagingException {
	int nummsg = folder.getMessageCount();
	Random rnd = new Random(System.currentTimeMillis());
	while (nummsg > 0) {
	    Message msg = folder.getMessage(rnd.nextInt(nummsg) + 1);
	    msg.setFlag(Flags.Flag.DELETED, true);
	    folder.expunge();
	    nummsg--;
	    for (int j = 1; j <= nummsg; j++) {
		msg = folder.getMessage(j);
		if (msg.getMessageNumber() != j) {
		    System.out.println("FAIL: Wrong message! Got " +
				    msg.getMessageNumber() + ", Expected " + j);
		}
	    }
	}
    }

    private static void testExpungeOther(Folder folder)
				throws MessagingException {
	Folder other = folder.getStore().getFolder(folder.getFullName());
	other.open(Folder.READ_WRITE);
	Message msg = other.getMessage(2);
	msg.setFlag(Flags.Flag.DELETED, true);
	other.expunge();
	System.out.println("waiting for expunge notification");
	try { Thread.sleep(2000); } catch (InterruptedException ex) { }
	folder.isOpen();
	try {
	    msg = folder.getMessage(2);
	    msg.getFlags();
	    if (!msg.isExpunged()) {
		System.out.println("FAIL: Message 2 is not expunged!");
	    }
	} catch (MessageRemovedException mex) {
	    System.out.println("SUCCESS: message 2 is removed!");
	}
	msg = folder.getMessage(1);
	if (Integer.valueOf(msg.getSubject()) != 1) {
	    System.out.println("FAIL: Message 1 is wrong!");
	}
	msg = folder.getMessage(3);
	if (Integer.valueOf(msg.getSubject()) != 3) {
	    System.out.println("FAIL: Message 3 is wrong!");
	}
	other.close(false);
    }
}
