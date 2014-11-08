/*
 * Copyright (c) 2009-2014 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2014 Jason Mehrens. All Rights Reserved.
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

import com.sun.mail.util.logging.CollectorFormatter;
import com.sun.mail.util.logging.CompactFormatter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * A compact formatter used to summarize an error report.
 *
 * @author Jason Mehrens
 */
public final class SummaryFormatter extends Formatter {

    /**
     * The line formatter.
     */
    private final CompactFormatter format;
    /**
     * The footer formatter.
     */
    private final CollectorFormatter footer;

    /**
     * Creates the formatter.
     */
    public SummaryFormatter() {
        format = new CompactFormatter("[%4$s]\t%5$s %6$s%n");
        footer = new CollectorFormatter("\nThese {3} messages occurred between "
                + "{7,time,EEE, MMM dd HH:mm:ss:S ZZZ yyyy} and "
                + "{8,time,EEE, MMM dd HH:mm:ss:S ZZZ yyyy}\n", format, null);
    }

    /**
     * Gets the header information.
     *
     * @param h the handler or null.
     * @return the header.
     */
    @Override
    public String getHead(Handler h) {
        footer.getHead(h);
        return format.getHead(h);
    }

    /**
     * Formats the given record.
     *
     * @param record the log record.
     * @return the formatted record.
     * @throws NullPointerException if record is null.
     */
    public String format(LogRecord record) {
        String data = format.format(record);
        footer.format(record); //Track record times for footer.
        return data;
    }

    /**
     * Gets and resets the footer information.
     *
     * @param h the handler or null.
     * @return the footer.
     */
    @Override
    public String getTail(Handler h) {
        format.getTail(h);
        return footer.getTail(h);
    }
}
