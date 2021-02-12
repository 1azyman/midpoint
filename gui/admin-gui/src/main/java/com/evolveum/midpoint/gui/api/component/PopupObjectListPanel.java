/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.gui.api.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.evolveum.midpoint.web.component.data.column.*;

import com.evolveum.midpoint.web.session.UserProfileStorage;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.repo.common.expression.ExpressionUtil;
import com.evolveum.midpoint.repo.common.expression.ExpressionVariables;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ExpressionConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.MiscSchemaUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItem;
import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.component.util.SelectableBeanImpl;
import com.evolveum.midpoint.web.component.util.SerializableSupplier;
import com.evolveum.midpoint.web.page.admin.server.dto.OperationResultStatusPresentationProperties;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ExpressionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

public abstract class PopupObjectListPanel<O extends ObjectType> extends ObjectListPanel<O> {
    private static final long serialVersionUID = 1L;

    private static final Trace LOGGER = TraceManager.getTrace(PopupObjectListPanel.class);

    private boolean multiselect;

    /**
     * @param defaultType specifies type of the object that will be selected by default
     */
    public PopupObjectListPanel(String id, Class<? extends O> defaultType, boolean multiselect) {
        this(id, defaultType, null, multiselect);
        this.multiselect = multiselect;
    }

    public PopupObjectListPanel(String id, Class<? extends O> defaultType, Collection<SelectorOptions<GetOperationOptions>> options,
                                boolean multiselect) {
        super(id, defaultType, options);
        this.multiselect = multiselect;
    }

    @Override
    protected IColumn<SelectableBean<O>, String> createCheckboxColumn() {
        if (isMultiselect()) {
            return new CheckBoxHeaderColumn<SelectableBean<O>>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdateRow(AjaxRequestTarget target, DataTable table, IModel<SelectableBean<O>> rowModel, IModel<Boolean> selected) {
                    super.onUpdateRow(target, table, rowModel, selected);
                    onUpdateCheckbox(target, rowModel);
                }

                @Override
                protected void onUpdateHeader(AjaxRequestTarget target, boolean selected, DataTable table) {
                    super.onUpdateHeader(target, selected, table);
                    onUpdateCheckbox(target, null);
                }

                @Override
                protected IModel<Boolean> getEnabled(IModel<SelectableBean<O>> rowModel) {
                    return PopupObjectListPanel.this.getCheckBoxEnableModel(rowModel);
                }
            };
        }
        return null;
    }

    @Override
    protected void objectDetailsPerformed(AjaxRequestTarget target, O object) {
        onSelectPerformed(target, object);
    }

    @Override
    protected boolean isObjectDetailsEnabled(IModel<SelectableBean<O>> rowModel) {
        return !isMultiselect();
    }

    private IModel<String> evaluateColumnExpression(Object object, ExpressionType expression) {
        Task task = getPageBase().createSimpleTask("evaluate column expression");
        try {
            ExpressionVariables expressionVariables = new ExpressionVariables();
            expressionVariables.put(ExpressionConstants.VAR_OBJECT, object, object.getClass());
            String stringValue = ExpressionUtil.evaluateStringExpression(expressionVariables, getPageBase().getPrismContext(), expression,
                    MiscSchemaUtil.getExpressionProfile(), getPageBase().getExpressionFactory(), "evaluate column expression",
                    task, task.getResult()).iterator().next();
            return Model.of(stringValue);
        } catch (SchemaException | ExpressionEvaluationException | ObjectNotFoundException | CommunicationException
                | ConfigurationException | SecurityViolationException e) {
            LOGGER.error("Couldn't execute expression for name column");
            OperationResult result = task.getResult();
            OperationResultStatusPresentationProperties props = OperationResultStatusPresentationProperties.parseOperationalResultStatus(result.getStatus());
            return getPageBase().createStringResource(props.getStatusLabelKey());
        }
    }

    @Override
    protected List<IColumn<SelectableBean<O>, String>> createDefaultColumns() {
        List<IColumn<SelectableBean<O>, String>> columns = new ArrayList<>();
        columns.addAll(ColumnUtils.getDefaultColumns(getType(), getPageBase()));
        return columns;
    }

    protected void onSelectPerformed(AjaxRequestTarget target, O object) {

    }

    @Override
    protected List<InlineMenuItem> createInlineMenu() {
        return null;
    }

    @Override
    protected void addCustomActions(@NotNull List<InlineMenuItem> actionsList, SerializableSupplier<Collection<? extends O>> objectsSupplier) {
    }

    protected void onUpdateCheckbox(AjaxRequestTarget target, IModel<SelectableBean<O>> rowModel) {

    }

    protected IModel<Boolean> getCheckBoxEnableModel(IModel<SelectableBean<O>> rowModel) {
        return Model.of(true);
    }

    protected String getStorageKey() {
        return null;
    }

    public boolean isMultiselect() {
        return multiselect;
    }

    @Override
    protected boolean enableSavePageSize() {
        return false;
    }

    @Override
    protected UserProfileStorage.TableId getTableId() {
        return null;
    }
}
