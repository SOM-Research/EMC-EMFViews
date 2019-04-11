package org.eclipse.epsilon.emc.emfviews;

import java.util.Collection;
import java.util.List;

import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.operations.AbstractOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.CollectOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.IAbstractOperationContributor;

public class EMFViewsDelegateCollectOperation extends CollectOperation {

	@Override
	public Object execute(Object target, NameExpression operationNameExpression, List<Parameter> iterators, List<Expression> expressions, IEolContext context) throws EolRuntimeException {
		EMFViewsCollectionWrapper wrapper = (EMFViewsCollectionWrapper) target;
		Collection<Object> baseCollection = wrapper.getBaseCollection();
		if(baseCollection instanceof IAbstractOperationContributor) {
			AbstractOperation op = ((IAbstractOperationContributor) baseCollection).getAbstractOperation(operationNameExpression.getName());
			Object o = op.execute(target, operationNameExpression, iterators, expressions, context);
			return new EMFViewsCollectionWrapper(baseCollection);
		} else {
			// is it enough or do we need any additional processing here?
			return super.execute(baseCollection, operationNameExpression, iterators, expressions, context);
		}
	}

}
