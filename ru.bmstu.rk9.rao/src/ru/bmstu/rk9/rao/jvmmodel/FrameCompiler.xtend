package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;

import ru.bmstu.rk9.rao.rao.Frame;
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.lib.animation.AnimationContext
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper

class FrameCompiler extends RaoEntityCompiler {
	def static asClass(Frame frame, JvmDeclaredType it, boolean isPreIndexingPhase) {

		val frameQualifiedName = QualifiedName.create(qualifiedName, frame.name)

		return frame.toClass(frameQualifiedName) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.animation.AnimationFrame)

			members += frame.toMethod("getTypeName", typeRef(String)) [
				final = true
				annotations += overrideAnnotation
				body = '''
					return "«frameQualifiedName»";
				'''
			]

			for (method : frame.defaultMethods) {
				members += method.toMethod(method.name, typeRef(void)) [
					if (method.name == DefaultMethodsHelper.FrameMethodInfo.DRAW.name)
						parameters += frame.toParameter("it", typeRef(AnimationContext))
					for (param : method.parameters)
						parameters += method.toParameter(param.name, param.parameterType)
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
					body = method.body
				]
			}
		]
	}
}
