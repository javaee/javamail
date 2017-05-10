/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.mail.iap.Response;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Test the ENVELOPE class.
 */
public class EnvelopeTest {
    /**
     * Test workaround for Yahoo IMAP bug that returns a bogus space
     * character when one of the recipients is "undisclosed-recipients".
     */
    @Test
    public void testYahooUndisclosedRecipientsBug() throws Exception {
	IMAPResponse response = new IMAPResponse(
    "* 2 FETCH (INTERNALDATE \"24-Apr-2012 20:28:58 +0000\" " +
    "RFC822.SIZE 155937 " +
    "ENVELOPE (\"Wed, 28 Sep 2011 11:16:17 +0100\" \"test\" " +
    "((NIL NIL \"xxx\" \"tju.edu.cn\")) " +
    "((NIL NIL \"xxx\" \"gmail.com\")) " +
    "((NIL NIL \"xxx\" \"tju.edu.cn\")) " +
    "((\"undisclosed-recipients\" NIL " +
	"\"\\\"undisclosed-recipients\\\"\" NIL )) " +
    // here's the space inserted by Yahoo IMAP ^
    "NIL NIL NIL " +
    "\"<xxx@mail.gmail.com>\"))");
	FetchResponse fr = new FetchResponse(response);
	// no exception means it worked
    }

    /**
     * Test workaround for Yahoo IMAP bug that returns an empty list
     * instad of NIL for some addresses in ENVELOPE response.
     */
    @Test
    public void testYahooEnvelopeAddressListBug() throws Exception {
	IMAPResponse response = new IMAPResponse(
    "* 2 FETCH (RFC822.SIZE 2567 INTERNALDATE \"29-Apr-2011 13:49:01 +0000\" " +
    "ENVELOPE (\"Fri, 29 Apr 2011 19:19:01 +0530\" \"test\" " +
    "((\"xxx\" NIL \"xxx\" \"milium.com.br\")) " +
    "((\"xxx\" NIL \"xxx\" \"milium.com.br\")) " +
    "((NIL NIL \"xxx\" \"live.hk\")) () NIL NIL NIL " +
    "\"<20110429134718.70333732030A@mail2.milium.com.br>\"))");
	FetchResponse fr = new FetchResponse(response);
	// no exception means it worked
    }
}
