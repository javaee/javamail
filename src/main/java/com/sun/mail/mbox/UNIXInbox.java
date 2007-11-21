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

package com.sun.mail.mbox;

import java.io.*;

public class UNIXInbox extends UNIXFolder implements InboxFile {
    String user;

    /*
     * Superclass UNIXFile loads the library containing all the
     * native code and sets the "loaded" flag if successful.
     */

    public UNIXInbox(String user, String name) {
	super(name);
	this.user = user;
    }

    public boolean lock(String mode) {
	if (!loaded)
	    return false;
	if (!maillock(user, 5))
	    return false;
	if (!super.lock(mode)) {
	    mailunlock();
	    return false;
	}
	return true;
    }

    public void unlock() { 
	super.unlock();
	if (loaded)
	    mailunlock();
    }

    public void touchlock() {
	if (loaded)
	    touchlock0();
    }

    private transient RandomAccessFile lockfile; // the user's ~/.Maillock file
    private String lockfileName;	// its name

    public boolean openLock(String mode) {
	if (mode.equals("r"))
	    return true;
	if (lockfileName == null) {
	    String home = System.getProperty("user.home");
	    lockfileName = home + File.separator + ".Maillock";
	}
	try {
	    lockfile = new RandomAccessFile(lockfileName, mode);
	    boolean ret = UNIXFile.lock(lockfile.getFD(), mode);
	    if (!ret)
		closeLock();
	    return ret;
	} catch (IOException ex) {
	}
	return false;
    }

    public void closeLock() {
	if (lockfile == null)
	    return;
	try {
	    lockfile.close();
	} catch (IOException ex) {
	} finally {
	    lockfile = null;
	}
    }

    private native boolean maillock(String user, int retryCount);
    private native void mailunlock();
    private native void touchlock0();
}
