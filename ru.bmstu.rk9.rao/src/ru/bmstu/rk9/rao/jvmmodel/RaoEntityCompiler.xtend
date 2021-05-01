package ru.bmstu.rk9.rao.jvmmodel

import java.util.List
import java.util.function.Function
import org.eclipse.emf.common.util.EList
import org.eclipse.xtext.common.types.JvmAnnotationReference
import org.eclipse.xtext.common.types.JvmAnnotationType
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.common.types.JvmFormalParameter
import org.eclipse.xtext.common.types.JvmMember
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.ResourceType
import java.util.ArrayList
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.impl.TypesFactoryImpl
import org.eclipse.xtext.common.types.JvmGenericType
import java.util.HashMap
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

abstract class RaoEntityCompiler {
	protected final static boolean isSimulatorIdOn = true;
	protected static extension JvmTypesBuilder currentJvmTypesBuilder;
	protected static extension JvmTypeReferenceBuilder currentJvmTypeReferenceBuilder;
	protected static extension IJvmModelAssociations associations;
	
	public static List<String> resourceTypes = new ArrayList();
	public static HashMap<String, JvmGenericType>  entitiesToClasses = new HashMap();
	
	def protected static initializeCurrent(JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations
	) {
		currentJvmTypesBuilder = jvmTypesBuilder;
		currentJvmTypeReferenceBuilder = jvmTypeReferenceBuilder;
		RaoEntityCompiler.associations = associations;
	}

	def protected static JvmAnnotationReference overrideAnnotation() {
		val anno = TypesFactoryImpl.eINSTANCE.createJvmAnnotationReference
		val annoType = typeRef(Override).type as JvmAnnotationType
		anno.setAnnotation(annoType)
		return anno
	}

	def protected static <T> String createEnumerationString(List<T> objects, Function<T, String> fun) {
		return '''
			«FOR o : objects»«fun.apply(o)»«IF objects.indexOf(o) != objects.size - 1», «ENDIF»«ENDFOR»
		'''
	}
	
	def protected static JvmFormalParameter createSimulatorIdParameter(EObject context) {
		return (context.toParameter(getSimulatorIdFieldName(), typeRef(int)));
	}

	def protected static JvmField createSimulatorIdField(EObject raoEntity) {
		return raoEntity.toField(getSimulatorIdFieldName(), typeRef(int)) [
			final = true
		];
	}
	
	
	def protected static JvmConstructor createSimulatorIdConstructor(EObject raoEntity) {
		return (raoEntity.toConstructor [
			visibility = JvmVisibility.PUBLIC
			if (isSimulatorIdOn) {
				parameters += createSimulatorIdParameter(raoEntity)
				body = '''
					«FOR param : parameters»
						this.«param.name» = «param.name»;
					«ENDFOR»
			'''
			}
		]);
	}
	
	def static JvmConstructor createModelConstructor(EObject raoEntity) {
		return (raoEntity.toConstructor [ eObj |
			eObj.visibility = JvmVisibility.PUBLIC
			if (isSimulatorIdOn) {
				eObj.parameters += createSimulatorIdParameter(raoEntity)
				eObj.body = '''
					«FOR param : eObj.parameters»
						this.«param.name» = «param.name»;
					«ENDFOR»
					«FOR resourceName : resourceTypes»
						«BuilderCompiler.createLineOfBuilderFieldInitialization(resourceName)»
					«ENDFOR»
				'''
			}
		])
	}
	
	def protected static JvmOperation createSimulatorIdGetter(EObject raoEntity) {
		return  raoEntity.toMethod("get" + getSimulatorIdFieldName().toFirstUpper(), typeRef(int)) [
					body = '''
						return this.«getSimulatorIdFieldName()»;
					'''
				]
	}
	
	def static addSimulatorIdField(EList<JvmMember> list, EObject raoEntity) {
		if (isSimulatorIdOn) {
			list += createSimulatorIdField(raoEntity)
		}
	}
	
	def static addSimulatorIdGetter(EList<JvmMember> list, EObject raoEntity) {
		if (isSimulatorIdOn) {
			list += createSimulatorIdGetter(raoEntity)
		}
	}

	def protected static String getSimulatorIdFieldName() {
		return "simulatorId";
	}
	
	def static rememberResourceType(ResourceType resourceType) {
		resourceTypes += resourceType.name;
	}
	
	public def static addResourceClass(ResourceType resourceType, JvmGenericType genericType) {
		ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.entitiesToClasses.put(resourceType.name, genericType);
	}
	
	def static cleanCachedResourceTypes() {
		resourceTypes.clear();
		ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.entitiesToClasses.clear();
	}
}