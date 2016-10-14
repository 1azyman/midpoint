/*
 * Copyright (c) 2010-2016 Evolveum
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
package com.evolveum.midpoint.schema.util;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathSegment;
import com.evolveum.midpoint.prism.path.NameItemPathSegment;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.ResourceShadowDiscriminator;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.processor.*;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Methods that would belong to the ResourceObjectShadowType class but cannot go there
 * because of JAXB.
 * 
 * @author Radovan Semancik
 */
public class ShadowUtil {
	
	public static Collection<ResourceAttribute<?>> getPrimaryIdentifiers(ShadowType shadowType) {
		return getPrimaryIdentifiers(shadowType.asPrismObject());
	}
	
	// TODO: rename to getPrimaryIdentifiers
	public static Collection<ResourceAttribute<?>> getPrimaryIdentifiers(PrismObject<? extends ShadowType> shadow) {
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		if (attributesContainer == null) {
			return null;
		}
		return attributesContainer.getPrimaryIdentifiers();	
	}
	
	public static Collection<ResourceAttribute<?>> getSecondaryIdentifiers(ShadowType shadowType) {
		return getSecondaryIdentifiers(shadowType.asPrismObject());
	}
	
	public static Collection<ResourceAttribute<?>> getSecondaryIdentifiers(PrismObject<? extends ShadowType> shadow) {
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		if (attributesContainer == null) {
			return null;
		}
		return attributesContainer.getSecondaryIdentifiers();	
	}
	
	public static ResourceAttribute<String> getSecondaryIdentifier(PrismObject<? extends ShadowType> shadow) throws SchemaException {
		Collection<ResourceAttribute<?>> secondaryIdentifiers = getSecondaryIdentifiers(shadow);
		if (secondaryIdentifiers == null || secondaryIdentifiers.isEmpty()) {
			return null;
		}
		if (secondaryIdentifiers.size() > 1) {
			throw new SchemaException("Too many secondary identifiers in "+shadow+": "+secondaryIdentifiers);
		}
		return (ResourceAttribute<String>) secondaryIdentifiers.iterator().next();
	}
	
	public static Collection<ResourceAttribute<?>> getSecondaryIdentifiers(Collection<? extends ResourceAttribute<?>> identifiers, ObjectClassComplexTypeDefinition objectClassDefinition) throws SchemaException {
		if (identifiers == null) {
			return null;
		}
		Collection<ResourceAttribute<?>> secondaryIdentifiers = new ArrayList<>();
		for (ResourceAttribute<?> identifier: identifiers) {
			if (objectClassDefinition.isSecondaryIdentifier(identifier.getElementName())) {
				secondaryIdentifiers.add(identifier);
			}
		}
		return secondaryIdentifiers;
	}
	
	public static String getSecondaryIdentifierRealValue(PrismObject<? extends ShadowType> shadow) throws SchemaException {
		ResourceAttribute<String> secondaryIdentifier = getSecondaryIdentifier(shadow);
		if (secondaryIdentifier == null) {
			return null;
		}
		return secondaryIdentifier.getRealValue();
	}
	
	public static ResourceAttribute<?> getSecondaryIdentifier(ObjectClassComplexTypeDefinition objectClassDefinition, 
			Collection<? extends ResourceAttribute<?>> identifiers) throws SchemaException {
		if (identifiers == null) {
			return null;
		}
		ResourceAttribute<?> secondaryIdentifier = null;
		for (ResourceAttribute<?> identifier: identifiers) {
			if (identifier.getDefinition().isSecondaryIdentifier(objectClassDefinition)) {
				if (secondaryIdentifier != null) {
					throw new SchemaException("More than one secondary identifier in "+objectClassDefinition);
				}
				secondaryIdentifier = identifier;
			}
		}
		return secondaryIdentifier;
	}
	
	public static Collection<ResourceAttribute<?>> getAllIdentifiers(PrismObject<? extends ShadowType> shadow) {
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		if (attributesContainer == null) {
			return null;
		}
		return attributesContainer.getAllIdentifiers();	
	}
	
	public static Collection<ResourceAttribute<?>> getAllIdentifiers(ShadowType shadow) {
		return getAllIdentifiers(shadow.asPrismObject());
	}
	
