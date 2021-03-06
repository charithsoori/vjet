/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.mod.internal.ui.typehierarchy;

import org.eclipse.dltk.mod.ui.DLTKPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Action to let the label provider show the defining type of the method
 */
public class SortByDefiningTypeAction extends Action {
	
	private MethodsViewer fMethodsViewer;
	
	/** 
	 * Creates the action.
	 */
	public SortByDefiningTypeAction(MethodsViewer viewer, boolean initValue) {
		super(TypeHierarchyMessages.SortByDefiningTypeAction_label); 
		setDescription(TypeHierarchyMessages.SortByDefiningTypeAction_description); 
		setToolTipText(TypeHierarchyMessages.SortByDefiningTypeAction_tooltip); 
		
		DLTKPluginImages.setLocalImageDescriptors(this, "definingtype_sort_co.gif"); //$NON-NLS-1$

		fMethodsViewer= viewer;
		
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.SORT_BY_DEFINING_TYPE_ACTION);
 
		setChecked(initValue);
	}
	
	/*
	 * @see Action#actionPerformed
	 */	
	public void run() {
		BusyIndicator.showWhile(fMethodsViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				fMethodsViewer.sortByDefiningType(isChecked());
			}
		});		
	}	
}
