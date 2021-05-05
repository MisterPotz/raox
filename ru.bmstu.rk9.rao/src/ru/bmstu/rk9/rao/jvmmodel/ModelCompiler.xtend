package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import ru.bmstu.rk9.rao.rao.RaoModel
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import java.util.Arrays
import org.eclipse.xtext.common.types.JvmFormalParameter
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenation

/**
 * compiler for RaoModels
 */
class ModelCompiler extends RaoEntityCompiler {
	
	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}
	
	def asConstructor(RaoModel model, 
		JvmDeclaredType it,
		 boolean isPreIndexingPhase,
		 ProxyBuilderHelpersStorage proxyBuildersStorage){ 
		// as for now, models accept simulatorid as a parameter
		val List<JvmFormalParameter> parameters = Arrays.asList(SimulatorIdCodeUtil.createSimulatorIdParameter(jvmTypesBuilder, jvmTypeReferenceBuilder, model))
		
		// probably i can't pass null here, but if i don't set parallel execution here it might be ok
		val additionalLines = proxyBuildersStorage.collectedProxyBuilders.stream().reduce(new StringConcatenation(), [accum, newVal | 
			accum.append(newVal.codeToAppendToParentScopeConstructor);
			return accum
		], null)
		
		return apply [extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			model.toConstructor[ constructor |
				constructor.parameters += parameters
				constructor.visibility = JvmVisibility.PUBLIC
				constructor.body = '''
					«CodeGenerationUtil.createInitializingList(parameters)»
					«additionalLines»
				'''
			]
		]
	}
	
}