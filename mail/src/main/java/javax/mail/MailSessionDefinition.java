/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

package javax.mail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Repeatable;

/**
 * Annotation used by Java EE applications to define a <code>MailSession</code>
 * to be registered with JNDI.  The <code>MailSession</code> may be configured
 * by setting the annotation elements for commonly used <code>Session</code>
 * properties.  Additional standard and vendor-specific properties may be
 * specified using the <code>properties</code> element.
 * <p>
 * The session will be registered under the name specified in the
 * <code>name</code> element.  It may be defined to be in any valid
 * <code>Java EE</code> namespace, and will determine the accessibility of
 * the session from other components.
 *
 * @since JavaMail 1.5
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MailSessionDefinitions.class)
public @interface MailSessionDefinition {

    /**
     * Description of this mail session.
     *
     * @return	the description
     */
    String description() default "";

    /**
     * JNDI name by which the mail session will be registered.
     *
     * @return	the JNDI name
     */
    String name();

    /**
     * Store protocol name.
     *
     * @return	the store protocol name
     */
    String storeProtocol() default "";

    /**
     * Transport protocol name.
     *
     * @return	the transport protocol name
     */
    String transportProtocol() default "";

    /**
     * Host name for the mail server.
     *
     * @return	the host name
     */
    String host() default "";

    /**
     * User name to use for authentication.
     *
     * @return	the user name
     */
    String user() default "";

    /**
     * Password to use for authentication.
     *
     * @return	the password
     */
    String password() default "";

    /**
     * From address for the user.
     *
     * @return	the from address
     */
    String from() default "";

    /**
     * Properties to include in the Session.
     * Properties are specified using the format:
     * <i>propertyName=propertyValue</i> with one property per array element.
     *
     * @return	the properties
     */
    String[] properties() default {};
}
