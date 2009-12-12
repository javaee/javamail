/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2009 Jason Mehrens. All rights reserved.
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

package com.sun.mail.util.logging;

import java.lang.reflect.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;
import javax.mail.*;
import junit.framework.TestCase;

/**
 *
 * @author jason.mehrens
 */
public class MailHandlerTest extends TestCase {

    public MailHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testIsLoggable() {
        //System.out.println("isLoggable");
        Level[] lvls = getAllLevels();
        if (lvls.length > 0) {
            LogRecord record = new LogRecord(Level.INFO, "");
            for (int i = 0; i < lvls.length; i++) {
                testLoggable(lvls[i], null);
                testLoggable(lvls[i], record);
            }
        } else {
            fail("No predefined levels.");
        }
    }

    private void testLoggable(Level lvl, LogRecord record) {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.setLevel(lvl);
        ConsoleHandler console = null;
        boolean result = false;
        boolean expect = true;
        try {
            result = instance.isLoggable(record);
            console = new ConsoleHandler();
            console.setErrorManager(em);
            console.setLevel(lvl);
            expect = console.isLoggable(record);
        } catch (RuntimeException mailEx) {
            try {
                if (console == null) {
                    console.isLoggable(record);
                    fail("MailHandler threw and exception: " + mailEx);
                } else {
                    result = true;
                    expect = true;
                }
            } catch (RuntimeException consoleEx) {
                assertEquals(mailEx.getClass(), consoleEx.getClass());
                result = false;
                expect = false;
            }
        }
        assertEquals(expect, result);

        instance.setLevel(Level.INFO);
        instance.setFilter(BooleanFilter.FALSE);
        instance.setAttachmentFormatters(new Formatter[]{new SimpleFormatter(), new XMLFormatter()});
        //null filter makes all records INFO and above loggable.
        instance.setAttachmentFilters(new Filter[]{BooleanFilter.FALSE, null});
        assertEquals(instance.isLoggable(new LogRecord(Level.FINEST, "")), false);
        assertEquals(instance.isLoggable(new LogRecord(Level.INFO, "")), true);
        assertEquals(instance.isLoggable(new LogRecord(Level.WARNING, "")), true);
        assertEquals(instance.isLoggable(new LogRecord(Level.SEVERE, "")), true);

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testPublish() {
        //System.out.println("publish");
        Level[] lvls = getAllLevels();

        MailHandler instance = new MailHandler(lvls.length + 2);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);
        instance.setLevel(Level.ALL);
        instance.setFilter(null);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter(null);

        for (int i = 0; i < lvls.length; i++) {
            instance.publish(new LogRecord(lvls[i], ""));
        }

        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();

        assertEquals(em.exceptions.size(), 1);
        assertEquals(em.exceptions.get(0) instanceof MessagingException, true);
    }

    public void testBadFormatters() {
        MailHandler instance = new MailHandler();
        instance.setLevel(Level.ALL);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setComparator(new ThrowComparator());
        instance.setFormatter(new ThrowFormatter());
        instance.setSubject(new ThrowFormatter());
        instance.setAttachmentFormatters(new Formatter[]{new ThrowFormatter()});
        instance.setAttachmentNames(new Formatter[]{new ThrowFormatter()});

        LogRecord record = new LogRecord(Level.INFO, "");
        instance.publish(record);
        instance.close();

        assertEquals(!em.exceptions.isEmpty(), true);
    }

