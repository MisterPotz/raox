package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.common.types.JvmTypeReference
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.common.types.JvmFormalParameter
import java.util.List
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import java.util.function.Function

class CodeGenerationUtil {
	def static JvmOperation associateGetter(
		extension JvmTypesBuilder currentJvmTypesBuilder,
		EObject raoEntity,
		String name,
		JvmTypeReference typeRef
	) {
		return raoEntity.toMethod("get" + name.toFirstUpper(), typeRef) [
			body = '''
				return this.«name»;
			'''
		]
	}

	def static JvmConstructor associateConstructor(
		extension JvmTypesBuilder builder,
		EObject sourceObject,
		List<JvmFormalParameter> parameters,
		/* Nullable */
		Procedure1<? super JvmConstructor> additionalBlock
	) {
		return sourceObject.toConstructor [
			it.parameters.addAll(parameters)
			body = createInitializingList(parameters)
			if (additionalBlock !== null) {
				additionalBlock.apply(it)
			}
		]
	}
	
	

	def static StringConcatenationClient createInitializingList(List<JvmFormalParameter> parameters) {
		return '''
			«FOR param : parameters»
			this.«param.name» = «param.name»;
			«ENDFOR»
		'''
	}
	
	def protected static <T> String createEnumerationString(List<T> objects, Function<T, String> fun) {
		return '''
			«FOR o : objects»«fun.apply(o)»«IF objects.indexOf(o) != objects.size - 1», «ENDIF»«ENDFOR»
		'''
	}
}