	public static ResourceAttribute<String> getNamingAttribute(ShadowType shadow){
		return getNamingAttribute(shadow.asPrismObject());
	}
	
	public static ResourceAttribute<String> getNamingAttribute(PrismObject<? extends ShadowType> shadow) {
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		if (attributesContainer == null) {
			return null;
		}
		return attributesContainer.getNamingAttribute();	
	}
	
	public static Collection<ResourceAttribute<?>> getAttributes(ShadowType shadowType) {
		return getAttributes(shadowType.asPrismObject());
	}
	
	public static Collection<ResourceAttribute<?>> getAttributes(PrismObject<? extends ShadowType> shadow) {
		return getAttributesContainer(shadow).getAttributes();	
	}
	
	public static <T> ResourceAttribute<T> getAttribute(PrismObject<? extends ShadowType> shadow, QName attrName) {
		return getAttributesContainer(shadow).findAttribute(attrName);	
	}
	
	public static ResourceAttributeContainer getAttributesContainer(ShadowType shadowType) {
		return getAttributesContainer(shadowType.asPrismObject());
	}
	
	public static ResourceAttributeContainer getAttributesContainer(PrismObject<? extends ShadowType> shadow) {
		return getAttributesContainer(shadow, ShadowType.F_ATTRIBUTES);
	}
	
	public static ResourceAttributeContainer getAttributesContainer(PrismObject<? extends ShadowType> shadow, QName containerName) {
		return getAttributesContainer(shadow.getValue(), containerName);
	}
	
	public static ResourceAttributeContainer getAttributesContainer(PrismContainerValue<?> cval, QName containerName) {
		PrismContainer attributesContainer = cval.findContainer(containerName);
		if (attributesContainer == null) {
			return null;
		}
		if (attributesContainer instanceof ResourceAttributeContainer) {
			return (ResourceAttributeContainer)attributesContainer;
		} else {
			throw new SystemException("Expected that <"+containerName.getLocalPart()+"> will be ResourceAttributeContainer but it is "+attributesContainer.getClass());
		}
	}
	
	public static ResourceAttributeContainer getOrCreateAttributesContainer(PrismObject<? extends ShadowType> shadow, 
			ObjectClassComplexTypeDefinition objectClassDefinition) {
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		if (attributesContainer != null) {
			return attributesContainer;
		}
		ResourceAttributeContainer emptyContainer = ResourceAttributeContainer.createEmptyContainer(ShadowType.F_ATTRIBUTES, objectClassDefinition);
		try {
			shadow.add(emptyContainer);
		} catch (SchemaException e) {
			throw new SystemException("Unexpected schema error: "+e.getMessage(), e);
		}
		return emptyContainer;
	}
	
	public static ObjectClassComplexTypeDefinition getObjectClassDefinition(ShadowType shadow) {
		// TODO: maybe we can do something more intelligent here
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		return attributesContainer.getDefinition().getComplexTypeDefinition();
	}
	
	public static ObjectClassComplexTypeDefinition getObjectClassDefinition(PrismObject<? extends ShadowType> shadow) {
		// TODO: maybe we can do something more intelligent here
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		return attributesContainer.getDefinition().getComplexTypeDefinition();
	}

	
	public static String getResourceOid(ShadowType shadowType) {
		return getResourceOid(shadowType.asPrismObject());
	}
	
	public static String getResourceOid(PrismObject<ShadowType> shadow) {
		PrismReference resourceRef = shadow.findReference(ShadowType.F_RESOURCE_REF);
		if (resourceRef == null) {
			return null;
		}
		return resourceRef.getOid();
	}

	public static PolyString getResourceName(ShadowType shadowType) {
		return getResourceName(shadowType.asPrismObject());
	}

	public static PolyString getResourceName(PrismObject<ShadowType> shadow) {
		PrismReference resourceRef = shadow.findReference(ShadowType.F_RESOURCE_REF);
		if (resourceRef == null) {
			return null;
		}
		return resourceRef.getTargetName();
	}

