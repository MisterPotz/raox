package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.Logic
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

class LogicCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	def asClass(Logic logic, JvmDeclaredType it, boolean isPreIndexingPhase) {

		val logicQualifiedName = QualifiedName.create(qualifiedName, logic.name)

		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			return logic.toClass(logicQualifiedName) [
				static = true
				superTypes += typeRef(ru.bmstu.rk9.rao.lib.dpt.Logic)

				for (activity : logic.activities) {
					members += activity.toField(activity.name, typeRef(ru.bmstu.rk9.rao.lib.dpt.Activity)) [
						visibility = JvmVisibility.PRIVATE
					]

					members +=
						activity.toMethod("initialize" + activity.name.toFirstUpper,
							typeRef(ru.bmstu.rk9.rao.lib.dpt.Activity)) [
							visibility = JvmVisibility.PRIVATE
							final = true
							body = activity.constructor
						]
				}

				members += logic.toMethod("initializeActivities", typeRef(void)) [
					visibility = JvmVisibility.PROTECTED
					final = true
					annotations += overrideAnnotation()
					body = '''
						«FOR activity : logic.activities»
							this.«activity.name» = initialize«activity.name.toFirstUpper»();
							this.«activity.name».setName("«activity.name»");
							addActivity(this.«activity.name»);
						«ENDFOR»
					'''
				]

				members += logic.toMethod("getTypeName", typeRef(String)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += overrideAnnotation()
					body = '''
						return "«logicQualifiedName»";
					'''
				]

				for (method : logic.defaultMethods) {
					members += method.toMethod(method.name, typeRef(void)) [
						visibility = JvmVisibility.PUBLIC
						final = true
						annotations += overrideAnnotation()
						body = method.body
					]
				}
			]

		]
	}
}
