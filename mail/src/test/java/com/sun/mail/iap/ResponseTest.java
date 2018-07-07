/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.iap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

/**
 * Test response parsing.
 */
public class ResponseTest {
    // timeout the test in case of infinite loop
    @Rule
    public Timeout timeout = Timeout.seconds(5);

    private static String[] atomTests = {
	"atom", "atom ", "atom(", "atom)", "atom{", "atom*", "atom%",
	"atom\"", "atom\\ ", "atom]", "atom\001", "atom\177"
    };

    private static String[] astringTests = {
	"atom", "atom ", "atom(", "atom)", "atom{", "atom*", "atom%",
	"atom\"", "atom\\ ", "atom\001", "atom\177", "\"atom\"",
	"{4}\r\natom"
    };

    /**
     * Test parsing atoms.
     */
    @Test
    public void testAtom() throws Exception {
	for (String s : atomTests) {
	    Response r = new Response("* " + s);
	    assertEquals("atom", r.readAtom());
	}
	for (String s : atomTests) {
	    Response r = new Response("* " + s + " ");
	    assertEquals("atom", r.readAtom());
	}
    }

    /**
     * Test parsing astrings.
     */
    @Test
    public void testAString() throws Exception {
	for (String s : astringTests) {
	    Response r = new Response("* " + s);
	    assertEquals("atom", r.readAtomString());
	}
	for (String s : astringTests) {
	    Response r = new Response("* " + s + " ");
	    assertEquals("atom", r.readAtomString());
	}
    }

    /**
     * Test the special case where an astring can include ']'.
     */
    @Test
    public void testAStringSpecial() throws Exception {
	Response r = new Response("* " + "atom] ");
	assertEquals("atom]", r.readAtomString());
    }

    /**
     * Test astring lists.
     */
    @Test
    public void testAStringList() throws Exception {
	Response r = new Response("* " + "(A B C)");
	assertArrayEquals(new String[] { "A", "B", "C" },
			    r.readAtomStringList());
    }

    @Test
    public void testAStringListInitialSpace() throws Exception {
	Response r = new Response("* " + "( A B C)");
	assertArrayEquals(new String[] { "A", "B", "C" },
			    r.readAtomStringList());
    }

    @Test
    public void testAStringListTrailingSpace() throws Exception {
	Response r = new Response("* " + "(A B C )");
	assertArrayEquals(new String[] { "A", "B", "C" },
			    r.readAtomStringList());
    }

    @Test
    public void testAStringListInitialAndTrailingSpace() throws Exception {
	Response r = new Response("* " + "( A B C )");
	assertArrayEquals(new String[] { "A", "B", "C" },
			    r.readAtomStringList());
    }

    @Test
    public void testAStringListMultipleSpaces() throws Exception {
	Response r = new Response("* " + "(A  B    C)");
	assertArrayEquals(new String[] { "A", "B", "C" },
			    r.readAtomStringList());
    }

    @Test
    public void testAStringListQuoted() throws Exception {
	Response r = new Response("* " + "(A B \"C\")");
	assertArrayEquals(new String[] { "A", "B", "C" },
			    r.readAtomStringList());
    }

    /**
     * Test astring lists with more data following.
     */
    @Test
    public void testAStringListMore() throws Exception {
	Response r = new Response("* " + "(A B \"C\") atom");
	assertArrayEquals(new String[] { "A", "B", "C" },
			    r.readAtomStringList());
	assertEquals("atom", r.readAtomString());
    }

    /**
     * Test empty astring lists.
     */
    @Test
    public void testAStringListEmpty() throws Exception {
	Response r = new Response("* " + "()");
	assertArrayEquals(new String[0], r.readAtomStringList());
    }

    /**
     * Test empty astring lists with more data following.
     */
    @Test
    public void testAStringListEmptyMore() throws Exception {
	Response r = new Response("* " + "() atom");
	assertArrayEquals(new String[0], r.readAtomStringList());
	assertEquals("atom", r.readAtomString());
    }

    /**
     * Test readStringList
     */
    @Test
    public void testBadStringList() throws Exception {
	Response response = new Response(
			    "* (\"name\", \"test\", \"version\", \"1.0\")");
        String[] list = response.readStringList();
	// anything other than an infinite loop timeout is considered success
    }
}
