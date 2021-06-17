package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmFormalParameter
import java.util.List
import ru.bmstu.rk9.rao.jvmmodel.CodeGenerationUtil.NameableMember

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
	
	def StringConcatenationClient createConstructorBody(
		List<ConstructorParameter> parameters,
		 StringConcatenationClient additionalLines
	) {
		val superParams = parameters.filter [param | param.addToSuperInitialization]
		val superLine = CodeGenerationUtil.createSuperInitializationLine(superParams.map[
			new NameableMember(it.parameter).setSubstitutionValue(it.substitutionValue)
		].toList)
		val paramsToInitialize = parameters.filter [ param | param.initializeInConstructor ]
		return '''
			«superLine»
			«FOR param : paramsToInitialize»
			this.«IF param.isUseHiddenName»_«ELSE»«""»«ENDIF»«param.parameter.name» = «param.substitutionValue»;
			«ENDFOR»
			«IF additionalLines !== null »«additionalLines»«ENDIF»
			'''
	}
	
	def static String createLineOfBuilderFieldInitialization(String thisVariableName, String builderClassName) {
		return '''this.«thisVariableName»  = new «builderClassName»(this.«SimulatorIdContract.SIMULATOR_ID_NAME»);'''
	}
}