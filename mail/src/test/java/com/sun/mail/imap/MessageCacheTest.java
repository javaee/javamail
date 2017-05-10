/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.imap;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test the IMAP MessageCache.
 */
public class MessageCacheTest {
    /**
     * Test that when a message is expunged and a new message is added,
     * the new message has the expected sequence number.
     */
    @Test
    public void testExpungeAdd() throws Exception {
	// test a range of values to find boundary condition errors
	for (int n = 1; n <= 100; n++) {
	    //System.out.println("MessageCache.testExpungeAdd: test " + n);
	    // start with one message
	    MessageCache mc = new MessageCache(1, false);
	    // add the remaining messages (eat into SLOP)
	    mc.addMessages(n - 1, 2);
	    // now expunge a message to cause the seqnums array to be created
	    mc.expungeMessage(1);
	    // and add one more message
	    mc.addMessages(1, n);
	    //System.out.println("  new seqnum " + mc.seqnumOf(n + 1));
	    // does the new message have the expected sequence number?
	    assertEquals(mc.seqnumOf(n + 1), n);
	}
    }

    /**
     * Test that when a message is expunged and new messages are added,
     * the new messages have the expected sequence number.  Similar to
     * the above, but the seqnums array is created first, then expanded.
     */
    @Test
    public void testExpungeAddExpand() throws Exception {
	// test a range of values to find boundary condition errors
	for (int n = 2; n <= 100; n++) {
	    //System.out.println("MessageCache.testExpungeAdd: test " + n);
	    // start with two messages
	    MessageCache mc = new MessageCache(2, false);
	    // now expunge a message to cause the seqnums array to be created
	    mc.expungeMessage(1);
	    // add the remaining messages (eat into SLOP)
	    mc.addMessages(n - 1, 2);
	    //System.out.println("  new seqnum " + mc.seqnumOf(n + 1));
	    // does the new message have the expected sequence number?
	    assertEquals(mc.seqnumOf(n + 1), n);
	}
    }
}
