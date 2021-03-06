/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.mod.ui.browsing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.mod.core.IField;
import org.eclipse.dltk.mod.core.IMember;
import org.eclipse.dltk.mod.core.IMethod;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.core.IPackageDeclaration;
import org.eclipse.dltk.mod.core.ISourceModule;
import org.eclipse.dltk.mod.core.IType;
import org.eclipse.dltk.mod.internal.ui.editor.DLTKEditorMessages;
import org.eclipse.dltk.mod.ui.DLTKPluginImages;
import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
import org.eclipse.dltk.mod.ui.MembersOrderPreferenceCache;
import org.eclipse.dltk.mod.ui.ModelElementSorter;
import org.eclipse.dltk.mod.ui.PreferenceConstants;
import org.eclipse.dltk.mod.ui.ScriptElementLabels;
import org.eclipse.dltk.mod.ui.actions.MemberFilterActionGroup;
import org.eclipse.dltk.mod.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.dltk.mod.ui.viewsupport.ProblemTreeViewer;
import org.eclipse.dltk.mod.ui.viewsupport.ScriptUILabelProvider;
import org.eclipse.dltk.mod.ui.viewsupport.SourcePositionSorter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.IShowInTargetList;

public class MembersView extends ScriptBrowsingPart implements
		IPropertyChangeListener {

	private MemberFilterActionGroup fMemberFilterActionGroup;
	public static final String VIEW_ID = "org.eclipse.dltk.mod.ui.browsing.MembersView";
	ProblemTreeViewer viewer = null;

	/**
	 * Category filter action group.
	 * 
	 * @since 3.2
	 */
	// private CategoryFilterActionGroup fCategoryFilterActionGroup;
	public MembersView() {
		setHasWorkingSetFilter(false);
		setHasCustomSetFilter(true);
		DLTKUIPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.browsing.JavaBrowsingPart#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class key) {
		if (key == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { DLTKUIPlugin.ID_SCRIPTEXPLORER };
				}

			};
		}
		return super.getAdapter(key);
	}

	/**
	 * Creates and returns the label provider for this part.
	 * 
	 * @return the label provider
	 * @see org.eclipse.jface.viewers.ILabelProvider
	 */
	protected ScriptUILabelProvider createLabelProvider() {
		return new AppearanceAwareLabelProvider(
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS
						| ScriptElementLabels.F_APP_TYPE_SIGNATURE
						| ScriptElementLabels.ALL_CATEGORY,
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS, DLTKUIPlugin
						.getDefault().getPreferenceStore());
	}

	/**
	 * Returns the context ID for the Help system
	 * 
	 * @return the string used as ID for the Help context
	 */
	protected String getHelpContextId() {
		// return IJavaHelpContextIds.MEMBERS_VIEW;
		return ""; //$NON-NLS-1$
	}

	protected String getLinkToEditorKey() {
		return PreferenceConstants.LINK_BROWSING_MEMBERS_TO_EDITOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.browsing.JavaBrowsingPart#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected StructuredViewer createViewer(Composite parent) {
		viewer = new ProblemTreeViewer(parent, SWT.MULTI);
		ColoredViewersManager.install(viewer);
		fMemberFilterActionGroup = new MemberFilterActionGroup(viewer,
				DLTKUIPlugin.getDefault().getPreferenceStore());
		return viewer;
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		// Add by Oliver,2009-08-17. This part code is for sorting the elements
		// in the member view. If we want to use the toolbar menu to control the
		// order of element in the member view, please use
		// LexicalSortingAction.
		final SourcePositionSorter fSourcePositonComparator = new SourcePositionSorter();
		BusyIndicator.showWhile(viewer.getControl().getDisplay(),
				new Runnable() {
					public void run() {
						viewer.setComparator(fSourcePositonComparator);
					}
				});
	}

	/**
	 * This class is for the sort of member view. Import part can be sorted in
	 * front of property and method, or not. If we want to add the toolbar menu
	 * on the member view, we can add this action in the fillToolBar method.
	 * 
	 * @author xiuguo
	 */
	class LexicalSortingAction extends Action {

		private ModelElementSorter fComparator = new ModelElementSorter();
		private SourcePositionSorter fSourcePositonComparator = new SourcePositionSorter();

		public LexicalSortingAction() {
			super();
			// PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
			// IJavaHelpContextIds.LEXICAL_SORTING_OUTLINE_ACTION);
			setText(DLTKEditorMessages.ScriptOutlinePage_Sort_label);
			DLTKPluginImages.setLocalImageDescriptors(this,
					"alphab_sort_co.gif"); //$NON-NLS-1$
			setToolTipText(DLTKEditorMessages.ScriptOutlinePage_Sort_tooltip);
			setDescription(DLTKEditorMessages.ScriptOutlinePage_Sort_description);

			boolean checked = DLTKUIPlugin.getDefault().getPreferenceStore()
					.getBoolean("LexicalSortingAction.isChecked");// fStore
			valueChanged(checked, false);
		}

		public void run() {
			valueChanged(isChecked(), true);
		}

		private void valueChanged(final boolean on, boolean store) {
			setChecked(on);
			BusyIndicator.showWhile(viewer.getControl().getDisplay(),
					new Runnable() {
						public void run() {
							if (on) {
								viewer.setComparator(fComparator);
							} else {
								viewer.setComparator(fSourcePositonComparator);
							}
						}
					});

			if (store) {
				DLTKUIPlugin.getDefault().getPreferenceStore().setValue(
						"LexicalSortingAction.isChecked", on); //$NON-NLS-1$
			}
		}
	}

	protected void fillToolBar(IToolBarManager tbm) {
		// Add by Oliver. 2009-08-17. This toolbar menu is to sort the members
		// sequence. Before the import segment is at the bottom of this view.
		// Now we can control the display sequence by this action.
		tbm.add(new LexicalSortingAction());
		fMemberFilterActionGroup.contributeToToolBar(tbm);
		super.fillToolBar(tbm);

	}

	/*
	 * @see org.eclipse.jdt.internal.ui.browsing.JavaBrowsingPart#fillActionBars(org.eclipse.ui.IActionBars)
	 * @since 3.2
	 */
	protected void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		// fCategoryFilterActionGroup = new
		// CategoryFilterActionGroup(getViewer(),
		// getViewSite().getId(), getCategoryFilterActionGroupInput());
		// fCategoryFilterActionGroup.contributeToViewMenu(actionBars
		// .getMenuManager());

	}

	/*
	 * @see org.eclipse.jdt.internal.ui.browsing.JavaBrowsingPart#setInput(java.lang.Object)
	 * @since 3.2
	 */
	protected void setInput(Object input) {
		super.setInput(input);
		// if (fCategoryFilterActionGroup != null)
		// fCategoryFilterActionGroup
		// .setInput(getCategoryFilterActionGroupInput());
	}

	private IModelElement[] getCategoryFilterActionGroupInput() {
		Object input = getInput();
		if (input instanceof IModelElement)
			return new IModelElement[] { (IModelElement) input };
		return new IModelElement[0];
	}

	/**
	 * Answers if the given <code>element</code> is a valid input for this
	 * part.
	 * 
	 * @param element
	 *            the object to test
	 * @return <true> if the given element is a valid input
	 */
	protected boolean isValidInput(Object element) {
		if (element instanceof ISourceModule || element instanceof IType) {
			return true;
		}
		return false;
	}

	protected IContentProvider createContentProvider() {
		final ITreeContentProvider original = (ITreeContentProvider) super
				.createContentProvider();
		return new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				Object[] children = original.getChildren(parentElement);
				List newChildren = new ArrayList();
				for (int i = 0; i < children.length; i++) {
					if ((children[i] instanceof IField
							|| children[i] instanceof IMethod || children[i] instanceof IPackageDeclaration)) {
						newChildren.add(children[i]);
					}
				}
				return newChildren.toArray();
			}

			public Object getParent(Object element) {
				return original.getParent(element);
			}

			public boolean hasChildren(Object element) {
				// original.getParent(element);
				return false;
			}

			public Object[] getElements(Object inputElement) {
				Object[] children = original.getElements(inputElement);
				List newChildren = new ArrayList();
				for (int i = 0; i < children.length; i++) {
					if ((children[i] instanceof IField
							|| children[i] instanceof IMethod || children[i] instanceof IPackageDeclaration)) {
						newChildren.add(children[i]);
					}
				}
				return newChildren.toArray();
			}

			public void dispose() {
				original.dispose();
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				original.inputChanged(viewer, oldInput, newInput);
			}
		};
	}

	/**
	 * Answers if the given <code>element</code> is a valid element for this
	 * part.
	 * 
	 * @param element
	 *            the object to test
	 * @return <true> if the given element is a valid element
	 */
	protected boolean isValidElement(Object element) {
		if (element instanceof IField || element instanceof IMethod
				|| element instanceof IPackageDeclaration)
			return true;
		return false;
	}

	/**
	 * Finds the element which has to be selected in this part.
	 * 
	 * @param je
	 *            the Java element which has the focus
	 * @return the element to select
	 */
	protected IModelElement findElementToSelect(IModelElement je) {
		if (je == null)
			return null;

		switch (je.getElementType()) {
		case IModelElement.TYPE:
			return je;
		case IModelElement.METHOD:
			// case IModelElement.INITIALIZER:
		case IModelElement.FIELD:
		case IModelElement.PACKAGE_DECLARATION:
			return je;
		case IModelElement.SOURCE_MODULE:
			return je;
		}
		return je;
	}

	/**
	 * Finds the closest Java element which can be used as input for this part
	 * and has the given Java element as child.
	 * 
	 * @param je
	 *            the Java element for which to search the closest input
	 * @return the closest Java element used as input for this part, or
	 *         <code>null</code>
	 */
	protected IModelElement findInputForJavaElement(IModelElement je) {
		if (je == null
				|| !je.exists()
				|| (je.getScriptProject() != null && !je.getScriptProject()
						.isOnBuildpath(je)))
			return null;

		switch (je.getElementType()) {
		// case IModelElement.TYPE:
		// return je;
		// case IModelElement.SOURCE_MODULE:
		// // return getTypeForCU((ISourceModule) je);
		// return je;
		// case IModelElement.PACKAGE_DECLARATION:
		// default:
		// return je;

		case IModelElement.TYPE:
			IType type = ((IType) je).getDeclaringType();
			if (type == null)
				return je;
			else
				return findInputForJavaElement(type);
			// case IModelElement.COMPILATION_UNIT:
			// return getTypeForCU((ICompilationUnit)je);
			// case IModelElement.CLASS_FILE:
			// return findInputForJavaElement(((IClassFile)je).getType());
			// case IModelElement.IMPORT_DECLARATION:
			// return findInputForJavaElement(je.getParent());
		case IModelElement.PACKAGE_DECLARATION:
			// case IModelElement.IMPORT_CONTAINER:
			IModelElement parent = je.getParent();
			if (parent instanceof IType) {
				return parent;
			}
			// else if (parent instanceof IClassFile)
			// return findInputForJavaElement(parent);
			return null;
		default:
			if (je instanceof IMember)
				return findInputForJavaElement(((IMember) je)
						.getDeclaringType());

		}

		return null;
	}

	/*
	 * Implements method from IViewPart.
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		// fMemberFilterActionGroup.saveState(memento);
	}

	protected void restoreState(IMemento memento) {
		super.restoreState(memento);
		// fMemberFilterActionGroup.restoreState(memento);
		getViewer().getControl().setRedraw(false);
		getViewer().refresh();
		getViewer().getControl().setRedraw(true);
	}

	protected void hookViewerListeners() {
		super.hookViewerListeners();
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				TreeViewer viewer = (TreeViewer) getViewer();
				Object element = ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				if (viewer.isExpandable(element))
					viewer.setExpandedState(element, !viewer
							.getExpandedState(element));
			}
		});
	}

	boolean isInputAWorkingCopy() {
		Object input = getViewer().getInput();
		if (input instanceof IModelElement) {
			ISourceModule cu = (ISourceModule) ((IModelElement) input)
					.getAncestor(IModelElement.SOURCE_MODULE);
			if (cu != null)
				return cu.isWorkingCopy();
		}
		return false;
	}

	protected void restoreSelection() {
		IEditorPart editor = getViewSite().getPage().getActiveEditor();
		if (editor != null)
			setSelectionFromEditor(editor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (MembersOrderPreferenceCache.isMemberOrderProperty(event
				.getProperty())) {
			getViewer().refresh();
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.browsing.JavaBrowsingPart#dispose()
	 */
	public void dispose() {
		// if (fMemberFilterActionGroup != null) {
		// fMemberFilterActionGroup.dispose();
		// fMemberFilterActionGroup = null;
		// }
		// if (fCategoryFilterActionGroup != null) {
		// fCategoryFilterActionGroup.dispose();
		// fCategoryFilterActionGroup = null;
		// }
		super.dispose();
		DLTKUIPlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(this);
	}
}
