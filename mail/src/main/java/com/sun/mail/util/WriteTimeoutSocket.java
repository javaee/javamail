/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.util;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.nio.channels.SocketChannel;

/**
 * A special Socket that uses a ScheduledExecutorService to
 * implement timeouts for writes.  The write timeout is specified
 * (in milliseconds) when the WriteTimeoutSocket is created.
 *
 * @author	Bill Shannon
 */
public class WriteTimeoutSocket extends Socket {

    // delegate all operations to this socket
    private final Socket socket;
    // to schedule task to cancel write after timeout
    private final ScheduledExecutorService ses;
    // the timeout, in milliseconds
    private final int timeout;

    public WriteTimeoutSocket(Socket socket, int timeout) throws IOException {
	this.socket = socket;
	// XXX - could share executor with all instances?
        this.ses = Executors.newScheduledThreadPool(1);
	this.timeout = timeout;
    }

    public WriteTimeoutSocket(int timeout) throws IOException {
	this(new Socket(), timeout);
    }

    public WriteTimeoutSocket(InetAddress address, int port, int timeout)
				throws IOException {
	this(timeout);
	socket.connect(new InetSocketAddress(address, port));
    }

    public WriteTimeoutSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort, int timeout)
			throws IOException {
	this(timeout);
	socket.bind(new InetSocketAddress(localAddress, localPort));
	socket.connect(new InetSocketAddress(address, port));
    }

    public WriteTimeoutSocket(String host, int port, int timeout)
				throws IOException {
	this(timeout);
	socket.connect(new InetSocketAddress(host, port));
    }

    public WriteTimeoutSocket(String host, int port,
			InetAddress localAddress, int localPort, int timeout)
			throws IOException {
	this(timeout);
	socket.bind(new InetSocketAddress(localAddress, localPort));
	socket.connect(new InetSocketAddress(host, port));
    }

    // override all Socket methods and delegate to underlying Socket

    @Override
    public void connect(SocketAddress remote) throws IOException {
        socket.connect(remote, 0);
    }

    @Override
    public void connect(SocketAddress remote, int timeout) throws IOException {
	socket.connect(remote, timeout);
    }

    @Override
    public void bind(SocketAddress local) throws IOException {
	socket.bind(local);
    }

    @Override
    public SocketChannel getChannel() {
	return socket.getChannel();
    }

    @Override
    public InetAddress getInetAddress() {
	return socket.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
	return socket.getLocalAddress();
    }

    @Override
    public int getPort() {
	return socket.getPort();
    }

    @Override
    public int getLocalPort() {
	return socket.getLocalPort();
    }

    @Override
    public InputStream getInputStream() throws IOException {
	return socket.getInputStream();
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
	// wrap the returned stream to implement write timeout
        return new TimeoutOutputStream(socket.getOutputStream(), ses, timeout);
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        socket.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return socket.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        socket.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return socket.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        socket.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        socket.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return socket.getOOBInline();
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
	socket.setSoTimeout(timeout);
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return socket.getSoTimeout();
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        socket.setSendBufferSize(size);
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        return socket.getSendBufferSize();
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        socket.setReceiveBufferSize(size);
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        return socket.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        socket.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return socket.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        socket.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return socket.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        socket.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return socket.getReuseAddress();
    }

    @Override
    public void close() throws IOException {
	try {
	    socket.close();
	} finally {
	    ses.shutdownNow();
	}
    }

    @Override
    public void shutdownInput() throws IOException {
	socket.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
	socket.shutdownOutput();
    }

    @Override
    public String toString() {
	return socket.toString();
    }

    @Override
    public boolean isConnected() {
        return socket.isConnected();
    }

    @Override
    public boolean isBound() {
        return socket.isBound();
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return socket.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return socket.isOutputShutdown();
    }
}


/**
 * An OutputStream that wraps the Socket's OutputStream and uses
 * the ScheduledExecutorService to schedule a task to close the
 * socket (aborting the write) if the timeout expires.
 */
class TimeoutOutputStream extends OutputStream {
    private final OutputStream os;
    private final ScheduledExecutorService ses;
    private final Callable<Object> timeoutTask;
    private final int timeout;
    private byte[] b1;

    public TimeoutOutputStream(OutputStream os0, ScheduledExecutorService ses,
				int timeout) throws IOException {
	this.os = os0;
	this.ses = ses;
	this.timeout = timeout;
	timeoutTask = new Callable<Object>() {
	    public Object call() throws Exception {
		os.close();	// close the stream to abort the write
		return null;
	    }
	};
    }

    @Override
    public synchronized void write(int b) throws IOException {
	if (b1 == null)
	    b1 = new byte[1];
	b1[0] = (byte)b;
	this.write(b1);
    }

    @Override
    public synchronized void write(byte[] bs, int off, int len)
				throws IOException {
	if ((off < 0) || (off > bs.length) || (len < 0) ||
	    ((off + len) > bs.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return;
	}

	// Implement timeout with a scheduled task
	ScheduledFuture<Object> sf = null;
	try {
	    try {
		if (timeout > 0)
		    sf = ses.schedule(timeoutTask,
					timeout, TimeUnit.MILLISECONDS);
	    } catch (RejectedExecutionException ex) {
		// ignore it; Executor was shut down by another thread,
		// the following write should fail with IOException
	    }
	    os.write(bs, off, len);
	} finally {
	    if (sf != null)
		sf.cancel(true);
	}
    }

    @Override
    public void close() throws IOException {
	os.close();
    }
}
