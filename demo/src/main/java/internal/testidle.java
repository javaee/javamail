/*
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.sun.mail.imap.*;

/**
 * Test program for IDLE support in IMAP provider.
 *
 * Run several threads that access the folder's connection
 * while another thread runs the IDLE command.  A timer
 * thread checks that each thread is making progress, to
 * detect deadlock.
 *
 * XXX - Should have another thread add messages to the folder
 *	 to test the new message notification works properly.
 */
public class testidle {

    public static Store store;
    public static Folder folder;
    public static boolean showStructure = false;
    public static boolean saveAttachments = false;
    public static int attnum = 0;
    public static int totalTime;

    public static final AtomicBoolean stop = new AtomicBoolean(false);
    public static final AtomicInteger folderProgress = new AtomicInteger();
    public static final AtomicInteger msgProgress = new AtomicInteger();

    public static void main(String argv[]) {
	if (argv.length != 5) {
	    System.out.println(
		"Usage: testidle <host> <user> <password> <mbox> <time>");
	    System.exit(1);
	}

        try {
	    Properties props = System.getProperties();

	    // Get a Session object
	    Session session = Session.getInstance(props, null);
	    // session.setDebug(true);

	    // Get a Store object
	    store = session.getStore("imap");

	    store.addStoreListener(new StoreListener() {
		public void notification(StoreEvent e) {
		    System.out.println("StoreEvent: type " +
			e.getMessageType() + ", message " +
			e.getMessage());
		}
	    });

	    // Connect
	    store.connect(argv[0], argv[1], argv[2]);

	    // Open a Folder
	    folder = store.getFolder(argv[3]);
	    if (folder == null || !folder.exists()) {
		System.out.println("Invalid folder");
		System.exit(1);
	    }

	    folder.open(Folder.READ_WRITE);

	    // Add messageCountListener to listen for new messages
	    folder.addMessageCountListener(new MessageCountAdapter() {
		public void messagesAdded(MessageCountEvent ev) {
		    Message[] msgs = ev.getMessages();
		    System.out.println("Got " + msgs.length + " new messages");

		    // Just dump out the new messages
		    for (int i = 0; i < msgs.length; i++) {
			try {
			    System.out.println("-----");
			    System.out.println("Message " +
				msgs[i].getMessageNumber() + ":");
			    msgs[i].writeTo(System.out);
			} catch (IOException ioex) { 
			    ioex.printStackTrace();	
			} catch (MessagingException mex) {
			    mex.printStackTrace();
			}
		    }
		}
	    });

	    totalTime = Integer.parseInt(argv[4]);

	    new Thread("timer") {
		public void run() {
		    timer();
		}
	    }.start();

	    new Thread("message reader") {
		public void run() {
		    readMessages();
		}
	    }.start();

	    new Thread("folder reader") {
		public void run() {
		    readFolder();
		}
	    }.start();

	    new Thread("store idle") {
		public void run() {
		    storeIdle();
		}
	    }.start();


	    /*
	     * make sure two threads running idle works properly
	    new Thread("idle") {
		public void run() {
		    try {
			doIdle();
		    } catch (MessagingException mex) { }
		}
	    }.start();
	     */

	    doIdle();

	    System.out.println("connected " + store.isConnected());

	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Run the idle command until told to stop.
     */
    public static void doIdle() throws MessagingException {
	boolean supportsIdle = false;
	try {
	    if (folder instanceof IMAPFolder) {
		IMAPFolder f = (IMAPFolder)folder;
		f.idle();
		supportsIdle = true;
	    }
	} catch (FolderClosedException fex) {
	    throw fex;
	} catch (MessagingException mex) {
	    supportsIdle = false;
	}
	while (!stop.get()) {
	    if (supportsIdle && folder instanceof IMAPFolder) {
		IMAPFolder f = (IMAPFolder)folder;
		f.idle();
		/*
		System.out.println("IDLE done in " +
					Thread.currentThread().getName());
		 */
	    } else {
		try {
		    Thread.sleep(1000); // sleep for 1000 milliseconds
		} catch (InterruptedException ex) { }

		// This is to force the IMAP server to send us
		// EXISTS notifications. 
		folder.getMessageCount();
	    }
	}
    }

    /**
     * Monitor progress of threads and tell them to stop
     * when their time is up.
     */
    public static void timer() {
	long tend = System.currentTimeMillis() + totalTime;
	int fpcnt = folderProgress.get();
	int mpcnt = msgProgress.get();
	while (System.currentTimeMillis() < tend) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException ex) { }
	    int nfpcnt = folderProgress.get();
	    int nmpcnt = msgProgress.get();
	    if (!(nfpcnt > fpcnt && nmpcnt > mpcnt)) {
		System.out.println("THREAD STUCK?");
		System.out.printf("fpcnt %d nfpcnt %d\n", fpcnt, nfpcnt);
		System.out.printf("mpcnt %d nmpcnt %d\n", mpcnt, nmpcnt);
	    }
	    fpcnt = nfpcnt;
	    mpcnt = nmpcnt;
	}
	System.out.println("STOPPING");
	stop.set(true);
	try {
	    folder.getUnreadMessageCount();	// force IDLE to terminate
	} catch (MessagingException mex) { }
    }

