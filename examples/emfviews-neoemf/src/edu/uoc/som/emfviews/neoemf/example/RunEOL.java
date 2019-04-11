package edu.uoc.som.emfviews.neoemf.example;
import java.io.File;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.emc.emfviews.EMFViewsModel;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.execute.operations.EolOperationFactory;
import org.eclipse.gmt.modisco.java.JavaPackage;

import org.atlanmod.emfviews.core.EmfViewsFactory;
import org.atlanmod.emfviews.core.EpsilonResource;
import org.atlanmod.emfviews.virtuallinks.VirtualLinksPackage;

import fr.inria.atlanmod.neoemf.data.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.data.blueprints.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsURI;
import fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory;

public class RunEOL {
  static URI here = URI.createFileURI(new File(".").getAbsolutePath());

  static URI pathURI(String relativePath) {
    return URI.createFileURI(relativePath).resolve(here);
  }

  public static void main(String[] args) throws Exception {

    if (args.length != 2) {
      System.err.println("Usage: run-eol EVIEW EOL");
      System.exit(1);
    }

    URI viewPath = pathURI(args[0]);
    URI programPath = pathURI(args[1]);

    // Init Ecore packages that may be used by viewpoints
    EcorePackage.eINSTANCE.eClass();
    VirtualLinksPackage.eINSTANCE.eClass();
    JavaPackage.eINSTANCE.eClass();

    // Register model file extensions to be opened as EMF models
    Map<String, Object> ext2Fact = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();

    ext2Fact.put("ecore", new EcoreResourceFactoryImpl());
    ext2Fact.put("xmi", new XMIResourceFactoryImpl());
    Resource.Factory epsRF = new Resource.Factory() {
      @Override
      public Resource createResource(URI uri) {
        return new EpsilonResource(uri);
      }
    };
    ext2Fact.put("csv", epsRF);
    ext2Fact.put("bib", epsRF);
    ext2Fact.put("eview", new EmfViewsFactory());
    ext2Fact.put("eviewpoint", new EmfViewsFactory());

    // Register NeoEMF persistence backend and protocol to enable NeoEMF resource
    // loading.
    Resource.Factory.Registry.INSTANCE.getProtocolToFactoryMap().put(
      BlueprintsURI.SCHEME, PersistentResourceFactory.getInstance());
    PersistenceBackendFactoryRegistry.register(BlueprintsURI.SCHEME,
      BlueprintsPersistenceBackendFactory.getInstance());


    // Parse EOL program
    EolModule module = new EolModule();
    module.parse(new File(programPath.toFileString()));
    if (module.getParseProblems().size() > 0) {
      System.err.println("Parse errors occured...");
      for (ParseProblem problem : module.getParseProblems()) {
        System.err.println(problem.toString());
      }
      throw new Exception("Error in parsing ECL file.  See stderr for details");
    }
    module.getContext().setOperationFactory(new EolOperationFactory());

    // Add view using the EMF Views EMC
    EMFViewsModel m = new EMFViewsModel();
    m.setName("VIEW");
    m.setModelFileUri(viewPath);
    m.load();
    module.getContext().getModelRepository().addModel(m);

    // Execute EOL
    module.execute();
  }
}