	public static String getSingleStringAttributeValue(ShadowType shadow, QName attrName) {
		return getSingleStringAttributeValue(shadow.asPrismObject(), attrName);
	}

	public static String getSingleStringAttributeValue(PrismObject<ShadowType> shadow, QName attrName) {
		PrismContainer<?> attributesContainer = shadow.findContainer(ShadowType.F_ATTRIBUTES);
		if (attributesContainer == null) {
			return null;
		}
		PrismProperty<String> attribute = attributesContainer.findProperty(attrName);
		if (attribute == null) {
			return null;
		}
		return attribute.getRealValue();
	}
	
	public static String getMultiStringAttributeValueAsSingle(ShadowType shadow, QName attrName) {
		return getMultiStringAttributeValueAsSingle(shadow.asPrismObject(), attrName);
	}
	

	private static String getMultiStringAttributeValueAsSingle(PrismObject<ShadowType> shadow, QName attrName) {
		PrismContainer<?> attributesContainer = shadow.findContainer(ShadowType.F_ATTRIBUTES);
		if (attributesContainer == null) {
			return null;
		}
		PrismProperty<String> attribute = attributesContainer.findProperty(attrName);
		if (attribute == null) {
			return null;
		}
		Collection<String> realValues = attribute.getRealValues();
		if (realValues == null || realValues.isEmpty()) {
			return null;
		}
		if (realValues.size() > 1) {
			throw new IllegalStateException("More than one value in attribute "+attrName);
		}
		return realValues.iterator().next();
	}

	public static <T> List<T> getAttributeValues(ShadowType shadowType, QName attrName) {
		return getAttributeValues(shadowType.asPrismObject(), attrName);
	}
	
	public static <T> List<T> getAttributeValues(PrismObject<? extends ShadowType> shadow, QName attrName) {
		PrismContainer<?> attributesContainer = shadow.findContainer(ShadowType.F_ATTRIBUTES);
		if (attributesContainer == null || attributesContainer.isEmpty()) {
			return null;
		}
		PrismProperty<T> attr = attributesContainer.findProperty(attrName);
		if (attr == null) {
			return null;
		}
		List<T> values = new ArrayList<T>();
		for (PrismPropertyValue<T> pval : attr.getValues()) {
			values.add(pval.getValue());
		}
		if (values.isEmpty()) {
			return null;
		}
		return values;
	}
	
	public static <T> T getAttributeValue(ShadowType shadowType, QName attrName) throws SchemaException {
		return (T) getAttributeValue(shadowType.asPrismObject(), attrName);
	}
	
	public static <T> T getAttributeValue(PrismObject<? extends ShadowType> shadow, QName attrName) throws SchemaException {
		Collection<T> values = getAttributeValues(shadow, attrName);
		if (values == null || values.isEmpty()) {
			return null;
		}
		if (values.size() > 1) {
			throw new SchemaException("Attempt to get single value from multi-valued attribute "+attrName);
		}
		return values.iterator().next();
	}

	public static void setPassword(ShadowType shadowType, ProtectedStringType password) {
		CredentialsType credentialsType = shadowType.getCredentials();
		if (credentialsType == null) {
			credentialsType = new CredentialsType();
			shadowType.setCredentials(credentialsType);
		}
		PasswordType passwordType = credentialsType.getPassword();
		if (passwordType == null) {
			passwordType = new PasswordType();
			credentialsType.setPassword(passwordType);
		}
		passwordType.setValue(password);
	}

	public static ActivationType getOrCreateActivation(ShadowType shadowType) {
		ActivationType activation = shadowType.getActivation();
		if (activation == null) {
			activation = new ActivationType();
			shadowType.setActivation(activation);
		}
		return activation;
	}
	
    /**
     * This is not supposed to be used in production code! It is just for the tests.
     */
	public static void applyResourceSchema(PrismObject<? extends ShadowType> shadow,
			ResourceSchema resourceSchema) throws SchemaException {
		ShadowType shadowType = shadow.asObjectable();
		QName objectClass = shadowType.getObjectClass();
    	ObjectClassComplexTypeDefinition objectClassDefinition = resourceSchema.findObjectClassDefinition(objectClass);
    	applyObjectClass(shadow, objectClassDefinition);
	}
	
