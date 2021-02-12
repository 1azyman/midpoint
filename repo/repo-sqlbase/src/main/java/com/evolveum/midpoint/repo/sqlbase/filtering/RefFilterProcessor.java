/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.repo.sqlbase.filtering;

import com.querydsl.core.types.Predicate;

import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.repo.sqlbase.QueryException;
import com.evolveum.midpoint.repo.sqlbase.SqlQueryContext;

public class RefFilterProcessor implements FilterProcessor<RefFilter> {

    private final SqlQueryContext<?, ?, ?> context;

    public RefFilterProcessor(SqlQueryContext<?, ?, ?> context) {
        this.context = context;
    }

    @Override
    public Predicate process(RefFilter filter) throws QueryException {
        ItemPath filterPath = filter.getPath();
        ItemName itemName = filterPath.firstName();
        // TODO: value count and other attributes checks?
        if (!filterPath.isSingleName()) {
//            context.mapping().relationMapping(itemName); // TODO this way
            throw new QueryException("Filter with non-single path is not supported YET: " + filterPath);
        }
        if (filter.getRightHandSidePath() != null) {
            throw new QueryException("Filter with right-hand-side path is not supported YET: " + filterPath);
        }

        FilterProcessor<RefFilter> filterProcessor =
                context.createItemFilterProcessor(itemName);
        return filterProcessor.process(filter);
    }
}
