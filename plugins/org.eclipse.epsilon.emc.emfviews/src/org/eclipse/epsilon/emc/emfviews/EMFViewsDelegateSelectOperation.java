package org.eclipse.epsilon.emc.emfviews;

import java.util.Collection;
import java.util.List;

import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.operations.declarative.SelectOperation;

public class EMFViewsDelegateSelectOperation extends SelectOperation {

  @Override
  public Object execute(Object target, NameExpression operationNameExpression,
                        List<Parameter> iterators, List<Expression> expressions,
                        IEolContext context) throws EolRuntimeException {
    EMFViewsCollectionWrapper wrapper = (EMFViewsCollectionWrapper) target;
    Collection<Object> baseCollection = wrapper.getBaseCollection();
    return super.execute(baseCollection, operationNameExpression, iterators, expressions, context);
  }

}
