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
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

public class BuilderCompiler extends RaoEntityCompiler {
	
	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	/**
	* creates builder for passed [resourceType] 
	*/ 
	public def static JvmGenericType asBuilder(
		ResourceType resourceType,
		EObject raoModel,
		extension JvmTypesBuilder jvmTypesBuilder, extension JvmTypeReferenceBuilder typeReferenceBuilder,
				JvmDeclaredType it, 
		boolean isPreIndexingPhase) {

		
		val builderClass =  raoModel.toClass("sdf") [ builder |
			builder.static = true

			// TODO move this to resource type related compiler to use in ProxyBuilderHelper
//			builder.members += raoModel.toMethod("create", typeRef(resourceClass)) [
//				visibility = JvmVisibility.PUBLIC
//				for (param : resourceType.parameters)
//					parameters += raoModel.toParameter(param.declaration.name, param.declaration.parameterType)
//				body = '''
//					«IF isSimulatorIdOn»entitiesToClasses» resource = new «resourceType.name»(«CodeGenerationUtil.createEnumerationString(parameters, [name])»);
//					«ENDIF»
//						ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().addResource(resource);
//						ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(resource,
//							ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.CREATED);
//						return resource;
//				'''
//			]

		]
		return builderClass;
	}
}
