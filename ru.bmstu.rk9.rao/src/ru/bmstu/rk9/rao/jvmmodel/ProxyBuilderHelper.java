package ru.bmstu.rk9.rao.jvmmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * @author algor
 *
 */
public class ProxyBuilderHelper {
	private final EObject sourceElement;
	private final boolean targetClassStatic;
	private EObject jvmClassElement;
	private final JvmTypesBuilder jvmTypesBuilder;
	private final IJvmModelAssociations associations;
	private final JvmTypeReferenceBuilder jvmTypeReferenceBuilder;
	private boolean useHiddenFieldsName = false;
	private final ProxyBuilderHelperUtil util;
	/**
	 * if client doesn't decide to associate a builder - this value will be left
	 * null
	 */
	private JvmGenericType builderClass = null;
	/**
	 * Used to remember constructor of a builded class to later reuse its parameters
	 * in code generation. Could also be fetched from associations but I'm lazy and
	 * that could be more error-prone.
	 */
	private JvmConstructor constructorOfBuildedClass = null;
	private JvmGenericType buildedClass = null;

	/**
	 * sometimes we need to setup final fields lazily - in constructor. this field
	 * is for that purpose
	 */
	private final GeneratedCodeBodyBuilder codeToAppendToParentScopeConstructor;
	private final List<JvmMember> additionalMembersToParentScope;
	private final List<JvmMember> additionalMembersToParentInitializingScope;

	public ProxyBuilderHelper(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
			IJvmModelAssociations associations, EObject sourceElement, boolean isStatic) {
		this.sourceElement = sourceElement;
		this.targetClassStatic = isStatic;
		this.associations = associations;
		this.jvmTypesBuilder = jvmTypesBuilder;
		this.jvmTypeReferenceBuilder = jvmTypeReferenceBuilder;
		this.util = new ProxyBuilderHelperUtil(jvmTypesBuilder, targetClassStatic);
		this.codeToAppendToParentScopeConstructor = new GeneratedCodeBodyBuilder();
		this.additionalMembersToParentScope = new ArrayList<>();
		this.additionalMembersToParentInitializingScope = new ArrayList<>();
	}

	/**
	 * 
	 * @param givenParams paramaters that client wants to add to the created
	 *                    constructor
	 * @return constructor that accepts and initializes fields for both given
	 *         parameters and paramaters that this builder creates
	 */
	public JvmConstructor createConstructorForBuildedClass(boolean addSimulatorId, JvmFormalParameter... givenParams) {
		return createConstructorForBuildedClass(addSimulatorId, null, givenParams);
	}
	
	public JvmConstructor createConstructorForBuildedClass(boolean addSimulatorId,
			StringConcatenationClient additionalCode, 
			JvmFormalParameter... givenParams) {
		JvmConstructor createdCostructor = jvmTypesBuilder.toConstructor(sourceElement, p -> {
			if (addSimulatorId) {
				p.getParameters().add(SimulatorIdCodeUtil.createSimulatorIdParameter(jvmTypesBuilder,
						jvmTypeReferenceBuilder, sourceElement));
			}

			for (JvmFormalParameter param : givenParams) {
				p.getParameters().add(param);
			}

			jvmTypesBuilder.setBody(p, util.createConstructorBody(p, useHiddenFieldsName, additionalCode));
		});

		constructorOfBuildedClass = createdCostructor;
		return createdCostructor;
	}
	public JvmConstructor createConstructorForBuildedClass(JvmFormalParameter... givenParams) {
		return createConstructorForBuildedClass(true, givenParams);
	}
	
	public JvmConstructor createConstructorForBuildedClass(StringConcatenationClient additionalCode, JvmFormalParameter... givenParams) {
		return createConstructorForBuildedClass(true, additionalCode, givenParams);
	}
	
	/**
	 * 
	 * @param givenParams paramaters that client wants to add to the created
	 *                    constructor
	 * @return constructor that accepts and initializes fields for both given
	 *         parameters and paramaters that this builder creates
	 */
	public JvmConstructor createSimulatorIdConstructorForBuildedClass() {
		return SimulatorIdCodeUtil.createSimulatorIdConstructor(jvmTypesBuilder, jvmTypeReferenceBuilder,
				sourceElement, useHiddenFieldsName);
	}

