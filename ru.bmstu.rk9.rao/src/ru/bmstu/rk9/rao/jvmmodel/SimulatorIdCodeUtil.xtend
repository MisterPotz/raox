package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.common.types.JvmFormalParameter
import java.util.ArrayList
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtend2.lib.StringConcatenationClient

class SimulatorIdCodeUtil {
	
	def static JvmFormalParameter createSimulatorIdParameter(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder, EObject context) {
		return (context.toParameter(SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS)));
	}
	
//	def static JvmFormalParameter createSimulatorIdParameter(
//		extension JvmTypesBuilder builder,
//		extension JvmTypeReferenceBuilder typerefBuilder, 
//		EObject context,
//		boolean useHiddenName
//	) {
//		val String prefix = if (useHiddenName) GeneratedCodeContract.HIDDEN_FIELD_NAME_PREFIX else "";
//		return (context.toParameter(prefix + SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS)));
//	}
	

	 def static JvmField createSimulatorIdField(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder, EObject raoEntity, boolean useHiddenName) {
	 	val String prefix = if (useHiddenName) GeneratedCodeContract.HIDDEN_FIELD_NAME_PREFIX else "";
	 	return raoEntity.toField(prefix + SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS)) [
	 		final = true
	 	];
	 }
	
	def static JvmField createSimulatorIdField(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder, EObject raoEntity) {
		return raoEntity.toField(SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS)) [
			final = true
		];
	}
	
	def protected static JvmConstructor createSimulatorIdConstructor(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder, EObject entity) {		
		return createSimulatorIdConstructor(builder, typerefBuilder, entity, false)
	}
	
	def protected static JvmConstructor createSimulatorIdConstructor(
		extension JvmTypesBuilder builder,
		extension JvmTypeReferenceBuilder typerefBuilder, 
		EObject entity, 
		boolean useHiddenName
	) {		
		val params = new ArrayList();
		params.add(createSimulatorIdParameter(builder, typerefBuilder, entity));
		val String prefix = if (useHiddenName) GeneratedCodeContract.HIDDEN_FIELD_NAME_PREFIX else "";
		
		return CodeGenerationUtil.associateConstructor(builder, entity, params, prefix) [constructor |
			constructor.visibility = JvmVisibility.PUBLIC
		]
	}
	
	def protected static JvmOperation createSimulatorIdGetter(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder,EObject raoEntity) {
		return CodeGenerationUtil.associateGetter(builder, raoEntity, SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS))	}
		
	def protected static JvmOperation createSimulatorIdGetter(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder,EObject raoEntity, boolean useHiddenName) {
		return CodeGenerationUtil.associateGetter(builder, raoEntity, SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS), useHiddenName)	}
		
	def static StringConcatenationClient getSimulatorIdLine() {
		return '''getSimulatorId()'''
	}	
}