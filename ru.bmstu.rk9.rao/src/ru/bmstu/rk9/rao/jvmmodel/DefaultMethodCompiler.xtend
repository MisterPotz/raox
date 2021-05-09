package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.DefaultMethod
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import java.util.function.Supplier
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

class DefaultMethodCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	def rememberAsClass(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		switch (method.name) {
			case DefaultMethodsHelper.GlobalMethodInfo.INIT.name:
				method.rememberInitAsClass(it, isPreIndexingPhase, storage)
			case DefaultMethodsHelper.GlobalMethodInfo.TERMINATE_CONDITION.name:
				method.rememberTerminateAsClass(it, isPreIndexingPhase, storage)
		}
	}

	// run method may contain entities that exist only within initialized scope - hence, must be put into the scope itself
	def private rememberInitAsClass(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		val proxyBuilderHelper = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, method,
			false)
		storage.addNewProxyBuilder(proxyBuilderHelper)
		proxyBuilderHelper.addAdditionalParentInitializingScopeMembers(
			apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
				method.toClass(method.name) [
					superTypes += typeRef(Runnable)
					visibility = JvmVisibility.PROTECTED
					members += method.toMethod("run", typeRef(void)) [
						visibility = JvmVisibility.PUBLIC
						final = true
						annotations += overrideAnnotation
						body = method.body
					]
				]
			]
		)
	}

	def private rememberTerminateAsClass(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		val proxyBuilderHelper = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, method,
			false)
		storage.addNewProxyBuilder(proxyBuilderHelper)
		proxyBuilderHelper.addAdditionalParentInitializingScopeMembers(
			apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
				return method.toClass(method.name) [
					superTypes += typeRef(Supplier, {
						typeRef(Boolean)
					})
					visibility = JvmVisibility.PROTECTED
					members += method.toMethod("get", typeRef(Boolean)) [
						visibility = JvmVisibility.PUBLIC
						final = true
						annotations += overrideAnnotation
						body = method.body
					]
				]
			])
	}
}
