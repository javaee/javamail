/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2011 Jason Mehrens. All rights reserved.
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
import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.logging.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

/**
 * <tt>Handler</tt> that formats log records as an email message.
 *
 * <p>
 * This <tt>Handler</tt> will store a fixed number of log records used to
 * generate a single email message.  When the internal buffer reaches capacity,
 * all log records are formatted and placed in an email which is sent to an
 * email server.  The code to manually setup this handler can be as simple as
 * the following:
 *
 * <tt><pre>
 *      Properties props = new Properties();
 *      props.put("mail.smtp.host", "my-mail-server");
 *      props.put("mail.to", "me@example.com");
 *      props.put("verify", "local");
 *      MailHandler h = new MailHandler(props);
 *      h.setLevel(Level.WARNING);
 * </pre></tt>
 *
 * <p>
 * <b>Configuration:</b>
 * The LogManager must define at least one or more recipient addresses and a 
 * mail host for outgoing email.  The code to setup this handler via the
 * logging properties can be as simple as the following:
 *
 * <tt><pre>
 *      #Default MailHandler settings.
 *      com.sun.mail.util.logging.MailHandler.mail.smtp.host = my-mail-server
 *      com.sun.mail.util.logging.MailHandler.mail.to = me@example.com
 *      com.sun.mail.util.logging.MailHandler.level = WARNING
 *      com.sun.mail.util.logging.MailHandler.verify = local
 * </pre></tt>
 *
 * All mail properties documented in the <tt>Java Mail API</tt> cascade to the
 * LogManager by prefixing a key using the fully qualified class name of this
 * <tt>MailHandler</tt> dot mail property.  If the prefixed property is not
 * found, then the mail property itself is searched in the LogManager.
 * By default each <tt>MailHandler</tt> is initialized using the following
 * LogManager configuration properties.  If properties are not defined,
 * or contain invalid values, then the specified default values are used.
 *
 * <ul>
 * <li>com.sun.mail.util.logging.MailHandler.attachment.filters a comma 
 * separated list of <tt>Filter</tt> class names used to create each attachment.
 * The literal <tt>null</tt> is reserved for attachments that do not require
 * filtering. (default is no filters)
 *
 * <li>com.sun.mail.util.logging.MailHandler.attachment.formatters a comma 
 * separated list of <tt>Formatter</tt> class names used to create each
 * attachment. (default is no attachments)
 *
 * <li>com.sun.mail.util.logging.MailHandler.attachment.names a comma separated
 * list of names or <tt>Formatter</tt> class names of each attachment.  The
 * attachment file names must not contain any line breaks.
 * (default is no attachments names)
 * 
 * <li>com.sun.mail.util.logging.MailHandler.authenticator name of a 
 * {@linkplain javax.mail.Authenticator} class used to provide login credentials
 * to the email server. (default is <tt>null</tt>)
 *
 * <li>com.sun.mail.util.logging.MailHandler.capacity the max number of 
 * <tt>LogRecord</tt> objects include in each email message.
 * (defaults to <tt>1000</tt>)
 *
 * <li>com.sun.mail.util.logging.MailHandler.comparator name of a
 * {@linkplain java.util.Comparator} class used to sort the published
 * <tt>LogRecord</tt> objects prior to all formatting.
 * (defaults to <tt>null</tt> meaning records are unsorted).
 *
 * <!--
 * <li>com.sun.mail.util.logging.MailHandler.comparator.reverse a boolean 
 * <tt>true</tt> to reverse the order of the specified comparator or
 * <tt>false</tt> to retain the original order. (defaults to <tt>false</tt>)
 * -->
 * 
 * <li>com.sun.mail.util.logging.MailHandler.encoding the name of the character
 * set encoding to use (defaults to the default platform encoding).
 *
 * <li>com.sun.mail.util.logging.MailHandler.errorManager name of a 
 * <tt>ErrorManager</tt> class used to handle any configuration or mail
 * transport problems. (defaults to <tt>java.util.logging.ErrorManager</tt>)
 *
 * <li>com.sun.mail.util.logging.MailHandler.filter name of a <tt>Filter</tt>
 * class used for the body of the message. (defaults to <tt>null</tt>,
 * allow all records)
 *
 * <li>com.sun.mail.util.logging.MailHandler.formatter name of a 
 * <tt>Formatter</tt> class used to format the body of this message.
 * (defaults to <tt>java.util.logging.SimpleFormatter</tt>)
 *
 * <li>com.sun.mail.util.logging.MailHandler.level specifies the default level
 * for this <tt>Handler</tt> (defaults to <tt>Level.WARNING</tt>).
 *
 * <li>com.sun.mail.util.logging.MailHandler.mail.bcc a comma separated list of
 * addresses which will be blind carbon copied.  Typically, this is set to the
 * recipients that may need to be privately notified of a log message or 
 * notified that a log message was sent to a third party such as a support team.
 * (defaults to <tt>null</tt>, none)
 * 
 * <li>com.sun.mail.util.logging.MailHandler.mail.cc a comma separated list of
 * addresses which will be carbon copied.  Typically, this is set to the 
 * recipients that may need to be notified of a log message but, are not
 * required to provide direct support.  (defaults to <tt>null</tt>, none)
 * 
 * <li>com.sun.mail.util.logging.MailHandler.mail.from a comma separated list of
 * addresses which will be from addresses. Typically, this is set to the email
 * address identifying the user running the application.
 * (defaults to {@linkplain javax.mail.Message#setFrom()})
 *
 * <li>com.sun.mail.util.logging.MailHandler.mail.host the host name or IP
 * address of the email server. (defaults to <tt>null</tt>, no host)
 *
 * <li>com.sun.mail.util.logging.MailHandler.mail.reply.to a comma separated
 * list of addresses which will be reply-to addresses.  Typically, this is set
 * to the recipients that provide support for the application itself.
 * (defaults to <tt>null</tt>, none)
 *
 * <li>com.sun.mail.util.logging.MailHandler.mail.to a comma separated list of
 * addresses which will be send-to addresses. Typically, this is set to the
 * recipients that provide support for the application, system, and/or
 * supporting infrastructure.  (defaults to <tt>null</tt>, none)
 *
 * <li>com.sun.mail.util.logging.MailHandler.mail.sender a single address
 * identifying sender of the email; never equal to the from address.  Typically,
 * this is set to the email address identifying the application itself.
 * (defaults to <tt>null</tt>, none)
 *
 * <li>com.sun.mail.util.logging.MailHandler.pushLevel the level which will
 * trigger an early push. (defaults to <tt>Level.OFF</tt>, only push when full)
 *
 * <li>com.sun.mail.util.logging.MailHandler.pushFilter the name of a
 * <tt>Filter</tt> class used to trigger an early push.
 * (defaults to <tt>null</tt>, no early push)
 *
 * <li>com.sun.mail.util.logging.MailHandler.subject the name of a 
 * <tt>Formatter</tt> class or string literal used to create the subject line.
 * The subject line must not contain any line breaks. (defaults to empty string)
 *
 * <li>com.sun.mail.util.logging.MailHandler.verify <a name="verify">used</a> to
 * verify all of the <tt>Handler</tt> properties prior to a push.  If set to a 
 * value of <tt>local</tt> the <tt>Handler</tt> will only verify settings of the
 * local machine. If set to a value of <tt>remote</tt>, the <tt>Handler</tt>
 * will verify all local settings and try to establish a connection with the
 * email server.  If the value is not set, equal to an empty string, or equal to
 * the literal <tt>null</tt> then minimal or no settings are verified prior to a
 * push.  If this <tt>Handler</tt> is only implicitly closed by the
 * <tt>LogManager</tt>, then verification should be turned on.
 * (defaults to <tt>null</tt>, no verify).
 *
 * <p>
 * <b>Sorting:</b>
 * All <tt>LogRecord</tt> objects are ordered prior to formatting if this
 * <tt>Handler</tt> has a non null comparator.  Developers might be interested
 * in sorting the formatted email by thread id, time, and sequence properties
 * of a <tt>LogRecord</tt>.  Where as system administrators might be interested
 * in sorting the formatted email by thrown, level, time, and sequence
 * properties of a <tt>LogRecord</tt>.  If comparator for this handler is
 * <tt>null</tt> then the order is unspecified.
 *
 * <p>
 * <b>Formatting:</b>
 * The main message body is formatted using the <tt>Formatter</tt> returned by
 * <tt>getFormatter()</tt>.  Only records that pass the filter returned by
 * <tt>getFilter()</tt> will be included in the message body.  The subject
 * <tt>Formatter</tt> will see all <tt>LogRecord</tt> objects that were
 * published regardless of the current <tt>Filter</tt>.  The MIME type of the
 * message body can be {@linkplain FileTypeMap#setDefaultFileTypeMap overridden}
 * by adding a MIME {@linkplain MimetypesFileTypeMap entry} using the simple
 * class name of the body formatter as the file extension.  The MIME type of the
 * attachments can be overridden by changing the attachment file name extension
 * or by editing the default MIME entry for a specific file name extension.
 * 
 * <p>
 * <b>Attachments:</b>
 * This <tt>Handler</tt> allows multiple attachments per each email.
 * The attachment order maps directly to the array index order in this
 * <tt>Handler</tt> with zero index being the first attachment.  The number of
 * attachment formatters controls the number of attachments per email and
 * the content type of each attachment.  The attachment filters determine if a
 * <tt>LogRecord</tt> will be included in an attachment.  If an attachment 
 * filter is <tt>null</tt> then all records are included for that attachment.
 * Attachments without content will be omitted from email message.  The
 * attachment name formatters create the file name for an attachment.
 * Custom attachment name formatters can be used to generate an attachment name
 * based on the contents of the attachment.
 *
 * <p>
 * <b>Push Level and Push Filter:</b>
 * The push method, push level, and optional push filter can be used to
 * conditionally trigger a push at or prior to full capacity.  When a push
 * occurs, the current buffer is formatted into an email and is sent to the
 * email server.  If the push method, push level, or push filter trigger a push
 * then the outgoing email is flagged as high importance with urgent priority.
 *
 * <p>
 * <b>Buffering:</b>
 * Log records that are published are stored in an internal buffer.  When this
 * buffer reaches capacity the existing records are formatted and sent in an
 * email.  Any published records can be sent before reaching capacity by
 * explictly calling the <tt>flush</tt>, <tt>push</tt>, or <tt>close</tt> 
 * methods.  If a circular buffer is required then this handler can be wrapped
 * with a {@linkplain java.util.logging.MemoryHandler} typically with an
 * equivalent capacity, level, and push level.
 *
 * <p>
 * <b>Error Handling:</b>
 * If the transport of an email message fails, the email is converted to
 * a {@linkplain javax.mail.internet.MimeMessage#writeTo raw}
 * {@linkplain java.io.ByteArrayOutputStream#toString(java.lang.String) string}
 * and is then passed as the <tt>msg</tt> parameter to
 * {@linkplain Handler#reportError reportError} along with the exception
 * describing the cause of the failure.  This allows custom error managers to
 * store, {@linkplain javax.mail.internet.MimeMessage#MimeMessage(
 * javax.mail.Session, java.io.InputStream) reconstruct}, and resend the
 * original MimeMessage.  The message parameter string is <b>not</b> a raw email
 * if it starts with value returned from <tt>Level.SEVERE.getName()</tt>.
 * Custom error managers can use the following test to determine if the 
 * <tt>msg</tt> parameter from this handler is a raw email:
 *
 * <tt><pre>
 * public void error(String msg, Exception ex, int code) {
 *      if (msg == null || msg.length() == 0 || msg.startsWith(Level.SEVERE.getName())) {
 *          super.error(msg, ex, code);
 *      } else {
 *          //The 'msg' parameter is a raw email.
 *      }
 * }
 * </pre></tt>
 * 
 * @author Jason Mehrens
 * @since JavaMail 1.4.3
 */
