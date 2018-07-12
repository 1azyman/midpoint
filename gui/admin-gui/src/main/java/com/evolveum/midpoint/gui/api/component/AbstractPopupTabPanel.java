/*
 * Copyright (c) 2010-2018 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.gui.api.component;

import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by honchar
 */
public abstract class AbstractPopupTabPanel<O extends ObjectType> extends BasePanel<O> {
    private static final long serialVersionUID = 1L;

    private static final String ID_OBJECT_LIST_PANEL = "objectListPanel";

    protected ObjectTypes type;
    protected List<O> preSelectedObjects = new ArrayList<>();

    public AbstractPopupTabPanel(String id, ObjectTypes type){
        super(id);
        this.type = type;
    }

    @Override
    protected void onInitialize(){
        super.onInitialize();
        add(initObjectListPanel());
        initParametersPanel();
    }

    private PopupObjectListPanel initObjectListPanel(){
        PopupObjectListPanel<O> listPanel = new PopupObjectListPanel<O>(ID_OBJECT_LIST_PANEL, (Class)type.getClassDefinition(),
                null, true, getPageBase(), getPreselectedObjects()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdateCheckbox(AjaxRequestTarget target) {
                getPreselectedObjects().clear();
                getPreselectedObjects().addAll(getSelectedObjectsList());

                onSelectionPerformed(target);
            }

            @Override
            protected IModel<Boolean> getCheckBoxEnableModel(IModel<SelectableBean<O>> rowModel){
                return getObjectSelectCheckBoxEnableModel(rowModel);
            }

            @Override
            protected ObjectQuery addFilterToContentQuery(ObjectQuery query) {
                return AbstractPopupTabPanel.this.addFilterToContentQuery(query);
            }

        };
        listPanel.add(new VisibleEnableBehaviour(){
            private static final long serialVersionUID = 1L;

            public boolean isVisible(){
                return isObjectListPanelVisible();
            }
        });
        listPanel.setOutputMarkupId(true);
        return listPanel;
    }

    protected abstract void initParametersPanel();

    protected List<O> getPreselectedObjects(){
        return preSelectedObjects;
    }

    protected List<O> getSelectedObjectsList(){
        PopupObjectListPanel objectListPanel = getObjectListPanel();
        if (objectListPanel == null){
            return new ArrayList();
        }
        return objectListPanel.getSelectedObjects();
    }

    protected PopupObjectListPanel getObjectListPanel(){
        return (PopupObjectListPanel)get(ID_OBJECT_LIST_PANEL);
    }

    protected void onSelectionPerformed(AjaxRequestTarget target){}

    protected IModel<Boolean> getObjectSelectCheckBoxEnableModel(IModel<SelectableBean<O>> rowModel){
        return Model.of(true);
    }

    protected ObjectQuery addFilterToContentQuery(ObjectQuery query){
        return query;
    }

    protected boolean isObjectListPanelVisible(){
        return true;
    }

    public ObjectTypes getType() {
        return type;
    }
}
