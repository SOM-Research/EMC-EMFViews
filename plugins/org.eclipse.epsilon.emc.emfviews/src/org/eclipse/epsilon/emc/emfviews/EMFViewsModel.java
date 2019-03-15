package org.eclipse.epsilon.emc.emfviews;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.atlanmod.emfviews.core.ViewResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.neoemf.NeoEMFModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IModel;

import fr.inria.atlanmod.neoemf.resource.PersistentResource;

import static java.text.MessageFormat.format;

public class EMFViewsModel extends EmfModel {

	private Map<Resource, IModel> models = new HashMap<>();
	
	@Override
	public Collection<EObject> getAllOfKind(String kind) throws EolModelElementTypeNotFoundException {
		System.out.println("EMF Views Driver: AllOfKind(" + kind + ")");
		// Assume that we are on NeoEMF for now
		IModel neoemfModel = models.values().iterator().next();
		return new EMFViewsCollectionWrapper(neoemfModel.getAllOfKind(kind));
		
		// Native EMF Views allInstances
//	    ViewResource r = (ViewResource) modelImpl;
//	    Collection<EObject> result = r.getView().getAllInstances(kind).collect(Collectors.toList());

	}
	
	@Override
	public void load() throws EolModelLoadingException {
		super.load();
		ViewResource r = (ViewResource) modelImpl;
		for(Resource resource : r.getView().getContributingModels()) {
			IModel model;
			if(resource instanceof PersistentResource) {
				model = initNeoEMFModel(resource);
			} // else if (additional supported backends)
			else {
				model = initDefaultEMFModel(resource);
			}
			models.put(resource, model);
		}
	}
	
	private IModel initNeoEMFModel(Resource resource) {
		NeoEMFModel model = new NeoEMFModel();
		System.out.println(format("Loading EMF connector {0} for model {1}", model.getClass().getSimpleName(), resource.getURI()));
		model.setResource(resource);
		model.initBackend();
		model.setGremlinSupport(true);
		return model;
	}
	
	private IModel initDefaultEMFModel(Resource resource) {
		EmfModel model = new EmfModel();
		System.out.println(format("Loading EMF connector {0} for model {1}", model.getClass().getSimpleName(), resource.getURI()));
		model.setResource(resource);
		return model;
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
