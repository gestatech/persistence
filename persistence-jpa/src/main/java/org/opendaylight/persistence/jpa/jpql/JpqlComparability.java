/**
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.persistence.jpa.jpql;

import javax.annotation.Nonnull;
import javax.persistence.Query;
import javax.persistence.metamodel.SingularAttribute;

import org.opendaylight.persistence.util.common.filter.ComparabilityCondition;

import com.google.common.base.Preconditions;

/**
 * Comparability condition predicate.
 * 
 * @param <P>
 *            type of the entity type of the entity (an object annotated with {@link javax.persistence.Entity})
 * @param <D>
 *            type of the attribute
 * @author Fabiel Zuniga
 * @author Nachiket Abhyankar
 */
class JpqlComparability<P, D extends Comparable<D>> implements JpqlPredicate {

    private Class<P> entityClass;
    private ComparabilityCondition<D> condition;
    private SingularAttribute<? super P, D> attribute;
    private String valueParameter;

    /**
     * Creates a comparability predicate.
     * 
     * @param condition
     *            comparability condition
     * @param attribute
     *            attribute to apply the condition to
     * @param entityClass
     *            persistent object class
     */
    public JpqlComparability(ComparabilityCondition<D> condition,
            SingularAttribute<? super P, D> attribute, Class<P> entityClass) {
        this(condition, attribute, entityClass, attribute.getName() + "Value");
    }

    /**
     * Creates a comparability predicate.
     * 
     * @param condition
     *            comparability condition
     * @param attribute
     *            attribute to apply the condition to
     * @param entityClass
     *            persistent object class
     * @param valueParameter
     *            name to use for the value parameter
     */
    public JpqlComparability(@Nonnull ComparabilityCondition<D> condition,
            @Nonnull SingularAttribute<? super P, D> attribute,
            @Nonnull Class<P> entityClass, String valueParameter) {
        this.condition = Preconditions.checkNotNull(condition, "condition");
        this.attribute = Preconditions.checkNotNull(attribute, "attribute");
        this.entityClass = Preconditions.checkNotNull(entityClass,
                "entityClass");
        this.valueParameter = valueParameter;
    }

    @Override
    public String getPredicate() {
        String attributeNameInQuery = JpqlUtil.getNameInQuery(this.attribute,
                this.entityClass);

        StringBuilder str = new StringBuilder(64);

        str.append(attributeNameInQuery);
        switch (this.condition.getMode()) {
            case LESS_THAN:
                str.append(" < ");
                break;
            case LESS_THAN_OR_EQUAL_TO:
                str.append(" <= ");
                break;
            case EQUAL:
                str.append(" = ");
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                str.append(" >= ");
                break;
            case GREATER_THAN:
                str.append(" > ");
                break;
        }
        str.append(JpqlUtil.getValueParameterInQuery(this.valueParameter));

        return str.toString();
    }

    @Override
    public void addParameters(Query query) {
        query.setParameter(this.valueParameter, this.condition.getValue());
    }
}