    public void testBadFilters() {
        LogRecord record = new LogRecord(Level.INFO, "");
        ConsoleHandler console = new ConsoleHandler();
        console.setFilter(new ThrowFilter());
        MailHandler instance = null;
        try {
            boolean expect = console.isLoggable(record);
            instance = new MailHandler();
            instance.setLevel(Level.ALL);
            instance.setFilter(new ThrowFilter());
            boolean result = instance.isLoggable(record);
            assertEquals(expect, result);
        }
        catch(RuntimeException expectEx) {
            if(instance == null) {
                try {
                    instance = new MailHandler();
                    instance.setLevel(Level.ALL);
                    instance.setFilter(new ThrowFilter());
                    instance.isLoggable(record);
                    fail("Doesn't match the console handler.");
                }
                catch(RuntimeException resultEx) {
                    assertEquals(resultEx.getClass(), expectEx.getClass());
                }
            }
            else {
                fail("Doesn't match the console handler.");
            }
        }
        instance.setFilter(null);

        
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.setAttachmentFormatters(new Formatter[]{new SimpleFormatter()});
        instance.setAttachmentFilters(new Filter[]{new ThrowFilter()});
        instance.setAttachmentNames(new String[]{"test.txt"});

        instance.publish(record);
        instance.close();

        assertEquals(!em.exceptions.isEmpty(), true);
    }

    public void testPushInsidePush() {
        //System.out.println("PushInsidePush");
        Level[] lvls = getAllLevels();

        MailHandler instance = new MailHandler(lvls.length + 2);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);
        instance.setLevel(Level.ALL);
        instance.setFilter(null);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter(null);

