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

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import javax.mail.util.*;
import javax.activation.*;

/*
 * Test multipart parsing.
 *
 * @author Bill Shannon
 */

public class multiparttest {

    static boolean verbose = false;
    static boolean debug = false;
    static boolean showStructure = false;
    static boolean saveAttachments = false;
    static boolean shared = false;
    static int attnum = 1;

    static final String data =
	"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static void main(String argv[]) {
	int maxsize = 10000;
	int optind;

	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-v")) {
		verbose = true;
	    } else if (argv[optind].equals("-D")) {
		debug = true;
	    } else if (argv[optind].equals("-s")) {
		showStructure = true;
	    } else if (argv[optind].equals("-S")) {
		saveAttachments = true;
	    } else if (argv[optind].equals("-F")) {
		shared = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
"Usage: multiparttest [-v] [-D] [-s] [-S] [mazsize]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	if (optind < argv.length)
	    maxsize = Integer.parseInt(argv[optind]);

        try {
	    // Get a Properties object
	    Properties props = System.getProperties();

	    // Get a Session object
	    Session session = Session.getInstance(props, null);
	    session.setDebug(debug);

	    long t0 = System.currentTimeMillis();
	    for (int size = 1; size <= maxsize; size++) {
		System.out.println("SIZE: " + size);
		/*
		 * Construct a multipart message with a part of the
		 * given size.
		 */
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress("me@example.com"));
		msg.setSubject("test multipart parsing");
		msg.setSentDate(new Date(0));
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText("main text\n");
		MimeBodyPart mbp3 = new MimeBodyPart();
		mbp3.setText("end text\n");
		MimeBodyPart mbp2 = new MimeBodyPart();
		byte[] part = new byte[size];
		for (int i = 0; i < size; i++) {
		    int j = i % 64;
		    if (j == 62)
			part[i] = (byte)'\r';
		    else if (j == 63)
			part[i] = (byte)'\n';
		    else
			part[i] = (byte)data.charAt((j + i / 64) % 62);
		}
		mbp2.setDataHandler(new DataHandler(
		    new ByteArrayDataSource(part, "text/plain")));

		MimeMultipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);
		mp.addBodyPart(mbp2);
		mp.addBodyPart(mbp3);
		msg.setContent(mp);
		msg.saveChanges();

		/*
		 * Write the message out to a byte array.
		 */
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		msg.writeTo(bos);
		bos.close();
		byte[] buf = bos.toByteArray();

		/*
		// to verify that the message is being constructed properly
		FileOutputStream fos = new FileOutputStream("msg.txt");
		fos.write(buf);
		fos.close();
		*/

		/*
		 * Construct a new message to parse the bytes.
		 */
		msg = new MimeMessage(session, shared ?
		    new SharedByteArrayInputStream(buf) :
		    new ByteArrayInputStream(buf));

		// verify that the part content is correct
		mp = (MimeMultipart)msg.getContent();
		mbp2 = (MimeBodyPart)mp.getBodyPart(1);
		InputStream is = mbp2.getInputStream();
		int k = 0;
		int c;
		while ((c = is.read()) >= 0) {
		    int j = k % 64;
		    byte e;
		    if (j == 62)
			e = (byte)'\r';
		    else if (j == 63)
			e = (byte)'\n';
		    else
			e = (byte)data.charAt((j + k / 64) % 62);
		    if (c != e) {
			System.out.println("ERROR: at byte " + k +
			    " expected " + (int)e + " got " + c);
			break;
		    }
		    k++;
		}
		if (c < 0 && k != size)
		    System.out.println("ERROR: expected size " + size +
					" got " + k);

		if (verbose)
		    dumpPart(msg);
	    }
	    long t = System.currentTimeMillis();
	    System.out.println(t - t0);

	} catch (Exception ex) {
	    System.out.println("Oops, got exception! " + ex.getMessage());
	    ex.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }

    public static void dumpPart(Part p) throws Exception {
	if (p instanceof Message)
	    dumpEnvelope((Message)p);

	boolean dumpStream = false;
	/*
	String ct = p.getContentType();
	try {
	    pr("CONTENT-TYPE: " + (new ContentType(ct)).toString());
	} catch (ParseException pex) {
	    pr("BAD CONTENT-TYPE: " + ct);
	}
	*/
	String filename = p.getFileName();
	if (filename != null)
	    pr("FILENAME: " + filename);

	/*
	 * Using isMimeType to determine the content type avoids
	 * fetching the actual content data until we need it.
	 */
	if (p.isMimeType("text/plain")) {
	    pr("This is plain text");
	    pr("---------------------------");
	    dumpStream = true;
	} else if (p.isMimeType("multipart/*")) {
	    pr("This is a Multipart");
	    pr("---------------------------");
	    Multipart mp = (Multipart)p.getContent();
	    level++;
	    int count = mp.getCount();
	    for (int i = 0; i < count; i++)
		dumpPart(mp.getBodyPart(i));
	    level--;
	} else if (p.isMimeType("message/rfc822")) {
	    pr("This is a Nested Message");
	    pr("---------------------------");
	    level++;
	    dumpPart((Part)p.getContent());
	    level--;
	} else {
	    dumpStream = true;
	}

	if (verbose && !showStructure && dumpStream) {
	    InputStream is = p.getInputStream();
	    // If "is" is not already buffered, wrap a BufferedInputStream
	    // around it.
	    if (!(is instanceof BufferedInputStream))
		is = new BufferedInputStream(is);
	    int c;
	    while ((c = is.read()) != -1)
		System.out.write(c);
	    System.out.println();
	}

	/*
	 * If we're saving attachments, write out anything that
	 * looks like an attachment into an appropriately named
	 * file.  Don't overwrite existing files to prevent
	 * mistakes.
	 */
	if (saveAttachments && level != 0 && !p.isMimeType("multipart/*")) {
	    String disp = p.getDisposition();
	    // many mailers don't include a Content-Disposition
	    if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
		if (filename == null)
		    filename = "Attachment" + attnum++;
		pr("Saving attachment to file " + filename);
		try {
		    File f = new File(filename);
		    if (f.exists())
			// XXX - could try a series of names
			throw new IOException("file exists");
		    OutputStream os =
			new BufferedOutputStream(new FileOutputStream(f));
		    InputStream is = p.getInputStream();
		    int c;
		    while ((c = is.read()) != -1)
			os.write(c);
		    os.close();
		} catch (IOException ex) {
		    pr("Failed to save attachment: " + ex);
		}
		pr("---------------------------");
	    }
	}
    }

    public static void dumpEnvelope(Message m) throws Exception {
	pr("This is the message envelope");
	pr("---------------------------");
	Address[] a;
	// FROM 
	if ((a = m.getFrom()) != null) {
	    for (int j = 0; j < a.length; j++)
		pr("FROM: " + a[j].toString());
	}

	// TO
	if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
	    for (int j = 0; j < a.length; j++) {
		pr("TO: " + a[j].toString());
		InternetAddress ia = (InternetAddress)a[j];
		if (ia.isGroup()) {
		    InternetAddress[] aa = ia.getGroup(false);
		    for (int k = 0; k < aa.length; k++)
			pr("  GROUP: " + aa[k].toString());
		}
	    }
	}

	// SUBJECT
	pr("SUBJECT: " + m.getSubject());

	// DATE
	Date d = m.getSentDate();
	pr("SendDate: " +
	    (d != null ? d.toString() : "UNKNOWN"));

	// FLAGS
	Flags flags = m.getFlags();
	StringBuffer sb = new StringBuffer();
	Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags

	boolean first = true;
	for (int i = 0; i < sf.length; i++) {
	    String s;
	    Flags.Flag f = sf[i];
	    if (f == Flags.Flag.ANSWERED)
		s = "\\Answered";
	    else if (f == Flags.Flag.DELETED)
		s = "\\Deleted";
	    else if (f == Flags.Flag.DRAFT)
		s = "\\Draft";
	    else if (f == Flags.Flag.FLAGGED)
		s = "\\Flagged";
	    else if (f == Flags.Flag.RECENT)
		s = "\\Recent";
	    else if (f == Flags.Flag.SEEN)
		s = "\\Seen";
	    else
		continue;	// skip it
	    if (first)
		first = false;
	    else
		sb.append(' ');
	    sb.append(s);
	}

	String[] uf = flags.getUserFlags(); // get the user flag strings
	for (int i = 0; i < uf.length; i++) {
	    if (first)
		first = false;
	    else
		sb.append(' ');
	    sb.append(uf[i]);
	}
	pr("FLAGS: " + sb.toString());

	// X-MAILER
	String[] hdrs = m.getHeader("X-Mailer");
	if (hdrs != null)
	    pr("X-Mailer: " + hdrs[0]);
	else
	    pr("X-Mailer NOT available");
    }

    static String indentStr = "                                               ";
    static int level = 0;

    /**
     * Print a, possibly indented, string.
     */
    public static void pr(String s) {
	if (!verbose)
	    return;
	if (showStructure)
	    System.out.print(indentStr.substring(0, level * 2));
	System.out.println(s);
    }
}
