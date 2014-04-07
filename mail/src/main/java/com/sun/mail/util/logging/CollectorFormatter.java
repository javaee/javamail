package com.sun.mail.util.logging;

import java.lang.reflect.UndeclaredThrowableException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * A LogRecord formatter that takes a sequence of LogRecords and combines them
 * into a single summary result. Formating of the head, LogRecord, and tail are
 * delegated to the wrapped formatter.
 *
 * <p>
 * The LogManager properties are:
 * <ul>
 * <li>&lt;formatter-name&gt;.comparator name of a
 * {@linkplain java.util.Comparator} class used to choose the collected
 * <tt>LogRecord</tt>. If a comparator is specified then the max
 * <tt>LogRecord</tt> is chosen. If a comparator is not specified then, the last
 * record is chosen (defaults to <tt>null</tt>).
 *
 * <li>&lt;formatter-name&gt;.comparator.reverse a boolean
 * <tt>true</tt> to collect the min <tt>LogRecord</tt> or <tt>false</tt> to
 * collect the max <tt>LogRecord</tt>. (defaults to <tt>false</tt>)
 *
 * <li>&lt;formatter-name&gt;.format the
 * {@linkplain java.text.MessageFormat MessageFormat} string used to format the
 * collected summary statistics. The arguments are explained in detail in the
 * {@linkplain #getTail(java.util.logging.Handler) getTail} documentation.
 * (defaults to "{0}{1}{2}{4,choice,-1#|0&lt;... {4,number,integer} more}\n")
 *
 * <li>&lt;formatter-name&gt;.formatter name of a <tt>Formatter</tt> class used
 * to format the collected LogRecord. (defaults to {@link CompactFormatter})
 *
 * </ul>
 *
 * @author Jason Mehrens
 * @since JavaMail 1.5.2
 */
public class CollectorFormatter extends Formatter {

    /**
     * Avoid depending on JMX runtime bean to get the start time.
     */
    private static final long INIT_TIME = System.currentTimeMillis();
    /**
     * The message format string used as the formatted output.
     */
    private final String fmt;
    /**
     * The formatter used to format the chosen log record.
     */
    private final Formatter formatter;
    /**
     * The comparator used to pick the log record to format.
     */
    private final Comparator<? super LogRecord> comparator;
    /**
     * The last accepted record. Synchronized access is preferred over volatile
     * for this class.
     */
    private LogRecord last;
    /**
     * The number of log records that have been formatted.
     */
    private long count;
    /**
     * The number of log records that have been formatted with a thrown object.
     */
    private long thrown;
    /**
     * The eldest log record time.
     */
    private long minMillis;
    /**
     * The newest log record time.
     */
    private long maxMillis;

    /**
     * Creates the formatter using the LogManager defaults.
     *
     * @throws SecurityException if a security manager exists and the caller
     * does not have <tt>LoggingPermission("control")</tt>.
     * @throws UndeclaredThrowableException if there are problems when loading
     * from the LogManager.
     */
    public CollectorFormatter() {
        final String p = getClass().getName();
        this.fmt = initFormat(p);
        this.formatter = initFormatter(p);
        this.comparator = initComparator(p);
        reset();
    }

    /**
     * Creates the formatter using the given format.
     *
     * @param format the message format.
     * @throws SecurityException if a security manager exists and the caller
     * does not have <tt>LoggingPermission("control")</tt>.
     * @throws UndeclaredThrowableException if there are problems when loading
     * from the LogManager.
     */
    public CollectorFormatter(String format) {
        final String p = getClass().getName();
        this.fmt = format == null ? initFormat(p) : format;
        this.formatter = initFormatter(p);
        this.comparator = initComparator(p);
        reset();
    }

    /**
     * Creates the formatter using the given values.
     *
     * @param format the format string.
     * @param f the formatter used on the collected log record or null.
     * @param c the comparator used to determine which log record to format or
     * null.
     * @throws SecurityException if a security manager exists and the caller
     * does not have <tt>LoggingPermission("control")</tt>.
     * @throws UndeclaredThrowableException if there are problems when loading
     * from the LogManager.
     */
    public CollectorFormatter(String format, Formatter f,
            Comparator<? super LogRecord> c) {
        final String p = getClass().getName();
        this.fmt = format == null ? initFormat(p) : format;
        this.formatter = f;
        this.comparator = c;
        reset();
    }

    /**
     * Accumulates log records which will be used to produce the final output.
     * The output is generated using the {@link getTail} method which also
     * resets this formatter back to its original state.
     *
     * @param record the record to store.
     * @return an empty string.
     * @throws NullPointerException if the given record is null.
     */
    @Override
    public String format(final LogRecord record) {
        if (record == null) {
            throw new NullPointerException();
        }

        boolean accepted;
        do {
            final LogRecord peek = peek();
            //The self compare of the first record acts like a type check.
            LogRecord update = apply(peek != null ? peek : record, record);
            if (peek != update) { //Not identical.
                update.getSourceMethodName(); //Infer caller.
                accepted = acceptAndUpdate(peek, update);
            } else {
                accepted = true;
                accept(record);
            }
        } while (!accepted);
        return "";
    }

    /**
     * Formats the collected LogRecord and summary statistics. The collected
     * results are reset after calling this method.
     *
     * <ol start='0'>
     * <li>{@code head} the
     * {@linkplain Formatter#getHead(java.util.logging.Handler) head} string
     * returned from the target formatter and
     * {@linkplain #finish(java.lang.String) finished} by this formatter.
     * <li>{@code formatted} the current log record
     * {@linkplain Formatter#format(java.util.logging.LogRecord) formatted} by
     * the target formatter and {@linkplain #finish(java.lang.String) finished}
     * by this formatter.
     * <li>{@code tail} the
     * {@linkplain Formatter#getTail(java.util.logging.Handler) tail} string
     * returned from the target formatter and
     * {@linkplain #finish(java.lang.String) finished} by this formatter.
     * <li>{@code count} the total number of log records
     * {@linkplain #format consumed} by this formatter.
     * <li>{@code remaining} the count minus one.
     * <li>{@code thrown} the total number of log records
     * {@linkplain #format consumed} by this formatter with an assigned
     * throwable.
     * <li>{@code normal messages} the count minus the thrown.
     * <li>{@code minMillis} the eldest log record event time
     * {@linkplain #format consumed} by this formatter. If no records were
     * formatted then this is set to the approximate start time of the JVM. By
     * default this parameter is defined as a number. The format type and format
     * style rules from the {@link java.text.MessageFormat} should be used to
     * convert this to a date or time.
     * <li>{@code maxMillis} the most recent log record event time
     * {@linkplain #format consumed} by this formatter. If no records were
     * formatted then this is set to the current time. By default this parameter
     * is defined as a number. The format type and format style rules from the
     * {@link java.text.MessageFormat} should be used to convert this to a date
     * or time.
     * </ol>
     *
     * @param h the handler or null.
     * @return the output string.
     */
    @Override
    public String getTail(final Handler h) {
        return formatRecord(h, true);
    }

    /**
     * Peeks at the current LogRecord and formats it.
     *
     * @return the current record formatted or the default toString.
     * @see #getTail(java.util.logging.Handler)
     */
    @Override
    public String toString() {
        String result;
        try {
            result = formatRecord((Handler) null, false);
        } catch (final RuntimeException ignore) {
            result = super.toString();
        }
        return result;
    }

    /**
     * Used to choose the collected LogRecord. This implementation returns the
     * greater of two LogRecords.
     *
     * @param t the current record.
     * @param u the record that could replace the current.
     * @return the greater of the given log records.
     * @throws NullPointerException may occur if either record is null.
     */
    protected LogRecord apply(final LogRecord t, final LogRecord u) {
        if (t == null || u == null) {
            throw new NullPointerException();
        }

        if (comparator != null) {
            return comparator.compare(t, u) >= 0 ? t : u;
        } else {
            return u;
        }
    }

    /**
     * Updates the summary statistics but does not store the given LogRecord.
     *
     * @param record the LogRecord used to collect statistics.
     */
    private synchronized void accept(final LogRecord record) {
        final long millis = record.getMillis();
        minMillis = Math.min(minMillis, millis);
        maxMillis = Math.max(maxMillis, millis);
        ++count;
        if (record.getThrown() != null) {
            ++thrown;
        }
    }

    /**
     * Resets all of the collected summary statistics including the LogRecord.
     */
    private synchronized void reset() {
        last = null;
        count = 0L;
        thrown = 0L;
        minMillis = Long.MAX_VALUE;
        maxMillis = Long.MIN_VALUE;
    }

    /**
     * Formats the given record with the head and tail.
     *
     * @param h the Handler or null.
     * @param reset true if the summary statistics and LogRecord should be reset
     * back to initial values.
     * @return the formatted string.
     * @see #getTail(java.util.logging.Handler)
     */
    private String formatRecord(final Handler h, final boolean reset) {
        final LogRecord record;
        final long c;
        final long t;
        long msl;
        long msh;
        synchronized (this) {
            record = last;
            c = count;
            t = thrown;
            msl = minMillis;
            msh = maxMillis;

            if (reset) { //BUG ID 6351685
                reset();
            }
        }

        if (c == 0L) {  //Use the estimated lifespan of this class.
            msl = INIT_TIME;
            msh = System.currentTimeMillis();
        }

        final String head;
        final String msg;
        final String tail;
        final Formatter f = this.formatter;
        if (f != null) {
            synchronized (f) {
                head = f.getHead(h);
                msg = record != null ? f.format(record) : "";
                tail = f.getTail(h);
            }
        } else {
            head = msg = tail = "";
        }

        Locale l = null;
        if (record != null) {
            ResourceBundle rb = record.getResourceBundle();
            l = rb == null ? null : rb.getLocale();
        }

        //NumberFormat used by the MessageFormat requires a non null locale.
        final MessageFormat mf;
        if (l == null) {
            mf = new MessageFormat(fmt);
        } else {
            mf = new MessageFormat(fmt, l);
        }

        /**
         * These arguments are described in the getTail documentation.
         */
        return mf.format(new Object[]{finish(head), finish(msg), finish(tail),
            c, (c - 1), t, (c - t), msl, msh});
    }

    /**
     * Applied to the head, format, and tail returned by the target formatter.
     * This implementation trims all input strings.
     *
     * @param s the string to transform.
     * @return the transformed string.
     * @throws NullPointerException if the given string is null.
     */
    protected String finish(String s) {
        return s.trim();
    }

    /**
     * Peek at the current log record.
     *
     * @return null or the current log record.
     */
    private synchronized LogRecord peek() {
        return this.last;
    }

    /**
     * Updates the summary statistics and stores given LogRecord if the expected
     * record matches the current record.
     *
     * @param e the expected record.
     * @param u the update record.
     * @return true if the update was performed.
     */
    private synchronized boolean acceptAndUpdate(LogRecord e, LogRecord u) {
        if (e == this.last) {
            accept(u);
            this.last = u;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the message format string from the LogManager or creates the default
     * message format string.
     *
     * @param p the class name prefix.
     * @return the format string.
     */
    private String initFormat(final String p) {
        final LogManager m = LogManagerProperties.getLogManager();
        String v = m.getProperty(p.concat(".format"));
        if (v == null || v.length() == 0) {
            v = "{0}{1}{2}{4,choice,-1#|0<... {4,number,integer} more}\n";
        }
        return v;
    }

    /**
     * Gets and creates the formatter from the LogManager or creates the default
     * formatter.
     *
     * @param p the class name prefix.
     * @return the formatter.
     */
    private Formatter initFormatter(final String p) {
        final LogManager m = LogManagerProperties.getLogManager();
        Formatter f;
        String v = m.getProperty(p.concat(".formatter"));
        if (v != null && v.length() != 0) {
            if (!"null".equalsIgnoreCase(v)) {
                try {
                    f = LogManagerProperties.newFormatter(v);
                } catch (final RuntimeException re) {
                    throw re;
                } catch (final Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            } else {
                f = null;
            }
        } else {
            //Don't force the byte code verifier to load the formatter.
            f = Formatter.class.cast(new CompactFormatter());
        }
        return f;
    }

    /**
     * Gets and creates the comparator from the LogManager or returns the
     * default comparator.
     *
     * @param p the class name prefix.
     * @return the comparator or null.
     * @throws IllegalArgumentException if it was specified that the comparator
     * should be reversed but no initial comparator was specified.
     * @throws UndeclaredThrowableException if the comparator can not be
     * created.
     */
    @SuppressWarnings("unchecked")
    private Comparator<? super LogRecord> initComparator(final String p) {
        final LogManager m = LogManagerProperties.getLogManager();
        Comparator<? super LogRecord> c;
        final String name = m.getProperty(p.concat(".comparator"));
        final String reverse = m.getProperty(p.concat(".comparator.reverse"));
        try {
            if (name != null) {
                if (!"null".equalsIgnoreCase(name)) {
                    c = LogManagerProperties.newComparator(name);
                    if (Boolean.parseBoolean(reverse)) {
                        assert c != null;
                        c = LogManagerProperties.reverseOrder(c);
                    }
                } else {
                    if (reverse != null) {
                        throw new IllegalArgumentException(
                                "No comparator to reverse.");
                    } else {
                        c = null; //No ordering.
                    }
                }
            } else {
                if (reverse != null) {
                    throw new IllegalArgumentException(
                            "No comparator to reverse.");
                } else {
                    //Don't force the byte code verifier to load the comparator.
                    c = Comparator.class.cast(SeverityComparator.getInstance());
                }
            }
        } catch (final RuntimeException re) {
            throw re; //Avoid catch all.
        } catch (final Exception e) {
            throw new UndeclaredThrowableException(e);
        }
        return c;
    }
}
