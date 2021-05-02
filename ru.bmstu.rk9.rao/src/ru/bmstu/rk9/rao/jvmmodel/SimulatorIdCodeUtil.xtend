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

class SimulatorIdCodeUtil {
	
	def static JvmFormalParameter createSimulatorIdParameter(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder, EObject context) {
		return (context.toParameter(SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS)));
	}

	def static JvmField createSimulatorIdField(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder, EObject raoEntity) {
		return raoEntity.toField(SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS)) [
			final = true
		];
	}
	
	def protected static JvmConstructor createSimulatorIdConstructor(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder, EObject entity) {		
		val params = new ArrayList();
		params.add(createSimulatorIdParameter(builder, typerefBuilder, entity));
		return CodeGenerationUtil.associateConstructor(builder, entity, params) [constructor |
			constructor.visibility = JvmVisibility.PUBLIC
		]
		
	}
	
	def protected static JvmOperation createSimulatorIdGetter(extension JvmTypesBuilder builder, extension JvmTypeReferenceBuilder typerefBuilder,EObject raoEntity) {
		return CodeGenerationUtil.associateGetter(builder, raoEntity, SimulatorIdContract.SIMULATOR_ID_NAME, typeRef(SimulatorIdContract.SIMULATOR_ID_CLASS))	}
	
}