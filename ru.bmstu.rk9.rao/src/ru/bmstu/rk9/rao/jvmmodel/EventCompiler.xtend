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
import ru.bmstu.rk9.rao.jvmmodel.CodeGenerationUtil.NameableMember
import java.util.Arrays

class EventCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	def asClass(Event event, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType parentClass, boolean isPreIndexingPhase, ProxyBuilderHelpersStorage storage) {

		val eventQualifiedName = QualifiedName.create(parentClass.qualifiedName, event.name)
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, typeReferenceBuilder, associations, event, false);
		storage.addNewProxyBuilder(pBH)

		// event class should be used inside initializing scope
		pBH.addAdditionalParentInitializingScopeMembers(apply [ extension b, extension tB |
			return event.toClass(eventQualifiedName) [ eventClass |
				eventClass.superTypes += typeRef(ru.bmstu.rk9.rao.lib.event.Event)

				// partially delegate creation of features to ProxyBuilderHelper here
				val parametersList = new ArrayList<JvmFormalParameter>();
				parametersList.add(event.toParameter("time", typeRef(double)));
				
				// these fields will go into local private fields of the generated event
				val fieldParametersList = new ArrayList<JvmFormalParameter>();
				fieldParametersList.addAll(event.parameters.map[it.toParameter(it.name, it.parameterType)])
	
				val unitedList = new ArrayList<JvmFormalParameter>();
				unitedList.addAll(parametersList)
				unitedList.addAll(fieldParametersList)
				
				eventClass.members += pBH.createConstructorForBuildedClass(
					unitedList
				);
				eventClass.members += pBH.createFieldsForBuildedClass(fieldParametersList);
				eventClass.members += pBH.createNecessaryMembersForBuildedClass()
				
				// because we need to have a builder class that has method 'plan' which will look like a static one
				// must create association for this wanted class
				pBH.buildedClass = eventClass
				val builderClass = pBH.associateBuilderClass [ builderFeatures, builderJvmGenericType |
					builderJvmGenericType.members += event.toMethod("plan", typeRef(Void.TYPE)) [
						visibility = JvmVisibility.PUBLIC
						final = true
						parameters += event.toParameter("time", typeRef(double))
						for (param : event.parameters)
							parameters += event.toParameter(param.name, param.parameterType)

						body = '''
							«typeRef(GeneratedCodeContract.INITIALIZATION_SCOPE_CLASS + "." + event.name)» event = «GeneratedCodeContract.INITIALIZATION_SCOPE_FIELD».new «eventClass.simpleName»(«String.join(", ", builderFeatures.buildedClassParameters)»);
							getSimulator().pushEvent(event);
						'''
					]
				]
				
				// adding field
				pBH.addAdditionalParentInitializingScopeMembers(event.toField(event.name, typeRef(builderClass)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					initializer = '''new «typeRef(builderClass)»(«pBH.builderClassConstructorParameters.map[new NameableMember(it).name].join(", ")»)'''
				])

				eventClass.members += event.toMethod("getName", typeRef(String)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += overrideAnnotation()
					body = '''
						return "«eventQualifiedName»";
					'''
				]

				eventClass.members += event.toMethod("execute", typeRef(void)) [
					visibility = JvmVisibility.PROTECTED
					final = true
					annotations += overrideAnnotation()
					body = event.body
				]

			]
		])
	}
}
