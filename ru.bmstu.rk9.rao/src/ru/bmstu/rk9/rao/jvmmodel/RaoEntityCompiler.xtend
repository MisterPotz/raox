package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmAnnotationReference
import org.eclipse.xtext.common.types.impl.TypesFactoryImpl
import org.eclipse.xtext.common.types.JvmAnnotationType
import java.util.List
import java.util.function.Function
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.impl.JvmDeclaredTypeImplCustom
import org.eclipse.xtext.common.types.JvmFormalParameter
import org.eclipse.xtext.common.types.impl.JvmFormalParameterImplCustom
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.common.types.impl.JvmFieldImplCustom
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.impl.JvmOperationImpl
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.impl.JvmConstructorImplCustom
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.emf.common.util.EList
import org.eclipse.xtext.common.types.JvmMember

abstract class RaoEntityCompiler {
	protected final static boolean isSimulatorIdOn = true;
	protected static extension JvmTypesBuilder currentJvmTypesBuilder;
	protected static extension JvmTypeReferenceBuilder currentJvmTypeReferenceBuilder;

	def protected static initializeCurrent(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder) {
		currentJvmTypesBuilder = jvmTypesBuilder;
		currentJvmTypeReferenceBuilder = jvmTypeReferenceBuilder;
	}

	def protected static JvmAnnotationReference overrideAnnotation() {
		val anno = TypesFactoryImpl.eINSTANCE.createJvmAnnotationReference
		val annoType = typeRef(Override).type as JvmAnnotationType
		anno.setAnnotation(annoType)
		return anno
	}

	def protected static <T> createEnumerationString(List<T> objects, Function<T, String> fun) {
		return '''
			«FOR o : objects»«fun.apply(o)»«IF objects.indexOf(o) != objects.size - 1», «ENDIF»«ENDFOR»
		'''
	}
	
	def protected static JvmFormalParameter createSimulatorIdParameter() {
		return (new JvmFormalParameterImplCustom().toParameter(getSimulatorIdFieldName(), typeRef(int)));
	}

	def protected static JvmField createSimulatorIdField() {
		return (new JvmFieldImplCustom().toField(getSimulatorIdFieldName(), typeRef(int)) [
			final = true
		]);
	}
	
	def protected static JvmConstructor createSimulatorIdConstructor() {
		return (new JvmConstructorImplCustom().toConstructor[
			visibility = JvmVisibility.PUBLIC
			if (isSimulatorIdOn) {
				parameters += createSimulatorIdParameter()
				body = '''
					«FOR param : parameters»
						this.«param.name» = «param.name»;
					«ENDFOR»
			'''
			}
		]);
	}
	
	def protected static JvmOperation createSimulatorIdGetter() {
		return  (new JvmFieldImplCustom()).toMethod("get" + getSimulatorIdFieldName().toFirstUpper(), typeRef(int)) [
					body = '''
						return this.«getSimulatorIdFieldName()»;
					'''
				]
	}
	
	def protected static addSimulatorIdField(EList<JvmMember> list) {
		if (isSimulatorIdOn) {
			list += createSimulatorIdField()
		}
	}
	
	def protected static addSimulatorIdGetter(EList<JvmMember> list) {
		if (isSimulatorIdOn) {
			list += createSimulatorIdGetter()
		}
	}

	def protected static String getSimulatorIdFieldName() {
		return "simulatorId";
	}
}