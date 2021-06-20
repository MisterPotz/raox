package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import ru.bmstu.rk9.rao.rao.RaoModel
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import java.util.Arrays
import org.eclipse.xtext.common.types.JvmFormalParameter
import java.util.List
import ru.bmstu.rk9.rao.jvmmodel.CodeGenerationUtil.NameableMember
import java.util.ArrayList

/**
 * compiler for RaoModels
 */
class ModelCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	def asMembersAndConstructor(
		RaoModel model,
		JvmDeclaredType it,
		boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage
	) {
		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			val proxyBuilders = storage.collectedProxyBuilders

			val initializingScopeType = model.toClass(GeneratedCodeContract.INITIALIZATION_SCOPE_CLASS) [
				final = true
				static = false
				visibility = JvmVisibility.PRIVATE
				// add members that need to go to initialization scope (the one with prepared model dependencies like simulatorid)
				members += proxyBuilders.flatMap[additionalMembersToParentInitializingScope]
			]

			val initializationField = model.toField(GeneratedCodeContract.INITIALIZATION_SCOPE_FIELD,
				typeRef(initializingScopeType)) [
				final = true
				static = false
				visibility = JvmVisibility.PRIVATE
			]

			val additionalMembers = proxyBuilders.flatMap[collectAdditionalMembers].toList
			val additionalCode = proxyBuilders.map[codeToAppendToParentScopeConstructor].reduce[c1, c2| c1.append(c2)].collectedLines
			additionalMembers += initializingScopeType
			additionalMembers += initializationField
			val superParameters= Arrays.asList(SimulatorIdCodeUtil.createSimulatorIdParameter(jvmTypesBuilder, jvmTypeReferenceBuilder, model))
			val List<JvmFormalParameter> varconstParameters = Arrays.asList(
				CodeGenerationUtil.createVarconstValuesParameter(jvmTypesBuilder, jvmTypeReferenceBuilder, model))
			val List<JvmFormalParameter> collectedParameters = new ArrayList(superParameters)
			
			collectedParameters.addAll(varconstParameters)
			val constructor = model.toConstructor [ constructor |
				constructor.parameters += collectedParameters
				constructor.visibility = JvmVisibility.PUBLIC
				constructor.body = '''
					«CodeGenerationUtil.createSuperInitializationLine(superParameters.map[ new NameableMember(it)])»
					«additionalCode»
					this.«GeneratedCodeContract.INITIALIZATION_SCOPE_FIELD» = new «typeRef(initializingScopeType)»();
				'''
			]
			
			additionalMembers += constructor
			return additionalMembers
		]
	}
}
