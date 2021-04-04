package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.rao.ResourceType
import java.util.Collection
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder

public class BuilderCompiler extends RaoEntityCompiler {
	public static String BUILDER_SUFFIX = "Builder";
	
	public def static asBuilder(ResourceType resourceType, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {

		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		val typeQualifiedName = QualifiedName.create(qualifiedName, resourceType.name)
		val clazz = resourceType.toClass(typeQualifiedName)
		
		return resourceType.toClass(typeQualifiedName + BUILDER_SUFFIX) [
			static = true
			
			members.addSimulatorIdField();
			members += createSimulatorIdConstructor()

			members += resourceType.toMethod("create", typeRef(clazz)) [
				visibility = JvmVisibility.PUBLIC
				for (param : resourceType.parameters)
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
				body = '''
				«IF isSimulatorIdOn»
					«resourceType.name» resource = new «resourceType.name»(«createEnumerationString(parameters, [name])», «simulatorIdFieldName»);
				«ELSE»
					«resourceType.name» resource = new «resourceType.name»(«createEnumerationString(parameters, [name])»);
				«ENDIF»
					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().addResource(resource);
					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(resource,
							ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.CREATED);
					return resource;
				'''
			]
			
			members.addSimulatorIdGetter();
		]
	}
	
	public def static asBuilderField(ResourceType resourceType, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {

		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		val typeQualifiedName = QualifiedName.create(qualifiedName, resourceType.name)
		val clazz = resourceType.toClass(typeQualifiedName)
		val clazzBuilder = resourceType.toClass(typeQualifiedName + BUILDER_SUFFIX)
		
		return resourceType.toField(resourceType.name + "Field", typeRef(clazzBuilder)) [
			visibility = JvmVisibility.PUBLIC
			initializer = '''
				new «resourceType.name + BUILDER_SUFFIX»(«IF isSimulatorIdOn»this.«simulatorIdFieldName»«ELSE»«ENDIF»)
			'''
		]
	}
}