	/**
	 * 
	 * @param givenParams
	 * @return list of fields that consist both of given parameters and parameters
	 *         that this builder creates
	 */
	public List<JvmField> createFieldsForBuildedClass(JvmFormalParameter... givenParams) {
		final String prefix;
		if (useHiddenFieldsName) {
			prefix = GeneratedCodeContract.HIDDEN_FIELD_NAME_PREFIX;
		} else {
			prefix = "";
		}
		List<JvmField> s = Arrays.asList(givenParams).stream().map(it -> {
			return jvmTypesBuilder.toField(sourceElement, prefix + it.getName(), it.getParameterType());
		}).collect(Collectors.toList());

		ArrayList<JvmField> toRet = new ArrayList<JvmField>();
		toRet.addAll(s);
//		toRet.add(SimulatorIdCodeUtil.createSimulatorIdField(jvmTypesBuilder, jvmTypeReferenceBuilder, sourceElement));
		return toRet;
	}

	public List<JvmMember> createNecessaryMembersForBuildedClass() {
		return Arrays.asList(
				SimulatorIdCodeUtil.createSimulatorIdField(jvmTypesBuilder, jvmTypeReferenceBuilder, sourceElement, useHiddenFieldsName),
				SimulatorIdCodeUtil.createSimulatorIdGetter(jvmTypesBuilder, jvmTypeReferenceBuilder, sourceElement, useHiddenFieldsName));
	}

	/**
	 * method must set up only additional methods
	 * 
	 * @param builderInitializer sets up any additional features of builder class
	 * @return
	 */
	public JvmGenericType associateBuilderClass(
			Procedure2<ProxyBuilderFeatures, ? super JvmGenericType> builderInitializer) {
		if (buildedClass == null) {
			throw new IllegalStateException(
					"to assocate a builder class one must have a reference to the builded class");
		}
		this.builderClass = jvmTypesBuilder.toClass(sourceElement, getBuilderClassName(), jvmGenericType -> {
			List<JvmMember> members = jvmGenericType.getMembers();

			// default constructor consisting solely of simulatorid
			members.add(CodeGenerationUtil.associateConstructor(jvmTypesBuilder, sourceElement,
					getBuilderClassConstructorParameters(), "", null));
			members.add(SimulatorIdCodeUtil.createSimulatorIdField(jvmTypesBuilder, jvmTypeReferenceBuilder,
					sourceElement));
			members.add(SimulatorIdCodeUtil.createSimulatorIdGetter(jvmTypesBuilder, jvmTypeReferenceBuilder,
					sourceElement));

			// adds additional features to the created class of builder
			builderInitializer.apply(createFeatures(), jvmGenericType);
		});

		return builderClass;
	}

	public String getPrivateParameterName(String parameterName) {
		if (useHiddenFieldsName) {
			return GeneratedCodeContract.HIDDEN_FIELD_NAME_PREFIX + parameterName;
		} else {
			return parameterName;
		}
	}
	
	public List<JvmFormalParameter> getBuilderClassConstructorParameters() {
		return Arrays.asList(SimulatorIdCodeUtil.createSimulatorIdParameter(jvmTypesBuilder, jvmTypeReferenceBuilder,
				sourceElement));
	}

	public void addCodeForParentScopeConstructor(StringConcatenationClient code) {
		this.codeToAppendToParentScopeConstructor.append(code);
	}

	public void addAdditionalParentScopeMembers(JvmMember... newMember) {
		this.additionalMembersToParentScope.addAll(Arrays.asList(newMember));
	}

	public void addAdditionalParentInitializingScopeMembers(JvmMember... newMember) {
		this.additionalMembersToParentInitializingScope.addAll(Arrays.asList(newMember));
	}

	public List<JvmMember> getAdditionalParentScopeMembers() {
		return this.additionalMembersToParentScope;
	}

	public List<JvmMember> getAdditionalMembersToParentInitializingScope() {
		return additionalMembersToParentInitializingScope;
	}

	public GeneratedCodeBodyBuilder getCodeToAppendToParentScopeConstructor() {
		return this.codeToAppendToParentScopeConstructor;
	}

	public void setBuildedClass(JvmGenericType buildedClass) {
		this.buildedClass = buildedClass;
	}

	public String getBuilderVariableName() {
		String buildedClassName = buildedClass.getSimpleName();
		return buildedClassName + BUILDER_FIELD_SUFFIX;
	}

	public String getBuilderClassName() {
		return createBuilderName(buildedClass.getSimpleName());
	}

	public String createInitializationLineForBuilderVariable() {
		String builderVariableName = getBuilderVariableName();
		String builderClassName = getBuilderClassName();

		return ProxyBuilderHelperUtil.createLineOfBuilderFieldInitialization(builderVariableName, builderClassName);
	}

	public boolean hasBuilderClass() {
		return builderClass != null;
	}

	public List<JvmMember> collectAdditionalMembers() {
		List<JvmMember> collected = new ArrayList<>();
		collected.addAll(additionalMembersToParentScope);
		collected.add(builderClass);
		return collected;
	}

