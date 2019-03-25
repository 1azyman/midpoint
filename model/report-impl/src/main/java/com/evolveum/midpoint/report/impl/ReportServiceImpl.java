/*
 * Copyright (c) 2010-2019 Evolveum
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
package com.evolveum.midpoint.report.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.common.LocalizationService;
import com.evolveum.midpoint.model.api.ModelAuthorizationAction;
import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.schema.SchemaHelper;
import com.evolveum.midpoint.security.enforcer.api.AuthorizationParameters;
import com.evolveum.midpoint.security.enforcer.api.SecurityEnforcer;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.evolveum.midpoint.audit.api.AuditEventRecord;
import com.evolveum.midpoint.audit.api.AuditService;
import com.evolveum.midpoint.repo.common.ObjectResolver;
import com.evolveum.midpoint.repo.common.expression.ExpressionFactory;
import com.evolveum.midpoint.repo.common.expression.ExpressionSyntaxException;
import com.evolveum.midpoint.repo.common.expression.ExpressionUtil;
import com.evolveum.midpoint.repo.common.expression.ExpressionVariables;
import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.model.common.expression.functions.FunctionLibrary;
import com.evolveum.midpoint.model.common.expression.script.ScriptExpression;
import com.evolveum.midpoint.model.common.expression.script.ScriptExpressionEvaluationContext;
import com.evolveum.midpoint.model.common.expression.script.ScriptExpressionFactory;
import com.evolveum.midpoint.model.common.expression.script.jsr223.Jsr223ScriptEvaluator;
import com.evolveum.midpoint.model.impl.expr.ExpressionEnvironment;
import com.evolveum.midpoint.model.impl.expr.ModelExpressionThreadLocalHolder;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.TypeFilter;
import com.evolveum.midpoint.report.api.ReportService;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.expression.ExpressionProfile;
import com.evolveum.midpoint.schema.expression.ScriptExpressionProfile;
import com.evolveum.midpoint.schema.expression.TypedValue;
import com.evolveum.midpoint.schema.expression.VariablesMap;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.MiscSchemaUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ScriptExpressionEvaluatorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;

@Component
public class ReportServiceImpl implements ReportService {

	private static final transient Trace LOGGER = TraceManager.getTrace(ReportServiceImpl.class);

	@Autowired private ModelService model;
	@Autowired private TaskManager taskManager;
	@Autowired private PrismContext prismContext;
	@Autowired private SchemaHelper schemaHelper;
	@Autowired private ExpressionFactory expressionFactory;
	@Autowired @Qualifier("modelObjectResolver") private ObjectResolver objectResolver;
	@Autowired private AuditService auditService;
	@Autowired private FunctionLibrary logFunctionLibrary;
	@Autowired private FunctionLibrary basicFunctionLibrary;
	@Autowired private FunctionLibrary midpointFunctionLibrary;
	@Autowired private LocalizationService localizationService;
	@Autowired private SecurityEnforcer securityEnforcer;
	@Autowired private ScriptExpressionFactory scriptExpressionFactory;
	
	private ExpressionProfile determineExpressionProfile() {
		// TODO TODO TODO TODO TODO
		return MiscSchemaUtil.getExpressionProfile();
	}
	
	private void setupExpressionProfiles(ScriptExpressionEvaluationContext context) {
		ExpressionProfile expressionProfile = determineExpressionProfile();
		context.setExpressionProfile(expressionProfile);

		// TODO TODO TODO TODO TODO
		ScriptExpressionProfile scriptExpressionProfile = null;
		context.setScriptExpressionProfile(scriptExpressionProfile);
	}

	@Override
	public ObjectQuery parseQuery(String query, VariablesMap parameters) throws SchemaException,
			ObjectNotFoundException, ExpressionEvaluationException, CommunicationException, ConfigurationException, SecurityViolationException {
		if (StringUtils.isBlank(query)) {
			return null;
		}

		
		ExpressionProfile expressionProfile = determineExpressionProfile();
		
		ObjectQuery parsedQuery;
		try {
			Task task = taskManager.createTaskInstance();
			ModelExpressionThreadLocalHolder.pushExpressionEnvironment(new ExpressionEnvironment<>(task, task.getResult()));
			SearchFilterType filter = prismContext.parserFor(query).parseRealValue(SearchFilterType.class);
			LOGGER.trace("filter {}", filter);
			ObjectFilter f = prismContext.getQueryConverter().parseFilter(filter, UserType.class);
			LOGGER.trace("f {}", f.debugDump());
			if (!(f instanceof TypeFilter)) {
				throw new IllegalArgumentException(
						"Defined query must contain type. Use 'type filter' in your report query.");
			}

			ObjectFilter subFilter = ((TypeFilter) f).getFilter();
			ObjectQuery q = prismContext.queryFactory().createQuery(subFilter);

			ExpressionVariables variables = new ExpressionVariables();
			variables.putAll(parameters);

			q = ExpressionUtil.evaluateQueryExpressions(q, variables, expressionProfile, expressionFactory, prismContext,
					"parsing expression values for report", task, task.getResult());
			((TypeFilter) f).setFilter(q.getFilter());
			parsedQuery = prismContext.queryFactory().createQuery(f);

			LOGGER.trace("report query:\n{}", parsedQuery.debugDumpLazily(1));
		} catch (SchemaException | ObjectNotFoundException | ExpressionEvaluationException | CommunicationException | ConfigurationException | SecurityViolationException e) {
			// TODO Auto-generated catch block
			throw e;
		} finally {
			ModelExpressionThreadLocalHolder.popExpressionEnvironment();
		}
		return parsedQuery;

	}

	@Override
	public Collection<PrismObject<? extends ObjectType>> searchObjects(ObjectQuery query,
			Collection<SelectorOptions<GetOperationOptions>> options) throws SchemaException,
			ObjectNotFoundException, SecurityViolationException, CommunicationException,
			ConfigurationException, ExpressionEvaluationException {
		// List<PrismObject<? extends ObjectType>> results = new ArrayList<>();

		// GetOperationOptions options = GetOperationOptions.createRaw();

		if (!(query.getFilter() instanceof TypeFilter)) {
			throw new IllegalArgumentException("Query must contain type filter.");
		}

		TypeFilter typeFilter = (TypeFilter) query.getFilter();
		QName type = typeFilter.getType();
		Class clazz = prismContext.getSchemaRegistry().determineCompileTimeClass(type);
		if (clazz == null) {
			clazz = prismContext.getSchemaRegistry().findObjectDefinitionByType(type).getCompileTimeClass();
		}

		ObjectQuery queryForSearch = prismContext.queryFactory().createQuery(typeFilter.getFilter());

		Task task = taskManager.createTaskInstance(ReportService.class.getName() + ".searchObjects()");
		OperationResult parentResult = task.getResult();

		// options.add(new
		// SelectorOptions(GetOperationOptions.createResolveNames()));
		GetOperationOptions getOptions = GetOperationOptions.createResolveNames();
		if (ShadowType.class.isAssignableFrom(clazz) && securityEnforcer.isAuthorized(ModelAuthorizationAction.RAW_OPERATION.getUrl(), null, AuthorizationParameters.EMPTY, null, task, parentResult)) {
			getOptions.setRaw(Boolean.TRUE);        // shadows in non-raw mode require specifying resource OID and kind (at least) - todo research this further
		} else {
			getOptions.setNoFetch(Boolean.TRUE);
		}
		options = SelectorOptions.createCollection(getOptions);
		List<PrismObject<? extends ObjectType>> results;
		try {
			results = model.searchObjects(clazz, queryForSearch, options, task, parentResult);
			return results;
		} catch (SchemaException | ObjectNotFoundException | SecurityViolationException
				| CommunicationException | ConfigurationException | ExpressionEvaluationException e) {
			// TODO Auto-generated catch block
			throw e;
		}

	}

	@Override
	public Collection<PrismContainerValue<? extends Containerable>> evaluateScript(String script,
			VariablesMap parameters) throws SchemaException, ExpressionEvaluationException,
			ObjectNotFoundException, CommunicationException, ConfigurationException, SecurityViolationException {
		List<PrismContainerValue<? extends Containerable>> results = new ArrayList<>();

		ExpressionVariables variables = new ExpressionVariables();
		variables.putAll(parameters);

		// special variable for audit report
		variables.put("auditParams", getConvertedParams(parameters));

		Task task = taskManager.createTaskInstance(ReportService.class.getName() + ".evaluateScript");

		ScriptExpressionEvaluationContext context = new ScriptExpressionEvaluationContext();
		context.setVariables(variables);
		context.setContextDescription("report script"); // TODO: improve
		context.setTask(task);
		context.setResult(task.getResult());
		setupExpressionProfiles(context);
		
		Object o = evaluateReportScript(script, context);

		if (o != null) {

			if (Collection.class.isAssignableFrom(o.getClass())) {
				Collection resultSet = (Collection) o;
				if (resultSet != null && !resultSet.isEmpty()) {
					for (Object obj : resultSet) {
						results.add(convertResultingObject(obj));
					}
				}

			} else {
				results.add(convertResultingObject(o));
			}
		}

		return results;
	}
	

	@Override
	public Object evaluate(String script, VariablesMap parameters) throws SchemaException, ExpressionEvaluationException,
			ObjectNotFoundException, CommunicationException, ConfigurationException, SecurityViolationException {

		ExpressionVariables variables = new ExpressionVariables();
		variables.addVariableDefinitions(parameters);

		// special variable for audit report
		variables.put("auditParams", getConvertedParams(parameters));

		Task task = taskManager.createTaskInstance(ReportService.class.getName() + ".evaluateScript");

		ScriptExpressionEvaluationContext context = new ScriptExpressionEvaluationContext();
		context.setVariables(variables);
		context.setContextDescription("report script"); // TODO: improve
		context.setTask(task);
		context.setResult(task.getResult());
		setupExpressionProfiles(context);

		Object o = evaluateReportScript(script, context);

		return o;

	}

	protected PrismContainerValue convertResultingObject(Object obj) {
		if (obj instanceof PrismObject) {
            return ((PrismObject) obj).asObjectable().asPrismContainerValue();
        } else if (obj instanceof Objectable) {
            return ((Objectable) obj).asPrismContainerValue();
        } else if (obj instanceof PrismContainerValue) {
            return (PrismContainerValue) obj;
        } else if (obj instanceof Containerable) {
            return ((Containerable) obj).asPrismContainerValue();
        } else {
            throw new IllegalStateException("Reporting script should return something compatible with PrismContainerValue, not a " + obj.getClass());
        }
	}

	public Collection<AuditEventRecord> evaluateAuditScript(String script, VariablesMap parameters)
			throws SchemaException, ExpressionEvaluationException, ObjectNotFoundException, CommunicationException, ConfigurationException, SecurityViolationException {
		Collection<AuditEventRecord> results = new ArrayList<>();

		ExpressionVariables variables = new ExpressionVariables();
			variables.put("auditParams", getConvertedParams(parameters));

		Task task = taskManager.createTaskInstance(ReportService.class.getName() + ".searchObjects()");

		ScriptExpressionEvaluationContext context = new ScriptExpressionEvaluationContext();
		context.setVariables(variables);
		
		context.setContextDescription("report script"); // TODO: improve
		context.setTask(task);
		context.setResult(task.getResult());
		setupExpressionProfiles(context);

		Object o = evaluateReportScript(script, context);

		if (o != null) {

			if (Collection.class.isAssignableFrom(o.getClass())) {
				Collection resultSet = (Collection) o;
				if (resultSet != null && !resultSet.isEmpty()) {
					for (Object obj : resultSet) {
						if (!(obj instanceof AuditEventRecord)) {
							LOGGER.warn("Skipping result, not an audit event record " + obj);
							continue;
						}
						results.add((AuditEventRecord) obj);
					}

				}

			} else {
				results.add((AuditEventRecord) o);
			}
		}

		return results;
	}

	private TypedValue<VariablesMap> getConvertedParams(VariablesMap parameters) {
		if (parameters == null) {
			return new TypedValue<>(null, VariablesMap.class);
		}

		VariablesMap resultParamMap = new VariablesMap();
		Set<Entry<String, TypedValue>> paramEntries = parameters.entrySet();
		for (Entry<String, TypedValue> e : paramEntries) {
			Object value = e.getValue().getValue();
			if (value instanceof PrismPropertyValue) {
				resultParamMap.put(e.getKey(), e.getValue().createTransformed(((PrismPropertyValue) value).getValue()));
			} else {
				resultParamMap.put(e.getKey(), e.getValue());
			}
		}

		return new TypedValue<>(resultParamMap, VariablesMap.class);
	}

	private Collection<FunctionLibrary> createFunctionLibraries() {
//		FunctionLibrary functionLib = ExpressionUtil.createBasicFunctionLibrary(prismContext,
//				prismContext.getDefaultProtector());
		FunctionLibrary midPointLib = new FunctionLibrary();
		midPointLib.setVariableName("report");
		midPointLib.setNamespace("http://midpoint.evolveum.com/xml/ns/public/function/report-3");
		ReportFunctions reportFunctions = new ReportFunctions(prismContext, schemaHelper, model, taskManager, auditService);
		midPointLib.setGenericFunctions(reportFunctions);
//
//		MidpointFunctionsImpl mp = new MidpointFunctionsImpl();
//		mp.

		Collection<FunctionLibrary> functions = new ArrayList<>();
		functions.add(basicFunctionLibrary);
		functions.add(logFunctionLibrary);
		functions.add(midpointFunctionLibrary);
		functions.add(midPointLib);
		return functions;
	}

	@Override
	public PrismContext getPrismContext() {
		return prismContext;
	}
	
	public <T> Object evaluateReportScript(String codeString, ScriptExpressionEvaluationContext context) throws ExpressionEvaluationException,
					ObjectNotFoundException, CommunicationException, ConfigurationException, SecurityViolationException, SchemaException {
		ScriptExpressionEvaluatorType expressionType = new ScriptExpressionEvaluatorType();
		expressionType.setCode(codeString);
		context.setExpressionType(expressionType);
		
		context.setFunctions(createFunctionLibraries());
		context.setObjectResolver(objectResolver);
		
		ScriptExpression scriptExpression = scriptExpressionFactory.createScriptExpression(
				expressionType, context.getOutputDefinition(), context.getExpressionProfile(), expressionFactory, context.getContextDescription(), context.getTask(), context.getResult());
		
		ModelExpressionThreadLocalHolder.pushExpressionEnvironment(new ExpressionEnvironment<>(context.getTask(), context.getResult()));
		List<PrismValue> expressionResult;
		try {
			expressionResult = scriptExpression.evaluate(context);
		} finally {
			ModelExpressionThreadLocalHolder.popExpressionEnvironment();
		}
		
		if (expressionResult == null || expressionResult.isEmpty()) {
			return null;
		}
		if (expressionResult.size() > 1) {
			throw new ExpressionEvaluationException("Too many results from expression "+context.getContextDescription());
		}
		return expressionResult.get(0).getRealValue();
	}
}
