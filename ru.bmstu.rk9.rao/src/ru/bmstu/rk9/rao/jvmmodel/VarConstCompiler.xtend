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
import ru.bmstu.rk9.rao.rao.VarConst
import ru.bmstu.rk9.rao.jvmmodel.ProxyBuilderHelper.ConstructorBuilder
import ru.bmstu.rk9.rao.lib.contract.RaoGenerationContract

class VarConstCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	def asClass(VarConst varconst, JvmDeclaredType it, boolean isPreIndexingPhase, ProxyBuilderHelpersStorage storage) {

		val vcQualifiedName = QualifiedName.create(qualifiedName, varconst.name + "VarConst")
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations,varconst, true)
		storage.addNewProxyBuilder(pBH)
		
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
						.substituteValue(varconst.start)
						.build(),
					new ConstructorParameter.Builder()
						.parameter(varconst.toParameter("stop", typeRef(Double)))
						.addToSuperInitialization()
						.dontAddAsParam()
						.substituteValue(varconst.stop)
						.build(),
					new ConstructorParameter.Builder()
						.parameter(varconst.toParameter("step", typeRef(Double)))
						.addToSuperInitialization()
						.dontAddAsParam()
						.substituteValue(varconst.step)
						.build()
				)
				
				pBH.addAdditionalParentScopeMembers(varconst.toField(varconst.name, typeRef(RaoGenerationContract.VARCONST_VALUES_VALUE)) [
					static = false
					final = false
				],
					varconst.toMethod(getGetterName(varconst), typeRef(RaoGenerationContract.VARCONST_VALUES_VALUE)) [
					parameters += CodeGenerationUtil.createVarconstValuesParameter(jvmTypesBuilder, jvmTypeReferenceBuilder, varconst)
					body = '''
						return «RaoGenerationContract.VARCONST_VALUES_NAME».get("«varconst.name»");
					'''
					]
				)
				pBH.addCodeForParentScopeConstructor('''this.«varconst.name» = «getGetterName(varconst)»(«RaoGenerationContract.VARCONST_VALUES_NAME»);''')
				
				val cnstr = new ConstructorBuilder(jvmTypesBuilder, jvmTypeReferenceBuilder,varconst).setParameters(params).build()
				
				members += cnstr
				members += varconst.toMethod("getName", typeRef(String)) [
					visibility = JvmVisibility.PUBLIC
					final = true

					body = '''
						return "«varconst.name»";
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
	
	def static getGetterName(VarConst varconst) {
		return '''get«varconst.name.toFirstUpper()»Value'''
	}
}
