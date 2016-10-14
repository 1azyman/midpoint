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

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.marshaller.JaxbDomHack;
import com.evolveum.midpoint.prism.marshaller.PrismBeanConverter;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessor;
import com.evolveum.midpoint.prism.lex.dom.DomLexicalProcessor;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyStringNormalizer;
import com.evolveum.midpoint.prism.schema.SchemaDefinitionFactory;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.util.PrismMonitor;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author semancik
 * @author mederly
 */
public interface PrismContext {

	String LANG_XML = "xml";
	String LANG_JSON = "json";
	String LANG_YAML = "yaml";

	void initialize() throws SchemaException, SAXException, IOException;

	XmlEntityResolver getEntityResolver();

	SchemaRegistry getSchemaRegistry();

	XNodeProcessor getXnodeProcessor();

	DomLexicalProcessor getParserDom();

	PrismBeanConverter getBeanConverter();

	JaxbDomHack getJaxbDomHack();

	SchemaDefinitionFactory getDefinitionFactory();

	PolyStringNormalizer getDefaultPolyStringNormalizer();

	Protector getDefaultProtector();

	PrismMonitor getMonitor();

	void setMonitor(PrismMonitor monitor);

	//region Parsing
	/**
	 * Creates a parser ready to process the given file.
	 * @param file File to be parsed.
	 * @return Parser that can be invoked to retrieve the (parsed) content of the file.
	 */
	@NotNull
	PrismParser parserFor(@NotNull File file);

	/**
	 * Creates a parser ready to process data from the given input stream.
	 * @param stream Input stream to be parsed.
	 * @return Parser that can be invoked to retrieve the (parsed) content of the input stream.
	 */
	@NotNull
	PrismParser parserFor(@NotNull InputStream stream);

	/**
	 * Creates a parser ready to process data from the given string.
	 * @param data String with the data to be parsed. It has be in UTF-8 encoding.
	 *             (For other encodings please use InputStream or File source.)
	 * @return Parser that can be invoked to retrieve the (parsed) content.
	 */
	@NotNull
	PrismParserNoIO parserFor(@NotNull String data);

	/**
	 * Creates a parser ready to process data from the given DOM element.
	 * @param element Element with the data to be parsed.
	 * @return Parser that can be invoked to retrieve the (parsed) content.
	 */
	@NotNull
	PrismParserNoIO parserFor(@NotNull Element element);

	@Deprecated	// user parserFor + parse instead
	<T extends Objectable> PrismObject<T> parseObject(File file) throws SchemaException, IOException;

	@Deprecated	// user parserFor + parse instead
	<T extends Objectable> PrismObject<T> parseObject(String dataString) throws SchemaException;
	//endregion

	//region Adopt methods
	<T extends Objectable> void adopt(PrismObject<T> object, Class<T> declaredType) throws SchemaException;

	<T extends Objectable> void adopt(PrismObject<T> object) throws SchemaException;

	void adopt(Objectable objectable) throws SchemaException;

	void adopt(Containerable containerable) throws SchemaException;

	void adopt(PrismContainerValue value) throws SchemaException;

	<T extends Objectable> void adopt(ObjectDelta<T> delta) throws SchemaException;

	<C extends Containerable, O extends Objectable> void adopt(C containerable, Class<O> type, ItemPath path) throws SchemaException;

	<C extends Containerable, O extends Objectable> void adopt(PrismContainerValue<C> prismContainerValue, Class<O> type,
			ItemPath path) throws SchemaException;

	<C extends Containerable, O extends Objectable> void adopt(PrismContainerValue<C> prismContainerValue, QName typeName,
			ItemPath path) throws SchemaException;
	//endregion

	//region Serializing
	/**
	 * Creates a serializer for the given language.
	 * @param language Language (like xml, json, yaml).
	 * @return The serializer.
	 */
	@NotNull
	PrismSerializer<String> serializerFor(@NotNull String language);

	/**
	 * Creates a serializer for XML language.
	 * @return The serializer.
	 */
	@NotNull
	PrismSerializer<String> xmlSerializer();

	/**
	 * Creates a serializer for JSON language.
	 * @return The serializer.
	 */
	@NotNull
	PrismSerializer<String> jsonSerializer();

	/**
	 * Creates a serializer for YAML language.
	 * @return The serializer.
	 */
	@NotNull
	PrismSerializer<String> yamlSerializer();

	/**
	 * Creates a serializer for DOM. The difference from XML serializer is that XML produces String output
	 * whereas this one produces a DOM Element.
	 * @return The serializer.
	 */
	@NotNull
	PrismSerializer<Element> domSerializer();

	/**
	 * Creates a serializer for XNode. The output of this serializer is intermediate XNode representation.
	 * @return The serializer.
	 */
	@NotNull
	PrismSerializer<XNode> xnodeSerializer();

	@Deprecated // use serializerFor + serialize instead
	<O extends Objectable> String serializeObjectToString(PrismObject<O> object, String language) throws SchemaException;

	/**
	 * TODO
	 * @param value
	 * @return
	 */
	boolean canSerialize(Object value);
	//endregion

	/**
	 * TODO
	 * @param item
	 * @return
	 * @throws SchemaException
	 */
	RawType toRawType(Item item) throws SchemaException;

	/**
	 * Creates a new PrismObject of a given static type.
	 * @param clazz Static type of the object to be created.
	 * @return New PrismObject.
	 * @throws SchemaException If a definition for the given class couldn't be found.
	 */
	@NotNull
	<O extends Objectable> PrismObject<O> createObject(@NotNull Class<O> clazz) throws SchemaException;

	/**
	 * Creates a new Objectable of a given static type.
	 * @param clazz Static type of the object to be created.
	 * @return New PrismObject's objectable content.
	 * @throws SchemaException If a definition for the given class couldn't be found.
	 */
	@NotNull
	<O extends Objectable> O createObjectable(@NotNull Class<O> clazz) throws SchemaException;
}
