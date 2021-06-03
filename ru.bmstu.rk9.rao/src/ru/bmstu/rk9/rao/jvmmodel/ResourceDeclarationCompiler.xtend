package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.rao.RaoModel

import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import java.util.Arrays

class ResourceDeclarationCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	// TODO later pass here global storage for proxyBuilderHelpers to later collect all accumulated info
	def asMembersForInitializingScope(ResourceDeclaration resource, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		val ProxyBuilderHelper proxyBuilderHelper = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder,
			associations, resource, false);
		storage.addNewProxyBuilder(proxyBuilderHelper)
		proxyBuilderHelper.addAdditionalParentInitializingScopeMembers(Arrays.asList(
			asField(resource, it, isPreIndexingPhase, proxyBuilderHelper),
			asGetter(resource, it, isPreIndexingPhase)
		))
		return null
	}

	// methods, related to a separate resource class
	def asField(ResourceDeclaration resource, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelper resourceDeclarationProxy) {
		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			val String name = resourceInitialValueName(resource.name)
			return resource.toField(name, resource.constructor.inferredType) [ field |
				field.visibility = JvmVisibility.PUBLIC
				// here no initializer must be given as all resources will be created in constructor
				// instead, pass initialization line into proxybuilder - it will be later used in model class
				// constructor
				field.initializer = resource.constructor
			]
		]
	}

	def asGetter(ResourceDeclaration resource, JvmDeclaredType it, boolean isPreIndexingPhase) {

		val resourceQualifiedName = QualifiedName.create(qualifiedName, resource.name)

		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |

			return resource.toMethod("get" + resource.name.toFirstUpper, resource.constructor.inferredType) [
				visibility = JvmVisibility.PUBLIC
				final = true
				body = '''
					if (!__initialized)
						return «resourceInitialValueName(resource.name)»;
					else
						return («returnType») ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().getResource(
								«returnType».class,
								"«resourceQualifiedName»");
				'''
			]

		]
	}

	def static resourceInitialValueName(String resourceName) {
		return "__" + resourceName + "InitialValue";
	}

	// methods related to all resource declarations
	def asGlobalInitializationMethod(RaoModel model, JvmDeclaredType it, boolean isPreIndexingPhase, ProxyBuilderHelpersStorage storage) {
		val proxyBuilderHelper = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, model,
			false)
		storage.addNewProxyBuilder(proxyBuilderHelper)
		val resources = model.objects.filter(typeof(ResourceDeclaration))
		val modelQualifiedNamePart = qualifiedName
		
		proxyBuilderHelper.addAdditionalParentInitializingScopeMembers(apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			return model.toClass("resourcesPreinitializer") [
				superTypes += typeRef(Runnable)
				visibility = JvmVisibility.PROTECTED

				members += model.toMethod("run", typeRef(void)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += overrideAnnotation
					// TODO uncomment when non static context for resources is ready
					body = '''
						«FOR resource : resources»
							«val resourceQualifiedName = QualifiedName.create(modelQualifiedNamePart, resource.name)»
							ru.bmstu.rk9.rao.lib.resource.Resource «resource.name» = «resourceInitialValueName(resource.name)»;
							«resource.name».setName("«resourceQualifiedName»");
						«ENDFOR»
							__initialized = true;
					'''
				]
			]

		]) 
	}

	def rememberAsGlobalInitializationState(RaoModel model, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		val proxyBuilderHelper = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, model,
			false)
		storage.addNewProxyBuilder(proxyBuilderHelper)
		proxyBuilderHelper.addAdditionalParentInitializingScopeMembers(
			apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
				return model.toField("__initialized", typeRef(boolean)) [
					visibility = JvmVisibility.PRIVATE
					final = false
					initializer = '''false'''
				]
			])
	}
}