public class MailHandler extends Handler {

    /**
     * Cache the off value.
     */
    private static final int offValue = Level.OFF.intValue();
    /**
     * Used to turn off security checks.
     */
    private volatile boolean sealed;
    /**
     * Determines if we are inside of a push.
     * Makes the handler properties read-only during a push.
     */
    private boolean isWriting;
    /**
     * Holds all of the email server properties.
     */
    private Properties mailProps;
    /**
     * Holds the authenticator required to login to the email server.
     */
    private Authenticator auth;
    /**
     * Holds the session object used to generate emails.
     * Sessions can be shared by multiple threads.
     * See BUGID 6228391
     */
    private Session session;
    /**
     * Holds all of the log records that will be used to create the email.
     */
    private LogRecord[] data;
    /**
     * The number of log records in the buffer.
     */
    private int size;
    /**
     * The maximum number of log records to format per email.
     * Used to roughly bound the size of an email.
     * Every time the capacity is reached, the handler will push.
     * The capacity will be negative if this handler is closed.
     * Negative values are used to ensure all records are pushed.
     */
    private int capacity;
    /**
     * Used to order all log records prior to formatting.  The main email body
     * and all attachments use the order determined by this comparator.  If no
     * comparator is present the log records will be in no specified order.
     */
    private Comparator/*<? super LogRecord>*/ comparator;
    /**
     * Holds the formatter used to create the subject line of the email.
     * A subject formatter is not required for the email message.
     * All published records pass through the subject formatter.
     */
    private Formatter subjectFormatter;
    /**
     * Holds the push level for this handler.
     * This is only required if an email must be sent prior to shutdown
     * or before the buffer is full.
     */
    private Level pushLevel;
    /**
     * Holds the push filter for trigger conditions requiring an early push.
     * Only gets called if the given log record is greater than or equal
     * to the push level and the push level is not Level.OFF.
     */
    private Filter pushFilter;
    /**
     * Holds the filters for each attachment.  Filters are optional for
     * each attachment.
     */
    private Filter[] attachmentFilters;
    /**
     * Holds the formatters that create the content for each attachment.
     * Each formatter maps directly to an attachment.  The formatters
     * getHead, format, and getTail methods are only called if one or more
     * log records pass through the attachment filters.
     */
    private Formatter[] attachmentFormatters;
    /**
     * Holds the formatters that create the file name for each attachment.
     * Each formatter must produce a non null and non empty name.
     * The final file name will be the concatenation of one getHead call, plus
     * all of the format calls, plus one getTail call.
     */
    private Formatter[] attachmentNames;
    /**
     * Used to override the content type for the body and set the content type
     * for each attachment.
     */
    private FileTypeMap contentTypes;

    /**
     * Creates a <tt>MailHandler</tt> that is configured by the
     * <tt>LogManager</tt> configuration properties.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public MailHandler() {
        init();
        sealed = true;
    }

    /**
     * Creates a mail handler with the specified capacity.
     * @param capacity of the internal buffer.
     * @throws IllegalArgumentException if <tt>capacity</tt> less than one.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public MailHandler(final int capacity) {
        init();
        sealed = true;
        setCapacity0(capacity);
    }

    /**
     * Creates a mail handler with the given mail properties.
     * The key/value pairs are defined in the <tt>Java Mail API</tt>
     * documentation.  This <tt>Handler</tt> will also search the
     * <tt>LogManager</tt> for defaults if needed.
     * @param props a non <tt>null</tt> properties object.
     * @throws NullPointerException if <tt>props</tt> is <tt>null</tt>.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public MailHandler(final Properties props) {
        init();
        sealed = true;
        setMailProperties0(props);
    }

    /**
     * Check if this <tt>Handler</tt> would actually log a given
     * <tt>LogRecord</tt> into its internal buffer.
     * <p>
     * This method checks if the <tt>LogRecord</tt> has an appropriate level and
     * whether it satisfies any <tt>Filter</tt> including any attachment filters.
     * However it does <b>not</b> check whether the <tt>LogRecord</tt> would
     * result in a "push" of the buffer contents.
     * <p>
     * @param record  a <tt>LogRecord</tt>
     * @return true if the <tt>LogRecord</tt> would be logged.
     */
    public boolean isLoggable(final LogRecord record) {
        int levelValue = getLevel().intValue();
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) {
            return false;
        }

        Filter body = getFilter();
        if (body == null || body.isLoggable(record)) {
            return true;
        }

