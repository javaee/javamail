/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.imap.protocol;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * This class holds the 'start' and 'end' for a range of UIDs.
 * Just like MessageSet except using long instead of int.
 */
public class UIDSet {

    public long start;
    public long end;

    public UIDSet() { }

    public UIDSet(long start, long end) {
	this.start = start;
	this.end = end;
    }

    /**
     * Count the total number of elements in a UIDSet
     */
    public long size() {
	return end - start + 1;
    }

    /**
     * Convert an array of longs into an array of UIDSets
     */
    public static UIDSet[] createUIDSets(long[] uids) {
	if (uids == null)
	    return null;
	List<UIDSet> v = new ArrayList<UIDSet>();
	int i,j;

	for (i=0; i < uids.length; i++) {
	    UIDSet ms = new UIDSet();
	    ms.start = uids[i];

	    // Look for contiguous elements
	    for (j=i+1; j < uids.length; j++) {
		if (uids[j] != uids[j-1] +1)
		    break;
	    }
	    ms.end = uids[j-1];
	    v.add(ms);
	    i = j-1; // i gets incremented @ top of the loop
	}
	UIDSet[] uidset = new UIDSet[v.size()];	
	return v.toArray(uidset);
    }

    /**
     * Parse a string in IMAP UID range format.
     *
     * @since	JavaMail 1.5.1
     */
    public static UIDSet[] parseUIDSets(String uids) {
	if (uids == null)
	    return null;
	List<UIDSet> v = new ArrayList<UIDSet>();
	StringTokenizer st = new StringTokenizer(uids, ",:", true);
	long start = -1;
	UIDSet cur = null;
	try {
	    while(st.hasMoreTokens()) {
		String s = st.nextToken();
		if (s.equals(",")) {
		    if (cur != null)
			v.add(cur);
		    cur = null;
		} else if (s.equals(":")) {
		    // nothing to do, wait for next number
		} else {	// better be a number
		    long n = Long.parseLong(s);
		    if (cur != null)
			cur.end = n;
		    else
			cur = new UIDSet(n, n);
		}
	    }
	} catch (NumberFormatException nex) {
	    // give up and return what we have so far
	}
	if (cur != null)
	    v.add(cur);
	UIDSet[] uidset = new UIDSet[v.size()];
	return v.toArray(uidset);
    }

    /**
     * Convert an array of UIDSets into an IMAP sequence range.
     */
    public static String toString(UIDSet[] uidset) {
	if (uidset == null)
	    return null;
	if (uidset.length == 0) // Empty uidset
	    return "";

	int i = 0;  // uidset index
	StringBuilder s = new StringBuilder();
	int size = uidset.length;
	long start, end;

	for (;;) {
	    start = uidset[i].start;
	    end = uidset[i].end;

	    if (end > start)
		s.append(start).append(':').append(end);
	    else // end == start means only one element
		s.append(start);
	
	    i++; // Next UIDSet
	    if (i >= size) // No more UIDSets
		break;
	    else
		s.append(',');
	}
	return s.toString();
    }

    /**
     * Convert an array of UIDSets into a array of long UIDs.
     *
     * @since	JavaMail 1.5.1
     */
    public static long[] toArray(UIDSet[] uidset) {
	//return toArray(uidset, -1);
	if (uidset == null)
	    return null;
	long[] uids = new long[(int)UIDSet.size(uidset)];
	int i = 0;
	for (UIDSet u : uidset) {
	    for (long n = u.start; n <= u.end; n++)
		uids[i++] = n;
	}
	return uids;
    }

    /**
     * Convert an array of UIDSets into a array of long UIDs.
     * Don't include any UIDs larger than uidmax.
     *
     * @since	JavaMail 1.5.1
     */
    public static long[] toArray(UIDSet[] uidset, long uidmax) {
	if (uidset == null)
	    return null;
	long[] uids = new long[(int)UIDSet.size(uidset, uidmax)];
	int i = 0;
	for (UIDSet u : uidset) {
	    for (long n = u.start; n <= u.end; n++) {
		if (uidmax >= 0 && n > uidmax)
		    break;
		uids[i++] = n;
	    }
	}
	return uids;
    }

    /**
     * Count the total number of elements in an array of UIDSets.
     */
    public static long size(UIDSet[] uidset) {
	long count = 0;

	if (uidset != null)
	    for (UIDSet u : uidset)
		count += u.size();
	
	return count;
    }

    /**
     * Count the total number of elements in an array of UIDSets.
     * Don't count UIDs greater then uidmax.
     *
     * @since	JavaMail 1.5.1
     */
    private static long size(UIDSet[] uidset, long uidmax) {
	long count = 0;

	if (uidset != null)
	    for (UIDSet u : uidset) {
		if (uidmax < 0)
		    count += u.size();
		else if (u.start <= uidmax) {
		    if (u.end < uidmax)
			count += u.end - u.start + 1;
		    else
			count += uidmax - u.start + 1;
		}
	    }
	
	return count;
    }
}
