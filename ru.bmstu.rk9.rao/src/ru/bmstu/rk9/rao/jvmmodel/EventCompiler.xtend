package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.Event
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import java.util.ArrayList
import org.eclipse.xtext.common.types.JvmFormalParameter

class EventCompiler extends RaoEntityCompiler {
	def static asClass(Event event, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {

		val eventQualifiedName = QualifiedName.create(qualifiedName, event.name)
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, typeReferenceBuilder, associations, event, false, true);
		
		return event.toClass(eventQualifiedName) [
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.event.Event)
			
			val parametersList = new ArrayList<JvmFormalParameter>(); 
			
			parametersList.add(event.toParameter("time", typeRef(double)));
			parametersList.addAll(event.parameters.map[ it.toParameter(it.name, it.parameterType) ])
			
			members += pBH.createConstructor(parametersList);
			val fields = pBH.createFields(parametersList);
			members.addAll(fields)

			members += event.toMethod("getName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = '''
					return "«eventQualifiedName»";
				'''
			]

			members += event.toMethod("execute", typeRef(void)) [
				visibility = JvmVisibility.PROTECTED
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = event.body
			]

			members += event.toMethod("plan", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				parameters += event.toParameter("time", typeRef(double))
				for (param : event.parameters)
					parameters += event.toParameter(param.name, param.parameterType)

				body = '''
					«event.name» event = new «event.name»(«createEnumerationString(parameters, [name])»);
					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.pushEvent(event);
				'''
			]
			
			pBH.rememberFunctionsToProxy("plan");
		]
	}
}
