package edu.uoc.som.emfviews.neoemf.example;
import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.emc.emfviews.EMFViewsModel;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.execute.operations.EolOperationFactory;
import org.eclipse.gmt.modisco.java.JavaPackage;

import org.atlanmod.emfviews.core.EmfViewsFactory;
import org.atlanmod.emfviews.core.EpsilonResource;
import org.atlanmod.emfviews.core.ViewResource;
import org.atlanmod.emfviews.virtuallinks.VirtualLinksPackage;

import fr.inria.atlanmod.neoemf.data.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.data.blueprints.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsURI;
import fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory;

public class RunEOL {
  static String here = new File(".").getAbsolutePath();

  static URI resourceURI(String relativePath) {
    return URI.createFileURI(here + relativePath);
  }

  public static void main(String[] args) throws Exception {
    EcorePackage.eINSTANCE.eClass();
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
    VirtualLinksPackage.eINSTANCE.eClass();
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
    Resource.Factory epsRF = new Resource.Factory() {
      @Override
      public Resource createResource(URI uri) {
        return new EpsilonResource(uri);
      }
    };
    JavaPackage.eINSTANCE.eClass();
    /*
     * Register NeoEMF persistence backend and protocol to enable NeoEMF resource
     * loading.
     */
    Resource.Factory.Registry.INSTANCE.getProtocolToFactoryMap().put(BlueprintsURI.SCHEME,
      PersistentResourceFactory.getInstance());
    PersistenceBackendFactoryRegistry.register(BlueprintsURI.SCHEME,
      BlueprintsPersistenceBackendFactory.getInstance());
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("eview", new EmfViewsFactory());
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("eviewpoint", new EmfViewsFactory());

    // Register Log metamodel
    Resource log = (new ResourceSetImpl()).getResource(resourceURI("/metamodels/Log.ecore"), true);
    EPackage logPackage = (EPackage) log.getContents().get(0);
    EPackage.Registry.INSTANCE.put(logPackage.getNsURI(), logPackage);
    System.out.printf("Loaded %s metamodel from %s\n", logPackage.getNsURI(), log.getURI());

    // Run EOL query on the view
    EolModule module = new EolModule();
    module.parse("VIEW!Event.all.collect(e | Sequence { e.name, e.javaVariables }).println();");
    if (module.getParseProblems().size() > 0) {
      System.err.println("Parse errors occured...");
      for (ParseProblem problem : module.getParseProblems()) {
        System.err.println(problem.toString());
      }
      throw new Exception("Error in parsing ECL file.  See stderr for details");
    }
    module.getContext().setOperationFactory(new EolOperationFactory());

    // Add view
    // Need the viewpoint as well
    EMFViewsModel m = new EMFViewsModel();
    m.setName("VIEW");
    m.setModelFileUri(resourceURI("/views/with-log/with-log.eview"));
    m.setMetamodelFileUri(resourceURI("/views/with-log/with-log.eviewpoint"));
    m.load();
    module.getContext().getModelRepository().addModel(m);

    // Execute the module
    System.out.format("EOL result: %s", module.execute());
  }
}
