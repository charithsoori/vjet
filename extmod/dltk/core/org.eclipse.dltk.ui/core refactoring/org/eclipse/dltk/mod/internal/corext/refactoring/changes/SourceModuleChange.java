/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.mod.internal.corext.refactoring.changes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.mod.core.ScriptModelUtil;
import org.eclipse.dltk.mod.core.ISourceModule;
import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.UndoEdit;


public class SourceModuleChange extends TextFileChange {

	private ISourceModule fCUnit;
	
	/**
	 * Creates a new <code>SourceModuleChange</code>.
	 * 
	 * @param name the change's name mainly used to render the change in the UI
	 * @param cunit the compilation unit this text change works on
	 */
	public SourceModuleChange(String name, ISourceModule cunit) {
		super(name, getFile(cunit));
		Assert.isNotNull(cunit);
		fCUnit= cunit;
		setTextType("java"); //$NON-NLS-1$
	}
	
	private static IFile getFile(ISourceModule cunit) {
		return (IFile) cunit.getResource();
	}
	
	/* non java-doc
	 * Method declared in IChange.
	 */
	public Object getModifiedElement(){
		return fCUnit;
	}
	
	/**
	 * Returns the compilation unit this change works on.
	 * 
	 * @return the compilation unit this change works on
	 */
	public ISourceModule getSourceModule() {
		return fCUnit;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected IDocument acquireDocument(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 2); //$NON-NLS-1$
		fCUnit.becomeWorkingCopy(null, new SubProgressMonitor(pm, 1));
		return super.acquireDocument(new SubProgressMonitor(pm, 1));
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void releaseDocument(IDocument document, IProgressMonitor pm) throws CoreException {
		super.releaseDocument(document, pm);
		try {
			fCUnit.discardWorkingCopy();
		} finally {
			if (!isDocumentAcquired()) {
				if (fCUnit.isWorkingCopy())
					ScriptModelUtil.reconcile(fCUnit);
				else
					fCUnit.makeConsistent(pm);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Change createUndoChange(UndoEdit edit, ContentStamp stampToRestore) {
		try {
			return new UndoSourceModuleChange(getName(), fCUnit, edit, stampToRestore, getSaveMode());
		} catch (CoreException e) {
			DLTKUIPlugin.log(e);
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(Class adapter) {
		if (ISourceModule.class.equals(adapter))
			return fCUnit;
		return super.getAdapter(adapter);
	}
}

