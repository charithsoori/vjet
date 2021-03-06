/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*
 * ElementSelectorImpl.java
 *
 * Modified from Open-Source CSS Parser (Steady State Software) under
 * GNU Lesser General Public License.
 * 
 */

package org.ebayopensource.dsf.css.parser.selectors;

import java.io.Serializable;

import org.ebayopensource.dsf.css.sac.IElementSelector;
import org.ebayopensource.dsf.css.sac.ISelector;

public class DElementSelector implements IElementSelector, Serializable {

	private String m_localName;

	public DElementSelector(String localName) {
		m_localName = localName;
	}

	public short getSelectorType() {
		return ISelector.SAC_ELEMENT_NODE_SELECTOR;
	}

	public String getNamespaceURI() {
		return null;
	}

	public String getLocalName() {
		return m_localName;
	}

	public String toString() {
		return (getLocalName() != null) ? getLocalName() : "*";
	}
	
	//added API
	public void setLocalName(String localName) {
		m_localName = localName;
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
