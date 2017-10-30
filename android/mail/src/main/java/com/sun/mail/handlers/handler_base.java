/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.handlers;

import java.io.IOException;
import javax.activation.*;

/**
 * Base class for other DataContentHandlers.
 */
public abstract class handler_base implements DataContentHandler {

    /**
     * Return an array of ActivationDataFlavors that we support.
     * Usually there will be only one.
     *
     * @return	array of ActivationDataFlavors that we support
     */
    protected abstract ActivationDataFlavor[] getDataFlavors();

    /**
     * Given the flavor that matched, return the appropriate type of object.
     * Usually there's only one flavor so just call getContent.
     *
     * @param	aFlavor	the ActivationDataFlavor
     * @param	ds	DataSource containing the data
     * @return	the object
     * @exception	IOException	for errors reading the data
     */
    protected Object getData(ActivationDataFlavor aFlavor, DataSource ds)
				throws IOException {
	return getContent(ds);
    }

    /**
     * Return the DataFlavors for this <code>DataContentHandler</code>.
     *
     * @return The DataFlavors
     */
    public ActivationDataFlavor[] getTransferDataFlavors() {
	return getDataFlavors().clone();
    }

    /**
     * Return the Transfer Data of type DataFlavor from InputStream.
     *
     * @param	df	The DataFlavor
     * @param	ds	The DataSource corresponding to the data
     * @return	the object
     * @exception	IOException	for errors reading the data
     */
    public Object getTransferData(ActivationDataFlavor df, DataSource ds) 
			throws IOException {
	ActivationDataFlavor[] adf = getDataFlavors();
	for (int i = 0; i < adf.length; i++) {
	    // use ActivationDataFlavor.equals, which properly
	    // ignores Content-Type parameters in comparison
	    if (adf[i].equals(df))
		return getData(adf[i], ds);
	}
	return null;
    }
}
