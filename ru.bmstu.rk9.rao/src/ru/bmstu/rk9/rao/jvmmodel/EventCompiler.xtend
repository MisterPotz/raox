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
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, typeReferenceBuilder, associations, event, false);
		
		return apply [ extension b, extension tB |
			return event.toClass(eventQualifiedName) [
				superTypes += typeRef(ru.bmstu.rk9.rao.lib.event.Event)

				// partially delegate creation of features to ProxyBuilderHelper here
				val parametersList = new ArrayList<JvmFormalParameter>();

				parametersList.add(event.toParameter("time", typeRef(double)));
				parametersList.addAll(event.parameters.map[it.toParameter(it.name, it.parameterType)])

				members += pBH.createConstructorForBuildedClass(parametersList);
				
				val fields = pBH.createFieldsForBuildedClass(parametersList);
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

				// because we need to have a builder class that has method 'plan' which will look like a static one
				// must create association for this wanted class
				pBH.associateBuilderClass [ builderFeatures, builderJvmGenericType | 
					builderJvmGenericType.members += event.toMethod("plan", typeRef(double)) [
						visibility = JvmVisibility.PUBLIC
						final = true
						parameters += event.toParameter("time", typeRef(double))
						for (param : event.parameters)
							parameters += event.toParameter(param.name, param.parameterType)
	
						body = '''
							«event.name» event = new «event.name»(«String.join(", ", builderFeatures.buildedClassParameters)»);
							ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.pushEvent(event);
						'''
					]
				]
			]

		]
	}
}
