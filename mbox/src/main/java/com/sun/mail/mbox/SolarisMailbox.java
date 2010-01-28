/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;

public class SolarisMailbox extends Mailbox {
    private String home;
    private String user;

    public SolarisMailbox() {
	home = System.getenv("HOME");
	if (home == null)
	    home = System.getProperty("user.home");
	user = System.getProperty("user.name");
    }

    public MailFile getMailFile(String user, String folder) {
	if (folder.equalsIgnoreCase("INBOX"))
	    return new UNIXInbox(user, filename(user, folder));
	else
	    return new UNIXFolder(filename(user, folder));
    }

    /**
     * Given a name of a mailbox folder, expand it to a full path name.
     */
    public String filename(String user, String folder) {
	try {
	    switch (folder.charAt(0)) {
	    case '/':
		return folder;
	    case '~':
		int i = folder.indexOf(File.separatorChar);
		String tail = "";
		if (i > 0) {
		    tail = folder.substring(i);
		    folder = folder.substring(0, i);
		}
		if (folder.length() == 1)
		    return home + tail;
		else
		    return "/home/" + folder.substring(1) + tail;	// XXX
	    default:
		if (folder.equalsIgnoreCase("INBOX")) {
		    if (user == null)	// XXX - should never happen
			user = this.user;
		    String inbox = System.getenv("MAIL");
		    if (inbox == null)
			inbox = "/var/mail/" + user;
		    return inbox;
		} else
		    return home + File.separator + folder;
	    }
	} catch (StringIndexOutOfBoundsException e) {
	    return folder;
	}
    }
}