	/**
	 * 
	 * @param parentSourceObject should be a more common object (e.g. class of a
	 *                           model) but that is not a necessary requirement
	 * @return field that should be added as a member to the class associated with
	 *         parent source element
	 */
	public JvmField addFieldForBuilderVariable(EObject parentSourceObject) {
		return jvmTypesBuilder.toField(sourceElement, getBuilderVariableName(),
				jvmTypeReferenceBuilder.typeRef(builderClass), (jvmField) -> {
					jvmField.setFinal(true);
					jvmField.setVisibility(JvmVisibility.PUBLIC);
					jvmField.setStatic(false);
				});
	}

	/**
	 * Must be run after all related functions are added to <members> of the
	 * sourceClass that this helper is related to
	 * 
	 * @param namesOfFunctionsToProxy names of functions that should be delegated
	 *                                from builder class to the original one
	 */
	@Deprecated /*
				 * this type of methods must accept body, that must be embedded into structure
				 * that this helper creates
				 */
	public void rememberFunctionsToProxy(String... namesOfFunctionsToProxy) {
		// must get all operations currently associated with the object
		List<String> names = Arrays.asList(namesOfFunctionsToProxy);

		List<JvmOperation> operations = associations.getJvmElements(sourceElement).stream().filter(jvmEl -> {
			return (jvmEl instanceof JvmOperation);
		}).filter(jvmEl -> {
			String simpleName = ((JvmOperation) jvmEl).getSimpleName();
			return names.contains(simpleName);
		}).map(JvmOperation.class::cast).collect(Collectors.toList());

//		delegateOperations.clear();
//		delegateOperations.addAll(operations);
	}

	// TODO create field for proxifying of source class
	// TODO create initialization lines for the proxifyment field and fields that
	// are required by source
	// classes to be in model scope (like ResourceDeclaration), so remember them and
	// then initialize in
	// the constructor of the model

	public EObject getJvmClassElement() {
		return jvmClassElement;
	}

	public void setJvmClassElement(EObject jvmClassElement) {
		this.jvmClassElement = jvmClassElement;
	}

	public EObject getSourceElement() {
		return sourceElement;
	}

	public JvmGenericType getBuilderClass() {
		return builderClass;
	}

	//
//	public boolean isTargetClassStatic() {
//		return targetClassStatic;
//	}
//	
	public ProxyBuilderFeatures createFeatures() {
		return new ProxyBuilderFeaturesImpl();
	}
	
	public void setUseHiddenFieldName(boolean use) {
		this.useHiddenFieldsName = use;
	}

	public static String createBuilderName(String buildedClassName) {
		return buildedClassName + BUILDER_SUFFIX;
	}

	/**
	 * this string will be appended to the generated builder class name
	 */
	public static String BUILDER_SUFFIX = "Builder";
	/**
	 * this string will be appended to the generated builder class e.g.
	 * ResourceNameField (if suffix is Field)
	 */
	public static String BUILDER_FIELD_SUFFIX = "";

	class ProxyBuilderFeaturesImpl implements ProxyBuilderFeatures {

		@Override
		public List<String> getBuildedClassParameters() {
			return constructorOfBuildedClass.getParameters().stream().map((param) -> {
				return param.getName();
			}).collect(Collectors.toList());
		}

		// @Override
		// public Field<Integer> getSimulatorIdField() {
		// return new SimulatorIdFieldImpl();
		// }

	}

	class SimulatorIdFieldImpl extends Field<Integer> {
		public SimulatorIdFieldImpl() {
			super("simulatorId", Integer.class);
		}
	}

	/**
	 * Java entity that is present within ProxyBuilder generated builder class scope
	 * 
	 * @param <T>
	 */
	abstract static class Entity<T> {
		private final String name;
		/**
		 * for Field - class of field for Method - class of returned type
		 */
		private final Class<T> clazz;

		public String getName() {
			return name;
		}

		public Class<T> getType() {
			return clazz;
		}

		public Entity(String name, Class<T> fieldClazz) {
			super();
			this.name = name;
			this.clazz = fieldClazz;
		}
	}

	static class Field<T> extends Entity<T> {
		public Field(String name, Class<T> fieldClazz) {
			super(name, fieldClazz);

		}
	}

	static class Method<T> extends Entity<T> {
		public Method(String name, Class<T> clazz) {
			super(name, clazz);
		}
	}

	interface ProxyBuilderFeatures {
		// Field<Integer> getSimulatorIdField();
		List<String> getBuildedClassParameters();
	}

	/**
	 * SAM to combine scopes of JVM eobject and this builder
	 * 
	 * @author aleks
	 *
	 */
	interface ProxyBuilderMethodScope {
		Procedure1<? super JvmOperation> apply(ProxyBuilderFeatures entities);
	}
}