        return isAttachmentLoggable(record);
    }

    /**
     * Stores a <tt>LogRecord</tt> in the internal buffer.
     * <p>
     * The <tt>isLoggable</tt> method is called to check if the given log record
     * is loggable. If the given record is loggable, it is copied into
     * an internal buffer.  Then the record's level property is compared with
     * the push level. If the given level of the <tt>LogRecord</tt>
     * is greater than or equal to the push level then the push filter is
     * called.  If no push filter exists, the push filter returns true,
     * or the capacity of the internal buffer has been reached then all buffered
     * records are formatted into one email and sent to the server.
     *
     * @param  record  description of the log event.
     */
    public void publish(final LogRecord record) {
        /**
         * It is possible for the handler to be closed after the
         * call to isLoggable.  In that case, the current thread
         * will push to ensure that all published records are sent.
         * See close().
         */
        if (isLoggable(record)) {
            record.getSourceMethodName(); //infer caller, outside of lock
            publish0(record);
        }
    }

    /**
     * Performs the publish after the record has been filtered.
     * @param record the record.
     * @since JavaMail 1.4.5
     */
    private void publish0(final LogRecord record) {
        MessageContext ctx;
        boolean priority;
        synchronized (this) {
            if (size == data.length && size < capacity) {
                grow();
            }

            if (size < data.length) {
                data[size] = record;
                ++size; //be nice to client compiler.
                priority = isPushable(record);
                if (priority || size >= capacity) {
                    ctx = writeLogRecords(ErrorManager.WRITE_FAILURE);
                } else {
                    ctx = null;
                }
            } else { //drain the buffer and try again.
                priority = false;
                ctx = flushAndRePublish(record);
            }
        }

        if (ctx != null) {
            send(ctx, priority, ErrorManager.WRITE_FAILURE);
        }
    }

    /**
     * Flushes the buffer into a MessageContext and then tries to store
     * the given record.  This method will only be called in cases of
     * reentrant filters, formatters, or comparators.  The push filter is
     * the only filter allowed to be reentrant.  This is because the push filter
     * is never used during formatting and only called after a record is stored.
     * @param record the log record still needed to publish.
     * @return the MessageContext containing the previous push or null.
     * @since JavaMail 1.4.5
     */
    private MessageContext flushAndRePublish(final LogRecord record) {
        MessageContext ctx = writeLogRecords(ErrorManager.WRITE_FAILURE);
        if (ctx != null) { //try again with an empty buffer.
            /**
             * The common case should be a simple store, no push.
             * Rarely should we be pushing while holding a lock.
             */
            publish0(record);
        } else {
            /**
             * This record is lost, report this to the error manager.
             * Use ISE because this is a push inside of a push.
             */
            final SimpleFormatter f = new SimpleFormatter();
            final String msg = "Log record " + record.getSequenceNumber()
                + " was not published. "
                + head(f) + format(f, record) + tail(f, "");
            reportError(msg,
                    new IllegalStateException("Unable to drain buffer."),
                    ErrorManager.WRITE_FAILURE);
        }
        return ctx;
    }

    /**
     * Pushes any buffered records to the email server as high importance with
     * urgent priority.  The internal buffer is then cleared.  Does nothing if
     * called from inside a push.
     * @see #flush()
     */
    public void push() {
        push(true, ErrorManager.FLUSH_FAILURE);
    }

    /**
     * Pushes any buffered records to the email server as normal priority.
     * The internal buffer is then cleared.  Does nothing if called from inside
     * a push.
     * @see #push()
     */
    public void flush() {
        push(false, ErrorManager.FLUSH_FAILURE);
    }

    /**
     * Prevents any other records from being published.
     * Pushes any buffered records to the email server as normal priority.
     * The internal buffer is then cleared.  Once this handler is closed it
     * will remain closed.
     * <p>
     * If this <tt>Handler</tt> is only implicitly closed by the
     * <tt>LogManager</tt>, then <a href="#verify">verification</a> should be
     * turned on.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @see #flush()
     */
    public void close() {
        MessageContext ctx = null;
        synchronized (this) {
            super.setLevel(Level.OFF); //security check first.
            try {
                ctx = writeLogRecords(ErrorManager.CLOSE_FAILURE);
            } finally {
                /**
                 * The sign bit of the capacity is set to ensure that records
                 * that have passed isLoggable, but have yet to be added to the
                 * internal buffer, are immediately pushed as an email.
                 */
                if (this.capacity > 0) {
                    this.capacity = -this.capacity;
                }

                if (size == 0 && data.length != 1) { //ensure not inside a push.
                    this.data = new LogRecord[1];
                }
            }
        }

        if (ctx != null) {
            send(ctx, false, ErrorManager.CLOSE_FAILURE);
        }
    }

    /**
     * Set the log level specifying which message levels will be
     * logged by this <tt>Handler</tt>.  Message levels lower than this
     * value will be discarded.
     * @param newLevel   the new value for the log level
     * @throws NullPointerException if <tt>newLevel</tt> is <tt>null</tt>.
     * @throws SecurityException  if a security manager exists and
     *          the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public synchronized void setLevel(final Level newLevel) {
        if (this.capacity > 0) {
            super.setLevel(newLevel);
        } else { //don't allow a closed handler to be opened (half way).
            if (newLevel == null) {
                throw new NullPointerException();
            }
            checkAccess();
        }
    }

    /**
     * Gets the push level.  The default is <tt>Level.OFF</tt> meaning that
     * this <tt>Handler</tt> will only push when the internal buffer is full.
     * @return the push level.
     */
    public final synchronized Level getPushLevel() {
        return this.pushLevel;
    }

    /**
     * Sets the push level.  This level is used to trigger a push so that
     * all pending records are formatted and sent to the email server.  When
     * the push level triggers a send, the resulting email is flagged as
     * high importance with urgent priority.
     * @param level Level object.
     * @throws NullPointerException if <tt>level</tt> is <tt>null</tt>.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws IllegalStateException if called from inside a push.
     */
    public final synchronized void setPushLevel(final Level level) {
        checkAccess();
        if (level == null) {
            throw new NullPointerException();
        }

        if (isWriting) {
            throw new IllegalStateException();
        }
        this.pushLevel = level;
    }

    /**
     * Gets the push filter.  The default is <tt>null</tt>.
     * @return the push filter or <tt>null</tt>.
     */
    public final synchronized Filter getPushFilter() {
        return this.pushFilter;
    }

    /**
     * Sets the push filter.  This filter is only called if the given
     * <tt>LogRecord</tt> level was greater than the push level.  If this
     * filter returns <tt>true</tt>, all pending records are formatted and sent
     * to the email server.  When the push filter triggers a send, the resulting
     * email is flagged as high importance with urgent priority.
     * @param filter push filter or <tt>null</tt>
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws IllegalStateException if called from inside a push.
     */
    public final synchronized void setPushFilter(final Filter filter) {
        checkAccess();
        if (isWriting) {
            throw new IllegalStateException();
        }
        this.pushFilter = filter;
    }

    /**
     * Gets the comparator used to order all <tt>LogRecord</tt> objects prior
     * to formatting.  If <tt>null</tt> then the order is unspecified.
     * @return the <tt>LogRecord</tt> comparator.
     */
    public final synchronized Comparator/*<? super LogRecord>*/ getComparator() {
        return this.comparator;
    }

    /**
     * Sets the comparator used to order all <tt>LogRecord</tt> objects prior
     * to formatting.  If <tt>null</tt> then the order is unspecified.
     * @param c the <tt>LogRecord</tt> comparator.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws IllegalStateException if called from inside a push.
     */
    public final synchronized void setComparator(Comparator/*<? super LogRecord>*/ c) {
        checkAccess();
        if (isWriting) {
            throw new IllegalStateException();
        }
        this.comparator = c;
    }

    /**
     * Gets the number of log records the internal buffer can hold.  When
     * capacity is reached, <tt>Handler</tt> will format all <tt>LogRecord</tt>
     * objects into one email message.
     * @return the capacity.
     */
    public final synchronized int getCapacity() {
        assert capacity != Integer.MIN_VALUE && capacity != 0 : capacity;
        return Math.abs(capacity);
    }

    /**
     * Gets the <tt>Authenticator</tt> used to login to the email server.
     * @return an <tt>Authenticator</tt> or <tt>null</tt> if none is required.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public final synchronized Authenticator getAuthenticator() {
        checkAccess();
        return this.auth;
    }

    /**
     * Sets the <tt>Authenticator</tt> used to login to the email server.
     * @param auth an <tt>Authenticator</tt> object or null if none is required.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws IllegalStateException if called from inside a push.
     */
    public final void setAuthenticator(final Authenticator auth) {
        checkAccess();

        Session settings;
        synchronized (this) {
            if (isWriting) {
                throw new IllegalStateException();
            }
            this.auth = auth;
            settings = this.fixUpSession();
        }
        verifySettings(settings);
    }

    /**
     * Sets the mail properties used for the session.  The key/value pairs
     * are defined in the <tt>Java Mail API</tt> documentation.  This
     * <tt>Handler</tt> will also search the <tt>LogManager</tt> for defaults
     * if needed.
     * @param props a non <tt>null</tt> properties object.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws NullPointerException if <tt>props</tt> is <tt>null</tt>.
     * @throws IllegalStateException if called from inside a push.
     */
    public final void setMailProperties(Properties props) {
        this.setMailProperties0(props);
    }

    private void setMailProperties0(Properties props) {
        checkAccess();
        props = (Properties) props.clone();
        Session settings;
        synchronized (this) {
            if (isWriting) {
                throw new IllegalStateException();
            }
            this.mailProps = props;
            settings = this.fixUpSession();
        }
        verifySettings(settings);
    }

    /**
     * Gets a copy of the mail properties used for the session.
     * @return a non null properties object.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public final Properties getMailProperties() {
        checkAccess();
        final Properties props;
        synchronized (this) {
            props = this.mailProps;
        }
        return (Properties) props.clone();
    }

    /**
     * Gets the attachment filters.  If the attachment filter does not
     * allow any <tt>LogRecord</tt> to be formatted, the attachment may
     * be omitted from the email.
     * @return a non null array of attachment filters.
     */
    public final Filter[] getAttachmentFilters() {
        return (Filter[]) readOnlyAttachmentFilters().clone();
    }

    /**
     * Sets the attachment filters.
     * @param filters a non <tt>null</tt> array of filters.  A <tt>null</tt>
     * index value is allowed.  A <tt>null</tt> value means that all
     * records are allowed for the attachment at that index.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws NullPointerException if <tt>filters</tt> is <tt>null</tt>
     * @throws IndexOutOfBoundsException if the number of attachment
     * name formatters do not match the number of attachment formatters.
     * @throws IllegalStateException if called from inside a push.
     */
    public final void setAttachmentFilters(Filter[] filters) {
        checkAccess();
        filters = (Filter[]) filters.clone();
        synchronized (this) {
            if (this.attachmentFormatters.length != filters.length) {
                throw attachmentMismatch(this.attachmentFormatters.length, filters.length);
            }

            if (isWriting) {
                throw new IllegalStateException();
            }
            this.attachmentFilters = filters;
        }
    }

    /**
     * Gets the attachment formatters.  This <tt>Handler</tt> is using
     * attachments only if the returned array length is non zero.
     * @return a non <tt>null</tt> array of formatters.
     */
    public final Formatter[] getAttachmentFormatters() {
        Formatter[] formatters;
        synchronized (this) {
            formatters = this.attachmentFormatters;
        }
        return (Formatter[]) formatters.clone();
    }

    /**
     * Sets the attachment <tt>Formatter</tt> object for this handler.
     * The number of formatters determines the number of attachments per
     * email.  This method should be the first attachment method called.
     * To remove all attachments, call this method with empty array.
     * @param formatters a non null array of formatters.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws NullPointerException if the given array or any array index is
     * <tt>null</tt>.
     * @throws IllegalStateException if called from inside a push.
     */
    public final void setAttachmentFormatters(Formatter[] formatters) {
        checkAccess();
        formatters = (Formatter[]) formatters.clone();
        for (int i = 0; i < formatters.length; ++i) {
            if (formatters[i] == null) {
                throw new NullPointerException(atIndexMsg(i));
            }
        }

        synchronized (this) {
            if (isWriting) {
                throw new IllegalStateException();
            }

            this.attachmentFormatters = formatters;
            this.fixUpAttachmentFilters();
            this.fixUpAttachmentNames();
        }
    }

    /**
     * Gets the attachment name formatters.
     * If the attachment names were set using explicit names then
     * the names can be returned by calling <tt>toString</tt> on each
     * attachment name formatter.
     * @return non <tt>null</tt> array of attachment name formatters.
     */
    public final Formatter[] getAttachmentNames() {
        final Formatter[] formatters;
        synchronized (this) {
            formatters = this.attachmentNames;
        }
        return (Formatter[]) formatters.clone();
    }

    /**
     * Sets the attachment file name for each attachment.  The caller must
     * ensure that the attachment file names do not contain any line breaks.
     * This method will create a set of custom formatters.
     * @param names an array of names.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws IndexOutOfBoundsException if the number of attachment
     * names do not match the number of attachment formatters.
     * @throws IllegalArgumentException  if any name is empty.
     * @throws NullPointerException if any given array or name is <tt>null</tt>.
     * @throws IllegalStateException if called from inside a push.
     * @see Character#isISOControl(char)
     */
    public final void setAttachmentNames(String[] names) {
        checkAccess();

        Formatter[] formatters = new Formatter[names.length];
        for (int i = 0; i < names.length; ++i) {
            final String name = names[i];
            if (name != null) {
                if (name.length() > 0) {
                    formatters[i] = new TailNameFormatter(name);
                } else {
                    throw new IllegalArgumentException(atIndexMsg(i));
                }
            } else {
                throw new NullPointerException(atIndexMsg(i));
            }
        }

        synchronized (this) {
            if (this.attachmentFormatters.length != names.length) {
                throw attachmentMismatch(this.attachmentFormatters.length, names.length);
            }

            if (isWriting) {
                throw new IllegalStateException();
            }
            this.attachmentNames = formatters;
        }
    }

    /**
     * Sets the attachment file name formatters.  The format method of each
     * attachment formatter will see only the <tt>LogRecord</tt> objects that
     * passed its attachment filter during formatting. The format method will 
     * typically return an empty string. Instead of being used to format
     * records, it is used to gather information about the contents of an
     * attachment.  The <tt>getTail</tt> method should be used to construct the
     * attachment file name and reset any formatter collected state.  The
     * formatter must ensure that the attachment file name does not contain any
     * line breaks.  The <tt>toString</tt> method of the given formatter should
     * be overridden to provide a useful attachment file name, if possible.
     * @param formatters and array of attachment name formatters.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws IndexOutOfBoundsException if the number of attachment
     * name formatters do not match the number of attachment formatters.
     * @throws NullPointerException if any given array or name is <tt>null</tt>.
     * @throws IllegalStateException if called from inside a push.
     * @see Character#isISOControl(char)
     */
    public final void setAttachmentNames(Formatter[] formatters) {
        checkAccess();

        formatters = (Formatter[]) formatters.clone();
        for (int i = 0; i < formatters.length; ++i) {
            if (formatters[i] == null) {
                throw new NullPointerException(atIndexMsg(i));
            }
        }

        synchronized (this) {
            if (this.attachmentFormatters.length != formatters.length) {
                throw attachmentMismatch(this.attachmentFormatters.length, formatters.length);
            }

            if (isWriting) {
                throw new IllegalStateException();
            }

            this.attachmentNames = formatters;
        }
    }

    /**
     * Gets the formatter used to create the subject line.
     * If the subject was created using a literal string then
     * the <tt>toString</tt> method can be used to get the subject line.
     * @return the formatter.
     */
    public final synchronized Formatter getSubject() {
        return this.subjectFormatter;
    }

    /**
     * Sets a literal string for the email subject.  The caller must ensure that
     * the subject line does not contain any line breaks.
     * @param subject a non <tt>null</tt> string.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws NullPointerException if <tt>subject</tt> is <tt>null</tt>.
     * @throws IllegalStateException if called from inside a push.
     * @see Character#isISOControl(char)
     */
    public final void setSubject(final String subject) {
        if (subject != null) {
            this.setSubject(new TailNameFormatter(subject));
        } else {
            checkAccess();
            throw new NullPointerException();
        }
    }

    /**
     * Sets the subject formatter for email.  The format method of the subject
     * formatter will see all <tt>LogRecord</tt> objects that were published to
     * this <tt>Handler</tt> during formatting and will typically return an
     * empty string.  This formatter is used to gather information to create a
     * summary about what information is contained in the email.  The
     * <tt>getTail</tt> method should be used to construct the subject and reset
     * any formatter collected state.  The formatter must ensure that the
     * subject line does not contain any line breaks.  The <tt>toString</tt>
     * method of the given formatter should be overridden to provide a useful
     * subject, if possible.
     * @param format the subject formatter.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     * @throws NullPointerException if <tt>format</tt> is <tt>null</tt>.
     * @throws IllegalStateException if called from inside a push.
     * @see Character#isISOControl(char)
     */
    public final void setSubject(final Formatter format) {
        checkAccess();
        if (format == null) {
            throw new NullPointerException();
        }

        synchronized (this) {
            if (isWriting) {
                throw new IllegalStateException();
            }
            this.subjectFormatter = format;
        }
    }

    /**
     * Protected convenience method to report an error to this Handler's
     * ErrorManager.  This method will prefix all non null error messages with
     * <tt>Level.SEVERE.getName()</tt>.  This allows the receiving error
     * manager to determine if the <tt>msg</tt> parameter is a simple error
     * message or a raw email message.
     * @param msg    a descriptive string (may be null)
     * @param ex     an exception (may be null)
     * @param code   an error code defined in ErrorManager
     */
    protected void reportError(String msg, Exception ex, int code) {
        if (msg != null) {
            super.reportError(Level.SEVERE.getName() + ": " + msg, ex, code);
        } else {
            super.reportError(null, ex, code);
        }
    }

    /**
     * Calls log manager checkAccess if this is sealed.
     */
    final void checkAccess() {
        if (sealed) {
            LogManagerProperties.manager.checkAccess();
        }
    }

    /**
     * Determines the mimeType of a formatter from the getHead call.
     * This could be made protected, or a new class could be created to do
     * this type of conversion.  Currently, this is only used for the body
     * since the attachments are computed by filename.
     * Package-private for unit testing.
     * @param head any head string.
     * @return return the mime type or null for text/plain.
     */
    final String contentTypeOf(String head) {
        if (head != null && head.length() > 0) {
            final int MAX_CHARS = 25;
            if (head.length() > MAX_CHARS) {
                head = head.substring(0, MAX_CHARS);
            }
            try {
                final ByteArrayInputStream in;
                final String encoding = getEncoding();
                if (encoding == null) {
                    in = new ByteArrayInputStream(head.getBytes());
                } else {
                    in = new ByteArrayInputStream(head.getBytes(encoding));
                }
                assert in.markSupported() : in.getClass().getName();
                return URLConnection.guessContentTypeFromStream(in);
            } catch (final IOException IOE) {
                reportError(IOE.getMessage(), IOE, ErrorManager.FORMAT_FAILURE);
            }
        }
        return null; //text/plain
    }

    /**
     * Converts a mime message to a raw string or formats the reason
     * why message can't be changed to raw string and reports it.
     * @param msg the mime message.
     * @param ex the original exception.
     * @param code the ErrorManager code.
     * @since JavaMail 1.4.5
     */
    private void reportError(Message msg, Exception ex, int code) {
        try { //use super call so we do not prefix raw email.
            super.reportError(toRawString(msg), ex, code);
        } catch (final MessagingException rawMe) {
            reportError(toMsgString(rawMe), ex, code);
        } catch (final IOException rawIo) {
            reportError(toMsgString(rawIo), ex, code);
        }
    }

    /**
     * Determines the mimeType from the given file name.
     * Used to override the body content type and used for all attachments.
     * @param name the file name or class name.
     * @return the mime type or null for text/plain.
     */
    private String getContentType(final String name) {
        assert Thread.holdsLock(this);
        final String type = contentTypes.getContentType(name);
        if ("application/octet-stream".equalsIgnoreCase(type)) {
            return null; //formatters return strings, default to text/plain.
        }
        return type;
    }

    /**
     * Set the content for a part.
     * @param part the part to assign.
     * @param buf the formatted data.
     * @param type the mime type.
     * @throws MessagingException if there is a problem.
     */
    private void setContent(MimeBodyPart part, CharSequence buf, String type) throws MessagingException {
        final String encoding = getEncoding();
        if (type != null && !"text/plain".equals(type)) {
            if (encoding == null) {
                type = contentWithDefault(type);
            } else {
                type = contentWithEncoding(type, encoding);
            }

            try {
                DataSource source = new ByteArrayDataSource(buf.toString(), type);
                part.setDataHandler(new DataHandler(source));
            } catch (final IOException IOE) {
                reportError(IOE.getMessage(), IOE, ErrorManager.FORMAT_FAILURE);
                part.setText(buf.toString(), encoding);
            }
        } else {
            part.setText(buf.toString(), encoding);
        }
    }

    /**
     * Only call if encoding is not null.
     * Replaces the charset parameter with the current encoding.
     * @param type the content type.
     * @param encoding the encoding.
     * @return the type with a specified encoding.
     */
    private String contentWithEncoding(String type, String encoding) {
        try {
            final ContentType ct = new ContentType(type);
            ct.setParameter("charset", encoding);
            encoding = ct.toString();
            if (encoding != null) {
                type = encoding;
            }
        } catch (final MessagingException ME) {
            reportError(type, ME, ErrorManager.FORMAT_FAILURE);
        }
        return type;
    }

    /**
     * Removes the encoding parameter from the content type.
     * @param type the content type.
     * @return the content type without encoding.
     */
    private String contentWithDefault(String type) {
        try {
            final ContentType ct = new ContentType(type);
            if (ct.getParameter("charset") != null) {
                final ParameterList list = ct.getParameterList();
                list.remove("charset");
                ct.setParameterList(list);
                final String newType = ct.toString();
                if (newType != null) {
                    type = newType;
                }
            }
        } catch (final MessagingException ME) {
            reportError(type, ME, ErrorManager.FORMAT_FAILURE);
        }
        return type;
    }

    /**
     * Sets the capacity for this handler.  This method is kept private
     * because we would have to define a public policy for when the size is
     * greater than the capacity.
     * I.E. do nothing, flush now, truncate now, push now and resize.
     * @param newCapacity the max number of records.
     * @throws IllegalStateException if called from inside a push.
     */
    private synchronized void setCapacity0(final int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }

        if (isWriting) {
            throw new IllegalStateException();
        }

        if (this.capacity < 0) { //if closed, remain closed.
            this.capacity = -newCapacity;
        } else {
            this.capacity = newCapacity;
        }
    }

    /**
     * Gets the attachment filters under a lock.  The attachment filters
     * are treated as copy-on-write, so the returned array must never be
     * modified or published outside this class.
     * @return a read only array of filters.
     */
    private synchronized Filter[] readOnlyAttachmentFilters() {
        return this.attachmentFilters;
    }

    /**
     * Check that the log manager is creating a valid set of formatters.
     * This is only called during init.
     */
    private void fixUpAttachmentFormatters() {
        assert Thread.holdsLock(this);
        final int attachments = attachmentFormatters.length;
        for (int i = 0; i < attachments; ++i) {
            if (attachmentFormatters[i] == null) {
                final NullPointerException NPE = new NullPointerException(atIndexMsg(i));
                attachmentFormatters[i] = new SimpleFormatter();
                reportError("attachment formatter.", NPE, ErrorManager.OPEN_FAILURE);
            } else if (attachmentFormatters[i] instanceof TailNameFormatter) {
                final ClassNotFoundException CNFE =
                        new ClassNotFoundException(attachmentFormatters[i].toString());
                attachmentFormatters[i] = new SimpleFormatter();
                reportError("attachment formatter.", CNFE, ErrorManager.OPEN_FAILURE);
            }
        }
    }

    /**
     * Expand or shrink the attachment name formatters.
     * @return true if fixed.
     */
    private boolean fixUpAttachmentNames() {
        assert Thread.holdsLock(this);
        boolean fixed = false;
        final int expect = this.attachmentFormatters.length;
        final int current = this.attachmentNames.length;
        if (current != expect) {
            this.attachmentNames = (Formatter[]) copyOf(attachmentNames, expect);
            fixed = current != 0;
        }

        for (int i = 0; i < expect; ++i) {
            if (this.attachmentNames[i] == null) {
                this.attachmentNames[i] = new TailNameFormatter(
                        toString(this.attachmentFormatters[i]));
            }
        }
        return fixed;
    }

    /**
     * Expand or shrink the attachment filters.
     * @return true if fixed.
     */
    private boolean fixUpAttachmentFilters() {
        assert Thread.holdsLock(this);

        final int expect = this.attachmentFormatters.length;
        final int current = this.attachmentFilters.length;
        if (current != expect) {
            this.attachmentFilters = (Filter[]) copyOf(attachmentFilters, expect);
            return current != 0;
        }
        return false;
    }

    /**
     * Copies the given array. Can be removed when Java Mail requires Java 1.6.
     * @param a the original array.
     * @param size the new size.
     * @return new copy
     */
    private static Object[] copyOf(Object[] a, int size) {
        Object[] copy = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
        System.arraycopy(a, 0, copy, 0, Math.min(a.length, size));
        return copy;
    }

    /**
     * Sets the size to zero and clears the current buffer.
     */
    private void reset() {
        assert Thread.holdsLock(this);
        if (size < data.length) {
            Arrays.fill(data, 0, size, null);
        } else {
            Arrays.fill(data, null);
        }
        this.size = 0;
    }

    /**
     * Expands the internal buffer up to the capacity.
     */
    private void grow() {
        assert Thread.holdsLock(this);
        final int len = data.length;
        int newCapacity = len + (len >> 1) + 1;
        if (newCapacity > capacity || newCapacity < len) {
            newCapacity = capacity;
        }
        assert len != capacity : len;
        this.data = (LogRecord[]) copyOf(data, newCapacity);
    }

    /**
     * Configures the handler properties from the log manager.
     * @throws SecurityException  if a security manager exists and the
     * caller does not have <tt>LoggingPermission("control")</tt>.
     */
    private synchronized void init() {
        final LogManager manager = LogManagerProperties.manager;
        final String p = getClass().getName();
        this.mailProps = new Properties();
        this.contentTypes = FileTypeMap.getDefaultFileTypeMap();

        //Assign any custom error manager first so it can detect all failures.
        ErrorManager em = (ErrorManager) initObject(p.concat(".errorManager"), ErrorManager.class);
        if (em != null) {
            setErrorManager(em);
        }

        initLevel(manager, p);
        initFilter(p);
        initCapacity(manager, p);

        this.auth = (Authenticator) initObject(p.concat(".authenticator"), Authenticator.class);
        final Session settings = this.fixUpSession();

        initEncoding(manager, p);
        initFormatter(p);
        initComparator(p);
        initPushLevel(manager, p);

        this.pushFilter = (Filter) initObject(p.concat(".pushFilter"), Filter.class);

        initSubject(p);

        this.attachmentFormatters = (Formatter[]) initArray(p.concat(".attachment.formatters"), Formatter.class);
        this.attachmentFilters = (Filter[]) initArray(p.concat(".attachment.filters"), Filter.class);
        this.attachmentNames = (Formatter[]) initArray(p.concat(".attachment.names"), Formatter.class);

        fixUpAttachmentFormatters();

        if (fixUpAttachmentFilters()) {
            reportError("attachment filters.",
                    attachmentMismatch("length mismatch"), ErrorManager.OPEN_FAILURE);
        }

        if (fixUpAttachmentNames()) {
            reportError("attachment names.",
                    attachmentMismatch("length mismatch"), ErrorManager.OPEN_FAILURE);
        }
        verifySettings(settings);
    }

    private /*<T> T*/ Object objectFromNew(final String name,
            final Class/*<T>*/ type) throws NoSuchMethodException {
        Object obj = null;
        try {
            try {
                try {
                    Class clazz = LogManagerProperties.findClass(name);
                    if (type.isAssignableFrom(clazz)) {
                        return clazz.getConstructor((Class[]) null).
                                newInstance((Object[]) null);
                    } else {
                        throw new ClassCastException(clazz.getName()
                                + " cannot be cast to " + type.getName());
                    }
                } catch (final NoClassDefFoundError NCDFE) {
                    throw (ClassNotFoundException) new ClassNotFoundException(
                            NCDFE.getMessage()).initCause(NCDFE);
                }
            } catch (final ClassNotFoundException CNFE) {
                if (type == Formatter.class) {
                    return /*type.cast(*/ new TailNameFormatter(name);
                } else {
                    throw CNFE;
                }
            } catch (final ClassCastException CCE) {
                if (type == Formatter.class) {
                    return /*type.cast(*/ new TailNameFormatter(name);
                } else {
                    throw CCE;
                }
            }
        } catch (final NoSuchMethodException NSME) {
            throw NSME; //avoid catch all.
        } catch (final Exception E) {
            reportError(E.getMessage(), E, ErrorManager.OPEN_FAILURE);
        }
        return obj;
    }

    private /*<T> T*/ Object initObject(final String key, Class/*<T>*/ type) {
        String name = LogManagerProperties.manager.getProperty(key);
        if (name != null && name.length() > 0 && !"null".equalsIgnoreCase(name)) {
            try {
                return objectFromNew(name, type);
            } catch (NoSuchMethodException E) {
                reportError(E.getMessage(), E, ErrorManager.OPEN_FAILURE);
            }
        }
        return null;
    }

    private /*<T> T[]*/ Object[] initArray(final String key, Class/*<T>*/ type) {
        final String list = LogManagerProperties.manager.getProperty(key);
        if (list != null && list.length() > 0) {
            final String[] names = list.split(",");
            Object[] a = (Object[]) Array.newInstance(type, names.length);
            for (int i = 0; i < a.length; ++i) {
                names[i] = names[i].trim();
                if (!"null".equalsIgnoreCase(names[i])) {
                    try {
                        a[i] = objectFromNew(names[i], type);
                    } catch (NoSuchMethodException E) {
                        reportError(E.getMessage(), E, ErrorManager.OPEN_FAILURE);
                    }
                }
            }
            return a;
        } else {
            return /*(T[])*/ (Object[]) Array.newInstance(type, 0);
        }
    }

    private void initLevel(LogManager manager, String p) {
        assert Thread.holdsLock(this);
        try {
            final String val = manager.getProperty(p.concat(".level"));
            if (val != null) {
                super.setLevel(Level.parse(val));
            } else {
                super.setLevel(Level.WARNING);
            }
        } catch (final SecurityException SE) {
            throw SE;
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.OPEN_FAILURE);
            try {
                super.setLevel(Level.WARNING);
            } catch (RuntimeException fail) {
                reportError(fail.getMessage(), fail, ErrorManager.OPEN_FAILURE);
            }
        }
    }

    private void initFilter(String p) {
        assert Thread.holdsLock(this);
        try {
            super.setFilter((Filter) initObject(p.concat(".filter"), Filter.class));
        } catch (final SecurityException SE) {
            throw SE;
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.OPEN_FAILURE);
        }
    }

    private void initCapacity(LogManager manager, String p) {
        assert Thread.holdsLock(this);
        final int DEFAULT_CAPACITY = 1000;
        try {
            final String value = manager.getProperty(p.concat(".capacity"));
            if (value != null) {
                this.setCapacity0(Integer.parseInt(value));
            } else {
                this.setCapacity0(DEFAULT_CAPACITY);
            }
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.OPEN_FAILURE);
        }

        if (capacity <= 0) {
            capacity = DEFAULT_CAPACITY;
        }

        this.data = new LogRecord[1];
    }

    private void initEncoding(LogManager manager, String p) {
        assert Thread.holdsLock(this);
        try {
            super.setEncoding(manager.getProperty(p.concat(".encoding")));
        } catch (final SecurityException SE) {
            throw SE;
        } catch (final UnsupportedEncodingException UEE) {
            reportError(UEE.getMessage(), UEE, ErrorManager.OPEN_FAILURE);
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.OPEN_FAILURE);
        }
    }

    private void initFormatter(String p) {
        assert Thread.holdsLock(this);
        try {
            final Formatter formatter = (Formatter) initObject(p.concat(".formatter"), Formatter.class);
            if (formatter != null && formatter instanceof TailNameFormatter == false) {
                super.setFormatter(formatter);
            } else {
                super.setFormatter(new SimpleFormatter());
            }
        } catch (final SecurityException SE) {
            throw SE;
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.OPEN_FAILURE);
            try {
                super.setFormatter(new SimpleFormatter());
            } catch (RuntimeException fail) {
                reportError(fail.getMessage(), fail, ErrorManager.OPEN_FAILURE);
            }
        }
    }

    /*@SuppressWarnings("unchecked")*/
    private void initComparator(String p) {
        assert Thread.holdsLock(this);
        try {
            this.comparator = (Comparator) this.initObject(p.concat(".comparator"), Comparator.class);
        } catch (final Exception RE) {
            reportError(RE.getMessage(), RE, ErrorManager.OPEN_FAILURE);
        }

        /*try {
            final String reverse = manager.getProperty(p.concat(".comparator.reverse"));
            if (reverse != null) {
                if (Boolean.parseBoolean(reverse)) {
                    if (this.comparator != null) {
                        this.comparator = Collections.reverseOrder(this.comparator);
                    }
                    else {
                        throw new IllegalArgumentException("No comparator to reverse.");
                    }
                }
            }
        }
        catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.OPEN_FAILURE);
        }*/
    }

    private void initPushLevel(LogManager manager, String p) {
        assert Thread.holdsLock(this);
        try {
            final String val = manager.getProperty(p.concat(".pushLevel"));
            if (val != null) {
                this.pushLevel = Level.parse(val);
            }
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.OPEN_FAILURE);
        }

        if (this.pushLevel == null) {
            this.pushLevel = Level.OFF;
        }
    }

    private void initSubject(String p) {
        assert Thread.holdsLock(this);
        this.subjectFormatter = (Formatter) initObject(p.concat(".subject"), Formatter.class);
        if (this.subjectFormatter == null) {
            this.subjectFormatter = new TailNameFormatter("");
        }
    }

    /**
     * Check if any attachment would actually format the given
     * <tt>LogRecord</tt>.  This method does not check if the handler
     * is level is set to OFF or if the handler is closed.
     * @param record  a <tt>LogRecord</tt>
     * @return true if the <tt>LogRecord</tt> would be formatted.
     */
    private boolean isAttachmentLoggable(final LogRecord record) {
        final Filter[] filters = readOnlyAttachmentFilters();
        for (int i = 0; i < filters.length; ++i) {
            final Filter f = filters[i];
            if (f == null || f.isLoggable(record)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this <tt>Handler</tt> would push after storing the
     * <tt>LogRecord</tt> into its internal buffer.
     * @param record  a <tt>LogRecord</tt>
     * @return true if the <tt>LogRecord</tt> triggers an email push.
     */
    private boolean isPushable(final LogRecord record) {
        assert Thread.holdsLock(this);
        final int value = getPushLevel().intValue();
        if (value == offValue || record.getLevel().intValue() < value) {
            return false;
        }

        final Filter filter = getPushFilter();
        return filter == null || filter.isLoggable(record);
    }

    /**
     * Used to perform push or flush.
     * @param priority true for high priority otherwise false for normal.
     * @param code the error manager code.
     */
    private void push(final boolean priority, final int code) {
        MessageContext ctx = writeLogRecords(code);
        if (ctx != null) {
            send(ctx, priority, code);
        }
    }

    /**
     * Used to send the generated email or write its contents to the
     * error manager for this handler.  This method does not hold any
     * locks so new records can be added to this handler during a send or
     * failure.
     * @param ctx the message context or null.
     * @param priority true for high priority or false for normal.
     * @param code the ErrorManager code.
     */
    private void send(MessageContext ctx, boolean priority, int code) {
        final Message msg = ctx.getMessage();
        try {
            envelopeFor(ctx, priority);
            Transport.send(msg);
        } catch (final Exception E) {
            reportError(msg, E, code);
        }
    }

    /**
     * Performs a sort on the records if needed.
     * Any exception thrown during a sort is considered a formatting error.
     */
    private void sort() {
        assert Thread.holdsLock(this);
        if (comparator != null) {
            try {
                Arrays.sort(data, 0, size, comparator);
            } catch (final RuntimeException RE) {
                reportError(RE.getMessage(), RE, ErrorManager.FORMAT_FAILURE);
            }
        }
    }

    /**
     * Formats all records in the buffer and places the output in a Message.
     * This method holds a lock on this handler.
     * Normally code would not aggressively null locals but with this running
     * on older JVMs it seems better to trade time for space.
     * @param code the error manager code.
     * @return null if there are no records or is currently in a push.
     * Otherwise a new message context is created with a formatted message and
     * attached session.
     */
    private synchronized MessageContext writeLogRecords(final int code) {
        if (size == 0 || isWriting) {
            return null;
        }

        MimeMessage msg = null;
        isWriting = true;
        try {
            msg = new MimeMessage(session);

            sort();

            /**
             * Parts are lazily created when an attachment performs a getHead 
             * call.  Therefore, a null part at an index means that the head is
             * required.
             */
            MimeBodyPart[] parts = new MimeBodyPart[attachmentFormatters.length];

            /**
             * The buffers are lazily created when the part requires a getHead.
             */
            StringBuffer[] buffers = new StringBuffer[parts.length];

            String contentType = null;
            StringBuffer buf = null;

            appendSubject(msg, head(subjectFormatter));

            final Formatter bodyFormat = getFormatter();
            final Filter bodyFilter = getFilter();

            for (int ix = 0; ix < size; ++ix) {
                boolean filtered = true;
                final LogRecord r = data[ix];
                data[ix] = null; //clear while formatting.

                appendSubject(msg, format(subjectFormatter, r));

                if (bodyFilter == null || bodyFilter.isLoggable(r)) {
                    if (buf == null) {
                        buf = new StringBuffer();
                        final String head = head(bodyFormat);
                        buf.append(head);
                        contentType = contentTypeOf(head);
                    }
                    filtered = false;
                    buf.append(format(bodyFormat, r));
                }

                for (int i = 0; i < parts.length; ++i) {
                    final Filter af = attachmentFilters[i];
                    if (af == null || af.isLoggable(r)) {
                        if (parts[i] == null) {
                            parts[i] = createBodyPart(i);
                            buffers[i] = new StringBuffer();
                            buffers[i].append(head(attachmentFormatters[i]));
                            appendFileName(parts[i], head(attachmentNames[i]));
                        }
                        filtered = false;
                        appendFileName(parts[i], format(attachmentNames[i], r));
                        buffers[i].append(format(attachmentFormatters[i], r));
                    }
                }

                if (filtered) { //belongs to no mime part.
                   reportFilterError(r);
                }
            }
            this.size = 0;

            for (int i = parts.length - 1; i >= 0; --i) {
                if (parts[i] != null) {
                    appendFileName(parts[i], tail(attachmentNames[i], "err"));
                    buffers[i].append(tail(attachmentFormatters[i], ""));

                    if (buffers[i].length() > 0) {
                        String name = parts[i].getFileName();
                        if (name == null || name.length() == 0) {
                            name = toString(attachmentFormatters[i]);
                            parts[i].setFileName(name);
                        }
                        setContent(parts[i], buffers[i], getContentType(name));
                    } else {
                        parts[i] = null;
                    }
                    buffers[i] = null;
                }
            }

            if (buf != null) {
                buf.append(tail(bodyFormat, ""));
            } else {
                buf = new StringBuffer(0);
            }

            appendSubject(msg, tail(subjectFormatter, ""));

            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart body = createBodyPart();
            String altType = getContentType(bodyFormat.getClass().getName());
            setContent(body, buf, altType == null ? contentType : altType);
            buf = null;
            multipart.addBodyPart(body);

            for (int i = 0; i < parts.length; ++i) {
                if (parts[i] != null) {
                    multipart.addBodyPart(parts[i]);
                }
            }
            parts = null;
            msg.setContent(multipart);
            return new MessageContext(msg);
        } catch (final RuntimeException re) {
            this.reportError(re.getMessage(), re, code);
        } catch (final Exception e) {
            this.reportError(e.getMessage(), e, code);
        } finally {
            isWriting = false;
            if (size > 0) {
                reset();
            }
        }
        return null;
    }

    /**
     * Checks all of the settings if the caller requests a verify and a verify
     * was not performed yet and no verify is in progress.  A verify is
     * performed on create because this handler may be at the end of a handler
     * chain and therefore may not see any log records until LogManager.reset()
     * is called and at that time all of the settings have been cleared.
     * @param session the current session.
     * @since JavaMail 1.4.4
     */
    private void verifySettings(final Session session) {
        String key = "verify";
        String value;

        final Properties props = session.getProperties();
        synchronized (props) {
            value = (String) props.get(key); //search only props.
            if (value == null) {
                value = props.getProperty(key); //search parent props.
                if (props.get(key) == null) {
                    props.put(key, ""); //disable future checks.
                }
            } else {
                return; //verify complete or in progress.
            }
        }

        //perform the verify if needed.
        if (value != null && value.length() > 0 && !value.equals("null")) {
            verifySettings0(session, value);
        }
    }

    /**
     * Checks all of the settings using the given setting.
     * This triggers the LogManagerProperties to copy all of the mail
     * settings without explictly knowing them.  Once all of the properties
     * are copied this handler can handle LogManager.reset clearing all of the
     * properties.  It is expected that this method is, at most, only called
     * once per session.
     * @param session the current session.
     * @since JavaMail 1.4.4
     */
    private void verifySettings0(Session session, String verify) {
        assert verify != null : (String) null;
        if (!"local".equals(verify) && !"remote".equals(verify)) {
            this.reportError("Verify must be 'local' or 'remote'.",
                    new IllegalArgumentException(verify),
                    ErrorManager.OPEN_FAILURE);
            return;
        }

        //Perform all of the copy actions first.
        final MimeMessage abort = new MimeMessage(session);
        synchronized (this) { //create the subject.
            appendSubject(abort, head(this.subjectFormatter));
            appendSubject(abort, tail(this.subjectFormatter, ""));
        }
        envelopeFor(new MessageContext(abort), true); //calls saveChanges.

        String msg;
        if (InternetAddress.getLocalAddress(session) == null) {
            msg = "Local address is null.";
        } else {
            msg = "";
        }

        try {
            //Ensure transport provider is installed.
            Address[] all = abort.getAllRecipients();
            Transport t;
            try {
                if (all != null && all.length > 0) {
                    t = session.getTransport(all[0]);
                    session.getProperty("mail.transport.protocol"); //force copy
                } else {
                    MessagingException me =
                        new MessagingException("No recipient addresses.");
                    reportError(msg, me, ErrorManager.OPEN_FAILURE);
                    throw me;
                }
            } catch (final MessagingException protocol) {
                try {
                    t = session.getTransport();
                } catch (final MessagingException fail) {
                    fail.setNextException(protocol);
                    throw fail;
                }
            }

            if ("remote".equals(verify)) {
                t.connect();
                try {
                    /**
                     * A message without content will fail at message writeTo.
                     * This allows the handler to capture all mail properties.
                     */
                    t.sendMessage(abort, all);
                    final MessagingException write = new MessagingException(
                            "An empty message was sent.");
                    fixUpContent(abort, verify, write);
                    reportError(abort, write, ErrorManager.WRITE_FAILURE);
                } catch (MessagingException expectNoContent) {
                } finally {
                    t.close();
                }
            } else { //force a property copy.
                final String protocol = t.getURLName().getProtocol();
                session.getProperty("mail.host");
                session.getProperty("mail.user");
                session.getProperty("mail." + protocol + ".host");
                session.getProperty("mail." + protocol + ".port");
                session.getProperty("mail." + protocol + ".user");
            }

            Address[] from = abort.getFrom();
            Address sender = abort.getSender();
            if (abort.getHeader("From", ",") != null) {
                assert from != null;
                for (int i = 0; i < from.length; ++i) {
                    if (from[i].equals(sender)) {
                        reportError(msg, new MessagingException(
                            "Sender address equals from address."),
                            ErrorManager.OPEN_FAILURE);
                        break;
                    }
                }
            }

            //The null and zero length case is handled getting the transport.
            if (all != null) {
                for (int i = 0; i < all.length; ++i) {
                    Address a = all[i];
                    if (a instanceof InternetAddress) {
                        ((InternetAddress) a).validate();
                    }
                }
            }

            //Check the host name after the address checks.
            if ("local".equals(verify)) {
                try {
                    if (InetAddress.getLocalHost().getHostName().length() == 0) {
                        throw new UnknownHostException();
                    }
                } catch (final IOException IOE) {
                    reportError(msg, IOE, ErrorManager.OPEN_FAILURE);
                }
            }
        } catch (final MessagingException ME) {
            fixUpContent(abort, verify, ME);
            reportError(abort, ME, ErrorManager.OPEN_FAILURE);
        } catch (final RuntimeException RE) {
            fixUpContent(abort, verify, RE);
            reportError(abort, RE, ErrorManager.OPEN_FAILURE);
        }
    }

    /**
     * Creates and sets the message content from the given Throwable.
     * When verify fails, this method fixes the 'abort' message so that any
     * created envelope data can be used in the error manager.
     * @param msg the message with or without content.
     * @param verify the verify enum.
     * @param t the throwable or null.
     * @since JavaMail 1.4.5
     */
    private void fixUpContent(MimeMessage msg, String verify, Throwable t) {
        try { //add content so toRawString doesn't fail.
            final MimeMultipart multipart = new MimeMultipart();
            final MimeBodyPart body = new MimeBodyPart();
            body.setDisposition(Part.INLINE);
            body.setDescription("Formatted using " 
                    + (t == null ? Throwable.class.getName()
                    : t.getClass().getName()) + " and filtered with "
                    + verify + '.');
            setContent(body, toMsgString(t), "text/plain");
            multipart.addBodyPart(body);
            msg.setContent(multipart);
            msg.saveChanges();
        } catch (final MessagingException ME) {
            reportError("Unable to create body.", ME, ErrorManager.OPEN_FAILURE);
        } catch (final RuntimeException RE) {
            reportError("Unable to create body.", RE, ErrorManager.OPEN_FAILURE);
        }
    }

    /**
     * Used to update the cached session object based on changes in
     * mail properties or authenticator.
     * @return the current session.
     */
    private Session fixUpSession() {
        assert Thread.holdsLock(this);
        String p = getClass().getName();
        LogManagerProperties proxy = new LogManagerProperties(mailProps, p);
        session = Session.getInstance(proxy, auth);
        return session;
    }

    /**
     * Creates all of the envelope information for a message.
     * This method is safe to call outside of a lock because the message
     * context provides the safe snapshot of the mail properties.
     * @param ctx the MessageContext to write the envelope information.
     * @param priority true for high priority.
     */
    private void envelopeFor(MessageContext ctx, boolean priority) {
        final Message msg = ctx.getMessage();
        final Properties proxyProps = ctx.getSession().getProperties();
        setFrom(msg, proxyProps);
        setRecipient(msg, proxyProps, "mail.to", Message.RecipientType.TO);
        setRecipient(msg, proxyProps, "mail.cc", Message.RecipientType.CC);
        setRecipient(msg, proxyProps, "mail.bcc", Message.RecipientType.BCC);
        setReplyTo(msg, proxyProps);
        setSender(msg, proxyProps);
        setMailer(msg);
        setPriority(msg, priority);
        try {
            msg.setSentDate(new java.util.Date());
            msg.saveChanges();
        } catch (final MessagingException ME) {
            reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
        }
    }

    private MimeBodyPart createBodyPart() throws MessagingException {
        final MimeBodyPart part = new MimeBodyPart();
        part.setDisposition(Part.INLINE);
        part.setDescription(descriptionFrom(getFormatter(), getFilter()));
        return part;
    }

    private MimeBodyPart createBodyPart(final int index) throws MessagingException {
        assert Thread.holdsLock(this);
        final MimeBodyPart part = new MimeBodyPart();
        part.setDisposition(Part.ATTACHMENT);
        part.setDescription(descriptionFrom(
                attachmentFormatters[index], attachmentFilters[index]));
        return part;
    }

    private String descriptionFrom(Formatter formatter, Filter filter) {
        return "Formatted using " + formatter.getClass().getName()
                + " and filtered with " + (filter == null ? "no filter"
                : filter.getClass().getName()) + '.';
    }

    /**
     * Ensure that a formatter creates a valid string.
     * @param f the formatter.
     * @return the to string value or the class name.
     */
    private String toString(final Formatter f) {
        final String name = f.toString();
        if (name != null && name.length() > 0) {
            return name;
        } else {
            return f.getClass().getName();
        }
    }

    /**
     * Constructs a file name from a formatter.  This method is called often
     * but, rarely does any work.
     * @param part to append to.
     * @param chunk non null string to append.
     */
    private void appendFileName(final Part part, final String chunk) {
        if (chunk != null) {
            if (chunk.length() > 0) {
                appendFileName0(part, chunk);
            }
        } else {
            reportNullError(ErrorManager.FORMAT_FAILURE);
        }
    }

    /**
     * It is assumed that file names are short (less than 32 chars) and that in
     * most cases getTail will be the only method that will produce a result.
     * @param part to append to.
     * @param chunk non null string to append.
     */
    private void appendFileName0(final Part part, final String chunk) {
        try {
            final String old = part.getFileName();
            part.setFileName(old != null ? old.concat(chunk) : chunk);
        } catch (final MessagingException ME) {
            reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
        }
    }

    /**
     * Constructs a subject line from a formatter.
     * @param msg to append to.
     * @param chunk non null string to append.
     */
    private void appendSubject(final Message msg, final String chunk) {
        if (chunk != null) {
            if (chunk.length() > 0) {
                appendSubject0(msg, chunk);
            }
        } else {
            reportNullError(ErrorManager.FORMAT_FAILURE);
        }
    }

    /**
     * It is assumed that subject lines are short (less than 32 chars) and that
     * in most cases getTail will be the only method that will produce a result.
     * @param msg to append to.
     * @param chunk non null string to append.
     */
    private void appendSubject0(final Message msg, final String chunk) {
        try {
            final String old = msg.getSubject();
            assert msg instanceof MimeMessage;
            ((MimeMessage) msg).setSubject(old != null
                    ? old.concat(chunk) : chunk, getEncoding());
        } catch (final MessagingException ME) {
            reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
        }
    }

    /**
     * Used when a log record was loggable prior to being inserted
     * into the buffer and at the time of formatting was no longer loggable.
     * Filters were changed after publish but prior to a push or a bug in the
     * body filter or one of the attachment filters.
     * @param record that was not formatted.
     * @since JavaMail 1.4.5
     */
    private void reportFilterError(final LogRecord record) {
        assert Thread.holdsLock(this);
        final SimpleFormatter f = new SimpleFormatter();
        final String msg = "Log record " + record.getSequenceNumber()
                + " was filtered from all message parts.  "
                + head(f) + format(f, record) + tail(f, "");
        final String txt = getFilter() + ", " + Arrays.asList(readOnlyAttachmentFilters());
        reportError(msg, new IllegalArgumentException(txt), ErrorManager.FORMAT_FAILURE);
    }

    private void reportNullError(final int code) {
        reportError("null", new NullPointerException(), code);
    }

    private String head(final Formatter f) {
        try {
            return f.getHead(this);
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.FORMAT_FAILURE);
            return "";
        }
    }

    private String format(final Formatter f, final LogRecord r) {
        try {
            return f.format(r);
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.FORMAT_FAILURE);
            return "";
        }
    }

    private String tail(final Formatter f, final String def) {
        try {
            return f.getTail(this);
        } catch (final RuntimeException RE) {
            reportError(RE.getMessage(), RE, ErrorManager.FORMAT_FAILURE);
            return def;
        }
    }

    private void setMailer(final Message msg) {
        try {
            final Class mail = MailHandler.class;
            final Class k = getClass();
            String value;
            if (k == mail) {
                value = mail.getName();
            } else {
                try {
                    value = MimeUtility.encodeText(k.getName());
                } catch (final UnsupportedEncodingException E) {
                    reportError(E.getMessage(), E, ErrorManager.FORMAT_FAILURE);
                    value = k.getName().replaceAll("[^\\x00-\\x7F]", "\uu001A");
                }
                value = MimeUtility.fold(10, mail.getName() + " using the "
                            + value + " extension.");
            }
            msg.setHeader("X-Mailer", value);
        } catch (final MessagingException ME) {
            reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
        }
    }

    private void setPriority(final Message msg, boolean priority) {
        if (priority) {
            try {
                msg.setHeader("Importance", "High");
                msg.setHeader("Priority", "urgent");
                msg.setHeader("X-Priority", "2"); //High
            } catch (final MessagingException ME) {
                reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
            }
        }
    }

    private void setFrom(Message msg, Properties props) {
        final String from = props.getProperty("mail.from");
        if (from != null && from.length() > 0) {
            try {
                final InternetAddress[] address = InternetAddress.parse(from, false);
                if (address == null || address.length == 0) {
                    setDefaultFrom(msg);
                } else {
                    if (address.length == 1) {
                        msg.setFrom(address[0]);
                    } else { //greater than 1 address.
                        msg.addFrom(address);
                    }
                }
            } catch (final MessagingException ME) {
                reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
                setDefaultFrom(msg);
            }
        } else {
            setDefaultFrom(msg);
        }
    }

    private void setDefaultFrom(Message msg) {
        try {
            msg.setFrom();
        } catch (final MessagingException ME) {
            reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
        }
    }

    private void setReplyTo(Message msg, Properties props) {
        final String reply = props.getProperty("mail.reply.to");
        if (reply != null && reply.length() > 0) {
            try {
                final InternetAddress[] address = InternetAddress.parse(reply, false);
                if (address != null && address.length > 0) {
                    msg.setReplyTo(address);
                }
            } catch (final MessagingException ME) {
                reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
            }
        }
    }

    private void setSender(Message msg, Properties props) {
        assert msg instanceof MimeMessage : msg;
        final String sender = props.getProperty("mail.sender");
        if (sender != null && sender.length() > 0) {
            try {
                final InternetAddress[] address =
                        InternetAddress.parse(sender, false);
                if (address != null && address.length > 0) {
                    ((MimeMessage) msg).setSender(address[0]);
                    if (address.length > 1) {
                        reportError("Ignoring other senders.",
                                tooManyAddresses(address, 1),
                                ErrorManager.FORMAT_FAILURE);
                    }
                }
            } catch (final MessagingException ME) {
                reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
            }
        }
    }

    private static AddressException tooManyAddresses(Address[] address, int offset) {
        String msg = Arrays.asList(address).subList(offset, address.length).toString();
        return new AddressException(msg);
    }

    private void setRecipient(Message msg, Properties props,
            String key, Message.RecipientType type) {
        final String value = props.getProperty(key);
        if (value != null && value.length() > 0) {
            try {
                final InternetAddress[] address = InternetAddress.parse(value, false);
                if (address != null && address.length > 0) {
                    msg.setRecipients(type, address);
                }
            } catch (final MessagingException ME) {
                reportError(ME.getMessage(), ME, ErrorManager.FORMAT_FAILURE);
            }
        }
    }

    /**
     * Converts an email message to a raw string.  This raw string
     * is passed to the error manager to allow custom error managers
     * to recreate the original MimeMessage object.
     * @param msg a Message object.
     * @return the raw string or null if msg was null.
     * @throws MessagingException if there was a problem with the message.
     * @throws IOException if there was a problem.
     */
    private String toRawString(final Message msg) throws MessagingException, IOException {
        if (msg != null) {
            final int nbytes = Math.max(msg.getSize() + 1024, 1024);
            final ByteArrayOutputStream out = new ByteArrayOutputStream(nbytes);
            msg.writeTo(out);
            return out.toString("US-ASCII"); //raw message is always ASCII.
        } else { //Must match this.reportError behavior, see push method.
            return null; //null is the safe choice.
        }
    }

    /**
     * Converts a throwable to a message string.  This method is called when
     * Message.writeTo throws an exception.
     * @param t any throwable or null.
     * @return the throwable with a stack trace or the literal null.
     */
    private String toMsgString(final Throwable t) {
        if (t == null) {
           return "null";
        }
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        final PrintStream ps = new PrintStream(out);
        ps.println(t.getMessage());
        t.printStackTrace(ps);
        ps.flush();
        ps.close(); //BUG ID 6995537
        return out.toString();
    }

    private static RuntimeException attachmentMismatch(String msg) {
        return new IndexOutOfBoundsException(msg);
    }

    private static RuntimeException attachmentMismatch(int expected, int found) {
        return attachmentMismatch("Attachments mismatched, expected "
                + expected + " but given " + found + '.');
    }

    private static String atIndexMsg(int i) {
        return "At index: " + i + '.';
    }

    /**
     * Used for naming attachment file names and the main subject line.
     */
    private static final class TailNameFormatter extends Formatter {

        private final String name;

        TailNameFormatter(final String name) {
            assert name != null;
            this.name = name;
        }

        public final String format(LogRecord record) {
            return "";
        }

        public final String getTail(Handler h) {
            return name;
        }

        /**
         * Equals method.
         * @param o the other object.
         * @return true if equal
         * @since JavaMail 1.4.4
         */
        public final boolean equals(Object o) {
            if (o instanceof TailNameFormatter) {
                return name.equals(((TailNameFormatter) o).name);
            }
            return false;
        }

        /**
         * Hash code method.
         * @return the hash code.
         * @since JavaMail 1.4.4
         */
        public final int hashCode() {
            return getClass().hashCode() + name.hashCode();
        }

        public final String toString() {
            return name;
        }
    }
}
