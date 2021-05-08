package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.EntityCreation
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import java.util.HashMap
import ru.bmstu.rk9.rao.jvmmodel.CodeGenerationUtil.NameableMember
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver
import org.eclipse.xtext.xbase.compiler.output.TreeAppendable

class EntityCreationCompiler extends RaoEntityCompiler {
	private final IBatchTypeResolver typeResolver

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations, IBatchTypeResolver typeResolver) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
		this.typeResolver = typeResolver
	}
	
	def asField(EntityCreation entityCreation, JvmDeclaredType it, boolean isPreIndexingPhase, ProxyBuilderHelpersStorage storage) {
		val ProxyBuilderHelper proxyBuilderHelper = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, entityCreation, false)
		storage.addNewProxyBuilder(proxyBuilderHelper)
		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			val con = entityCreation.constructor.inferredType
			
			return entityCreation.toField(entityCreation.name, entityCreation.constructor.inferredType) [
				visibility = JvmVisibility.PUBLIC
				final = true
				initializer = (entityCreation.constructor)

				val HashMap<NameableMember, String> map = newHashMap(new NameableMember(it) -> '''«entityCreation.constructor.inferredType»;''')
				// don't use initializer here - initialize via proxy
//				initializer = entityCreation.constructor
//				proxyBuilderHelper.addCodeForParentScopeConstructor(
//					CodeGenerationUtil.createInitializingListWithValues(map)
//				)
			]

		]
	}
}
