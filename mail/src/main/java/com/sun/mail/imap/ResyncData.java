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

package com.sun.mail.imap;

import com.sun.mail.imap.protocol.UIDSet;

/**
 * Resynchronization data as defined by the QRESYNC extension
 * (<A HREF="http://www.ietf.org/rfc/rfc5162.txt">RFC 5162</A>).
 * An instance of <CODE>ResyncData</CODE> is supplied to the
 * {@link com.sun.mail.imap.IMAPFolder#open(int,com.sun.mail.imap.ResyncData)
 * IMAPFolder open} method.
 * The CONDSTORE <CODE>ResyncData</CODE> instance is used to enable the
 * CONDSTORE extension
 * (<A HREF="http://www.ietf.org/rfc/rfc4551.txt">RFC 4551</A>).
 * A <CODE>ResyncData</CODE> instance with uidvalidity and modseq values
 * is used to enable the QRESYNC extension.
 *
 * @since	JavaMail 1.5.1
 * @author	Bill Shannon
 */

public class ResyncData { 
    private long uidvalidity = -1;
    private long modseq = -1;
    private UIDSet[] uids = null;

    /**
     * Used to enable only the CONDSTORE extension.
     */
    public static final ResyncData CONDSTORE = new ResyncData(-1, -1);

    /**
     * Used to report on changes since the specified modseq.
     * If the UIDVALIDITY of the folder has changed, no message
     * changes will be reported.  The application must check the
     * UIDVALIDITY of the folder after open to make sure it's
     * the expected folder.
     *
     * @param	uidvalidity	the UIDVALIDITY
     * @param	modseq		the MODSEQ
     */
    public ResyncData(long uidvalidity, long modseq) {
	this.uidvalidity = uidvalidity;
	this.modseq = modseq;
	this.uids = null;
    }

    /**
     * Used to limit the reported message changes to those with UIDs
     * in the specified range.
     *
     * @param	uidvalidity	the UIDVALIDITY
     * @param	modseq		the MODSEQ
     * @param	uidFirst	the first UID
     * @param	uidLast		the last UID
     */
    public ResyncData(long uidvalidity, long modseq,
				long uidFirst, long uidLast) {
	this.uidvalidity = uidvalidity;
	this.modseq = modseq;
	this.uids = new UIDSet[] { new UIDSet(uidFirst, uidLast) };
    }

    /**
     * Used to limit the reported message changes to those with the
     * specified UIDs.
     *
     * @param	uidvalidity	the UIDVALIDITY
     * @param	modseq		the MODSEQ
     * @param	uids		the UID values
     */
    public ResyncData(long uidvalidity, long modseq, long[] uids) {
	this.uidvalidity = uidvalidity;
	this.modseq = modseq;
	this.uids = UIDSet.createUIDSets(uids);
    }

    /**
     * Get the UIDVALIDITY value specified when this instance was created.
     *
     * @return	the UIDVALIDITY value
     */
    public long getUIDValidity() {
	return uidvalidity;
    }

    /**
     * Get the MODSEQ value specified when this instance was created.
     *
     * @return	the MODSEQ value
     */
    public long getModSeq() {
	return modseq;
    }

    /*
     * Package private.  IMAPProtocol gets this data indirectly
     * using Utility.getResyncUIDSet().
     */
    UIDSet[] getUIDSet() {
	return uids;
    }
}
