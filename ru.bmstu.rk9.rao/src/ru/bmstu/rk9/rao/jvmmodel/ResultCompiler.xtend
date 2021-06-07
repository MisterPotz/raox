package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler
import ru.bmstu.rk9.rao.rao.DataSource
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper.DataSourceMethodInfo
import org.eclipse.xtext.common.types.JvmTypeReference
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import ru.bmstu.rk9.rao.rao.RaoEntity
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId
import ru.bmstu.rk9.rao.lib.result.Result

class ResultCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	// data source may contain code that references dependencies of model, thus must be put into initializationscope
	def rememberAsClass(DataSource dataSource, JvmDeclaredType it, boolean isPreIndexingPhase, ProxyBuilderHelpersStorage storage) {
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, dataSource, false)
		storage.addNewProxyBuilder(pBH)
		pBH.addAdditionalParentInitializingScopeMembers(
			apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			return dataSource.toClass(QualifiedName.create(qualifiedName, dataSource.name)) [
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
		)
	}

	// Result takes lambda that may contain references to model dependencies, thus this field must be contained inside initializing inner scope
	def rememberAsField(ru.bmstu.rk9.rao.rao.Result result, JvmDeclaredType it, boolean isPreIndexingPhase, ProxyBuilderHelpersStorage storage) {
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, result, false)
		storage.addNewProxyBuilder(pBH)
		pBH.addAdditionalParentInitializingScopeMembers(
			apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
				return result.toField(result.name, result.constructor.inferredType) [
					visibility = JvmVisibility.PUBLIC
					final = true
					initializer = result.constructor
				]

			]
		)
	}

	def rememberAsBuilder(RaoEntity entity, JvmDeclaredType type, boolean isPreIndexingPhase, ProxyBuilderHelpersStorage storage) {
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, entity, false)
		storage.addNewProxyBuilder(pBH)
		pBH.addAdditionalParentInitializingScopeMembers(
			apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder  |
			return entity.toField(Result.simpleName, typeRef(Result)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				initializer = '''new Result(«SimulatorIdCodeUtil.simulatorIdLine»)'''
			]
		])
	}
}
