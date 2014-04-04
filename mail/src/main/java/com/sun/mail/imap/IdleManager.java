/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.mail.imap;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import javax.mail.*;

import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.MailLogger;

/**
 * IdleManager uses the optional IMAP IDLE command
 * (<A HREF="http://www.ietf.org/rfc/rfc2177.txt">RFC 2177</A>)
 * to watch multiple folders for new messages.
 * IdleManager uses an Executor to execute tasks in separate threads.
 * An Executor is typically provided by an ExecutorService.
 * For example, for a Java SE application:
 * <blockquote><pre>
 *	ExecutorService es = Executors.newCachedThreadPool();
 *	final IdleManager idleManager = new IdleManager(session, es);
 * </pre></blockquote>
 * For a Java EE 7 application:
 * <blockquote><pre>
 *	{@literal @}Resource
 *	ManagedExecutorService es;
 *	final IdleManager idleManager = new IdleManager(session, es);
 * </pre></blockquote>
 * To watch for new messages in a folder, open the folder, register a listener,
 * and ask the IdleManager to watch the folder:
 * <blockquote><pre>
 *	Folder folder = store.getFolder("INBOX");
 *	folder.open(Folder.READ_WRITE);
 *	folder.addMessageCountListener(new MessageCountAdapter() {
 *	    public void messagesAdded(MessageCountEvent ev) {
 *		Folder folder = (Folder)ev.getSource();
 *		Message[] msgs = ev.getMessages();
 *		System.out.println("Folder: " + folder +
 *		    " got " + msgs.length + " new messages");
 *		// process new messages
 *		idleManager.watch(folder); // keep watching for new messages
 *	    }
 *	});
 *	idleManager.watch(folder);
 * </pre></blockquote>
 * This delivers the events for each folder in a separate thread, <b>NOT</b>
 * using the Executor.  To deliver all events in a single thread
 * using the Executor, set the following properties for the Session
 * (once), and then add listeners and watch the folder as above.
 * <blockquote><pre>
 *	// the following should be done once...
 *	Properties props = session.getProperties();
 *	props.put("mail.event.scope", "session"); // or "application"
 *	props.put("mail.event.executor", es);
 * </pre></blockquote>
 * Note that, after processing new messages in your listener, or doing any
 * other operations on the folder in any other thread, you need to tell
 * the IdleManager to watch for more new messages.  Unless, of course, you
 * close the folder.
 * <p>
 * The IdleManager is created with a Session, which it uses only to control
 * debug output.  A single IdleManager instance can watch multiple Folders
 * from multiple Stores and multiple Sessions.
 * <p>
 * Due to limitations in the Java SE nio support, a
 * {@link java.nio.channels.SocketChannel SocketChannel} must be used instead
 * of a {@link java.net.Socket Socket} to connect to the server.  However,
 * SocketChannels don't support all the features of Sockets, such as connecting
 * through a SOCKS proxy server.  SocketChannels also don't support
 * simultaneous read and write, which means that the
 * {@link com.sun.mail.imap.IMAPFolder#idle idle} method can't be used if
 * SocketChannels are being used; use this IdleManager instead.
 * To enable support for SocketChannels instead of Sockets, set the
 * <code>mail.imap.usesocketchannels</code> property in the Session used to
 * access the IMAP Folder.  (Or <code>mail.imaps.usesocketchannels</code> if
 * you're using the "imaps" protocol.)  This will effect all connections in
 * that Session, but you can create another Session without this property set
 * if you need to use the features that are incompatible with SocketChannels.
 * <p>
 * NOTE: The IdleManager, and all APIs and properties related to it, should
 * be considered <strong>EXPERIMENTAL</strong>.  They may be changed in the
 * future in ways that are incompatible with applications using the
 * current APIs.
 *
 * @since JavaMail 1.5.2
 */
public class IdleManager {
    private Executor es;
    private Selector selector;
    private MailLogger logger;
    private volatile boolean die = false;
    private Queue<IMAPFolder> toWatch = new ConcurrentLinkedQueue<IMAPFolder>();
    private Queue<IMAPFolder> toAbort = new ConcurrentLinkedQueue<IMAPFolder>();

    /**
     * Create an IdleManager.  The Session is used only to configure
     * debugging output.  The Executor is used to create the
     * "select" thread.
     *
     * @param	session	the Session containing configuration information
     * @param	es	the Executor used to create threads
     */
    public IdleManager(Session session, Executor es) throws IOException {
	logger = new MailLogger(this.getClass(), "DEBUG IMAP", session);
	this.es = es;
	selector = Selector.open();
	es.execute(new Runnable() {
	    public void run() {
		select();
	    }
	});
    }

    /**
     * Watch the Folder for new messages and other events using the IMAP IDLE
     * command.
     *
     * @param	folder	the folder to watch
     * @exception	MessagingException	for errors related to the folder
     * @exception	IOException	for SocketChannel errors
     */
    public synchronized void watch(Folder folder)
				throws IOException, MessagingException {
	if (!(folder instanceof IMAPFolder))
	    throw new MessagingException("Can only watch IMAP folders");
	IMAPFolder ifolder = (IMAPFolder)folder;
	SocketChannel sc = ifolder.getChannel();
	if (sc == null)
	    throw new MessagingException("Folder is not using SocketChannels");
	logger.log(Level.FINEST, "IdleManager watching {0}", ifolder);
	ifolder.startIdle(this);
	toWatch.add(ifolder);
	selector.wakeup();
    }

