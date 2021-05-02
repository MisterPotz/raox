package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.Event
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import java.util.ArrayList
import org.eclipse.xtext.common.types.JvmFormalParameter
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

class EventCompiler extends RaoEntityCompiler {
	
	
	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}
	
	def asClass(Event event, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {

		val eventQualifiedName = QualifiedName.create(qualifiedName, event.name)
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, typeReferenceBuilder, associations, event, false, true);
		return apply [ extension b, extension tB |
			return event.toClass(eventQualifiedName) [
				superTypes += typeRef(ru.bmstu.rk9.rao.lib.event.Event)

				val parametersList = new ArrayList<JvmFormalParameter>();

				parametersList.add(event.toParameter("time", typeRef(double)));
				parametersList.addAll(event.parameters.map[it.toParameter(it.name, it.parameterType)])

				members += pBH.createProxifiedClassConstructor(parametersList);
				val fields = pBH.createFields(parametersList);
				members.addAll(fields)

				members += event.toMethod("getName", typeRef(String)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += overrideAnnotation()
					body = '''
						return "«eventQualifiedName»";
					'''
				]

				members += event.toMethod("execute", typeRef(void)) [
					visibility = JvmVisibility.PROTECTED
					final = true
					annotations += overrideAnnotation()
					body = event.body
				]

				// TODO this method must be eliminated
//			members += event.toMethod("plan", typeRef(void)) [
//				visibility = JvmVisibility.PUBLIC
//				final = true
//				parameters += event.toParameter("time", typeRef(double))
//				for (param : event.parameters)
//					parameters += event.toParameter(param.name, param.parameterType)
//
//				body = '''
//					«event.name» event = new «event.name»(«createEnumerationString(parameters, [name])»);
//					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.pushEvent(event);
//				'''
//			]
				pBH.addDelegatedBuilderMethod("plan", typeRef(double)) [ builderEntities, operation |
					operation.visibility = JvmVisibility.PUBLIC
					operation.final = true
					operation.parameters += event.toParameter("time", typeRef(double))
					for (param : event.parameters)
						operation.parameters += event.toParameter(param.name, param.parameterType)

					operation.body = '''
						«event.name» event = new «event.name»(«CodeGenerationUtil.createEnumerationString(operation.parameters) [name]»);
						ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.pushEvent(event);
					'''
				]

				pBH.rememberFunctionsToProxy("plan");
			]

		]
	// TODO save somehow proxybuilder here to later use the information that the proxybuilder was given
	}
}
