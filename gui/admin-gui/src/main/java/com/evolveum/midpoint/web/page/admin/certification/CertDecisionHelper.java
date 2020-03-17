/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.page.admin.certification;

import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.web.component.data.column.IconColumn;
import com.evolveum.midpoint.web.component.data.column.LinkColumn;
import com.evolveum.midpoint.web.component.data.column.LinkPanel;
import com.evolveum.midpoint.web.page.admin.certification.dto.CertCaseOrWorkItemDto;
import com.evolveum.midpoint.web.page.admin.certification.dto.CertWorkItemDto;
import com.evolveum.midpoint.web.page.admin.certification.dto.SearchingUtils;
import com.evolveum.midpoint.web.page.admin.certification.handlers.CertGuiHandler;
import com.evolveum.midpoint.web.page.admin.certification.handlers.CertGuiHandlerRegistry;
import com.evolveum.midpoint.web.util.ObjectTypeGuiDescriptor;
import com.evolveum.midpoint.web.util.TooltipBehavior;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.List;

import static com.evolveum.midpoint.gui.api.util.WebComponentUtil.dispatchToObjectDetailsPage;

/**
 * Some common functionality used from PageCertCampaign and PageCertDecisions.
 * TODO finish the refactoring
 *
 * @author mederly
 */
public class CertDecisionHelper implements Serializable {

    public enum WhichObject {
        OBJECT, TARGET
    }

    <T extends CertCaseOrWorkItemDto> IColumn<T, String> createTypeColumn(final WhichObject which, final PageBase page) {
        IColumn column;
        column = new IconColumn<CertCaseOrWorkItemDto>(page.createStringResource("")) {
            @Override
            protected IModel<String> createIconModel(IModel<CertCaseOrWorkItemDto> rowModel) {
                ObjectTypeGuiDescriptor guiDescriptor = getObjectTypeDescriptor(which, rowModel);
                String icon = guiDescriptor != null ? guiDescriptor.getBlackIcon() : ObjectTypeGuiDescriptor.ERROR_ICON;
                return new Model<>(icon);
            }

            private ObjectTypeGuiDescriptor getObjectTypeDescriptor(WhichObject which, IModel<CertCaseOrWorkItemDto> rowModel) {
                QName targetType = rowModel.getObject().getObjectType(which);
                return ObjectTypeGuiDescriptor.getDescriptor(ObjectTypes.getObjectTypeFromTypeQName(targetType));
            }

            @Override
            public void populateItem(Item<ICellPopulator<CertCaseOrWorkItemDto>> item, String componentId, IModel<CertCaseOrWorkItemDto> rowModel) {
                super.populateItem(item, componentId, rowModel);
                ObjectTypeGuiDescriptor guiDescriptor = getObjectTypeDescriptor(which, rowModel);
                if (guiDescriptor != null) {
                    item.add(AttributeModifier.replace("title", page.createStringResource(guiDescriptor.getLocalizationKey())));
                    item.add(new TooltipBehavior());
                }
            }
        };
        return column;
    }

    <T extends CertCaseOrWorkItemDto> IColumn<T, String> createObjectNameColumn(final PageBase page, final String headerKey) {
        IColumn column;
        column = new LinkColumn<CertCaseOrWorkItemDto>(page.createStringResource(headerKey),
                SearchingUtils.OBJECT_NAME, CertCaseOrWorkItemDto.F_OBJECT_NAME) {

            @Override
            public void onClick(AjaxRequestTarget target, IModel<CertCaseOrWorkItemDto> rowModel) {
                CertCaseOrWorkItemDto dto = rowModel.getObject();
                dispatchToObjectDetailsPage(dto.getCertCase().getObjectRef(), page, false);
            }
        };
        return column;
    }

    <T extends CertCaseOrWorkItemDto> IColumn<T, String> createTargetNameColumn(final PageBase page, final String headerKey) {
        IColumn column;
        column = new LinkColumn<CertCaseOrWorkItemDto>(page.createStringResource(headerKey),
                SearchingUtils.TARGET_NAME, CertCaseOrWorkItemDto.F_TARGET_NAME) {

            @Override
            public void onClick(AjaxRequestTarget target, IModel<CertCaseOrWorkItemDto> rowModel) {
                CertCaseOrWorkItemDto dto = rowModel.getObject();
                dispatchToObjectDetailsPage(dto.getCertCase().getTargetRef(), page, false);
            }
        };
        return column;
    }

    public <T extends CertCaseOrWorkItemDto> IColumn<T, String> createReviewerNameColumn(final PageBase page, final String headerKey) {
        IColumn column;
        column = new AbstractColumn<T, String>(page.createStringResource(headerKey)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId,
                                     final IModel<T> rowModel) {
                CertCaseOrWorkItemDto dto = rowModel.getObject();
                RepeatingView reviewersPanel = new RepeatingView(componentId);
                if (dto instanceof CertWorkItemDto) {
                    List<ObjectReferenceType> reviewersList = ((CertWorkItemDto) dto).getReviewerRefList();
                    if (CollectionUtils.isNotEmpty(reviewersList)){
                        for (ObjectReferenceType reviewer : reviewersList){
                            reviewersPanel.add(new LinkPanel(reviewersPanel.newChildId(),
                                    Model.of(WebComponentUtil.getDisplayNameOrName(reviewer))) {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void onClick(AjaxRequestTarget target) {
                                    dispatchToObjectDetailsPage(reviewer, page, false);
                                }
                            });
                        }
                    }
                }
                cellItem.add(reviewersPanel);
            }

        };
        return column;
    }

    <T extends CertCaseOrWorkItemDto> IColumn<T, String> createConflictingNameColumn(final PageBase page, final String headerKey) {
        return new PropertyColumn<>(page.createStringResource(headerKey), CertCaseOrWorkItemDto.F_CONFLICTING_TARGETS);
    }

    <T extends CertCaseOrWorkItemDto> IColumn<T, String> createDetailedInfoColumn(final PageBase page) {
        IColumn column;
        column = new IconColumn<CertCaseOrWorkItemDto>(page.createStringResource("")) {

            @Override
            protected IModel<String> createIconModel(final IModel<CertCaseOrWorkItemDto> rowModel) {
                return new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return "fa fa-fw fa-info-circle text-info";
                    }
                };
            }

            @Override
            public void populateItem(Item<ICellPopulator<CertCaseOrWorkItemDto>> item, String componentId, IModel<CertCaseOrWorkItemDto> rowModel) {
                super.populateItem(item, componentId, rowModel);
                CertCaseOrWorkItemDto aCase = rowModel.getObject();
                String handlerUri;
                if (aCase instanceof CertWorkItemDto) {
                    handlerUri = aCase.getHandlerUri();
                } else {
                    handlerUri = ((PageCertCampaign) page).getCampaignHandlerUri();
                }
                CertGuiHandler handler = CertGuiHandlerRegistry.instance().getHandler(handlerUri);
                if (handler != null) {
                    String title = handler.getCaseInfoButtonTitle(rowModel, page);
                    item.add(AttributeModifier.replace("title", title));
                    item.add(new TooltipBehavior());
                }
            }
        };
        return column;
    }

}
