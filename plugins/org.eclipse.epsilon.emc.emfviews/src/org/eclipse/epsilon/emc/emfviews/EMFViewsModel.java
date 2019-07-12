package org.eclipse.epsilon.emc.emfviews;

import static java.text.MessageFormat.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.emc.neoemf.NeoEMFModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IModel;

import org.atlanmod.emfviews.core.EpsilonResource;
import org.atlanmod.emfviews.core.View;
import org.atlanmod.emfviews.core.ViewResource;

import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.util.logging.NeoLogger;

public class EMFViewsModel extends EmfModel {

  public EMFViewsModel(Resource modelImpl) {
    this.modelImpl = modelImpl;
  }

  private Map<Resource, IModel> models = new HashMap<>();
  private boolean forceDefaultEMFDriver = false;

  public void setForceDefaultEMFDriver(boolean use) {
    forceDefaultEMFDriver = use;
  }

  @Override
  public Collection<EObject> getAllOfKind(String kind) throws EolModelElementTypeNotFoundException {
    NeoLogger.info("EMF Views Driver: AllOfKind(" + kind + ")");

    // Go through each contributing model, find the one holding instances of
    // KIND, and delegate to it
    for (Entry<Resource, IModel> e : models.entrySet()) {
      if (e.getValue().hasType(kind)) {
        NeoLogger.info(String.format("Model %s has type %s\n", e.getKey().getURI(), kind));

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
    ViewResource r = (ViewResource) modelImpl;

    for(Resource resource : r.getView().getContributingModels()) {
      IModel model;

      if (resource instanceof EpsilonResource) {
        model = ((EpsilonResource) resource).getEpsilonModel();
      }
      else if (resource instanceof PersistentResource) {
     // Forcing the default EMF driver is useful for testing and benchmarking.
        if (forceDefaultEMFDriver) {
          model = initDefaultEMFModel(resource);
        } else {
          model = initNeoEMFModel(resource);
        }
      }
      // else if (additional supported backends)
      else {
        model = initDefaultEMFModel(resource);
      }
      models.put(resource, model);
      NeoLogger.info(String.format("Loaded EMF connector %s for model %s\n",
        model.getClass().getSimpleName(), resource.getURI()));
    }
  }

  private IModel initNeoEMFModel(Resource resource) {
    NeoEMFModel model = new NeoEMFModel();
    model.setResource(resource);
    model.initBackend();
    model.setGremlinSupport(true);
    return model;
  }

  private IModel initDefaultEMFModel(Resource resource) {
    EmfModel model = new EmfModel();
    model.setResource(resource);
    return model;
  }

  /**
   * Unload the view.
   */
  @Override
  public void disposeModel() {
    ViewResource r = (ViewResource) modelImpl;
    r.close();

    // This is trying to unload elements from the underlying resources, and
    // throwing exceptions in CDO because r.close above shuts down the CDO
    // backend.
    // FIXME: Do we need it?
    // super.disposeModel();
  }

}
