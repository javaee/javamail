/*
 * Copyright (c) 2009-2013 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2013 Jason Mehrens. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.sun.mail.util.logging.MailHandler;
import static java.lang.Character.isISOControl;
import static java.lang.Character.isValidCodePoint;
import java.text.MessageFormat;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

/**
 * Creates an attachment name based on the number of records and errors. The
 * pattern is a <tt>java.text.MesageFormat</tt> with two parameters. The first
 * parameter is the number of records formatted. The second is the number of
 * records with errors.
 *
 * @author Jason Mehrens
 */
public class SummaryNameFormatter extends Formatter {

    /**
     * The MessageFormat pattern.
     */
    private final String pattern;
    /**
     * The number of log records formatted.
     */
    private long count;
    /**
     * The number of log records that had a throwable object.
     */
    private long errors;

    /**
     * Creates a simple pattern.
     */
    public SummaryNameFormatter() {
        this("{0} records and {1} errors");
    }

    /**
     * Creates formatter using a message format style pattern.
     *
     * @param pattern the pattern.
     * @throws NullPointerException if pattern is null.
     * @throws IllegalArgumentException if pattern contains an ISO control
     * character or invalid code points.
     */
    public SummaryNameFormatter(final String pattern) {
        for (int i = 0; i < pattern.length(); ) {
            final int codePoint = pattern.codePointAt(i);
            if (!isValidCodePoint(codePoint) || isISOControl(codePoint)) {
                throw new IllegalArgumentException("At index " + i);
            }

            i += Character.charCount(codePoint);
        }
        this.pattern = pattern;
    }

    public synchronized String format(LogRecord r) {
        if (r.getThrown() != null) {
            errors++;
        }
        count++;
        return "";
    }

    @Override
    public synchronized String getTail(Handler h) {
        final long records = this.count; //read
        final long thrown = this.errors;
        this.count = 0; //reset
        this.errors = 0;
        return toString(records, thrown).concat(extFrom(h));
    }

    @Override
    public synchronized String toString() {
        return toString(count, errors);
    }

    /**
     * Create the toString based off of the current state.
     * @param count the number of formatted records.
     * @param errors the number of records with throwables.
     * @return the formatted message.
     */
    private String toString(final long count, final long errors) {
        return MessageFormat.format(pattern, count, errors);
    }

    /**
     * Attachment file names should end with a file extension.  The subject
     * will end with a period.
     * @param h the handler using this formatter.
     * @return the extension string.
     */
    private String extFrom(Handler h) {
        if (h instanceof MailHandler) {
            MailHandler mh = (MailHandler) h;
            if (mh.getSubject() != this) {
                Formatter[] content = mh.getAttachmentFormatters();
                Formatter[] names = mh.getAttachmentNames();
                assert content.length == names.length;
                for (int i = 0; i < content.length; i++) {
                    if (names[i] == this) {
                        if (content[i] instanceof XMLFormatter) {
                            return ".xml";
                        }
                        break;
                    }
                }
                return ".txt";
            }
            return ".";
        }
        return "";
    }
}
