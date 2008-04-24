/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.equinox.internal.p2.updatesite;

import org.eclipse.osgi.util.NLS;

/**
 * @since 1.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.equinox.internal.p2.updatesite.messages"; //$NON-NLS-1$

	public static String ErrorCreatingRepository;
	public static String ErrorReadingDigest;
	public static String ErrorReadingFeature;
	public static String ErrorReadingSite;
	public static String InvalidRepositoryLocation;
	public static String MalformedArchiveURL;
	public static String PlatformAdminNotRegistered;

	public static String io_failedRead;

	public static String repoMan_internalError;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// prevent instantiation
	}
}
