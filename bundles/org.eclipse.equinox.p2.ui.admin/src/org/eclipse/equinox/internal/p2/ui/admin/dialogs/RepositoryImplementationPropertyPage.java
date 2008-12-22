/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.ui.admin.dialogs;

import java.util.Map;
import org.eclipse.equinox.internal.p2.ui.admin.ProvAdminUIMessages;
import org.eclipse.equinox.internal.provisional.p2.ui.ProvUI;
import org.eclipse.equinox.internal.provisional.p2.ui.model.IRepositoryElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * PropertyPage that shows a repository's properties
 * 
 * @since 3.4
 */
public class RepositoryImplementationPropertyPage extends PropertyPage {
	private IRepositoryElement repositoryElement;
	private Composite composite;
	private Text name;
	private Text url;
	private Text description;
	private Table propertiesTable;

	protected Control createContents(Composite parent) {
		this.repositoryElement = getRepositoryElement();
		if (repositoryElement == null) {
			Label label = new Label(parent, SWT.DEFAULT);
			label.setText(ProvAdminUIMessages.RepositoryImplementationPropertyPage_NoRepositorySelected);
			return label;
		}
		noDefaultAndApplyButton();

		composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.widthHint = 350;
		composite.setLayoutData(data);

		Label urlLabel = new Label(composite, SWT.NONE);
		urlLabel.setText(ProvAdminUIMessages.RepositoryImplementationPropertyPage_LocationLabel);
		url = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		url.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(ProvAdminUIMessages.RepositoryImplementationPropertyPage_NameLabel);
		name = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel.setText(ProvAdminUIMessages.RepositoryImplementationPropertyPage_DescriptionLabel);
		data = new GridData();
		data.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(data);
		description = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = SWT.TOP;
		data.grabExcessVerticalSpace = true;
		description.setLayoutData(data);
		Label propertiesLabel = new Label(composite, SWT.NONE);
		propertiesLabel.setText(ProvAdminUIMessages.RepositoryImplementationPropertyPage_PropertiesLabel);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		propertiesLabel.setLayoutData(data);

		propertiesTable = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		data.grabExcessVerticalSpace = true;
		propertiesTable.setLayoutData(data);
		propertiesTable.setHeaderVisible(true);
		TableColumn nameColumn = new TableColumn(propertiesTable, SWT.NONE);
		nameColumn.setText(ProvAdminUIMessages.RepositoryImplementationPropertyPage_NameColumnLabel);
		TableColumn valueColumn = new TableColumn(propertiesTable, SWT.NONE);
		valueColumn.setText(ProvAdminUIMessages.RepositoryImplementationPropertyPage_ValueColumnLabel);

		initializeTable();

		nameColumn.pack();
		valueColumn.pack();

		return composite;

	}

	private void initializeTable() {
		if (getRepositoryElement() != null) {
			Map repoProperties = getRepositoryElement().getRepository(null).getProperties();
			if (repoProperties != null) {
				String[] propNames = (String[]) repoProperties.keySet().toArray(new String[repoProperties.size()]);
				for (int i = 0; i < propNames.length; i++) {
					TableItem item = new TableItem(propertiesTable, SWT.NULL);
					item.setText(new String[] {propNames[i], repoProperties.get(propNames[i]).toString()});
				}
			}
		}
	}

	private IRepositoryElement getRepositoryElement() {
		if (repositoryElement == null) {
			repositoryElement = (IRepositoryElement) ProvUI.getAdapter(getElement(), IRepositoryElement.class);
		}
		return repositoryElement;
	}

}
