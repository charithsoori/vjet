/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.mod.internal.ui.editor.selectionaction;

import org.eclipse.dltk.mod.core.ISourceRange;
import org.eclipse.dltk.mod.core.ISourceReference;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.internal.ui.editor.ScriptEditor;

public class StructureSelectEnclosingAction extends StructureSelectionAction {

	public StructureSelectEnclosingAction(ScriptEditor editor, SelectionHistory history) {
		super(SelectionActionMessages.StructureSelectEnclosing_label, editor, history);
		setToolTipText(SelectionActionMessages.StructureSelectEnclosing_tooltip);
		setDescription(SelectionActionMessages.StructureSelectEnclosing_description);
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.STRUCTURED_SELECT_ENCLOSING_ACTION);
	}

	/*
	 * This constructor is for testing purpose only.
	 */
	public StructureSelectEnclosingAction() {
	}

	ISourceRange internalGetNewSelectionRange(ISourceRange oldSourceRange, ISourceReference sr) throws ModelException {
		return null;
	}    
}
