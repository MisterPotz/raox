package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.EnumDeclaration
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

class EnumCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}
	
	def asType(EnumDeclaration enumDeclaration, JvmDeclaredType it, boolean isPreIndexingPhase) {
		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			return enumDeclaration.toEnumerationType(enumDeclaration.name) [
				visibility = JvmVisibility.PUBLIC
				static = true
				enumDeclaration.values.forEach [ value |
					members += value.toEnumerationLiteral(value.name)
				]
			]
		]
	}
}
