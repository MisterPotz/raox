package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmConstructor

class ProxyBuilderHelperUtil {
	private extension JvmTypesBuilder jvM;
	private boolean isStatic;
	
	new(JvmTypesBuilder jvM, boolean isStatic){
		this.jvM = jvM
		this.isStatic = isStatic;
	}
	
	def StringConcatenationClient createConstructorBody(JvmConstructor jvmConstructor) {
		return '''
			«FOR param : jvmConstructor.parameters»this.«param.name» = «param.name»;
							«ENDFOR»
			
		'''
	}
	
	def StringConcatenationClient createConstructorBody(JvmConstructor jvmConstructor, boolean useHiddenNames) {
		return '''
			«FOR param : jvmConstructor.parameters»this.«IF useHiddenNames»_«ELSE»«""»«ENDIF»«param.name» = «param.name»;
							«ENDFOR»
			
		'''
	}
	
	def static String createLineOfBuilderFieldInitialization(String thisVariableName, String builderClassName) {
		return '''this.«thisVariableName»  = new «builderClassName»(this.«SimulatorIdContract.SIMULATOR_ID_NAME»);'''
	}
}