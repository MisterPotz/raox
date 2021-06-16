package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import ru.bmstu.rk9.rao.lib.lambdaexpression.LambdaExpression
import org.eclipse.xtext.common.types.impl.JvmFormalParameterImplCustom
import ru.bmstu.rk9.rao.rao.VarConst
import java.lang.reflect.Constructor
import javax.management.ConstructorParameters
import ru.bmstu.rk9.rao.jvmmodel.ProxyBuilderHelper.ConstructorBuilder

class VarConstCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	def asClass(VarConst varconst, JvmDeclaredType it, boolean isPreIndexingPhase) {

		val vcQualifiedName = QualifiedName.create(qualifiedName, varconst.name + "VarConst")

		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			return varconst.toClass(vcQualifiedName) [
				static = true
				superTypes += typeRef(ru.bmstu.rk9.rao.lib.varconst.VarConst)

				if (varconst.lambda !== null)
					members += varconst.lambda.toField("lambda", typeRef(LambdaExpression))

				val params = Arrays.asList(
					new ConstructorParameter.Builder()
					.parameter(varconst.toParameter("start", typeRef(Double)))
					.addToSuperInitialization()
					.dontAddAsParam()
					.substituteValue(varconst.start).build(),
					new ConstructorParameter.Builder()
					.parameter(varconst.toParameter("stop", typeRef(Double)))
					.addToSuperInitialization()
					.dontAddAsParam()
					.substituteValue(varconst.stop).build(),
					new ConstructorParameter.Builder()
					.parameter(varconst.toParameter("step", typeRef(Double)))
					.addToSuperInitialization()
					.dontAddAsParam()
					.substituteValue(varconst.step).build()
				)
				
				val cnstr = new ConstructorBuilder(jvmTypesBuilder, jvmTypeReferenceBuilder,varconst).setParameters(params).build()
				
				members += cnstr
				members += varconst.toMethod("getName", typeRef(String)) [
					visibility = JvmVisibility.PUBLIC
					final = true

					body = '''
						return "«vcQualifiedName»";
					'''
				]

				members += varconst.toMethod("checkValue", typeRef(Boolean)) [
					visibility = JvmVisibility.PUBLIC

					parameters += varconst.toParameter("args", typeRef(HashMap, typeRef(String), typeRef(Double)))

					if (varconst.lambda !== null) {
						body = '''
							«FOR param : varconst.lambda.parameters»
								Double «param.name»;
							«ENDFOR»
							
							«FOR param : varconst.lambda.parameters»
								«param.name» = args.get("«param.name»");
							«ENDFOR»
							
							return this.checkLambda(«FOR o : varconst.lambda.parameters»«o.name»«IF varconst.lambda.parameters.indexOf(o) != varconst.lambda.parameters.size - 1», «ENDIF»«ENDFOR»);
						'''
					} else
						body = '''
							return true;
						'''
				]

				if (varconst.lambda !== null) {
					members += varconst.toMethod("checkLambda", typeRef(Boolean)) [
						visibility = JvmVisibility.PRIVATE

						for (param : varconst.lambda.parameters) {
							parameters += varconst.toParameter(param.name, typeRef(Double))
						}
						body = varconst.lambda.body
					]
				}

				members += varconst.toMethod("getAllDependencies", typeRef(ArrayList, typeRef(String))) [
					visibility = JvmVisibility.PUBLIC
					if (varconst.lambda !== null) {
						body = '''
							return new ArrayList<String>(«Arrays».asList(new String[] {«FOR o : varconst.lambda.parameters»"«o.name»"«IF varconst.lambda.parameters.indexOf(o) != varconst.lambda.parameters.size - 1», «ENDIF»«ENDFOR»}));
						'''
					} else {
						body = '''
							return new ArrayList<String>();
						'''
					}
				]
			]

		]
	}
}
