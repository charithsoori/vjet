package org.eclipse.dltk.mod.ui.dialogs;

import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.dltk.mod.core.DLTKCore;
import org.eclipse.dltk.mod.core.IScriptModel;
import org.eclipse.dltk.mod.core.IScriptProject;
import org.eclipse.dltk.mod.internal.ui.ModelElementComparator;
import org.eclipse.dltk.mod.internal.ui.StandardModelElementContentProvider;
import org.eclipse.dltk.mod.internal.ui.dialogs.StatusInfo;
import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
import org.eclipse.dltk.mod.ui.ModelElementLabelProvider;
import org.eclipse.dltk.mod.ui.preferences.PreferencesMessages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

public class ProjectSelectionDialog extends SelectionStatusDialog {

	// the visual selection widget group
	private TableViewer fTableViewer;
	private Set fProjectsWithSpecifics;
	private final String natureId;

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT= 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH= 300;
	
	private final static String DIALOG_SETTINGS_SHOW_ALL= "ProjectSelectionDialog.show_all"; //$NON-NLS-1$

	private ViewerFilter fFilter;

	public ProjectSelectionDialog(Shell parentShell, Set projectsWithSpecifics) {
		this(parentShell, projectsWithSpecifics, null);
	}

	public ProjectSelectionDialog(Shell parentShell, Set projectsWithSpecifics,
			String natureId) {
		super(parentShell);
		this.natureId = natureId;
		setTitle(PreferencesMessages.ProjectSelectionDialog_title);  
		setMessage(PreferencesMessages.ProjectSelectionDialog_desciption); 
		fProjectsWithSpecifics= projectsWithSpecifics;
		
		fFilter= new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return fProjectsWithSpecifics.contains(element);
			}
		};
	}
	

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite= (Composite) super.createDialogArea(parent);

		Font font= parent.getFont();
		composite.setFont(font);

		createMessageArea(composite);

		fTableViewer= new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				doSelectionChanged(((IStructuredSelection) event.getSelection()).toArray());
			}
		});
		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
                okPressed();
			}
		});
		GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint= SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint= SIZING_SELECTION_WIDGET_WIDTH;
		fTableViewer.getTable().setLayoutData(data);

		fTableViewer.setLabelProvider(new ModelElementLabelProvider());
		fTableViewer.setContentProvider(new StandardModelElementContentProvider());
		fTableViewer.setComparator(new ModelElementComparator());
		fTableViewer.getControl().setFont(font);
		if (natureId != null) {
			fTableViewer.addFilter(new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement,
						Object element) {
					if (element instanceof IScriptProject) {
						IScriptProject project = (IScriptProject) element;
						return natureId.equals(project.getLanguageToolkit()
								.getNatureId());
					}
					return true;
				}
			});
		}

		Button checkbox= new Button(composite, SWT.CHECK);
		checkbox.setText(PreferencesMessages.ProjectSelectionDialog_filter); 
		checkbox.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		checkbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateFilter(((Button) e.widget).getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				updateFilter(((Button) e.widget).getSelection());
			}
		});
		IDialogSettings dialogSettings= DLTKUIPlugin.getDefault().getDialogSettings();
		boolean doFilter= !dialogSettings.getBoolean(DIALOG_SETTINGS_SHOW_ALL) && !fProjectsWithSpecifics.isEmpty();
		checkbox.setSelection(doFilter);
		updateFilter(doFilter);
		
		IScriptModel input= DLTKCore.create(ResourcesPlugin.getWorkspace().getRoot());
		fTableViewer.setInput(input);
		
		doSelectionChanged(new Object[0]);
		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	protected void updateFilter(boolean selected) {
		if (selected) {
			fTableViewer.addFilter(fFilter);
		} else {
			fTableViewer.removeFilter(fFilter);
		}
		DLTKUIPlugin.getDefault().getDialogSettings().put(DIALOG_SETTINGS_SHOW_ALL, !selected);
	}

	private void doSelectionChanged(Object[] objects) {
		if (objects.length != 1) {
			updateStatus(new StatusInfo(IStatus.ERROR, "")); //$NON-NLS-1$
			setSelectionResult(null);
		} else {
			updateStatus(new StatusInfo()); 
			setSelectionResult(objects);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
	}
}
