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
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmMember
import java.util.HashMap

class CodeGenerationUtil {
	private interface Extensioner<T> {
		def T apply(JvmTypesBuilder b, JvmTypeReferenceBuilder tB);
	}
	
	protected def static <T> T apply(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, Extensioner<T> extensioner) {
		return extensioner.apply(jvmTypesBuilder, jvmTypeReferenceBuilder);
	}
	
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
	
	def static JvmOperation associateGetter(
		extension JvmTypesBuilder currentJvmTypesBuilder,
		EObject raoEntity,
		String name,
		JvmTypeReference typeRef, 
		boolean useHiddenName
	) {
		val String prefix = if (useHiddenName) GeneratedCodeContract.HIDDEN_FIELD_NAME_PREFIX else "";
		
		return raoEntity.toMethod("get" + name.toFirstUpper(), typeRef) [
			body = '''
				return this.«prefix + name»;
			'''
		]
	}

	def static JvmConstructor associateConstructor(
		extension JvmTypesBuilder builder,
		EObject sourceObject,
		List<JvmFormalParameter> parameters,
		String prefix,
		/* Nullable */
		Procedure1<? super JvmConstructor> additionalBlock
	) {
		return sourceObject.toConstructor [
			it.parameters.addAll(parameters)
			body = createInitializingList(parameters.map[new NameableMember(it)], prefix)
			if (additionalBlock !== null) {
				additionalBlock.apply(it)
			}
		]
	}
	
	

	def static StringConcatenationClient createInitializingList(List<NameableMember> parameters) {
		return '''
			«FOR param : parameters»
			this.«param.name» = «param.name»;
			«ENDFOR»
		'''
	}
	
	def static StringConcatenationClient createInitializingList(List<NameableMember> parameters, String prefix) {
		return '''
			«FOR param : parameters»
			this.«prefix + param.name» = «param.name»;
			«ENDFOR»
		'''
	}
	
	def static StringConcatenationClient createInitializingListWithValues(
		HashMap<NameableMember, String> parameters
	) {
		return '''
			«FOR param : parameters.entrySet»
			this.«param.key.name» = «param.value»;
			«ENDFOR»
		'''
	}
	
	
	def protected static <T> String createEnumerationString(List<T> objects, Function<T, String> fun) {
		return '''
			«FOR o : objects»«fun.apply(o)»«IF objects.indexOf(o) != objects.size - 1», «ENDIF»«ENDFOR»
		'''
	}

	public static class NameableMember {
		public final String name;

		new(JvmMember member) {
			this.name = member.simpleName;
		}
		
		new(JvmFormalParameter parameter) {
			this.name = parameter.name;
		}
	}

//	def JvmConstructor createModelConstructor( JvmTypesBuilder jvmTypesBuilder, 
//		JvmTypeReferenceBuilder jvmTypeReferenceBuilder, 
//		Boolean isSimulatorIdOn, 
//		List<String> resourceTypes,
//		EObject raoEntity
//	) {
//		return apply(jvmTypesBuilder, jvmTypeReferenceBuilder) [ extension b, extension tB |
//			return raoEntity.toConstructor [ eObj |
//				eObj.visibility = JvmVisibility.PUBLIC
//				if (isSimulatorIdOn) {
//					eObj.parameters += SimulatorIdCodeUtil.createSimulatorIdParameter(b, tB, raoEntity)
//					eObj.body = '''
//						«FOR param : eObj.parameters»
//							this.«param.name» = «param.name»;
//						«ENDFOR»
//						«FOR resourceName : resourceTypes»
//							«BuilderCompiler.createLineOfBuilderFieldInitialization(resourceName)»
//						«ENDFOR»
//					'''
//				}
//			]
//		]
//	}
}