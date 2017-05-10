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

package com.sun.mail.imap;

import javax.mail.AuthenticationFailedException;

/**
 * A special kind of AuthenticationFailedException that indicates that
 * the reason for the failure was an IMAP REFERRAL in the response code.
 * See <a href="http://www.ietf.org/rfc/rfc2221.txt">RFC 2221</a> for details.
 *
 * @since JavaMail 1.5.5
 */

public class ReferralException extends AuthenticationFailedException {

    private String url;
    private String text;

    private static final long serialVersionUID = -3414063558596287683L;

    /**
     * Constructs an ReferralException with the specified URL and text.
     *
     * @param text	the detail message
     * @param url	the URL
     */
    public ReferralException(String url, String text) {
	super("[REFERRAL " + url + "] " + text);
	this.url = url;
	this.text = text;
    }

    /**
     * Return the IMAP URL in the referral.
     *
     * @return	the IMAP URL
     */
    public String getUrl() {
	return url;
    }

    /**
     * Return the text sent by the server along with the referral.
     *
     * @return	the text
     */
    public String getText() {
	return text;
    }
}
