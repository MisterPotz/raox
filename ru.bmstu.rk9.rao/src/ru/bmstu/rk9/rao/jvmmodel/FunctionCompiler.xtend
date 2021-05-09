package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

class FunctionCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	def rememberAsMethod(FunctionDeclaration function, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		val proxyBuilderHelper = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations,
			function, false)
		storage.addNewProxyBuilder(proxyBuilderHelper)

		proxyBuilderHelper.addAdditionalParentInitializingScopeMembers(
			apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
				return function.toMethod(function.name, function.type) [
					for (param : function.parameters)
						parameters += function.toParameter(param.name, param.parameterType)
					visibility = JvmVisibility.PUBLIC
					static = true
					final = true
					body = function.body
				]

			])
	}
}
