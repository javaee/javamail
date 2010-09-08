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

import java.io.*;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * Test socket factory properties.
 * Needs to be run with an SMTP server that supports both SSL
 * connections and the STARTTLS command, e.g., mail-sfbay.sun.com.
 *
 * @author Bill Shannon
 */

public class socketfactorytest {

    public static void main(String[] argv) {
	String mailhost = "mail-sfbay.sun.com";
	String user = null, password = null;
	boolean debug = false;
	boolean auth = false;
	int optind;

	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-U")) {
		user = argv[++optind];
	    } else if (argv[optind].equals("-P")) {
		password = argv[++optind];
	    } else if (argv[optind].equals("-M")) {
		mailhost = argv[++optind];
	    } else if (argv[optind].equals("-d")) {
		debug = true;
	    } else if (argv[optind].equals("-A")) {
		auth = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
			"Usage: socketfactorytest [-U user] [-P passwd]]");
		System.out.println("\t[-M transport-host] [-d] [-A]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	try {

	    // first, no factories

	    Properties props = new Properties();
	    props.put("mail.smtp.host", mailhost);
	    Session session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    Transport t = session.getTransport("smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
		TestResult.success();
	    } finally {
		t.close();
	    }
	    TestResult.print("no factories");

	    // socket factory property

	    TestResult.reset();
	    props = new Properties();
	    props.put("mail.smtp.host", mailhost);
	    props.put("mail.smtp.socketFactory.class", "DummySocketFactory");
	    session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    t = session.getTransport("smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
	    } finally {
		t.close();
	    }
	    TestResult.print("socket factory property");

	    // socket factory object

	    TestResult.reset();
	    props = new Properties();
	    props.put("mail.smtp.host", mailhost);
	    props.put("mail.smtp.socketFactory", new DummySocketFactory());
	    session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    t = session.getTransport("smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
	    } finally {
		t.close();
	    }
	    TestResult.print("socket factory object");

	    // SSL socket factory property

	    TestResult.reset();
	    props = new Properties();
	    props.put("mail.smtp.host", mailhost);
	    props.put("mail.smtp.ssl.enable", "true");
	    props.put("mail.smtp.ssl.checkserveridentity", "true");
	    props.put("mail.smtp.ssl.socketFactory.class",
						"DummySSLSocketFactory");
	    session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    t = session.getTransport("smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
	    } finally {
		t.close();
	    }
	    TestResult.print("SSL socket factory property");

	    // SSL socket factory object

	    TestResult.reset();
	    props = new Properties();
	    props.put("mail.smtp.host", mailhost);
	    props.put("mail.smtp.ssl.enable", "true");
	    props.put("mail.smtp.ssl.checkserveridentity", "true");
	    props.put("mail.smtp.ssl.socketFactory",
						new DummySSLSocketFactory());
	    session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    t = session.getTransport("smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
	    } finally {
		t.close();
	    }
	    TestResult.print("SSL socket factory object");

	    // STARTTLS no socket factory

	    TestResult.reset();
	    props = new Properties();
	    props.put("mail.smtp.host", mailhost);
	    props.put("mail.smtp.starttls.enable", "true");
	    session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    t = session.getTransport("smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
		TestResult.success();
	    } finally {
		t.close();
	    }
	    TestResult.print("STARTTLS no socket factory");

	    // STARTTLS SSL socket factory property

	    TestResult.reset();
	    props = new Properties();
	    props.put("mail.smtp.host", mailhost);
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.ssl.checkserveridentity", "true");
	    props.put("mail.smtp.ssl.socketFactory.class",
						"DummySSLSocketFactory");
	    session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    t = session.getTransport("smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
	    } finally {
		t.close();
	    }
	    TestResult.print("STARTTLS SSL socket factory property");

	    // STARTTLS SSL socket factory object

	    TestResult.reset();
	    props = new Properties();
	    props.put("mail.smtp.host", mailhost);
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.ssl.checkserveridentity", "true");
	    props.put("mail.smtp.ssl.socketFactory",
						new DummySSLSocketFactory());
	    session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    t = session.getTransport("smtp");
	    try {
		if (auth)
		    t.connect(mailhost, user, password);
		else
		    t.connect();
	    } finally {
		t.close();
	    }
	    TestResult.print("STARTTLS SSL socket factory object");

	    // Mail SSL socket factory object

	    /*
	    TestResult.reset();
	    props = new Properties();
	    props.put("mail.imap.host", mailhost);
	    props.put("mail.imap.ssl.enable", "true");
	    props.put("mail.imap.ssl.checkserveridentity", "true");
	    MailSSLSocketFactory sf = new MailSSLSocketFactory("TLS");
	    //sf.setTrustAllHosts(true);
	    sf.setTrustedHosts(new String[] { "loghost" });
	    props.put("mail.imap.ssl.socketFactory", sf);
	    props.put("mail.imap.socketFactory.fallback", "false");
	    session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    Store st = session.getStore("imap");
	    try {
		if (auth)
		    st.connect(mailhost, user, password);
		else
		    st.connect();
		TestResult.success();
	    } finally {
		st.close();
	    }
	    TestResult.print("Mail SSL socket factory object");
	    */

	} catch (Exception e) {
	    System.out.println("Exception: " + e);
	}
    }
}
