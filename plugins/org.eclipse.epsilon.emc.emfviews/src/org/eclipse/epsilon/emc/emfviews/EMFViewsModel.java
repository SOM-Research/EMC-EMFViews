package org.eclipse.epsilon.emc.emfviews;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;

public class EMFViewsModel extends EmfModel {
	
	@Override
	public Collection<EObject> getAllOfType(String type) throws EolModelElementTypeNotFoundException {
		System.out.println("EMF Views Driver: AllOfType(" + type + ")");
		return super.getAllOfType(type);
	}
	
	@Override
	public Collection<EObject> getAllOfKind(String kind) throws EolModelElementTypeNotFoundException {
		System.out.println("EMF Views Driver: AllOfKind(" + kind + ")");
		return super.getAllOfKind(kind);
	}

}