    /**
     * Read all the messages in the folder and access all their content.
     */
    public static void readMessages() {
	try {
	    while (!stop.get()) {
		int cnt = folder.getMessageCount();
		for (int i = 1; i <= cnt; i++) {
		    //System.out.println("dump " + i);
		    Message msg = folder.getMessage(i);
		    dumpEnvelope(msg);
		    msgProgress.incrementAndGet();
		    try {
			Thread.sleep(25);
		    } catch (InterruptedException ex) { }
		}
	    }
	    System.out.println("messages DONE");
	} catch (Exception ex) {
	    System.out.println(ex);
	}
    }

    /**
     * Perform folder commands to compete with the message
     * access commands.
     */
    public static void readFolder() {
	try {
	    while (!stop.get()) {
		int cnt = folder.getUnreadMessageCount();
		store.isConnected();	// poke the store too
		folderProgress.incrementAndGet();
		try {
		    Thread.sleep(100);
		} catch (InterruptedException ex) { }
	    }
	    System.out.println("folder DONE");
	} catch (MessagingException mex) {
	    System.out.println(mex);
	}
    }

    /**
     * Run the idle command until told to stop.
     */
    public static void storeIdle() {
	boolean supportsIdle = false;
	try {
	    if (store instanceof IMAPStore) {
		IMAPStore s = (IMAPStore)store;
		s.idle();
		supportsIdle = true;
	    }
	} catch (MessagingException mex) {
	    supportsIdle = false;
	}
	try {
	    while (!stop.get()) {
		if (supportsIdle && store instanceof IMAPStore) {
		    IMAPStore s = (IMAPStore)store;
		    s.idle();
		    /*
		    */
		    System.out.println("IDLE done in " +
					    Thread.currentThread().getName());
		     /*
		     */
		} else {
		    try {
			Thread.sleep(1000); // sleep for 1000 milliseconds
		    } catch (InterruptedException ex) { }
		}
	    }
	} catch (MessagingException mex) {
	    System.out.println(mex);
	}
    }

    /**
     * Dump contents of message part.
     * (Copied from msgshow.java)
     */
    public static void dumpPart(Part p) throws Exception {
	if (p instanceof Message)
	    dumpEnvelope((Message)p);

	/** Dump input stream .. 

	InputStream is = p.getInputStream();
	// If "is" is not already buffered, wrap a BufferedInputStream
	// around it.
	if (!(is instanceof BufferedInputStream))
	    is = new BufferedInputStream(is);
	int c;
	while ((c = is.read()) != -1)
	    System.out.write(c);

	**/

	String ct = p.getContentType();
	try {
	    pr("CONTENT-TYPE: " + (new ContentType(ct)).toString());
	} catch (ParseException pex) {
	    pr("BAD CONTENT-TYPE: " + ct);
	}
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
	    if (!showStructure && !saveAttachments)
		System.out.println((String)p.getContent());
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
	    if (!showStructure && !saveAttachments) {
		/*
		 * If we actually want to see the data, and it's not a
		 * MIME type we know, fetch it and check its Java type.
		 */
		Object o = p.getContent();
		if (o instanceof String) {
		    pr("This is a string");
		    pr("---------------------------");
		    System.out.println((String)o);
		} else if (o instanceof InputStream) {
		    pr("This is just an input stream");
		    pr("---------------------------");
		    InputStream is = (InputStream)o;
		    int c;
		    while ((c = is.read()) != -1)
			System.out.write(c);
		} else {
		    pr("This is an unknown type");
		    pr("---------------------------");
		    pr(o.toString());
		}
	    } else {
		// just a separator
		pr("---------------------------");
	    }
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
		    ((MimeBodyPart)p).saveFile(f);
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
	/*
	if (showStructure)
	    System.out.print(indentStr.substring(0, level * 2));
	System.out.println(s);
	*/
    }
}
