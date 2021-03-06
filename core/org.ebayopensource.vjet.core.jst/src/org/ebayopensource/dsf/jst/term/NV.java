/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jst.term;

import java.io.Serializable;

import org.ebayopensource.dsf.jst.BaseJstNode;
import org.ebayopensource.dsf.jst.token.IExpr;
import org.ebayopensource.dsf.jst.traversal.IJstNodeVisitor;


/**
 * Immutable object
 * Name cannot be null
 */
public final class NV extends BaseJstNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final JstIdentifier m_name;
	private final IExpr m_value;
	
	//
	// Constructor
	//
	public NV(final String name, final JstLiteral value){
		this(name, (IExpr)value);
	}
	
	public NV(final String name, final IExpr value){
		this( new JstIdentifier(name), value);
	}
	
	
	public NV(final JstIdentifier name, final IExpr value){
		assert name != null : "name cannot be null";
		
		m_name = name;
		m_value = value;
		addChild(m_name);
		addChild(m_value);
	}
	
	//
	// API
	//
	public String getName() {
		return m_name.getName();
	}
	
	public JstIdentifier getIdentifier() {
		return m_name;
	}

	public IExpr getValue() {
		return m_value;
	}

	@Override
	public void accept(IJstNodeVisitor visitor){
		visitor.visit(this);
	}
	
	@Override
	public String toString(){
		return m_name.toSimpleTermText() + ":" + m_value;
	}
}