	private static void applyObjectClass(PrismObject<? extends ShadowType> shadow, 
			ObjectClassComplexTypeDefinition objectClassDefinition) throws SchemaException {
		PrismContainer<?> attributesContainer = shadow.findContainer(ShadowType.F_ATTRIBUTES);
		ResourceAttributeContainerDefinition racDef = new ResourceAttributeContainerDefinitionImpl(ShadowType.F_ATTRIBUTES,
				objectClassDefinition, objectClassDefinition.getPrismContext());
		attributesContainer.applyDefinition((PrismContainerDefinition) racDef, true);
	}
	
	public static PrismObjectDefinition<ShadowType> applyObjectClass(PrismObjectDefinition<ShadowType> shadowDefinition, 
			ObjectClassComplexTypeDefinition objectClassDefinition) throws SchemaException {
		PrismObjectDefinition<ShadowType> shadowDefClone = shadowDefinition.cloneWithReplacedDefinition(ShadowType.F_ATTRIBUTES,
				objectClassDefinition.toResourceAttributeContainerDefinition());
		return shadowDefClone;
	}

	/**
	 * Returns intent from the shadow. Backwards compatible with older accountType. May also adjust for default
	 * intent if necessary.
	 */
	public static String getIntent(ShadowType shadow) {
		if (shadow == null) {
			return null;
		}
		String intent = shadow.getIntent();
		if (intent != null) {
			return intent;
		}
		return null;
	}
	
	public static ShadowKindType getKind(ShadowType shadow) {
		if (shadow == null) {
			return null;
		}
		ShadowKindType kind = shadow.getKind();
		if (kind != null) {
			return kind;
		}
		return ShadowKindType.ACCOUNT;
	}


	public static <T> Collection<T> getAttributeValues(ShadowType shadow, QName attributeQname, Class<T> type) {
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		if (attributesContainer == null) {
			return null;
		}
		ResourceAttribute<T> attribute = attributesContainer.findAttribute(attributeQname);
		if (attribute == null) {
			return null;
		}
		return attribute.getRealValues(type);
	}

	public static QName getAttributeName(ItemPath attributePath, String message) throws SchemaException {
		if (attributePath == null || attributePath.isEmpty()) {
			return null;
		}
		ItemPathSegment firstPathSegment = attributePath.first();
    	if (!(firstPathSegment instanceof NameItemPathSegment)) {
    		throw new SchemaException(message + ": first path segment is not a name segment");
    	}
    	if (!QNameUtil.match(ShadowType.F_ATTRIBUTES, ((NameItemPathSegment) firstPathSegment).getName())) {
    		throw new SchemaException(message + ": first path segment is not "+ShadowType.F_ATTRIBUTES);
    	}
    	if (attributePath.size() < 1) {
    		throw new SchemaException(message + ": path too short ("+attributePath.size()+" segments)");
    	}
    	if (attributePath.size() > 2) {
    		throw new SchemaException(message + ": path too long ("+attributePath.size()+" segments)");
    	}
    	ItemPathSegment secondPathSegment = attributePath.getSegments().get(1);
    	if (!(secondPathSegment instanceof NameItemPathSegment)) {
    		throw new SchemaException(message + ": second path segment is not a name segment");
    	}
    	return ((NameItemPathSegment) secondPathSegment).getName();
	}
	
	public static void checkConsistence(PrismObject<? extends ShadowType> shadow, String desc) {
		PrismReference resourceRef = shadow.findReference(ShadowType.F_RESOURCE_REF);
    	if (resourceRef == null) {
    		throw new IllegalStateException("No resourceRef in "+shadow+" in "+desc);
    	}
    	if (StringUtils.isBlank(resourceRef.getOid())) {
    		throw new IllegalStateException("Null or empty OID in resourceRef in "+desc);
    	}
		ShadowType shadowType = shadow.asObjectable();
    	if (shadowType.getObjectClass() == null) {
    		throw new IllegalStateException("Null objectClass in "+desc);
    	}
    	PrismContainer<ShadowAttributesType> attributesContainer = shadow.findContainer(ShadowType.F_ATTRIBUTES);
    	if (attributesContainer != null) {
    		if (!(attributesContainer instanceof ResourceAttributeContainer)) {
    			throw new IllegalStateException("The attributes element expected to be ResourceAttributeContainer but it is "
    					+attributesContainer.getClass()+" instead in "+desc);
    		}
    		checkConsistency(attributesContainer.getDefinition(), " container definition in "+desc); 
    	}
    	
    	PrismContainerDefinition<ShadowAttributesType> attributesDefinition = 
    			shadow.getDefinition().findContainerDefinition(ShadowType.F_ATTRIBUTES);
    	checkConsistency(attributesDefinition, " object definition in "+desc);
	}
	
