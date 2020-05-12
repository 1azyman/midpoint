package com.evolveum.axiom.lang.impl;

import com.evolveum.axiom.concepts.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.evolveum.axiom.api.AxiomIdentifier;
import com.evolveum.axiom.lang.api.AxiomBuiltIn;
import com.evolveum.axiom.lang.api.stmt.AxiomStatement;
import com.evolveum.axiom.lang.impl.AxiomStatementImpl.Factory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class AxiomStatementBuilder<T> implements Lazy.Supplier<AxiomStatement<T>> {

    private final AxiomIdentifier identifier;
    private final AxiomStatementImpl.Factory<T, ? extends AxiomStatement<T>> factory;
    private T value;

    public AxiomStatementBuilder(AxiomIdentifier identifier, Factory<T, ? extends AxiomStatement<T>> factory) {
        this.identifier = identifier;
        this.factory = factory;
    }

    public AxiomStatementBuilder(AxiomIdentifier identifier) {
        this(identifier, AxiomStatementImpl.factory());
    }


    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    private List<Supplier<? extends AxiomStatement<?>>> childList = new ArrayList<>();
    private Multimap<AxiomIdentifier, Supplier<? extends AxiomStatement<?>>> children = HashMultimap.create();

    public void add(AxiomIdentifier type, Supplier<? extends AxiomStatement<?>> axiomStatement) {
        childList.add(axiomStatement);
        children.put(type, axiomStatement);
    }

    @Override
    public AxiomStatement<T> get() {
        return factory.create(identifier, value, buildChildList(), buildChildMap());
    }

    private Multimap<AxiomIdentifier, AxiomStatement<?>> buildChildMap() {
        return Multimaps.transformValues(children, (v) -> v.get());
    }

    private List<AxiomStatement<?>> buildChildList() {
        return Lists.transform(childList, (v) -> v.get());
    }
}
