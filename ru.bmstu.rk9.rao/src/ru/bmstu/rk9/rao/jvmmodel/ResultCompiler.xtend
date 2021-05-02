package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler
import ru.bmstu.rk9.rao.rao.DataSource
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper.DataSourceMethodInfo
import org.eclipse.xtext.common.types.JvmTypeReference
import ru.bmstu.rk9.rao.rao.Result
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations



class ResultCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}
	
	def asClass(DataSource dataSource, JvmDeclaredType it, boolean isPreIndexingPhase) {

		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			return dataSource.toClass(QualifiedName.create(qualifiedName, dataSource.name)) [
				static = true
				superTypes += typeRef(ru.bmstu.rk9.rao.lib.result.AbstractDataSource, {
					dataSource.evaluateType
				})

				members += dataSource.toConstructor [
					visibility = JvmVisibility.PUBLIC
					for (param : dataSource.parameters)
						parameters += param.toParameter(param.name, param.parameterType)
					body = '''
						«FOR param : parameters»this.«param.name» = «param.name»;
						«ENDFOR»
					'''
				]

				for (param : dataSource.parameters)
					members += param.toField(param.name, param.parameterType)

				if (!isPreIndexingPhase) {
					for (method : dataSource.defaultMethods) {

						var JvmTypeReference defaultMethodReturnType;

						switch (method.name) {
							case DataSourceMethodInfo.EVALUATE.name: {
								defaultMethodReturnType = dataSource.evaluateType;
							}
							case DataSourceMethodInfo.CONDITION.name: {
								defaultMethodReturnType = typeRef(boolean);
							}
						}

						members += method.toMethod(method.name, defaultMethodReturnType) [
							visibility = JvmVisibility.PUBLIC
							final = true
							annotations += overrideAnnotation()
							body = method.body
						]
					}
				}
			]

		]
	}

	def asField(Result result, JvmDeclaredType it, boolean isPreIndexingPhase) {
		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			return result.toField(result.name, result.constructor.inferredType) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				initializer = result.constructor
			]

		]
	}
}