	public static void checkConsistency(PrismContainerDefinition<ShadowAttributesType> attributesDefinition, String desc) {
		if (attributesDefinition == null) {
    		throw new IllegalStateException("No definition for <attributes> in "+desc);
    	}
    	if (!(attributesDefinition instanceof ResourceAttributeContainerDefinition)) {
    		throw new IllegalStateException("The attributes element definition expected to be ResourceAttributeContainerDefinition but it is "
					+attributesDefinition.getClass()+" instead in "+desc);
    	}
	}

    // TODO is this correct?
    public static boolean isAccount(ShadowType shadowType) {
        if (shadowType.getKind() != null) {
            return shadowType.getKind() == ShadowKindType.ACCOUNT;
        } else {
            return true;        // ???
        }
    }
    
    public static boolean isProtected(PrismObject<? extends ShadowType> shadow) {
    	if (shadow == null) {
    		return false;
    	}
    	
    	ShadowType shadowType = shadow.asObjectable();
    	Boolean protectedObject = shadowType.isProtectedObject();
    	return (protectedObject != null && protectedObject);
    }
    
    public static boolean isDead(ShadowType shadow){
    	return shadow.isDead() != null && shadow.isDead();
    }

	public static boolean matches(ShadowType shadowType, String resourceOid, ShadowKindType kind, String intent) {
		if (shadowType == null) {
			return false;
		}
		if (!resourceOid.equals(shadowType.getResourceRef().getOid())) {
			return false;
		}
		if (!MiscUtil.equals(kind, shadowType.getKind())) {
			return false;
		}
		if (intent == null) {
			return true;
		}
		return MiscUtil.equals(intent, shadowType.getIntent());
	}

	public static boolean matches(PrismObject<ShadowType> shadow, ResourceShadowDiscriminator discr) {
		return matches(shadow.asObjectable(), discr);
	}
	
	public static boolean matches(ShadowType shadowType, ResourceShadowDiscriminator discr) {
		if (shadowType == null) {
			return false;
		}
		if (!discr.getResourceOid().equals(shadowType.getResourceRef().getOid())) {
			return false;
		}
		if (!MiscUtil.equals(discr.getKind(), shadowType.getKind())) {
			return false;
		}
		return ResourceShadowDiscriminator.equalsIntent(shadowType.getIntent(), discr.getIntent());
	}

	
	
