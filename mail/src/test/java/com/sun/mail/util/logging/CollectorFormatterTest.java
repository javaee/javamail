package com.sun.mail.util.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The collector formatter tests.
 *
 * @author Jason Mehrens
 * @since JavaMail 1.5.2
 */
public class CollectorFormatterTest {

    /**
     * See LogManager.
     */
    private static final String LOG_CFG_KEY = "java.util.logging.config.file";

    /**
     * The line separator.
     */
    private static final String LINE_SEP = System.getProperty("line.separator");

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
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        checkJVMOptions();
    }

    @Test
    public void testFormatHead() {
        String msg = "message";
        XMLFormatter xml = new XMLFormatter();
        CollectorFormatter f = new CollectorFormatter("{0}", xml,
                (Comparator<LogRecord>) null);
        assertEquals("", f.getHead((Handler) null));
        f.format(new LogRecord(Level.SEVERE, msg));

        String result = f.getTail((Handler) null);
        String expect = f.finish(xml.getHead((Handler) null));
        assertEquals(result, expect);
        assertEquals("", f.getHead((Handler) null));
    }

    @Test(expected = NullPointerException.class)
    public void testFormatNull() {
        CollectorFormatter f = new CollectorFormatter("{1}", (Formatter) null,
                (Comparator<LogRecord>) null);
        f.format((LogRecord) null);
    }

    @Test
    public void testFormatFormat() {
        String msg = "message";
        XMLFormatter xml = new XMLFormatter();
        CollectorFormatter f = new CollectorFormatter("{1}", xml,
                (Comparator<LogRecord>) null);
        assertEquals("", f.getTail((Handler) null));
        LogRecord r;
        r = new LogRecord(Level.SEVERE, msg);
        f.format(r);

        String result = f.getTail((Handler) null);
        String expect = f.finish(xml.format(r));
        assertEquals(result, expect);
        assertEquals("", f.getTail((Handler) null));
    }

    @Test
    public void testFormatFormatLocale() throws Exception {
        String msg = "message";
        XMLFormatter xml = new XMLFormatter();
        CollectorFormatter f = new CollectorFormatter("{1}", xml,
                (Comparator<LogRecord>) null);

        assertEquals("", f.getTail((Handler) null));
        LogRecord r;
        r = new LogRecord(Level.SEVERE, LOG_CFG_KEY);
        Properties props = new Properties();
        props.put(LOG_CFG_KEY, msg);

        r.setResourceBundle(new LocaleResource(props, Locale.US));
        assertNotNull(r.getResourceBundle().getLocale());
        f.format(r);

        String result = f.getTail((Handler) null);
        String expect = f.finish(xml.format(r));
        assertEquals(result, expect);
        assertEquals(msg, f.formatMessage(r));
        assertEquals(msg, xml.formatMessage(r));
        assertEquals("", f.getTail((Handler) null));
    }

    @Test
    public void testFormatNoRecords() {
        XMLFormatter xml = new XMLFormatter();
        CollectorFormatter f = new CollectorFormatter("{0}{1}{2}", xml,
                (Comparator<LogRecord>) null);

        String result = f.getTail((Handler) null);
        String expect = f.getHead((Handler) null)
                + f.getTail((Handler) null);
        assertEquals(result, expect);
    }

    @Test(expected = NullPointerException.class)
    public void testFinishNull() {
        CollectorFormatter f = new CollectorFormatter();
        String result = f.finish((String) null);
        fail(result);
    }

    @Test
    public void testFinish() {
        CollectorFormatter f = new CollectorFormatter();
        String format = LINE_SEP + f.getClass().getName() + LINE_SEP;
        assertFalse(f.getClass().getName().equals(format));
        assertEquals(f.getClass().getName(), f.finish(format));
    }

    @Test
    public void testFormatTail() {
        String msg = "message";
        XMLFormatter xml = new XMLFormatter();
        CollectorFormatter f = new CollectorFormatter("{2}", xml,
                (Comparator<LogRecord>) null);
        f.format(new LogRecord(Level.SEVERE, msg));

        String result = f.getTail((Handler) null);
        String expect = f.finish(xml.getTail((Handler) null));
        assertEquals(result, expect);
        assertEquals(expect, f.getTail((Handler) null));
    }

    @Test
    public void testFormatCount() {
        String msg = "message";
        CollectorFormatter f = new CollectorFormatter("{3}", (Formatter) null,
                (Comparator<LogRecord>) null);
        assertEquals("0", f.getTail((Handler) null));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));

        String result = f.getTail((Handler) null);
        assertEquals(result, "4");
        assertEquals("0", f.getTail((Handler) null));
    }

    @Test
    public void testFormatRemaing() {
        String msg = "message";
        CollectorFormatter f = new CollectorFormatter("{4}", (Formatter) null,
                (Comparator<LogRecord>) null);
        assertEquals("-1", f.getTail((Handler) null));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));

        String result = f.getTail((Handler) null);
        assertEquals(result, "3");
        assertEquals("-1", f.getTail((Handler) null));
    }

    @Test
    public void testFormatThrown() {
        String msg = "message";
        CollectorFormatter f = new CollectorFormatter("{5}", (Formatter) null,
                (Comparator<LogRecord>) null);
        assertEquals("0", f.getTail((Handler) null));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));

        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setThrown(new Exception());
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setThrown(new Exception());
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setThrown(new Exception());
        f.format(r);

        String result = f.getTail((Handler) null);
        assertEquals(result, "3");
        assertEquals("0", f.getTail((Handler) null));
    }

    @Test
    public void testFormatNormal() {
        String msg = "message";
        CollectorFormatter f = new CollectorFormatter("{6}", (Formatter) null,
                (Comparator<LogRecord>) null);

        assertEquals("0", f.getTail((Handler) null));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));

        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setThrown(new Exception());
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setThrown(new Exception());
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setThrown(new Exception());
        f.format(r);

        String result = f.getTail((Handler) null);
        assertEquals(result, "2");
        assertEquals("0", f.getTail((Handler) null));
    }

    @Test
    public void testFormatMinMillis() {
        String msg = "message";
        CollectorFormatter f = new CollectorFormatter("{7,date,short} {7,time}",
                (Formatter) null,
                (Comparator<LogRecord>) null);

        long min = 100L;

        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min + 1000L);
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min + 2000L);
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min);
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min + 3000L);
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min + 4000L);
        f.format(r);

        String result = f.getTail((Handler) null);
        assertEquals(result, MessageFormat.format("{0,date,short} {0,time}", min));
    }

    @Test
    public void testFormatMaxMillis() {
        String msg = "message";
        CollectorFormatter f = new CollectorFormatter("{8,date,short} {8,time}",
                (Formatter) null,
                (Comparator<LogRecord>) null);

        long min = 100L;
        long high = 4000L;

        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min + 1000L);
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min + high);
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min + 2000L);
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min);
        f.format(r);

        r = new LogRecord(Level.SEVERE, msg);
        r.setMillis(min + 3000L);
        f.format(r);

        String result = f.getTail((Handler) null);
        assertEquals(result, MessageFormat.format("{0,date,short} {0,time}", min + high));
    }

    @Test
    public void testGetTail() {
        CollectorFormatter f = new CollectorFormatter(
                "{0}{1}{2}{3}{4}{5}{6}{7}{8}",
                (Formatter) null,
                (Comparator<LogRecord>) null);
        assertTrue(f.getTail((Handler) null).length() != 0);
        assertTrue(f.getTail((Handler) null).length() != 0);
    }

    @Test
    public void testNewDefaultFormatter() {
        String msg = "";
        CollectorFormatter f = new CollectorFormatter();
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));
        String result = f.getTail((Handler) null);
        assertTrue(result, result.length() != 0);
        assertTrue(result, result.contains("..."));
        assertTrue(result, result.contains("1"));
        assertTrue(result, result.contains("more"));
    }

    @Test
    public void testNewFormatterWithString() {
        CollectorFormatter f = new CollectorFormatter("{3}");
        f.format(new LogRecord(Level.SEVERE, ""));
        String result = f.getTail((Handler) null);
        assertEquals(result, "1");
    }

    @Test
    public void testNewFormatterNullString() {
        CollectorFormatter f = new CollectorFormatter((String) null);
        assertEquals(CollectorFormatter.class, f.getClass());
    }

    @Test
    public void testNewFormatterNullNonNullNonNull() {
        CollectorFormatter f = new CollectorFormatter((String) null,
                new XMLFormatter(), SeverityComparator.getInstance());
        assertEquals(CollectorFormatter.class, f.getClass());
    }

    @Test
    public void testNewFormatterNullNullNull() {
        CollectorFormatter f = new CollectorFormatter((String) null,
                (Formatter) null, (Comparator<LogRecord>) null);
        assertEquals(CollectorFormatter.class, f.getClass());
    }

    @Test
    public void testNewFormatterNonNullNullNull() {
        CollectorFormatter f = new CollectorFormatter("Test {0}",
                (Formatter) null, (Comparator<LogRecord>) null);
        assertEquals(CollectorFormatter.class, f.getClass());
    }

    @Test
    public void testToString() {
        String msg = "message";
        CollectorFormatter f = new CollectorFormatter("{3}", (Formatter) null,
                (Comparator<LogRecord>) null);

        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));
        f.format(new LogRecord(Level.SEVERE, msg));

        String result = f.toString();
        assertEquals(result, f.toString());
        assertEquals(result, f.getTail((Handler) null));
        assertFalse(result.equals(f.toString()));
    }

    @Test(expected = NullPointerException.class)
    public void testApplyNullAndNull() {
        CollectorFormatter f = new CollectorFormatter("{3}", (Formatter) null,
                (Comparator<LogRecord>) null);
        f.apply((LogRecord) null, (LogRecord) null);
    }

    @Test(expected = NullPointerException.class)
    public void testApplyNullAndLogRecord() {
        CollectorFormatter f = new CollectorFormatter("{3}", (Formatter) null,
                (Comparator<LogRecord>) null);
        f.apply((LogRecord) null, new LogRecord(Level.SEVERE, ""));
    }

    @Test(expected = NullPointerException.class)
    public void testApplyLogRecordAndNull() {
        CollectorFormatter f = new CollectorFormatter("{3}", (Formatter) null,
                (Comparator<LogRecord>) null);
        f.apply(new LogRecord(Level.SEVERE, ""), (LogRecord) null);
    }

    @Test
    public void testApplyWithoutComparator() {
        CollectorFormatter f = new CollectorFormatter("{3}", (Formatter) null,
                (Comparator<LogRecord>) null);
        LogRecord first = new LogRecord(Level.SEVERE, "");
        LogRecord second = new LogRecord(Level.WARNING, "");
        assertSame(second, f.apply(first, second));
    }

    @Test
    public void testApplyWithComparator() {
        CollectorFormatter f = new CollectorFormatter("{3}", (Formatter) null,
                SeverityComparator.getInstance());
        LogRecord first = new LogRecord(Level.SEVERE, "");
        LogRecord second = new LogRecord(Level.WARNING, "");
        assertSame(first, f.apply(first, second));
    }

    @Test
    public void testComparator() throws Exception {
        final String p = CollectorFormatter.class.getName();
        Properties props = new Properties();
        props.put(p.concat(".comparator"), SeverityComparator.class.getName());
        props.put(p.concat(".comparator.reverse"), "false");
        LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            CollectorFormatter cf = new CollectorFormatter();
            LogRecord first = new LogRecord(Level.SEVERE, Level.SEVERE.getName());
            LogRecord second = new LogRecord(Level.WARNING, Level.WARNING.getName());
            cf.format(second);
            cf.format(first);
            String result = cf.getTail((Handler) null);
            assertTrue(result, result.startsWith(Level.SEVERE.getName()));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testComparatorReverse() throws Exception {
        final String p = CollectorFormatter.class.getName();
        Properties props = new Properties();
        props.put(p.concat(".comparator"), SeverityComparator.class.getName());
        props.put(p.concat(".comparator.reverse"), "true");
        LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            CollectorFormatter cf = new CollectorFormatter();
            LogRecord first = new LogRecord(Level.SEVERE, Level.SEVERE.getName());
            LogRecord second = new LogRecord(Level.WARNING, Level.WARNING.getName());
            cf.format(second);
            cf.format(first);
            String result = cf.getTail((Handler) null);
            assertTrue(result, result.startsWith(Level.WARNING.getName()));
        } finally {
            manager.reset();
        }
    }

    private void read(LogManager manager, Properties props) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        props.store(out, CollectorFormatterTest.class.getName());
        manager.readConfiguration(new ByteArrayInputStream(out.toByteArray()));
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
