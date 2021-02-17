/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.provisioning.api;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.AcknowledgementSink;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;

/**
 * TODO
 *
 * It is comparable on the sequential number.
 */
public interface SynchronizationEvent extends AcknowledgementSink, DebugDumpable, Comparable<SynchronizationEvent> {

    /**
     * BEWARE! Can be null for unfinished changes.
     */
    ResourceObjectShadowChangeDescription getChangeDescription();

    /**
     * Sequential number of this event.
     */
    int getSequentialNumber();

    /**
     * Value against which the events are to be ordered: events A and B having A.sequentialNumber
     * less than B.sequentialNumber must be processed in that order if their correlation value is the
     * same. (Which means that they refer to the same resource object.)
     */
    Object getCorrelationValue();

    /**
     * Is the event ready to be processed?
     *
     * TODO
     */
    boolean isComplete();

    /**
     * Is the event "empty", and therefore should be skipped?
     * This means no error has occurred, but simply there is nothing to do.
     * Like a deletion of already-deleted account.
     */
    boolean isNotApplicable();

    boolean isError();

    // TODO!!!
    String getErrorMessage();

    // TODO
    String getShadowOid();

    // TODO
    PrismObject<ShadowType> getShadowedObject();
}
