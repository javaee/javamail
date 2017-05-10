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

package javax.mail.internet;

import java.io.*;
import java.util.*;
import javax.mail.internet.InternetAddress;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test InternetAddress folding.
 *
 * @author Bill Shannon
 */

@RunWith(Parameterized.class)
public class InternetAddressFoldTest {
    private InternetAddress[] orig;
    private String expect;

    private static List<Object[]> testData;

    public InternetAddressFoldTest(InternetAddress[] orig, String expect) {
	this.orig = orig;
	this.expect = expect;
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
	testData = new ArrayList<>();
	parse(new BufferedReader(new InputStreamReader(
	    InternetAddressFoldTest.class.getResourceAsStream("addrfolddata"))));
	return testData;
    }

    /**
     * Read the data from the test file.  Format is:
     *
     * FOLD N
     * address1$
     * ...
     * addressN$
     * EXPECT
     * address1, ..., addressN$
     */
    private static void parse(BufferedReader in) throws Exception {
	String line;
	while ((line = in.readLine()) != null) {
	    if (line.startsWith("#") || line.length() == 0)
		continue;
	    if (!line.startsWith("FOLD"))
		throw new IOException("TEST DATA FORMAT ERROR, MISSING FOLD");
	    int count = Integer.parseInt(line.substring(5));
	    InternetAddress[] orig = new InternetAddress[count];
	    for (int i = 0; i < count; i++)
		orig[i] = new InternetAddress(readString(in));
	    String e = in.readLine();
	    if (!e.equals("EXPECT"))
		throw new IOException("TEST DATA FORMAT ERROR, MISSING EXPECT");
	    String expect = readString(in);
	    testData.add(new Object[] { orig, expect });
	}
    }

    /**
     * Read a string that ends with '$', preserving all characters,
     * especially including CR and LF.
     */
    private static String readString(BufferedReader in) throws IOException {
	StringBuffer sb = new StringBuffer();
	int c;
	while ((c = in.read()) != '$')
	    sb.append((char)c);
	in.readLine();	// throw away rest of line
	return sb.toString();
    }

    @Test
    public void testFold() {
	Assert.assertEquals("Fold", expect, InternetAddress.toString(orig, 0));
    }
}
