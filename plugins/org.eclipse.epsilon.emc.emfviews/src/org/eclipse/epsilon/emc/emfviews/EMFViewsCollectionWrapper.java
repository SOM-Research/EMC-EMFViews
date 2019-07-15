package org.eclipse.epsilon.emc.emfviews;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.epsilon.eol.execute.operations.AbstractOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.IAbstractOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.declarative.SelectOneOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.SelectOperation;

public class EMFViewsCollectionWrapper implements Collection<EObject>, IAbstractOperationContributor {

//	private IModel model;

	private Collection baseCollection;

	public EMFViewsCollectionWrapper(Collection<?> baseCollection) {
//		this.model = model;
		this.baseCollection = baseCollection;
	}

	public Collection getBaseCollection() {
		return baseCollection;
	}

	@Override
	public boolean add(EObject e) {
		return baseCollection.add(e);
	}

	@Override
	public boolean addAll(Collection c) {
		return baseCollection.addAll(c);
	}

	@Override
	public void clear() {
		baseCollection.clear();
	}

	@Override
	public boolean contains(Object o) {
		return baseCollection.contains(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		return baseCollection.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return baseCollection.isEmpty();
	}

	@Override
	public Iterator iterator() {
		return baseCollection.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return baseCollection.remove(o);
	}

	@Override
	public boolean removeAll(Collection c) {
		return baseCollection.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		return baseCollection.retainAll(c);
	}

	@Override
	public int size() {
		return baseCollection.size();
	}

	@Override
	public Object[] toArray() {
		return baseCollection.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return baseCollection.toArray(a);
	}

	@Override
	public AbstractOperation getAbstractOperation(String name) {
	  if("collect".equals(name)) {
	    return new EMFViewsDelegateCollectOperation();
	  }
	  else if ("select".equals(name)) {
	    return new SelectOperation();
	  }
	  else if ("selectOne".equals(name)) {
	    return new SelectOneOperation();
	  }
	  throw new UnsupportedOperationException();
	}

}
