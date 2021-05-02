package ru.bmstu.rk9.rao.jvmmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
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
	private JvmTypeReferenceBuilder jTRB;

	private final ProxyBuilderHelperUtil util;
	private final boolean createProxyBuilder;
	private final ArrayList<JvmOperation> delegateOperations = new ArrayList<JvmOperation>();

	public ProxyBuilderHelper(
			JvmTypesBuilder jvmTypesBuilder, 
			JvmTypeReferenceBuilder jTRB,
			IJvmModelAssociations associations, 
			EObject sourceElement, 
			boolean isStatic, 
			boolean createProxyBuilder) {
		this.sourceElement = sourceElement;
		this.targetClassStatic = isStatic;
		this.associations = associations;
		this.jvmTypesBuilder = jvmTypesBuilder;
		this.createProxyBuilder = createProxyBuilder;
		this.jTRB = jTRB;
		this.util = new ProxyBuilderHelperUtil(jvmTypesBuilder, targetClassStatic);
	}

	/**
	 * 
	 * @param givenParams paramaters that client wants to add to the created constructor
	 * @return constructor that accepts and initializes fields for both given parameters and paramaters that this
	 * 	builder creates
	 */
	public JvmConstructor createProxifiedClassConstructor(JvmFormalParameter... givenParams) {
		return jvmTypesBuilder.toConstructor(sourceElement, p -> {
			for (JvmFormalParameter param : givenParams) {
				p.getParameters().add(param);
			}

			if (isTargetClassStatic()) {
				p.getParameters().add(RaoEntityCompiler.createSimulatorIdParameter(sourceElement));
			}

			jvmTypesBuilder.setBody(p, util.createConstructorBody(p));
		});
	}

	/**
	 * 
	 * @param givenParams
	 * @return list of fields that consist both of given parameters and parameters that this builder creates
	 */
	public List<JvmField> createFields(JvmFormalParameter... givenParams) {
		List<JvmField> s = Arrays.asList(givenParams).stream().map(it -> {
			return jvmTypesBuilder.toField(sourceElement, it.getName(), it.getParameterType());
		}).collect(Collectors.toList());

		ArrayList<JvmField> toRet = new ArrayList<JvmField>();
		toRet.addAll(s);
		if (isTargetClassStatic()) {
			toRet.add(RaoEntityCompiler.createSimulatorIdField(sourceElement));
		}
		return toRet;
	}

	/**
	 * Must be run after all related functions are added to <members> of the
	 * sourceClass that this helper is related to
	 * 
	 * @param namesOfFunctionsToProxy names of functions that should be delegated
	 *                                from builder class to the original one
	 */
	@Deprecated /*this type of methods must accept body, that must be embedded into structure that this helper creates */
	public void rememberFunctionsToProxy(String... namesOfFunctionsToProxy) {
		// must get all operations currently associated with the object
		List<String> names = Arrays.asList(namesOfFunctionsToProxy);

		List<JvmOperation> operations = associations.getJvmElements(sourceElement).stream().filter(jvmEl -> {
			return (jvmEl instanceof JvmOperation);
		}).filter(jvmEl -> {
			String simpleName = ((JvmOperation) jvmEl).getSimpleName();
			return names.contains(simpleName);
		}).map(jv -> ((JvmOperation) jv)).collect(Collectors.toList());

//		delegateOperations.clear();
//		delegateOperations.addAll(operations);
	}
	
	/**
	 * must here associate builder class with a new method that is defined by a client
	 * @param sourceElement element that is being mapped to java domain
	 * @param name - name of created method
	 * @param returnType - what type the method must return
 	 * @param initializer Procedure that accepts not only the created jvmoperation but 
	 */
	public void addDelegatedBuilderMethod(/* @Nullable */ String name, /* @Nullable */ JvmTypeReference returnType,
			// 
			/* @Nullable */ Procedure2<ProxyBuilderEntities, ? super JvmOperation> methodScope) {
		
		EObject sourceElement = getSourceElement();
		
		Procedure1<? super JvmOperation> initializer = jvmOperation -> {
			methodScope.apply(createEntities(), jvmOperation);
		};
		
		JvmOperation result = typesFactory.createJvmOperation();
		result.setSimpleName(name);
		result.setVisibility(JvmVisibility.PUBLIC);
		result.setReturnType(cloneWithProxies(returnType));
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

	public boolean isTargetClassStatic() {
		return targetClassStatic;
	}
	
	public ProxyBuilderEntities createEntities() {
		return new ProxyBuilderEntitiesImpl();
	}
	
	
	class ProxyBuilderEntitiesImpl implements ProxyBuilderEntities {

		@Override
		public Field<Integer> getSimulatorIdField() {
			return new SimulatorIdFieldImpl();
		}
		
	}
	
	class SimulatorIdFieldImpl extends Field<Integer> {
		public SimulatorIdFieldImpl() {
			super("simulatorId", Integer.class);
		}
	}
	 
	
	/**
	 * Java entity that is present within ProxyBuilder generated builder class scope
	 * @param <T>
	 */
	abstract static class Entity<T> {
		private final String name;
		/**
		 * for Field - class of field
		 * for Method - class of returned type
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
	
	interface ProxyBuilderEntities {
		Field<Integer> getSimulatorIdField();
	}
	
	/**
	 * SAM to combine scopes of JVM eobject and this builder
	 * @author aleks
	 *
	 */
	interface ProxyBuilderMethodScope {
		 Procedure1<? super JvmOperation> apply(ProxyBuilderEntities entities);
	}
}
