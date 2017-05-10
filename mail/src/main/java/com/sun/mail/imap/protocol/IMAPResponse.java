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

package com.sun.mail.imap.protocol;

import java.io.*;
import java.util.*;
import com.sun.mail.util.*;
import com.sun.mail.iap.*;

/**
 * This class represents a response obtained from the input stream
 * of an IMAP server.
 *
 * @author  John Mani
 */

public class IMAPResponse extends Response {
    private String key;
    private int number;

    public IMAPResponse(Protocol c) throws IOException, ProtocolException {
	super(c);
	init();
    }

    private void init() throws IOException, ProtocolException {
	// continue parsing if this is an untagged response
	if (isUnTagged() && !isOK() && !isNO() && !isBAD() && !isBYE()) {
	    key = readAtom();

	    // Is this response of the form "* <number> <command>"
	    try {
		number = Integer.parseInt(key);
		key = readAtom();
	    } catch (NumberFormatException ne) { }
	}
    }

    /**
     * Copy constructor.
     *
     * @param	r	the IMAPResponse to copy
     */
    public IMAPResponse(IMAPResponse r) {
	super((Response)r);
	key = r.key;
	number = r.number;
    }

    /**
     * For testing.
     *
     * @param	r	the response string
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public IMAPResponse(String r) throws IOException, ProtocolException {
	this(r, true);
    }

    /**
     * For testing.
     *
     * @param	r	the response string
     * @param	utf8	UTF-8 allowed?
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.6.0
     */
    public IMAPResponse(String r, boolean utf8)
				throws IOException, ProtocolException {
	super(r, utf8);
	init();
    }

    /**
     * Read a list of space-separated "flag-extension" sequences and 
     * return the list as a array of Strings. An empty list is returned
     * as null.  Each item is expected to be an atom, possibly preceeded
     * by a backslash, but we aren't that strict; we just look for strings
     * separated by spaces and terminated by a right paren.  We assume items
     * are always ASCII.
     *
     * @return	the list items as a String array
     */
    public String[] readSimpleList() {
	skipSpaces();

	if (buffer[index] != '(') // not what we expected
	    return null;
	index++; // skip '('

	List<String> v = new ArrayList<>();
	int start;
	for (start = index; buffer[index] != ')'; index++) {
	    if (buffer[index] == ' ') { // got one item
		v.add(ASCIIUtility.toString(buffer, start, index));
		start = index+1; // index gets incremented at the top
	    }
	}
	if (index > start) // get the last item
	    v.add(ASCIIUtility.toString(buffer, start, index));
	index++; // skip ')'
	
	int size = v.size();
	if (size > 0)
	    return v.toArray(new String[size]);
	else  // empty list
	    return null;
    }

    public String getKey() {
	return key;
    }

    public boolean keyEquals(String k) {
	if (key != null && key.equalsIgnoreCase(k))
	    return true;
	else
	    return false;
    }

    public int getNumber() {
	return number;
    }
}