        instance.setFormatter(new SimpleFormatter() {

            public String getHead(Handler h) {
                try {
                    h.flush();
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String getTail(Handler h) {
                final Filter filter = h.getFilter();
                try {
                    h.setFilter(filter);
                } catch (Throwable T) {
                    fail(T.toString());
                }

                final Level lvl = h.getLevel();
                try {
                    h.setLevel(lvl);
                } catch (Throwable T) {
                    fail(T.toString());
                }

                final String enc = h.getEncoding();
                try {
                    h.setEncoding(enc);
                } catch (Throwable T) {
                    fail(T.toString());
                }


                try {
                    h.setFormatter(new SimpleFormatter());
                } catch (Throwable T) {
                    fail(T.toString());
                }

                try {
                    h.close();
                    assertEquals(h.getLevel(), Level.OFF);
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getTail(h);
            }
        });


        Formatter push = new SimpleFormatter() {

            public String getHead(Handler h) {
                try {
                    ((MailHandler) h).push();
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String getTail(Handler h) {
                try {
                    ((MailHandler) h).push();
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getTail(h);
            }
        };

        Formatter atFor = new SimpleFormatter() {

            public String getHead(Handler h) {
                MailHandler mh = (MailHandler) h;
                Formatter[] f = mh.getAttachmentFormatters();
                try {
                    mh.setAttachmentFormatters(f);
                    fail("Mutable formatter.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String getTail(Handler h) {
                getHead(h);
                return super.getTail(h);
            }
        };

        Formatter atName = new SimpleFormatter() {

            public String getHead(Handler h) {
                MailHandler mh = (MailHandler) h;
                Formatter[] f = mh.getAttachmentNames();
                try {
                    mh.setAttachmentNames(f);
                    fail("Mutable formatter.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String getTail(Handler h) {
                getHead(h);
                return super.getTail(h);
            }
        };

        Formatter atFilter = new SimpleFormatter() {

            public String getHead(Handler h) {
                MailHandler mh = (MailHandler) h;
                Filter[] f = mh.getAttachmentFilters();
                try {
                    mh.setAttachmentFilters(f);
                    fail("Mutable filters.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String getTail(Handler h) {
                getHead(h);
                return super.getTail(h);
            }
        };

        Formatter nameComp = new Formatter() {

            public String getHead(Handler h) {
                MailHandler mh = (MailHandler) h;
                Comparator c = mh.getComparator();
                try {
                    mh.setComparator(c);
                    fail("Mutable comparator.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String format(LogRecord r) {
                return "";
            }

            public String getTail(Handler h) {
                getHead(h);
                return "name.txt";
            }
        };

        Formatter nameMail = new Formatter() {

            public String getHead(Handler h) {
                MailHandler mh = (MailHandler) h;
                Properties props = mh.getMailProperties();
                try {
                    mh.setMailProperties(props);
                    fail("Mutable props.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String format(LogRecord r) {
                return "";
            }

            public String getTail(Handler h) {
                getHead(h);
                return "name.txt";
            }
        };

        Formatter nameSub = new Formatter() {

            public String getHead(Handler h) {
                MailHandler mh = (MailHandler) h;
                Formatter f = mh.getSubject();
                try {
                    mh.setSubject(f);
                    fail("Mutable subject.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String format(LogRecord r) {
                return "";
            }

            public String getTail(Handler h) {
                getHead(h);
                return "name.txt";
            }
        };

        Formatter nameAuth = new Formatter() {

            public String getHead(Handler h) {
                MailHandler mh = (MailHandler) h;
                Authenticator a = mh.getAuthenticator();
                try {
                    mh.setAuthenticator(a);
                    fail("Mutable Authenticator.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            public String format(LogRecord r) {
                return "";
            }

            public String getTail(Handler h) {
                getHead(h);
                return "name.txt";
            }
        };

        instance.setAttachmentFormatters(new Formatter[]{push, atFor, atName, atFilter});
        instance.setAttachmentNames(new Formatter[]{nameComp, nameMail, nameSub, nameAuth});

        for (int i = 0; i < lvls.length; i++) {
            instance.publish(new LogRecord(lvls[i], ""));
        }
        instance.flush();

        for (int i = 0; i < em.exceptions.size(); i++) {
            assertEquals(em.exceptions.get(i) instanceof RuntimeException, false);
        }
    }

    public void testPush() {
        //System.out.println("push");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.push();

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testFlush() {
        //System.out.println("flush");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.flush();

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testClose() {
        //System.out.println("close");
        LogRecord record = new LogRecord(Level.INFO, "");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        int capacity = instance.getCapacity();

        assertNotNull(instance.getLevel());

        instance.setLevel(Level.ALL);
        assertEquals(instance.isLoggable(record), true);

        instance.close();

        assertEquals(instance.isLoggable(record), false);
        assertEquals(instance.getLevel(), Level.OFF);

        instance.setLevel(Level.ALL);
        assertEquals(instance.getLevel(), Level.OFF);

        assertEquals(capacity, instance.getCapacity());
        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testLevel() {
        //System.out.println("Level");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getLevel());

        try {
            instance.setLevel(null);
            fail("Null level was allowed");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        Level[] lvls = getAllLevels();
        for (int i = 0; i < lvls.length; i++) {
            instance.setLevel(lvls[i]);
            assertEquals(lvls[i], instance.getLevel());
        }

        instance.close();
        for (int i = 0; i < lvls.length; i++) {
            instance.setLevel(lvls[i]);
            assertEquals(Level.OFF, instance.getLevel());
        }
        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testPushLevel() {
        //System.out.println("PushLevel");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getPushLevel());

        try {
            instance.setPushLevel(null);
            fail("Null level was allowed");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        Level[] lvls = getAllLevels();
        for (int i = 0; i < lvls.length; i++) {
            instance.setPushLevel(lvls[i]);
            assertEquals(lvls[i], instance.getPushLevel());
        }

        instance.close();
        for (int i = 0; i < lvls.length; i++) {
            instance.setPushLevel(lvls[i]);
            assertEquals(lvls[i], instance.getPushLevel());
        }
        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testPushFilter() {
        //System.out.println("PushFilter");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.setPushFilter(BooleanFilter.TRUE);
        assertEquals(instance.getPushFilter(), BooleanFilter.TRUE);

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testComparator() {
        //System.out.println("Comparator");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        Comparator uselessComparator = new UselessComparator();
        Comparator result = instance.getComparator();
        assertEquals(uselessComparator.equals(result), false);

        instance.setComparator(uselessComparator);
        result = instance.getComparator();

        assertEquals(uselessComparator.equals(result), true);

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testCapacity() {
        //System.out.println("Capacity");

        try {
            new MailHandler(-1);
            fail("Negative capacity was allowed.");
        } catch (IllegalArgumentException pass) {
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            new MailHandler(0);
            fail("Zero capacity was allowed.");
        } catch (IllegalArgumentException pass) {
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            new MailHandler(1);
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }


        final int expResult = 20;
        MailHandler instance = new MailHandler(20);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        int result = instance.getCapacity();
        assertEquals(expResult, result);
        instance.close();

        result = instance.getCapacity();
        assertEquals(expResult, result);
        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testAuthenticator() {
        //System.out.println("Authenticator");

        Authenticator auth = new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("", "");
            }
        };

        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        try {
            instance.setAuthenticator(instance.getAuthenticator());
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            instance.setAuthenticator(auth);
            assertEquals(auth, instance.getAuthenticator());
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testMailProperties() {
        //System.out.println("MailProperties");
        Properties props = new Properties();
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getMailProperties());

        try {
            instance.setMailProperties(null);
            fail("Null was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        instance.setMailProperties(props);
        Properties stored = instance.getMailProperties();

        assertNotNull(stored);
        assertEquals(props == stored, false);

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testAttachmentFilters() {
        //System.out.println("AttachmentFilters");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        Filter[] result = instance.getAttachmentFilters();
        assertNotNull(result);
        assertEquals(result.length, instance.getAttachmentFormatters().length);


        assertEquals(instance.getAttachmentFilters() == result, false);

        if (instance.getAttachmentFormatters().length != 0) {
            instance.setAttachmentFormatters(new Formatter[0]);
        }

        try {
            instance.setAttachmentFilters(null);
            fail("Null allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            instance.setAttachmentFilters(new Filter[0]);
        } catch (RuntimeException re) {
            fail(re.toString());
        }


        try {
            assertEquals(instance.getAttachmentFormatters().length, 0);

            instance.setAttachmentFilters(new Filter[]{BooleanFilter.TRUE});
            fail("Filter to formatter mismatch.");
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setAttachmentFormatters(new Formatter[]{new SimpleFormatter(), new XMLFormatter()});

        try {
            assertEquals(instance.getAttachmentFormatters().length, 2);

            instance.setAttachmentFilters(new Filter[]{BooleanFilter.TRUE});
            fail("Filter to formatter mismatch.");
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            assertEquals(instance.getAttachmentFormatters().length, 2);
            Filter[] filters = new Filter[]{BooleanFilter.TRUE, BooleanFilter.TRUE};
            assertEquals(instance.getAttachmentFormatters().length, filters.length);
            instance.setAttachmentFilters(filters);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            assertEquals(instance.getAttachmentFormatters().length, 2);
            Filter[] filters = new Filter[]{null, null};
            assertEquals(instance.getAttachmentFormatters().length, filters.length);
            instance.setAttachmentFilters(filters);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            assertEquals(instance.getAttachmentFormatters().length, 2);
            instance.setAttachmentFilters(new Filter[0]);
            fail("Filter to formatter mismatch.");
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            assertEquals(instance.getAttachmentFormatters().length, 2);
            Filter[] filters = new Filter[]{null, null};
            instance.setAttachmentFilters(filters);
            filters[0] = BooleanFilter.TRUE;
            assertEquals(filters[0], filters[0]);
            assertEquals(filters[0].equals(instance.getAttachmentFilters()[0]), false);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testAttachmentFormatters() {
        //System.out.println("AttachmentFormatters");
        MailHandler instance = new MailHandler();

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        Formatter[] result = instance.getAttachmentFormatters();
        assertNotNull(result);
        assertEquals(result == instance.getAttachmentFormatters(), false);

        assertEquals(result.length, instance.getAttachmentFilters().length);
        assertEquals(result.length, instance.getAttachmentNames().length);

        result = new Formatter[]{new SimpleFormatter(), new XMLFormatter()};
        instance.setAttachmentFormatters(result);

        assertEquals(result.length, instance.getAttachmentFilters().length);
        assertEquals(result.length, instance.getAttachmentNames().length);

        result[0] = new XMLFormatter();
        result[1] = new SimpleFormatter();
        assertEquals(result[1].getClass(), instance.getAttachmentFormatters()[0].getClass());
        assertEquals(result[0].getClass(), instance.getAttachmentFormatters()[1].getClass());

        try {
            instance.setAttachmentFormatters(null);
            fail("Null was allowed.");
        } catch (NullPointerException NPE) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        result[0] = null;
        try {
            instance.setAttachmentFormatters(result);
            fail("Null index was allowed.");
        } catch (NullPointerException NPE) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }


        result = new Formatter[0];
        try {
            instance.setAttachmentFormatters(result);
            assertEquals(result.length, instance.getAttachmentFilters().length);
            assertEquals(result.length, instance.getAttachmentNames().length);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testAttachmentNames_StringArr() {
        //System.out.println("AttachmentNames");
        Formatter[] names = null;
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        names = instance.getAttachmentNames();
        assertNotNull(names);

        try {
            instance.setAttachmentNames((String[]) null);
            fail("Null was allowed.");
        } catch (RuntimeException re) {
            assertEquals(NullPointerException.class, re.getClass());
        }

        if (instance.getAttachmentFormatters().length > 0) {
            instance.setAttachmentFormatters(new Formatter[0]);
        }

        try {
            instance.setAttachmentNames(new String[0]);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            instance.setAttachmentNames(new String[1]);
            fail("Mismatch with attachment formatters.");
        } catch (NullPointerException pass) {
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setAttachmentFormatters(new Formatter[]{new SimpleFormatter(), new XMLFormatter()});
        try {
            instance.setAttachmentNames(new String[2]);
            fail("Null index was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        Formatter[] formatters = instance.getAttachmentFormatters();
        names = instance.getAttachmentNames();

        assertEquals(names[0].toString(), String.valueOf(formatters[0]));
        assertEquals(names[1].toString(), String.valueOf(formatters[1]));

        String[] stringNames = new String[]{"error.txt", "error.xml"};
        instance.setAttachmentNames(stringNames);
        assertEquals(stringNames[0], instance.getAttachmentNames()[0].toString());
        assertEquals(stringNames[1], instance.getAttachmentNames()[1].toString());

        stringNames[0] = "info.txt";
        assertEquals(stringNames[0].equals(instance.getAttachmentNames()[0].toString()), false);

        try {
            instance.setAttachmentNames(new String[0]);
            fail("Names mismatch formatters.");
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testAttachmentNames_FormatterArr() {
        //System.out.println("AttachmentNames");
        Formatter[] formatters = null;
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getAttachmentNames());

        try {
            instance.setAttachmentNames((Formatter[]) null);
            fail("Null was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        if (instance.getAttachmentFormatters().length > 0) {
            instance.setAttachmentFormatters(new Formatter[0]);
        }

        try {
            instance.setAttachmentNames(new Formatter[2]);
            fail("formatter mismatch.");
        } catch (NullPointerException pass) {
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setAttachmentFormatters(
                new Formatter[]{new SimpleFormatter(), new XMLFormatter()});

        assertEquals(instance.getAttachmentFormatters().length, instance.getAttachmentNames().length);

        formatters = new Formatter[]{new SimpleFormatter(), new XMLFormatter()};
        instance.setAttachmentNames(formatters);
        formatters[0] = new XMLFormatter();
        assertEquals(formatters[0].equals(instance.getAttachmentNames()[0]), false);

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testSubject_String() {
        //System.out.println("Subject");
        String subject = "Test subject.";
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getSubject());

        try {
            instance.setSubject((String) null);
            fail("Null subject was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setSubject(subject);
        assertEquals(subject, instance.getSubject().toString());

        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testSubject_Formatter() {
        //System.out.println("Subject");
        Formatter format = new SimpleFormatter();
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getSubject());

        try {
            instance.setSubject((Formatter) null);
            fail("Null subject was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setSubject(format);
        assertEquals(instance.getSubject(), format);


        assertEquals(em.exceptions.isEmpty(), true);
    }

    public void testReportError() {
        //System.out.println("reportError");
        MailHandler instance = new MailHandler();
        instance.setErrorManager(new ErrorManager() {

            public void error(String msg, Exception ex, int code) {
                assertNull(msg);
            }
        });

        instance.reportError(null, null, ErrorManager.GENERIC_FAILURE);


        instance.setErrorManager(new ErrorManager() {

            public void error(String msg, Exception ex, int code) {
                assertEquals(msg.indexOf(Level.SEVERE.getName()), 0);
            }
        });

        instance.reportError("simple message.", null, ErrorManager.GENERIC_FAILURE);

    }

    public void testSecurityManager() {
        class LogSecurityManager extends SecurityManager {
            boolean secure = false;
            public void checkPermission(java.security.Permission perm) {
                if(secure) {
                    super.checkPermission(perm);
                }
            }

            public void checkPermission(java.security.Permission perm, Object context) {
                if(secure) {
                    super.checkPermission(perm, context);
                }
            }
        }

        final LogSecurityManager manager = new LogSecurityManager();
        System.setSecurityManager(manager);

        manager.secure = false;
        MailHandler h = new MailHandler();
        manager.secure = true;

        try {
            h.setAttachmentFilters(new Filter[]{new ThrowFilter()});
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setAttachmentFormatters(new Formatter[]{new ThrowFormatter()});
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setAttachmentNames(new String[]{"error.txt"});
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setAttachmentNames(new Formatter[]{new ThrowFormatter()});
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setAuthenticator(null);
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setComparator(null);
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setLevel(Level.ALL);
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setMailProperties(new Properties());
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setPushFilter(null);
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setPushLevel(Level.OFF);
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setSubject(new ThrowFormatter());
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setSubject("test");
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.getAuthenticator();
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.getMailProperties();
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.close();
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            h.setLevel(Level.ALL);
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            new MailHandler();
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            new MailHandler(100);
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }

        try {
            new MailHandler(new Properties());
            fail("Missing secure check.");
        }
        catch(SecurityException pass) {
        }
        catch(Exception fail) {
            fail(fail.toString());
        }
        manager.secure = false;
        System.setSecurityManager(null);
    }

    /**
     * Test must run last.
     */
    public void testZInit() {
        final String key = "java.util.logging.config.file";
        assertNull(System.getProperty(key));
        String tmp = System.getProperty("java.io.tmpdir");
        if(tmp == null) {
            tmp = System.getProperty("user.home");
        }

        final String p = MailHandler.class.getName();
        File dir = new File(tmp);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        try {
            File cfg = File.createTempFile("mailhandler_test", ".properties", dir);
            cfg.deleteOnExit();
            System.setProperty(key, cfg.getAbsolutePath());
            try {
                Properties props = new Properties();
                FileOutputStream out = new FileOutputStream(cfg);
                try {
                    props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
                    props.put(p.concat(".capacity"), "10");
                    props.put(p.concat(".level"), "ALL");
                    props.put(p.concat(".formatter"), XMLFormatter.class.getName());
                    props.put(p.concat(".filter"), ThrowFilter.class.getName());
                    props.put(p.concat(".pushLevel"), "WARNING");
                    props.put(p.concat(".pushFilter"), ThrowFilter.class.getName());
                    props.put(p.concat(".comparator"), ThrowComparator.class.getName());
                    props.put(p.concat(".encoding"), "UTF-8");

                    props.put(p.concat(".attachment.filters"),
                            "null, "+ ThrowFilter.class.getName()+ ", "+
                            ThrowFilter.class.getName());

                    props.put(p.concat(".attachment.formatters"),
                            SimpleFormatter.class.getName()+ ", "+
                            XMLFormatter.class.getName() +", "+
                            SimpleFormatter.class.getName());

                    props.put(p.concat(".attachment.names"), "msg.txt, "+ SimpleFormatter.class.getName() +", error.txt");

                    props.store(out, "Mail handler test file.");
                }
                finally {
                    out.close();
                }

                LogManager.getLogManager().readConfiguration();
                MailHandler h = new MailHandler();
                assertEquals(h.getCapacity(), 10);
                assertEquals(h.getLevel(), Level.ALL);
                assertEquals(h.getFilter().getClass(), ThrowFilter.class);
                assertEquals(h.getFormatter().getClass(), XMLFormatter.class);
                assertEquals(h.getPushLevel(), Level.WARNING);
                assertEquals(h.getPushFilter().getClass(), ThrowFilter.class);
                assertEquals(h.getEncoding(), "UTF-8");
                assertEquals(h.getAttachmentFormatters().length, 3);
                assertEquals(h.getAttachmentFilters().length, 3);
                assertEquals(h.getAttachmentNames().length, 3);
                
                InternalErrorManager em = (InternalErrorManager)h.getErrorManager();
                assertTrue(em.exceptions.isEmpty());

                for(int i=0; i<em.exceptions.size(); i++) {
                    System.out.println(em.exceptions.get(i));
                }

                h.close();
                assertEquals(em.exceptions.isEmpty(), true);

                props.remove(p.concat(".attachment.filters"));
                LogManager.getLogManager().readConfiguration();

                h = new MailHandler();
                em = (InternalErrorManager)h.getErrorManager();
                assertTrue(em.exceptions.isEmpty());
                assertEquals(h.getAttachmentFormatters().length, 3);
                h.close();

                props.remove(p.concat(".attachment.names"));
                LogManager.getLogManager().readConfiguration();

                h = new MailHandler();
                em = (InternalErrorManager)h.getErrorManager();
                assertTrue(em.exceptions.isEmpty());
                assertEquals(h.getAttachmentFormatters().length, 3);
                h.close();
            }
            finally {
                //no way to clear a SystemProperty in 1.4
                boolean v;
                v = cfg.delete();
                assertTrue(v);

                v = cfg.createNewFile();
                assertTrue(v);
                
                LogManager.getLogManager().readConfiguration();
            }
        }
        catch(IOException IOE) {
            IOE.printStackTrace();
            assertTrue(false);
        }
    }

    private Level[] getAllLevels() {
        Field[] fields = Level.class.getFields();
        List a = new ArrayList(fields.length);
        for (int i = 0; i < fields.length; i++) {
            if (Modifier.isStatic(fields[i].getModifiers()) &&
                    Level.class.isAssignableFrom(fields[i].getType())) {
                try {
                    a.add(fields[i].get(null));
                } catch (IllegalArgumentException ex) {
                    fail(ex.toString());
                } catch (IllegalAccessException ex) {
                    fail(ex.toString());
                }
            }
        }
        return (Level[]) a.toArray(new Level[a.size()]);
    }


    public static class ThrowFilter implements Filter {

        public boolean isLoggable(LogRecord record) {
            throw new RuntimeException(record.toString());
        }
    }

    public static final class ThrowComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            throw new RuntimeException();
        }
    }

    public static final class ThrowFormatter extends Formatter {

        public String format(LogRecord record) {
            throw new RuntimeException("format");
        }

        public String getHead(Handler h) {
            throw new RuntimeException("head");
        }

        public String getTail(Handler h) {
            throw new RuntimeException("head");
        }
    }

    private static class UselessComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    public static final class BooleanFilter implements Filter {

        static final BooleanFilter TRUE = new BooleanFilter(true);
        static final BooleanFilter FALSE = new BooleanFilter(false);
        private final boolean value;

        public BooleanFilter() {
            this(false);
        }

        private BooleanFilter(boolean v) {
            this.value = v;
        }

        public boolean isLoggable(LogRecord r) {
            return value;
        }
    }

    public static final class InternalErrorManager extends ErrorManager {

        final List exceptions = new ArrayList();

        public InternalErrorManager() {
        }

        public void error(String msg, Exception ex, int code) {
            exceptions.add(ex);
        }
    }
}
