/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.mail.auth;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * NTLM authentication using reflection with the jcifs classes.
 *
 * @author Luís Serralheiro
 */
public class Ntlm {

    private PrintStream debugout;	// if not null, debug output stream

    public Ntlm(PrintStream debugout) {
        this.debugout = debugout;
        // NtlmPasswordAuthentication.debugout = debugout;
    }

    public String generateType1Msg(boolean useUnicode, int flags,
            String domain, String workstation) throws IOException {
        try {
            Class t1MClass = Class.forName("jcifs.ntlmssp.Type1Message");
	    Constructor t1Mconstructor = t1MClass.getConstructor(
		new Class[] { int.class, String.class, String.class });
	    Object t1m = t1Mconstructor.newInstance(
		new Object[] { new Integer(flags), domain, workstation });
	    if (debugout != null)
		debugout.println("DEBUG NTLM: type 1 message: " + t1m);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            BASE64EncoderStream out =
                    new BASE64EncoderStream(bout, Integer.MAX_VALUE);
	    if (debugout != null)
		debugout.println("DEBUG NTLM: type 1 message length: " +
		    ((byte[]) t1MClass.getMethod("toByteArray",
			new Class[0]).invoke(t1m, null)).length);
            out.write((byte[]) t1MClass.getMethod("toByteArray",
                    new Class[0]).invoke(t1m, null));
            out.flush();
            out.close();
            return new String(bout.toByteArray());
        } catch (IOException ioex) {
	    if (debugout != null)
		ioex.printStackTrace(debugout);
	    throw ioex;		// pass it on
        } catch (Exception ex) {
	    if (debugout != null)
		ex.printStackTrace(debugout);
        }
        return null;
    }

    public String generateType3Msg(
            String username,
            String password,
            String domain,
            String workstation,
            String challenge,
	    int flags,
            int lmCompatibility) throws IOException {
        try {
            Object t2m;
            Class t2MClass = Class.forName("jcifs.ntlmssp.Type2Message");
            Constructor t2Mconstructor =
                    t2MClass.getConstructor(new Class[]{Class.forName("[B")});
            try {
                BASE64DecoderStream in = new BASE64DecoderStream(
                        new ByteArrayInputStream(challenge.getBytes()));
                byte[] bytes = new byte[in.available()];
                in.read(bytes);
                t2m = t2Mconstructor.newInstance(new Object[]{bytes});
            } catch (IOException ex) {
                IOException ioex = new IOException("Invalid Type2 message");
		ioex.initCause(ex);
                throw ioex;
            }
            Class t3MClass = Class.forName("jcifs.ntlmssp.Type3Message");
            Constructor t3Mconstructor = t3MClass.getConstructor(
                    new Class[]{
                        t2MClass,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
			int.class});
            Object t3m = t3Mconstructor.newInstance(new Object[]{
                        t2m,
                        password,
                        domain == null ? "" : domain,
                        username,
                        workstation,
			new Integer(flags)});

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            BASE64EncoderStream out =
                    new BASE64EncoderStream(bout, Integer.MAX_VALUE);
            out.write((byte[]) t3MClass.getMethod("toByteArray",
                    new Class[0]).invoke(t3m, null));
            out.flush();
            out.close();
            return new String(bout.toByteArray());
        } catch (InvocationTargetException itex) {
	    if (debugout != null)
		itex.printStackTrace(debugout);
	    Throwable t = itex.getTargetException();
	    if (t instanceof IOException)
		throw (IOException)t;
	    IOException ioex = new IOException(
				"Ntlm.generateType3Msg failed" +
				"; Exception: " + t);
	    ioex.initCause(itex);
	    throw ioex;
        } catch (IOException ioex) {
	    if (debugout != null)
		ioex.printStackTrace(debugout);
	    throw ioex;		// pass it on
        } catch (Exception ex) {
	    if (debugout != null)
		ex.printStackTrace(debugout);
        }
        return null;
    }
}
