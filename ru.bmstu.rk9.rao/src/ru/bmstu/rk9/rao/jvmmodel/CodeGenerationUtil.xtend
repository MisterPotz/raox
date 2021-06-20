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
import org.eclipse.xtend2.lib.StringConcatenation
import ru.bmstu.rk9.rao.lib.contract.RaoGenerationContract
import org.eclipse.xtext.common.types.JvmField

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
	
	def static StringConcatenationClient createSuperInitializationLine(List<NameableMember> parameters) {
		val enumerationString = CodeGenerationUtilJava.createEnumerationString(parameters) [ param | param.getName ]
		return '''
			super(«enumerationString»);
		'''
	}
	
	def static JvmConstructor constructorWithSuper(extension JvmTypesBuilder builder, 
		EObject sourceObject, 
		List<JvmFormalParameter> parameters
	) {
		return sourceObject.toConstructor[
			visibility = JvmVisibility.PUBLIC
			it.parameters += parameters
			body = '''
			«createSuperInitializationLine(parameters.map[new NameableMember(it)])»
			'''
		]
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
	
	def static JvmFormalParameter createVarconstValuesParameter(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder refBuilder, EObject source) {
		return source.toParameter(RaoGenerationContract.VARCONST_VALUES_NAME, typeRef(
					RaoGenerationContract.VARCONST_VALUES_CLASS,
					typeRef(RaoGenerationContract.VARCONST_VALUES_KEY),
					typeRef(RaoGenerationContract.VARCONST_VALUES_VALUE)
				))
	}

	public static class NameableMember {
		private final String name;
		public String substitutionValue = null;
		
		new(JvmMember member) {
			this.name = member.simpleName;
		}
		
		new(JvmFormalParameter parameter) {
			this.name = parameter.name;
		}
		
		def NameableMember setSubstitutionValue(String value) {
			this.substitutionValue = value;
			return this;
		}
		
		def String getName() {
			if (substitutionValue == null) {
				return name;
			}
			return substitutionValue;
		}
		
	}
}