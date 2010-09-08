/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.pop3;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test is connected.
 * 
 * @author sbo
 */
public final class POP3StoreTest {
    
    /**
     * Check is connected.
     */
    @Test
    public void testIsConnected() {
        POP3Server server = null;
        try {
            final POP3Handler handler = new POP3HandlerNoopErr();
            server = new POP3Server(handler, 26421);
            server.start();
            Thread.sleep(1000);
            
            final Properties properties = new Properties();
            properties.setProperty("mail.pop3.host", "localhost");
            properties.setProperty("mail.pop3.port", "26421");
            final Session session = Session.getInstance(properties);
            //session.setDebug(true);
            
            final Store store = session.getStore("pop3");
            try {
                store.connect("test", "test");
                final Folder folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
                
                // Check
                assertFalse(folder.isOpen());
            } finally {
                store.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (server != null) {
                server.quit();
            }
        }
    }
    
    /**
     * Custom handler. Returns ERR for NOOP.
     * 
     * @author sbo
     */
    private static final class POP3HandlerNoopErr extends POP3Handler {
        
        /**
         * {@inheritDoc}
         */
	@Override
        public void noop() throws IOException {
            this.println("-ERR");
        }
    }
}
