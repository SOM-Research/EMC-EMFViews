package org.eclipse.epsilon.emc.emfviews;

import static java.text.MessageFormat.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.epsilon.emc.neoemf.NeoEMFModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IModel;

import org.atlanmod.emfviews.core.EpsilonResource;
import org.atlanmod.emfviews.core.View;
import org.atlanmod.emfviews.core.ViewResource;

import fr.inria.atlanmod.neoemf.resource.PersistentResource;

public class EMFViewsModel extends EmfModel {

  private Map<Resource, IModel> models = new HashMap<>();

  @Override
  public Collection<EObject> getAllOfKind(String kind) throws EolModelElementTypeNotFoundException {
    System.out.println("EMF Views Driver: AllOfKind(" + kind + ")");

    // Go through each contributing model, find the one holding instances of
    // KIND, and delegate to it
    for (Entry<Resource, IModel> e : models.entrySet()) {
      if (e.getValue().hasType(kind)) {
        System.out.printf("Model %s has type %s\n", e.getKey().getURI(), kind);

        // Maybe return a thunk instead of eagerly creating the collection?
        Stream<?> result = e.getValue().getAllOfKind(kind).stream();
        View v = ((ViewResource) modelImpl).getView();

        if (e.getKey() instanceof EpsilonResource) {
          EpsilonResource r = (EpsilonResource) e.getKey();
          result = result.map(r::asEObject);
        }

        return new EMFViewsCollectionWrapper(
          ((Stream<EObject>) result).map(v::getVirtual)
          .collect(Collectors.toList()));
      }
    }

    // Didn't find anything
    throw new RuntimeException(format("Cannot find kind %s in contributing models", kind));
  }

  @Override
  public boolean hasType(String type) {
    for (IModel m : models.values())
      if (m.hasType(type))
        return true;

    return super.hasType(type);
  }


  @Override
  public void load() throws EolModelLoadingException {
    super.load();
    ViewResource r = (ViewResource) modelImpl;

    for(Resource resource : r.getView().getContributingModels()) {
      IModel model;
      if (resource instanceof EpsilonResource) {
        model = ((EpsilonResource) resource).getEpsilonModel();
      }
      else if (resource instanceof PersistentResource) {
        model = initNeoEMFModel(resource);
      }
      // else if (additional supported backends)
      else {
        model = initDefaultEMFModel(resource);
      }
      models.put(resource, model);
    }
  }

  private IModel initNeoEMFModel(Resource resource) {
    NeoEMFModel model = new NeoEMFModel();
    System.out.printf("Loading EMF connector %s for model %s\n",
      model.getClass().getSimpleName(), resource.getURI());
    model.setResource(resource);
    model.initBackend();
    model.setGremlinSupport(true);
    return model;
  }

  private IModel initDefaultEMFModel(Resource resource) {
    EmfModel model = new EmfModel();
    System.out.printf("Loading EMF connector %s for model %s\n",
      model.getClass().getSimpleName(), resource.getURI());
    model.setResource(resource);
    return model;
  }

  /**
   * Unload the view.
   */
  @Override
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