    /**
     * Request that the specified folder abort an IDLE command.
     * We can't do the abort directly because the DONE message needs
     * to be sent through the (potentially) SSL socket, which means
     * we need to be in blocking I/O mode.  We can only switch to
     * blocking I/O mode when not selecting, so wake up the selector,
     * which will process this request when it wakes up.
     */
    synchronized void requestAbort(IMAPFolder folder) {
	toAbort.add(folder);
	selector.wakeup();
    }

    /**
     * Run the {@link java.nio.channels.Selector#select select} loop
     * to poll each watched folder for events sent from the server.
     */
    private void select() {
	die = false;
	try {
	    while (!die) {
		watchAll();
		logger.finest("IdleManager waiting...");
		int ns = selector.select();
		if (logger.isLoggable(Level.FINEST))
		    logger.log(Level.FINEST,
			"IdleManager selected {0} channels", ns);
		if (die || Thread.currentThread().isInterrupted())
		    break;

		/*
		 * Process any selected folders.  We cancel the
		 * selection key for any selected folder, so if we
		 * need to continue watching that folder it's added
		 * to the toWatch list again.  We can't actually
		 * register that folder again until the previous
		 * selectionkey is cancelled, so we call selectNow()
		 * just for the side effect of cancelling the selection
		 * keys.  But if selectNow() selects something, we
		 * process it before adding folders from the toWatch
		 * queue.  And so on until there is nothing to do, at
		 * which point it's safe to register folders from the
		 * toWatch queue.
		 */
		while (processKeys() && selector.selectNow() > 0)
		    ;
	    }
	} catch (InterruptedIOException ex) {
	    logger.log(Level.FINE, "IdleManager interrupted", ex);
	} catch (IOException ex) {
	    logger.log(Level.FINE, "IdleManager got exception", ex);
	} finally {
	    try {
		unwatchAll();
		selector.close();
	    } catch (IOException ex2) {
		// nothing to do...
	    }
	    logger.fine("IdleManager exiting");
	}
    }

    /**
     * Register all of the folders in the queue with the selector,
     * switching them to nonblocking I/O mode first.
     */
    private void watchAll() {
	/*
	 * Pull each of the folders from the toWatch queue
	 * and register it.
	 */
	IMAPFolder folder;
	while ((folder = toWatch.poll()) != null) {
	    logger.log(Level.FINEST,
		    "IdleManager adding {0} to selector", folder);
	    SocketChannel sc = folder.getChannel();
	    if (sc == null)
		continue;
	    try {
		// has to be non-blocking to select
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ, folder);
	    } catch (IOException ex) {
		// oh well, nothing to do
		logger.log(Level.FINEST,
		    "IdleManager can't register folder", ex);
	    }
	}
    }

    /**
     * Process the selected keys, returning true if any folders have
     * been added to the watch list.
     */
    private boolean processKeys() throws IOException {
	boolean more = false;
	/*
	 * First, process any folders that we need to abort.
	 */
	IMAPFolder folder;
	while ((folder = toAbort.poll()) != null) {
	    logger.log(Level.FINE,
		"IdleManager aborting IDLE for folder: {0}", folder);
	    SocketChannel sc = folder.getChannel();
	    if (sc == null)
		continue;
	    SelectionKey sk = sc.keyFor(selector);
	    // have to cancel so we can switch back to blocking I/O mode
	    if (sk != null)
		sk.cancel();
	    // switch back to blocking to allow normal I/O
	    sc.configureBlocking(true);
	    folder.idleAbort();	// send the DONE message
	    // watch for OK response to DONE
	    toWatch.add(folder);
	    more = true;
	}

	/*
	 * Now, process any channels with data to read.
	 */
	Set<SelectionKey> selectedKeys = selector.selectedKeys();
	for (SelectionKey sk : selectedKeys) {
	    selectedKeys.remove(sk);	// only process each key once
	    // have to cancel so we can switch back to blocking I/O mode
	    sk.cancel();
	    folder = (IMAPFolder)sk.attachment();
	    logger.log(Level.FINE,
		"IdleManager selected folder: {0}", folder);
	    SelectableChannel sc = sk.channel();
	    // switch back to blocking to allow normal I/O
	    sc.configureBlocking(true);
	    try {
		if (folder.handleIdle(false)) {
		    // more to do with this folder, select on it again
		    // XXX - what if we also added it above?
		    toWatch.add(folder);
		    more = true;
		} else {
		    // done watching this folder,
		    logger.log(Level.FINE,
			"IdleManager done watching folder {0}", folder);
		}
	    } catch (MessagingException ex) {
		// something went wrong, stop watching this folder
		logger.log(Level.FINE,
		    "IdleManager got exception for folder: " + folder,
		    ex);
	    }
	}
	return more;
    }

    /**
     * Stop watching all folders.  Cancel any selection keys and,
     * most importantly, switch the channel back to blocking mode.
     */
    private void unwatchAll() {
	Set<SelectionKey> keys = selector.keys();
	for (SelectionKey sk : keys) {
	    // have to cancel so we can switch back to blocking I/O mode
	    sk.cancel();
	    IMAPFolder folder = (IMAPFolder)sk.attachment();
	    logger.log(Level.FINE,
		"IdleManager no longer watching folder: {0}", folder);
	    SelectableChannel sc = sk.channel();
	    // switch back to blocking to allow normal I/O
	    try {
		sc.configureBlocking(true);
	    } catch (IOException ex) {
		// ignore it, channel might be closed
	    }
	}
    }

    /**
     * Stop the IdleManager.  The IdleManager can not be restarted.
     */
    public synchronized void stop() {
	die = true;
	logger.finest("IdleManager stopping");
	selector.wakeup();
    }
}
