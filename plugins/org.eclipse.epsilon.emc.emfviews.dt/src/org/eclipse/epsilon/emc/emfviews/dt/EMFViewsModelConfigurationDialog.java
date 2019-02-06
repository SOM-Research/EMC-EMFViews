package org.eclipse.epsilon.emc.emfviews.dt;

import org.eclipse.epsilon.emc.emf.dt.EmfModelConfigurationDialog;

public class EMFViewsModelConfigurationDialog extends EmfModelConfigurationDialog {

	@Override
	protected String getModelName() {
		return "EMF Views Model";
	}
	
	@Override
	protected String getModelType() {
		return "emf-views";
	}
	
}
