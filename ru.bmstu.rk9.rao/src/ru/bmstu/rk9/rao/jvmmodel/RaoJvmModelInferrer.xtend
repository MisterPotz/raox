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

import static extension ru.bmstu.rk9.rao.jvmmodel.DefaultMethodCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EntityCreationCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EnumCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EventCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.FrameCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.FunctionCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.GeneratorCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.LogicCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.PatternCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.ResourceDeclarationCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.ResourceTypeCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.SearchCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.ResultCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.VarConstCompiler.*
import static extension ru.bmstu.rk9.rao.naming.RaoNaming.*
import static extension ru.bmstu.rk9.rao.jvmmodel.BuilderCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.*
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import org.eclipse.xtext.common.types.JvmOperation

class RaoJvmModelInferrer extends AbstractModelInferrer {
	@Inject extension JvmTypesBuilder jvmTypesBuilder
	@Inject IJvmModelAssociations associations
	
	def JvmField createSimIdField(EObject rao) {
		return rao.toField("simId", typeRef(int)) [
			final = true
		]
	}
	
	def dispatch void infer(RaoModel element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		val ass = associations
		acceptor.accept(element.toClass(QualifiedName.create(element.eResource.URI.projectName, element.nameGeneric))) [ context |
			RaoEntityCompiler.cleanCachedResourceTypes();
			initializeCurrent(jvmTypesBuilder, _typeReferenceBuilder, associations);
			
			context.members += SimulatorIdCodeUtil.createSimulatorIdField(jvmTypesBuilder, _typeReferenceBuilder, element)
			context.members += SimulatorIdCodeUtil.createSimulatorIdGetter(jvmTypesBuilder, _typeReferenceBuilder, element)
			
			for (entity : element.objects) {
				entity.compileRaoEntity(context, isPreIndexingPhase)
				if (entity instanceof ResourceType) {
					compileResourceBuilder(element, entity, context, isPreIndexingPhase)
				}
			}
			context.members += element.createModelConstructor;
			
			element.compileResourceInitialization(context, isPreIndexingPhase)
			
			val elems = ass.getJvmElements(element)
			for (i : elems) {
				println(i)
			}
			
			for (i : element.objects) {
				println(i)
				if (i instanceof Event) {
					val associate = ass.getJvmElements(i)
						.filter[ it instanceof JvmOperation]
					for (g : associate) {
						val s = g as JvmOperation
						
						println("\t" + s.simpleName + "\t" + s.parameters + "\t" + s)
					}
				}
			}
		]
	}

	def dispatch compileRaoEntity(EntityCreation entity, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase && entity.constructor !== null)
			members += entity.asField(it, isPreIndexingPhase)
	}
	
	def dispatch compileRaoEntity(VarConst varconst, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += varconst.asClass(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(EnumDeclaration enumDeclaration, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += enumDeclaration.asType(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(FunctionDeclaration function, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += function.asMethod(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += method.asClass(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(ResourceType resourceType, JvmDeclaredType it, boolean isPreIndexingPhase) {
		/*  that is required for the proper inflation of model constructor
		 *  where fields of builders for each resource are initiated
		 */
		val clazz = resourceType.asClass(it, isPreIndexingPhase)
		members += clazz
		addResourceClass(resourceType, clazz);
	}
	
	def compileResourceBuilder(RaoModel model, ResourceType resourceType, JvmDeclaredType it, boolean isPreIndexingPhase) {
		RaoEntityCompiler.rememberResourceType(resourceType)
		members += resourceType.asBuilder(model, jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
		members += resourceType.asBuilderField(model, jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Generator generator, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += generator.asClass(it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Event event, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += event.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Pattern pattern, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += pattern.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Logic logic, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += logic.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Search search, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += search.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Frame frame, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += frame.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(ResourceDeclaration resource, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += resource.asGetter(it, isPreIndexingPhase)
		members += resource.asField(it, isPreIndexingPhase)
	}
	def dispatch compileRaoEntity(DataSource dataSource, JvmDeclaredType it, boolean isPreIndexingPhase) {

		members += dataSource.asClass(it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Result result, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase && result.constructor !== null)
			members += result.asField(it, isPreIndexingPhase);
	}

	def compileResourceInitialization(RaoModel element, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += element.asGlobalInitializationMethod(it, isPreIndexingPhase)
		members += element.asGlobalInitializationState(it, isPreIndexingPhase)
	}
}