	public static String getHumanReadableName(PrismObject<? extends ShadowType> shadow) {
		if (shadow == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		ShadowType shadowType = shadow.asObjectable();
		ShadowKindType kind = shadowType.getKind();
		if (kind != null) {
			sb.append(kind).append(" ");
		}
		sb.append("shadow ");
		boolean first = true;
		for(ResourceAttribute iattr: getPrimaryIdentifiers(shadow)) {
			if (first) {
				sb.append("[");
				first  = false;
			} else {
				sb.append(",");
			}
			sb.append(iattr.getElementName().getLocalPart());
			sb.append("=");
			sb.append(iattr.getRealValue());
		}
		if (first) {
			sb.append("[");
		}
		sb.append("]");
		return shadow.toString();
	}
	
	public static String getHumanReadableName(ShadowType shadowType) {
		if (shadowType == null) {
			return "null";
		}
		return getHumanReadableName(shadowType.asPrismObject());
	}

	public static boolean isFullShadow(PrismObject<ShadowType> shadow) {
		ShadowType shadowType = shadow.asObjectable();
		if (shadowType.getCachingMetadata() == null) {
			return false;
		}
		return shadowType.getCachingMetadata().getRetrievalTimestamp() != null;
	}

	public static <T extends ShadowType> PolyString determineShadowName(ShadowType shadow)
			throws SchemaException {
		return determineShadowName(shadow.asPrismObject());
	}

	public static <T extends ShadowType> PolyString determineShadowName(PrismObject<T> shadow)
			throws SchemaException {
		String stringName = determineShadowStringName(shadow);
		if (stringName == null) {
			return null;
		}
		return new PolyString(stringName);
	}

	public static <T extends ShadowType> String determineShadowStringName(PrismObject<T> shadow)
			throws SchemaException {
		ResourceAttributeContainer attributesContainer = getAttributesContainer(shadow);
		if (attributesContainer == null) {
			return null;
		}
		ResourceAttribute<String> namingAttribute = attributesContainer.getNamingAttribute();
		if (namingAttribute == null || namingAttribute.isEmpty()) {
			// No naming attribute defined. Try to fall back to identifiers.
			Collection<ResourceAttribute<?>> identifiers = attributesContainer.getPrimaryIdentifiers();
			// We can use only single identifiers (not composite)
			if (identifiers.size() == 1) {
				PrismProperty<?> identifier = identifiers.iterator().next();
				// Only single-valued identifiers
				Collection<PrismPropertyValue<?>> values = (Collection) identifier.getValues();
				if (values.size() == 1) {
					PrismPropertyValue<?> value = values.iterator().next();
					// and only strings
					if (value.getValue() instanceof String) {
						return (String) value.getValue();
					}
				}
			} else {
				return attributesContainer.findAttribute(SchemaConstants.ICFS_NAME).getValue(String.class).getValue();
			}
			// Identifier is not usable as name
			// TODO: better identification of a problem
			throw new SchemaException("No naming attribute defined (and identifier not usable)");
		}
		// TODO: Error handling
		List<PrismPropertyValue<String>> possibleValues = namingAttribute.getValues();

		if (possibleValues.size() > 1) {
			throw new SchemaException(
					"Cannot determine name of shadow. Found more than one value for naming attribute (attr: "
							+ namingAttribute.getElementName() + ", values: {}" + possibleValues + ")");
		}

		PrismPropertyValue<String> value = possibleValues.iterator().next();

		if (value == null) {
			throw new SchemaException("Naming attribute has no value. Could not determine shadow name.");
		}

		return value.getValue();
		// return
		// attributesContainer.getNamingAttribute().getValue().getValue();
	}

	public static ResourceObjectIdentification getResourceObjectIdentification(
			PrismObject<ShadowType> shadow, ObjectClassComplexTypeDefinition objectClassDefinition) {
		return new ResourceObjectIdentification(objectClassDefinition, 
				ShadowUtil.getPrimaryIdentifiers(shadow), ShadowUtil.getSecondaryIdentifiers(shadow));
	}

	public static boolean matchesAttribute(ItemPath path, QName attributeName) {
		return (ShadowType.F_ATTRIBUTES.equals(ItemPath.getFirstName(path)) && 
				QNameUtil.match(ItemPath.getFirstName(path.rest()), attributeName));
	}

	public static boolean hasPrimaryIdentifier(Collection<? extends ResourceAttribute<?>> identifiers,
			ObjectClassComplexTypeDefinition objectClassDefinition) {
		for (ResourceAttribute identifier: identifiers) {
			if (objectClassDefinition.isPrimaryIdentifier(identifier.getElementName())) {
				return true;
			}
		}
		return false;
	}

	public static ResourceAttribute<?> fixAttributePath(ResourceAttribute<?> attribute) throws SchemaException {
		if (attribute == null) {
			return null;
		}
		if (ShadowType.F_ATTRIBUTES.equals(ItemPath.getFirstName(attribute.getPath()))) {
			return attribute;
		}
		ResourceAttribute<?> fixedAttribute = attribute.clone();
		ResourceAttributeContainer container = new ResourceAttributeContainer(ShadowType.F_ATTRIBUTES, null, attribute.getPrismContext());
		container.createNewValue().add(fixedAttribute);
		return fixedAttribute;
	}

}
