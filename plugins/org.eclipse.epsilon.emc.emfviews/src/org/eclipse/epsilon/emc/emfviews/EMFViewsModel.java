package org.eclipse.epsilon.emc.emfviews;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import org.atlanmod.emfviews.core.ViewResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;

import fr.inria.atlanmod.neoemf.resource.PersistentResource;

public class EMFViewsModel extends EmfModel {

	@Override
	public Collection<EObject> getAllOfKind(String kind) throws EolModelElementTypeNotFoundException {
		System.out.println("EMF Views Driver: AllOfKind(" + kind + ")");
		Instant start = Instant.now();
		Collection<EObject> result = super.getAllOfKind(kind);
		Instant end = Instant.now();
		System.out.println("Done: " + Duration.between(start, end).toMillis() + "ms");
		return result;
	}

	/**
	 * Unload the view.
	 */
	public void disposeModel() {
		ViewResource r = (ViewResource) modelImpl;
		/*
		 * Quickfix: we need to manually unload the NeoEMF resource contributing to the
		 * view, otherwise the database lock is not released and its not possible to
		 * compute another EOL program on top of it.
		 * 
		 * @fmdkdd this should probably be moved in EMFViews core, we need to discuss this.
		 */
		for (Resource resource : r.getView().getContributingModels()) {
			if (resource instanceof PersistentResource) {
				((PersistentResource) resource).close();
			}
		}
		super.disposeModel();
	}

}
