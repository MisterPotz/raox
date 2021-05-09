package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;

import ru.bmstu.rk9.rao.rao.Frame;
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.lib.animation.AnimationContext
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import ru.bmstu.rk9.rao.lib.animation.AnimationFrame

class FrameCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	// probably code of a frame may contain references to dependencies, so must be put into initialization scope
	def rememberAsClass(Frame frame, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		val frameQualifiedName = QualifiedName.create(qualifiedName, frame.name)

		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, frame, false)
		storage.addNewProxyBuilder(pBH)

		pBH.addAdditionalParentInitializingScopeMembers(
			apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
				return frame.toClass(frameQualifiedName) [
					superTypes += typeRef(AnimationFrame)

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
							annotations += overrideAnnotation()
							body = method.body
						]
					}
				]
			])
	}
}
