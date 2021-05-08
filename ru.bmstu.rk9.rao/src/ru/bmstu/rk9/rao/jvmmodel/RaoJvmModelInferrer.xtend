package ru.bmstu.rk9.rao.jvmmodel

import com.google.inject.Inject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.Frame
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import ru.bmstu.rk9.rao.rao.Generator
import ru.bmstu.rk9.rao.rao.Logic
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RaoModel
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Search
import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.EntityCreation
import ru.bmstu.rk9.rao.rao.VarConst
import ru.bmstu.rk9.rao.rao.DataSource

import static extension ru.bmstu.rk9.rao.naming.RaoNaming.*
import static extension ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.*
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import java.util.List
import java.util.ArrayList
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver
import org.eclipse.xtext.common.types.JvmVisibility

class RaoJvmModelInferrer extends AbstractModelInferrer implements ProxyBuilderHelpersStorage {
	@Inject extension JvmTypesBuilder jvmTypesBuilder
	@Inject IJvmModelAssociations associations
	
	@Inject IBatchTypeResolver typeResolver
// it is better to use extensions this way, because if they are used statically, the ide performance is poor
	extension EntityCreationCompiler entityCreationCompiler;
	extension VarConstCompiler varconstCompiler;
	extension EnumCompiler enumCompiler;
	extension FunctionCompiler functionCompiler;
	extension DefaultMethodCompiler defaultMethodCompiler;
	extension ResourceTypeCompiler resourceTypeCompiler;
	extension GeneratorCompiler generatorCompiler;
	extension EventCompiler eventCompiler;
	extension PatternCompiler patternCompiler;
	extension LogicCompiler logicCompiler;
	extension SearchCompiler searchCompiler;
	extension FrameCompiler frameCompiler;
	extension ResourceDeclarationCompiler resourceDeclarationCompiler;
	extension ResultCompiler dataSourceCompiler;
	extension ModelCompiler modelCompiler;

	private final List<ProxyBuilderHelper> proxyBuilderHelpers;

	def JvmField createSimIdField(EObject rao) {
		return rao.toField("simId", typeRef(int)) [
			final = true
		]
	}

	new() {
		this.proxyBuilderHelpers = new ArrayList();
	}

	def init() {
		this.entityCreationCompiler = new EntityCreationCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations, typeResolver);
		this.varconstCompiler = new VarConstCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations)
		this.enumCompiler = new EnumCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.functionCompiler = new FunctionCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.defaultMethodCompiler = new DefaultMethodCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.resourceTypeCompiler = new ResourceTypeCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.generatorCompiler = new GeneratorCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.eventCompiler = new EventCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.patternCompiler = new PatternCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.logicCompiler = new LogicCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.searchCompiler = new SearchCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.frameCompiler = new FrameCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.resourceDeclarationCompiler = new ResourceDeclarationCompiler(jvmTypesBuilder, _typeReferenceBuilder,
			associations);
		this.dataSourceCompiler = new ResultCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
		this.modelCompiler = new ModelCompiler(jvmTypesBuilder, _typeReferenceBuilder, associations);
	}

	def dispatch void infer(RaoModel element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		init();
		acceptor.accept(element.toClass(QualifiedName.create(element.eResource.URI.projectName, element.nameGeneric))) [ context |
			RaoEntityCompiler.cleanCachedResourceTypes();

			context.members +=
				SimulatorIdCodeUtil.createSimulatorIdField(jvmTypesBuilder, _typeReferenceBuilder, element)
			context.members +=
				SimulatorIdCodeUtil.createSimulatorIdGetter(jvmTypesBuilder, _typeReferenceBuilder, element)

			element.compileResourceInitialization(context, isPreIndexingPhase, this)

			for (entity : element.objects.filter[ !((it instanceof EntityCreation) || (it instanceof ResourceDeclaration))]) {
				entity.compileRaoEntity(context, isPreIndexingPhase, this)
			}
				
			val initializingScopeType = element.toClass("InitializingScope") [
				final = true
				static = false 
				visibility = JvmVisibility.PRIVATE
				
				for (entity : element.objects.filter[((it instanceof EntityCreation) || (it instanceof ResourceDeclaration))]) {
					entity.compileRaoEntity(it, isPreIndexingPhase, this)
				}
				for (proxyBuilder : collectedProxyBuilders) {
					for (member : proxyBuilder.additionalMembersToParentInitializingScope) {
						members += member
					}
				}
			]
			
			context.members += initializingScopeType
			
			context.members += element.toField("initializingScope", typeRef(initializingScopeType)) [
				final = true 
				static =false
				visibility = JvmVisibility.PRIVATE
			]
			
			// create constructor with initializations from all proxybuilders that were collected
			context.members += modelCompiler.asConstructor(element, context, isPreIndexingPhase, this, initializingScopeType);
			context.members += modelCompiler.asAdditionalMembers(element, context, isPreIndexingPhase, this)
		]
	}

	def dispatch compileRaoEntity(EntityCreation entity, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		if (!isPreIndexingPhase && entity.constructor !== null)
			members += entity.asField(it, isPreIndexingPhase, storage)
	}

	def dispatch compileRaoEntity(VarConst varconst, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += varconst.asClass(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(EnumDeclaration enumDeclaration, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += enumDeclaration.asType(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(FunctionDeclaration function, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += function.asMethod(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += method.asClass(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(ResourceType resourceType, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		/*  that is required for the proper inflation of model constructor
		 *  where fields of builders for each resource are initiated
		 */
		members += resourceType.asClass(it, isPreIndexingPhase, this)
	}

//	def compileResourceBuilder(RaoModel model, ResourceType resourceType, JvmDeclaredType it, boolean isPreIndexingPhase) {
//		RaoEntityCompiler.rememberResourceType(resourceType)
//		members += resourceType.asBuilder(model, jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
//		members += resourceType.asBuilderField(model, jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
//	}
	def dispatch compileRaoEntity(Generator generator, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += generator.asClass(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Event event, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += event.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Pattern pattern, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += pattern.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Logic logic, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += logic.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Search search, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += search.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Frame frame, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += frame.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(ResourceDeclaration resource, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		resource.asMembersForInitializingScope(it, isPreIndexingPhase, storage)
	}

	def dispatch compileRaoEntity(DataSource dataSource, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {

		members += dataSource.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Result result, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		if (!isPreIndexingPhase && result.constructor !== null)
			members += result.asField(it, isPreIndexingPhase);
	}

	def compileResourceInitialization(RaoModel element, JvmDeclaredType it, boolean isPreIndexingPhase,
		ProxyBuilderHelpersStorage storage) {
		members += element.asGlobalInitializationMethod(it, isPreIndexingPhase)
		members += element.asGlobalInitializationState(it, isPreIndexingPhase)
	}

	override addNewProxyBuilder(ProxyBuilderHelper newBuilder) {
		proxyBuilderHelpers.add(newBuilder);
	}

	override getCollectedProxyBuilders() {
		return proxyBuilderHelpers
	}

}
