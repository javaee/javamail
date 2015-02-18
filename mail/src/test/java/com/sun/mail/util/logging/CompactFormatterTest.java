/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2013-2015 Jason Mehrens. All rights reserved.
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
package com.sun.mail.util.logging;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import javax.mail.internet.MimeUtility;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Compact formatter tests.
 *
 * @author Jason Mehrens
 * @since JavaMail 1.5.2
 */
public class CompactFormatterTest {

    private static final String UNKNOWN_CLASS_NAME
            = CompactFormatterTest.class.getName().concat("Foo");

    /**
     * The line separator.
     */
    private static final String LINE_SEP = System.getProperty("line.separator");

    /**
     * The max width.
     */
    private static final int MAX_PRE = 160;
    /**
     * The default left to right pattern.
     */
    private static final String LEFT_TO_RIGHT = "%7$#." + MAX_PRE + "s%n";

    /**
     * See LogManager.
     */
    private static final String LOG_CFG_KEY = "java.util.logging.config.file";

    private static void checkJVMOptions() throws Exception {
        assertTrue(CollectorFormatterTest.class.desiredAssertionStatus());
        assertNull(System.getProperty("java.util.logging.manager"));
        assertNull(System.getProperty("java.util.logging.config.class"));
        assertNull(System.getProperty(LOG_CFG_KEY));
        assertEquals(LogManager.class, LogManager.getLogManager().getClass());
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        checkJVMOptions();
        assertTrue("Recompile tests to include source line numbers.",
                new Throwable().getStackTrace()[0].getLineNumber() >= 0);
        try {
            throw new AssertionError(Class.forName(UNKNOWN_CLASS_NAME));
        } catch (ClassNotFoundException expect) {
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        checkJVMOptions();
    }

    @Test
    public void testNewFormatterWithPattern() {
        CompactFormatter cf = new CompactFormatter("%4$s");
        String result = cf.format(new LogRecord(Level.SEVERE, ""));
        assertEquals(Level.SEVERE.getLocalizedName(), result);
    }

    @Test
    public void testNewFormatterNullPattern() {
        CompactFormatter cf = new CompactFormatter((String) null);
        assertEquals(CompactFormatter.class, cf.getClass());
    }

    @Test
    public void testGetHeadAndGetTail() {
        CompactFormatter cf = new CompactFormatter();
        assertEquals("", cf.getHead(null));
        assertEquals("", cf.getTail(null));
    }

    @Test
    public void testFormatWithMessage() {
        LogRecord record = new LogRecord(Level.SEVERE, "message");
        CompactFormatter cf = new CompactFormatter();
        String result = cf.format(record);
        assertTrue(result, result.startsWith(record.getMessage()));
        assertTrue(result, result.endsWith(LINE_SEP));
    }

    @Test
    public void testFormatWithMessagePrecisionOverWidth() {
        LogRecord record = new LogRecord(Level.SEVERE, "message");
        record.setThrown(new Throwable("thrown"));
        CompactFormatter cf = new CompactFormatter("%7$#6.12s");
        String result = cf.format(record);
        assertEquals("mes|Throwable", result);
    }

    @Test
    public void testFormatWithMessageWidthOverPrecision() {
        LogRecord record = new LogRecord(Level.SEVERE, "message");
        record.setThrown(new Throwable("thrown"));
        CompactFormatter cf = new CompactFormatter("%7$#12.6s");
        String result = cf.format(record);
        assertEquals("mes\u0020\u0020\u0020|Thr\u0020\u0020\u0020", result);
    }

    @Test
    public void testFormatWithMessageEmpty() {
        LogRecord record = new LogRecord(Level.SEVERE, "");
        CompactFormatter cf = new CompactFormatter();
        String result = cf.format(record);
        assertEquals(result, LINE_SEP);
    }

    @Test
    public void testFormatMessageSurrogate() {
        LogRecord record = new LogRecord(Level.SEVERE,
                "a\ud801\udc00\ud801\udc00\ud801\udc00\ud801\udc00");
        record.setThrown(new Throwable("thrown"));
        CompactFormatter cf = new CompactFormatter("%7$#.6s%n");
        String result = cf.format(record);
        assertTrue(result, result.startsWith("a\ud801\udc00"));
        assertTrue(result, result.endsWith("|Thr" + LINE_SEP));
    }

    @Test
    public void testFormatWithMessageAndThrownLeftToRight() {
        LogRecord record = new LogRecord(Level.SEVERE, "message");
        record.setThrown(new Throwable("thrown"));
        CompactFormatter cf = new CompactFormatter();
        String result = cf.format(record);
        assertTrue(result, result.startsWith(record.getMessage()));
        assertTrue(result, result.contains("|"));
        assertTrue(result, result.contains(Throwable.class.getSimpleName()));
        assertTrue(result, result.contains(CompactFormatterTest.class.getSimpleName()));
        assertTrue(result, result.contains("testFormatWithMessageAndThrown"));
        assertTrue(result, result.contains(String.valueOf(
                record.getThrown().getStackTrace()[0].getLineNumber())));
        assertTrue(result, result.endsWith(LINE_SEP));
    }

    @Test
    public void testFormatWithThrownLeftToRight() {
        testFormatWithThrown(LEFT_TO_RIGHT);
    }

    private void testFormatWithThrown(String fmt) {
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setThrown(new Throwable("thrown"));
        CompactFormatter cf = new CompactFormatter(fmt);
        String result = cf.format(record);
        assertFalse(result, result.startsWith("|"));
        assertTrue(result, result.contains(record.getThrown().getMessage()));
        assertTrue(result, result.endsWith(LINE_SEP));
    }

    @Test(expected = NullPointerException.class)
    public void testFormatMessageNull() {
        CompactFormatter cf = new CompactFormatter();
        cf.formatMessage((LogRecord) null);
        fail(cf.toString());
    }

    @Test
    public void testFormatMessageLocale() throws Exception {
        String msg = "message";
        CompactFormatter cf = new CompactFormatter("%5$s");
        Properties props = new Properties();
        props.put(LOG_CFG_KEY, msg);

        LogRecord r = new LogRecord(Level.SEVERE, LOG_CFG_KEY);
        r.setResourceBundle(new LocaleResource(props, Locale.US));
        assertNotNull(r.getResourceBundle().getLocale());
        String result = cf.format(r);
        assertEquals(msg, result);
        assertEquals(msg, cf.formatMessage(r));
    }

    @Test
    public void testFormatMessage_LogRecordLeftToRight() {
        testFormatMessage_LogRecord(LEFT_TO_RIGHT);
    }

    private void testFormatMessage_LogRecord(String fmt) {
        Exception e = new IOException();
        assertNull(e.getMessage(), e.getMessage());

        e = new Exception(e.toString(), e);
        assertNotNull(e.getMessage(), e.getMessage());

        LogRecord record = new LogRecord(Level.SEVERE, e.toString());
        record.setThrown(e);
        CompactFormatter cf = new CompactFormatter(fmt);
        String result = cf.format(record);

        assertTrue(result, result.contains("|"));
        assertTrue(e.toString(), e.toString().contains(Exception.class.getPackage().getName()));
        assertTrue(e.toString(), e.toString().contains(IOException.class.getPackage().getName()));
        int idx;
        idx = result.indexOf(Exception.class.getSimpleName());
        assertTrue(result, idx >= 0);

        idx = result.indexOf(IOException.class.getSimpleName(), idx);
        assertTrue(result, idx >= 0);

        assertTrue(result, result.contains(record.getThrown().getClass().getSimpleName()));
        assertTrue(result, result.contains(record.getThrown().getCause().getClass().getSimpleName()));

        assertFalse(result, result.contains(Exception.class.getPackage().getName()));
        assertFalse(result, result.contains(IOException.class.getPackage().getName()));

        assertFalse(result, result.contains(Exception.class.getName()));
        assertFalse(result, result.contains(IOException.class.getName()));

        assertTrue(result, result.endsWith(LINE_SEP));
    }

    @Test(timeout = 30000)
    public void testFormatMessage_LogRecordEvil() {
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setThrown(createEvilThrowable());
        CompactFormatter cf = new CompactFormatter();
        cf.formatMessage(record);
    }

    @Test
    public void testFormatMaxMessageWidthLeftToRight() {
        testFormatMaxMessageWidth(LEFT_TO_RIGHT, MAX_PRE);
    }

    private void testFormatMaxMessageWidth(String fmt, int width) {
        assertTrue(fmt, fmt.contains(Integer.toString(width)));
        assertTrue(String.valueOf(width), width < Integer.MAX_VALUE / 4);

        CompactFormatter cf = new CompactFormatter(fmt);
        LogRecord record = new LogRecord(Level.SEVERE, "");
        int padding = LINE_SEP.length();
        for (int i = 1; i < width; i++) {
            record.setMessage(rpad("", i, "A"));
            String result = cf.format(record);
            assertTrue(result, result.length() == i + padding);
            assertTrue(result, result.endsWith(LINE_SEP));
        }

        for (int i = width; i <= (width * 4); i++) {
            record.setMessage(rpad("", i, "A"));
            String result = cf.format(record);
            assertTrue(result, result.length() == width + padding);
            assertTrue(result, result.endsWith(LINE_SEP));
        }
    }

    @Test
    public void testFormatMaxThrowableWidthLeftToRight() {
        String fmt = LEFT_TO_RIGHT;
        int width = MAX_PRE;
        assertTrue(fmt, fmt.contains(Integer.toString(width)));
        assertTrue(String.valueOf(width), width < Integer.MAX_VALUE / 4);

        CompactFormatter cf = new CompactFormatter(fmt);
        LogRecord record = new LogRecord(Level.SEVERE, "");
        int padding = LINE_SEP.length();
        for (int i = 0; i < width; i++) {
            record.setThrown(new Throwable(rpad("", i, "A")));
            String result = cf.format(record);
            assertTrue(result, result.length() <= width + padding);

            assertTrue(result, result.endsWith(LINE_SEP));
        }

        for (int i = width; i <= (width * 4); i++) {
            record.setThrown(new Throwable(rpad("", i, "A")));
            String result = cf.format(record);
            assertTrue(result.length() + ", " + (width + padding),
                    result.length() == width + padding);
            assertTrue(result, result.endsWith(LINE_SEP));
        }
    }

    @Test
    public void testFormatMessage_Throwable() {
        Exception e = new IOException(Exception.class.getName());
        e = new Exception(e.toString(), e);
        assertNotNull(e.getMessage(), e.getMessage());

        CompactFormatter cf = new CompactFormatter();
        String result = cf.formatMessage(e);
        assertEquals(result, Exception.class.getSimpleName());
    }

    @Test(timeout = 30000)
    public void testFormatMessage_ThrowableEvil() {
        CompactFormatter cf = new CompactFormatter();
        cf.formatMessage(createEvilThrowable());
    }

    @Test
    public void testFormatLevel() {
        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        String result = cf.formatLevel(record);
        assertEquals(record.getLevel().getLocalizedName(), result);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatLevelNull() {
        CompactFormatter cf = new CompactFormatter();
        cf.formatLevel((LogRecord) null);
        fail(cf.toString());
    }

    @Test
    public void testFormatLogger() {
        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setSourceMethodName(null);
        record.setSourceClassName(null);
        record.setLoggerName(Object.class.getName());
        String result = cf.formatLoggerName(record);
        assertEquals(Object.class.getSimpleName(), result);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatLoggerNull() {
        CompactFormatter cf = new CompactFormatter();
        cf.formatLoggerName((LogRecord) null);
        fail(cf.toString());
    }

    @Test
    public void testFormatMillis() {
        String p = "%1$tc";
        CompactFormatter cf = new CompactFormatter(p);
        LogRecord r = new LogRecord(Level.SEVERE, "");
        assertEquals(String.format(p, r.getMillis()),
                cf.format(r));
    }

    @Test
    public void testFormatMillisLocale() throws Exception {
        String p = "%1$tc";
        CompactFormatter cf = new CompactFormatter(p);
        Properties props = new Properties();
        LogRecord r = new LogRecord(Level.SEVERE, "");
        r.setResourceBundle(new LocaleResource(props, Locale.ENGLISH));
        assertEquals(String.format(Locale.ENGLISH, p, r.getMillis()),
                cf.format(r));
    }

    @Test
    public void testFormatMillisByParts() {
        String p = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp";
        CompactFormatter cf = new CompactFormatter(p);
        LogRecord r = new LogRecord(Level.SEVERE, "");
        assertEquals(String.format(p, r.getMillis()),
                cf.format(r));
    }

    @Test
    public void testFormatMillisByPartsLocale() throws Exception {
        String p = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp";
        CompactFormatter cf = new CompactFormatter(p);
        Properties props = new Properties();
        LogRecord r = new LogRecord(Level.SEVERE, "");
        r.setResourceBundle(new LocaleResource(props, Locale.ENGLISH));
        assertEquals(String.format(Locale.ENGLISH, p, r.getMillis()),
                cf.format(r));
    }

    @Test
    public void testFormatMillisAsLong() {
        String p = "%1$tQ";
        CompactFormatter cf = new CompactFormatter(p);
        LogRecord r = new LogRecord(Level.SEVERE, "");
        assertEquals(String.format(p, r.getMillis()),
                cf.format(r));
    }

    @Test
    public void testFormatSourceByLogger() {
        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setSourceMethodName(null);
        record.setSourceClassName(null);
        record.setLoggerName(Object.class.getName());
        String result = cf.formatSource(record);
        assertEquals(Object.class.getSimpleName(), result);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatSourceNull() {
        CompactFormatter cf = new CompactFormatter();
        cf.formatSource((LogRecord) null);
        fail(cf.toString());
    }

    @Test
    public void testFormatSourceByClass() {
        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setSourceMethodName(null);
        record.setSourceClassName(Object.class.getName());
        record.setLoggerName("");
        String result = cf.formatSource(record);
        assertEquals(Object.class.getSimpleName(), result);
    }

    @Test
    public void testFormatSourceByClassAndMethod() {
        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setSourceMethodName("method");
        record.setSourceClassName(Object.class.getName());
        record.setLoggerName("");
        String result = cf.formatSource(record);
        assertFalse(result, record.getSourceClassName().equals(record.getSourceMethodName()));
        assertTrue(result, result.startsWith(Object.class.getSimpleName()));
        assertTrue(result, result.endsWith(record.getSourceMethodName()));
    }

    @Test
    public void testFormatThrownNullThrown() {
        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        String result = cf.formatThrown(record);
        assertTrue(result, result.startsWith(cf.formatMessage(record.getThrown())));
        assertTrue(result, result.endsWith(cf.formatBackTrace(record)));
    }

    @Test(timeout = 30000)
    public void testFormatThrownEvilThrown() {
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setThrown(createEvilThrowable());
        CompactFormatter cf = new CompactFormatter();
        cf.formatThrown(record);
    }

    @Test
    public void testFormatThrown() {
        Exception e = new IOException("Fake I/O");
        e = new Exception(e.toString(), e);
        assertNotNull(e.getMessage(), e.getMessage());

        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setThrown(e);
        String result = cf.formatThrown(record);
        assertTrue(result, result.startsWith(e.getCause().getClass().getSimpleName()));
        assertTrue(result, result.contains(cf.formatMessage(record.getThrown())));
        assertTrue(result, result.endsWith(cf.formatBackTrace(record)));
    }

    @Test(expected = NullPointerException.class)
    public void testFormatThrownNullRecord() {
        CompactFormatter cf = new CompactFormatter();
        cf.formatThrown((LogRecord) null);
        fail(cf.toString());
    }

    @Test
    public void testFormatExample1() {
        String p = "%7$#.160s%n";
        LogRecord r = new LogRecord(Level.SEVERE, "Encoding failed.");
        RuntimeException npe = new NullPointerException();
        StackTraceElement frame = new StackTraceElement("java.lang.String",
                "getBytes", "String.java", 913);
        npe.setStackTrace(new StackTraceElement[]{frame});
        r.setThrown(npe);
        CompactFormatter cf = new CompactFormatter(p);
        cf.format(r);
    }

    @Test
    public void testFormatExample2() {
        String p = "%1$tc %2$s%n%4$s: %5$s%6$s%n";
        LogRecord r = new LogRecord(Level.SEVERE, "Encoding failed.");
        r.setSourceClassName("MyClass");
        r.setSourceMethodName("fatal");
        r.setMillis(1258723764000L);
        RuntimeException npe = new NullPointerException();
        StackTraceElement frame = new StackTraceElement("java.lang.String",
                "getBytes", "String.java", 913);
        npe.setStackTrace(new StackTraceElement[]{frame});
        r.setThrown(npe);
        CompactFormatter cf = new CompactFormatter(p);
        cf.format(r);
    }

    @Test
    public void testFormatBackTrace() {
        Exception e = new IOException("Fake I/O");
        e = new Exception(e.toString(), e);
        assertNotNull(e.getMessage(), e.getMessage());

        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setThrown(e);
        String result = cf.formatBackTrace(record);
        assertTrue(result, result.startsWith("CompactFormatterTest"));
        assertTrue(result, result.contains("testFormatBackTrace"));
        assertTrue(result, Character.isDigit(result.charAt(result.length() - 2)));
    }

    @Test
    public void testFormatBackTraceUnknown() {
        Exception e = new IOException("Fake I/O");
        e.setStackTrace(new StackTraceElement[]{
            new StackTraceElement(CompactFormatterTest.class.getName(),
            "testFormatBackTrace", null, -2)});
        assertNotNull(e.getMessage(), e.getMessage());

        CompactFormatter cf = new CompactFormatter();
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setThrown(e);
        String result = cf.formatBackTrace(record);
        assertTrue(result, result.startsWith("CompactFormatterTest"));
        assertTrue(result, result.contains("testFormatBackTrace"));
    }

    @Test(expected = NullPointerException.class)
    public void testFormatBackTraceNull() {
        CompactFormatter cf = new CompactFormatter();
        cf.formatBackTrace((LogRecord) null);
        fail(cf.toString());
    }

    @Test(timeout = 30000)
    public void testFormatBackTraceEvil() {
        LogRecord record = new LogRecord(Level.SEVERE, "");
        record.setThrown(createEvilThrowable());
        CompactFormatter cf = new CompactFormatter();
        cf.formatBackTrace(record);
    }

    @Test
    public void testApply() {
        CompactFormatter cf = new CompactFormatter();
        assertNull(cf.apply((Throwable) null));

        final Throwable t = new Throwable();
        Throwable e = cf.apply(t);
        assertSame(t, e);
    }

    @Test
    public void testApplyNull() {
        assertNull(new CompactFormatter().apply(null));
    }

    @Test(timeout = 30000)
    public void testApplyEvil() {
        CompactFormatter cf = new CompactFormatter();
        assertNotNull(cf.apply(createEvilThrowable()));
    }

    private Throwable createEvilThrowable() {
        Throwable third = new Throwable();
        Throwable second = new Throwable(third);
        Throwable first = new Throwable(second);
        return third.initCause(first);  // Pure evil.
    }

    @Test(expected = NullPointerException.class)
    public void testIgnoreNull() {
        CompactFormatter cf = new CompactFormatter();
        cf.ignore((StackTraceElement) null);
        fail(cf.toString());
    }

    @Test
    public void testIgnoreKnownClass() {
        CompactFormatter cf = new CompactFormatter();
        String n = cf.getClass().getName();
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "format", f, 20);
        assertFalse(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreUnknownClass() {
        CompactFormatter cf = new CompactFormatter();
        String n = UNKNOWN_CLASS_NAME;
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "format", f, 20);
        assertFalse(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnorePrivateInnerClass() {
        CompactFormatter cf = new CompactFormatter();
        String n = Arrays.asList("foo", "bar", "baz").getClass().getName();
        assertTrue(n, n.contains("$"));

        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "size", f, 20);
        assertFalse(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnorePrivateStaticInnerClass() {
        CompactFormatter cf = new CompactFormatter();
        String n = Collections.emptySet().getClass().getName();
        assertTrue(n, n.contains("$"));

        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "size", f, 20);
        assertFalse(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreStaticUtilityClass() {
        CompactFormatter cf = new CompactFormatter();
        String n = MimeUtility.class.getName();
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "encodeText", f, 400);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreStaticUtilityClassUtilError() {
        CompactFormatter cf = new CompactFormatter();
        String n = getClass().getName().concat("MimeUtility");
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "encodeText", f, 400);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreStaticUtilityClassWithSError() {
        CompactFormatter cf = new CompactFormatter();
        String n = getClass().getName().concat("Collections");
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "nCopies", f, 400);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreStaticUtilityClassWithSeError() {
        CompactFormatter cf = new CompactFormatter();
        String n = getClass().getName().concat("Properties");
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "get", f, 400);
        assertFalse(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreSyntheticMethod() {
        CompactFormatter cf = new CompactFormatter();
        String n = UNKNOWN_CLASS_NAME;
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "access$100", f, 10);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreNativeMethod() {
        CompactFormatter cf = new CompactFormatter();
        String n = UNKNOWN_CLASS_NAME;
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "foo", f, -2);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreReflectMethod() {
        CompactFormatter cf = new CompactFormatter();
        String n = java.lang.reflect.Method.class.getName();
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "invoke", f, 10);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreReflectMethodApiError() {
        CompactFormatter cf = new CompactFormatter();
        String n = "java.lang.reflect.".concat(getClass().getName());
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "invoke", f, 10);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreReflectMethodSunError() {
        CompactFormatter cf = new CompactFormatter();
        String n = "sun.reflect.".concat(getClass().getName());
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "invoke", f, 10);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreReflectConstructor() {
        CompactFormatter cf = new CompactFormatter();
        String n = java.lang.reflect.Constructor.class.getName();
        String f = n.concat(".java");
        StackTraceElement s = new StackTraceElement(n, "newInstance", f, 10);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testIgnoreUnknownLine() {
        CompactFormatter cf = new CompactFormatter();
        String n = UNKNOWN_CLASS_NAME;
        StackTraceElement s = new StackTraceElement(n, "foo", null, -1);
        assertTrue(s.toString(), cf.ignore(s));
    }

    @Test
    public void testToAlternate() {
        CompactFormatter cf = new CompactFormatter();
        assertEquals("", cf.toAlternate(LINE_SEP));
    }

    @Test
    public void testToAlternateNull() {
        CompactFormatter cf = new CompactFormatter();
        assertNull(cf.toAlternate((String) null));
    }

    private static String rpad(String s, int len, String p) {
        if (s.length() < len) {
            StringBuilder sb = new StringBuilder(len);
            sb.append(s);
            for (int i = sb.length(); i < len; ++i) {
                sb.append(p, 0, 1);
            }
            return sb.toString();
        } else {
            return s;
        }
    }

    /**
     * A properties resource bundle with locale.
     */
    private static class LocaleResource extends PropertyResourceBundle {

        /**
         * The locale
         */
        private final Locale locale;

        /**
         * Creates the locale resource.
         *
         * @param p the properties.
         * @param l the locale.
         * @throws IOException if there is a problem.
         */
        LocaleResource(Properties p, Locale l) throws IOException {
            super(toStream(p));
            locale = l;
        }

        private static InputStream toStream(Properties p) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            p.store(out, LocaleResource.class.getName());
            return new ByteArrayInputStream(out.toByteArray());
        }

        @Override
        public Locale getLocale() {
            return locale;
        }
    }
}
