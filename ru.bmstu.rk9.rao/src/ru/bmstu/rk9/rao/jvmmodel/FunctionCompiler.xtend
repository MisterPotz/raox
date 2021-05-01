package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder

class FunctionCompiler extends RaoEntityCompiler {
	def static asMethod(FunctionDeclaration function, JvmDeclaredType it, boolean isPreIndexingPhase) {

		return function.toMethod(function.name, function.type) [
			for (param : function.parameters)
				parameters += function.toParameter(param.name, param.parameterType)
			visibility = JvmVisibility.PUBLIC
			static = true
			final = true
			body = function.body
		]
	}
}
