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

package com.sun.mail.mbox;

import java.io.*;

public class UNIXInbox extends UNIXFolder implements InboxFile {
    private final String user;

    private static final long serialVersionUID = 651261842162777620L;

    /*
     * Superclass UNIXFile loads the library containing all the
     * native code and sets the "loaded" flag if successful.
     */

    public UNIXInbox(String user, String name) {
	super(name);
	this.user = user;
	if (user == null)
	    throw new NullPointerException("user name is null in UNIXInbox");
    }

    public boolean lock(String mode) {
	if (lockType == NATIVE) {
	    if (!loaded)
		return false;
	    if (!maillock(user, 5))
		return false;
	}
	if (!super.lock(mode)) {
	    if (loaded)
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
    private transient String lockfileName;	// its name

    public boolean openLock(String mode) {
	if (mode.equals("r"))
	    return true;
	if (lockfileName == null) {
	    String home = System.getProperty("user.home");
	    lockfileName = home + File.separator + ".Maillock";
	}
	try {
	    lockfile = new RandomAccessFile(lockfileName, mode);
	    boolean ret;
	    switch (lockType) {
	    case NONE:
		ret = true;
		break;
	    case NATIVE:
	    default:
		ret = UNIXFile.lock(lockfile.getFD(), mode);
		break;
	    case JAVA:
		ret = lockfile.getChannel().
		    tryLock(0L, Long.MAX_VALUE, !mode.equals("rw")) != null;
		break;
	    }
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

    public boolean equals(Object o) {
	if (!(o instanceof UNIXInbox))
	    return false;
	UNIXInbox other = (UNIXInbox)o;
	return user.equals(other.user) && super.equals(other);
    }

    public int hashCode() {
	return super.hashCode() + user.hashCode();
    }

    private native boolean maillock(String user, int retryCount);
    private native void mailunlock();
    private native void touchlock0();
}
