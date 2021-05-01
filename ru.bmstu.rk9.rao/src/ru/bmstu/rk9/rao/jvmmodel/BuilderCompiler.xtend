package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.rao.ResourceType
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmGenericType

public class BuilderCompiler extends RaoEntityCompiler {
	/**
	*	this string will be appended to the generated builder class name
	*/
	public static String BUILDER_SUFFIX = "Builder";
	/**
	 * this string will be appended to the generated builder class
	 * e.g. ResourceNameField (if suffix is Field)
	 */
	public static String BUILDER_FIELD_SUFFIX = "";

	/**
	* creates builder for passed [resourceType] 
	*/ 
	public def static JvmGenericType asBuilder(
		ResourceType resourceType,
		EObject raoModel,
		JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, 
		boolean isPreIndexingPhase) {

		val resourceClass = entitiesToClasses.get(resourceType.name);
		val builderClassName = createBuilderNameForResource(resourceType.name);
		
		val builderClass =  raoModel.toClass(builderClassName) [ builder |
			builder.static = true

			builder.members.addSimulatorIdField(raoModel);
			builder.members += createSimulatorIdConstructor(raoModel)
			builder.members += raoModel.toMethod("create", typeRef(resourceClass)) [
				visibility = JvmVisibility.PUBLIC
				for (param : resourceType.parameters)
					parameters += raoModel.toParameter(param.declaration.name, param.declaration.parameterType)
				body = '''
					«IF isSimulatorIdOn»entitiesToClasses» resource = new «resourceType.name»(«createEnumerationString(parameters, [name])»);
					«ENDIF»
						ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().addResource(resource);
						ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(resource,
							ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.CREATED);
						return resource;
				'''
			]

			builder.members.addSimulatorIdGetter(raoModel);
		]
		entitiesToClasses.put(builderClassName, builderClass)
		return builderClass;
	}

	public def static asBuilderField(
		ResourceType resourceType,
		 EObject context, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {

		val builderClassName = createBuilderNameForResource(resourceType.name);
		val builderClass = entitiesToClasses.get(builderClassName);
		
		return resourceType.toField(resourceType.name + BUILDER_FIELD_SUFFIX, typeRef(builderClass)) [
			visibility = JvmVisibility.PUBLIC
			final = true
		// Field must be initialized only once in constructor of the model, not here
//			initializer = '''
//				new «resourceType.name + BUILDER_SUFFIX»(«IF isSimulatorIdOn»this.«simulatorIdFieldName»«ELSE»«ENDIF»)
//			'''
		]
	}

	public def static String createBuilderNameForResource(String resourceName) {
		return resourceName + BUILDER_SUFFIX;
	}

	public def static StringConcatenationClient createLineOfBuilderFieldInitialization(String resourceName) {
		val builderName = createBuilderNameForResource(resourceName);

		return '''this.«resourceName + BUILDER_FIELD_SUFFIX»  = new «builderName»(«IF isSimulatorIdOn»this.«simulatorIdFieldName»«ELSE»«ENDIF»);'''
	}
}
