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

public class DefaultMailbox extends Mailbox {
    private final String home;

    private static final boolean homeRelative =
				Boolean.getBoolean("mail.mbox.homerelative");

    public DefaultMailbox() {
	home = System.getProperty("user.home");
    }

    public MailFile getMailFile(String user, String folder) {
	return new DefaultMailFile(filename(user, folder));
    }

    public String filename(String user, String folder) {
	try {
	    char c = folder.charAt(0);
	    if (c == File.separatorChar) {
		return folder;
	    } else if (c == '~') {
		int i = folder.indexOf(File.separatorChar);
		String tail = "";
		if (i > 0) {
		    tail = folder.substring(i);
		    folder = folder.substring(0, i);
		}
		return home + tail;
	    } else {
		if (folder.equalsIgnoreCase("INBOX"))
		    folder = "INBOX";
		if (homeRelative)
		    return home + File.separator + folder;
		else
		    return folder;
	    }
	} catch (StringIndexOutOfBoundsException e) {
	    return folder;
	}
    }
}

class DefaultMailFile extends File implements MailFile {
    protected transient RandomAccessFile file;

    private static final long serialVersionUID = 3713116697523761684L;

    DefaultMailFile(String name) {
	super(name);
    }

    public boolean lock(String mode) {
	try {
	    file = new RandomAccessFile(this, mode);
	    return true;
	} catch (FileNotFoundException fe) {
	    return false;
	} catch (IOException ie) {
	    file = null;
	    return false;
	}
    }

    public void unlock() { 
	if (file != null) {
	    try {
		file.close();
	    } catch (IOException e) {
		// ignore it
	    }
	    file = null;
	}
    }

    public void touchlock() {
    }

    public FileDescriptor getFD() {
	if (file == null)
	    return null;
	try {
	    return file.getFD();
	} catch (IOException e) {
	    return null;
	}
    }
}
