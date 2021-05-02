package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.EntityCreation
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

class EntityCreationCompiler extends RaoEntityCompiler {


	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}
	
	def asField(EntityCreation entityCreation, JvmDeclaredType it, boolean isPreIndexingPhase) {

		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			return entityCreation.toField(entityCreation.name, entityCreation.constructor.inferredType) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				initializer = entityCreation.constructor
			]

		]
	}
}
