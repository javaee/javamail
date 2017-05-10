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

package javax.mail;

import java.util.EventListener;
import java.util.Vector;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executor;
import javax.mail.event.MailEvent;

/**
 * Package private class used by Store & Folder to dispatch events.
 * This class implements an event queue, and a dispatcher thread that
 * dequeues and dispatches events from the queue.
 *
 * @author	Bill Shannon
 */
class EventQueue implements Runnable {

    private volatile BlockingQueue<QueueElement> q;
    private Executor executor;

    private static WeakHashMap<ClassLoader,EventQueue> appq;

    /**
     * A special event that causes the queue processing task to terminate.
     */
    static class TerminatorEvent extends MailEvent {
	private static final long serialVersionUID = -2481895000841664111L;

	TerminatorEvent() {
	    super(new Object());
	}

	@Override
	public void dispatch(Object listener) {
	    // Kill the event dispatching thread.
	    Thread.currentThread().interrupt();
	}
    }

    /**
     * A "struct" to put on the queue.
     */
    static class QueueElement {
	MailEvent event = null;
	Vector<? extends EventListener> vector = null;

	QueueElement(MailEvent event, Vector<? extends EventListener> vector) {
	    this.event = event;
	    this.vector = vector;
	}
    }

    /**
     * Construct an EventQueue using the specified Executor.
     * If the Executor is null, threads will be created as needed.
     */
    EventQueue(Executor ex) {
	this.executor = ex;
    }

    /**
     * Enqueue an event.
     */
    synchronized void enqueue(MailEvent event,
	    Vector<? extends EventListener> vector) {
	// if this is the first event, create the queue and start the event task
	if (q == null) {
	    q = new LinkedBlockingQueue<>();
	    if (executor != null) {
		executor.execute(this);
	    } else {
		Thread qThread = new Thread(this, "JavaMail-EventQueue");
		qThread.setDaemon(true);  // not a user thread
		qThread.start();
	    }
	}
	q.add(new QueueElement(event, vector));
    }

    /**
     * Terminate the task running the queue, but only if there is a queue.
     */
    synchronized void terminateQueue() {
	if (q != null) {
	    Vector<EventListener> dummyListeners = new Vector<>();
	    dummyListeners.setSize(1); // need atleast one listener
	    q.add(new QueueElement(new TerminatorEvent(), dummyListeners));
	    q = null;
	}
    }

    /**
     * Create (if necessary) an application-scoped event queue.
     * Application scoping is based on the thread's context class loader.
     */
    static synchronized EventQueue getApplicationEventQueue(Executor ex) {
	ClassLoader cl = Session.getContextClassLoader();
	if (appq == null)
	    appq = new WeakHashMap<>();
	EventQueue q = appq.get(cl);
	if (q == null) {
	    q = new EventQueue(ex);
	    appq.put(cl, q);
	}
	return q;
    }

    /**
     * Pull events off the queue and dispatch them.
     */
    @Override
    public void run() {

	BlockingQueue<QueueElement> bq = q;
	if (bq == null)
	    return;
	try {
	    loop:
	    for (;;) {
		// block until an item is available
		QueueElement qe = bq.take();
		MailEvent e = qe.event;
		Vector<? extends EventListener> v = qe.vector;

		for (int i = 0; i < v.size(); i++)
		    try {
			e.dispatch(v.elementAt(i));
		    } catch (Throwable t) {
			if (t instanceof InterruptedException)
			    break loop;
			// ignore anything else thrown by the listener
		    }

		qe = null; e = null; v = null;
	    }
	} catch (InterruptedException e) {
	    // just die
	}
    }
}
