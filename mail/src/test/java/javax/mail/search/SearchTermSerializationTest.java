/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.mail.search;

import java.io.*;
import java.util.Date;

import javax.mail.*;
import javax.mail.search.*;
import javax.mail.internet.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * SearchTerm serialization test.
 *
 * @author Bill Shannon
 */

public class SearchTermSerializationTest {

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
	// construct a SearchTerm using all SearchTerm types
	SearchTerm term = new AndTerm(new SearchTerm[] {
	    new BodyTerm("text"),
	    new FlagTerm(new Flags(Flags.Flag.RECENT), true),
	    new FromStringTerm("foo@bar"),
	    new HeaderTerm("X-Mailer", "dtmail"),
	    new MessageIDTerm("12345@sun.com"),
	    new MessageNumberTerm(42),
	    new NotTerm(
		new OrTerm(
		    new ReceivedDateTerm(ReceivedDateTerm.LT, new Date()),
		    new RecipientStringTerm(Message.RecipientType.CC, "foo")
		)
	    ),
	    new RecipientTerm(MimeMessage.RecipientType.NEWSGROUPS,
				new NewsAddress("comp.lang.java", "newshost")),
	    new SentDateTerm(SentDateTerm.NE, new Date()),
	    new SizeTerm(SizeTerm.LT, 1000),
	    new SubjectTerm("test")
	});

	// serialize it to a byte array
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	ObjectOutputStream oos = new ObjectOutputStream(bos);
	oos.writeObject(term);
	bos.close();

	// read it back in
	ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
	ObjectInputStream ois = new ObjectInputStream(bis);
	SearchTerm term2 = (SearchTerm)ois.readObject();

	// compare it with the original
	assertEquals(term, term2);
    }
}
