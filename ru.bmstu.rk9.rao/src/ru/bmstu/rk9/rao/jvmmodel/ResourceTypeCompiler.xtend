package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.rao.ResourceType
import org.eclipse.xtext.naming.QualifiedName
import java.util.Collection
import org.eclipse.xtext.common.types.JvmPrimitiveType
import java.nio.ByteBuffer
import ru.bmstu.rk9.rao.rao.FieldDeclaration
import ru.bmstu.rk9.rao.lib.database.Database.DataType
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import java.util.ArrayList
import org.eclipse.xtext.common.types.JvmFormalParameter

class ResourceTypeCompiler extends RaoEntityCompiler {

	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
		IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}

	def asClass(ResourceType resourceType, JvmDeclaredType parentJvmObject, boolean isPreIndexingPhase, ProxyBuilderHelpersStorage storage) {

		val typeQualifiedName = QualifiedName.create(parentJvmObject.qualifiedName, resourceType.name)
		val pBH = new ProxyBuilderHelper(jvmTypesBuilder, jvmTypeReferenceBuilder, associations, resourceType, false);
		storage.addNewProxyBuilder(pBH)
		
		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |

			return resourceType.toClass(typeQualifiedName) [
				static = true

				superTypes += typeRef(ru.bmstu.rk9.rao.lib.resource.ComparableResource, {
					typeRef
				})

				val parametersList = new ArrayList<JvmFormalParameter>();

				parametersList.addAll(resourceType.parameters.map [
					resourceType.toParameter(it.declaration.name, it.declaration.parameterType)
				])

				members += pBH.createFieldsForBuildedClass(parametersList)
				members += pBH.createConstructorForBuildedClass(parametersList);
				members += pBH.createNecessaryMembersForBuildedClass()
				members += pBH.createSimulatorIdConstructorForBuildedClass()
				pBH.buildedClass = it
				
				pBH.associateBuilderClass [ features, builderClassType |
					builderClassType.members += resourceType.toMethod("create", typeRef) [
						visibility = JvmVisibility.PUBLIC
						for (param : resourceType.parameters)
							parameters += resourceType.toParameter(param.declaration.name, param.declaration.parameterType)
						body = '''
							«resourceType.name» resource = new «resourceType.name»(«String.join(", ", features.buildedClassParameters)»);
							ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().addResource(resource);
							ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(resource,
									ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.CREATED);
							return resource;
						'''

					]
					builderClassType.members += resourceType.toMethod("getAny", typeRef) [
						visibility = JvmVisibility.PUBLIC
						final = true
						body = '''
							return ru.bmstu.rk9.rao.lib.runtime.RaoCollectionExtensions.any(getAll());
						'''
					]

					builderClassType.members += resourceType.toMethod("getAll", typeRef(Collection, {
						typeRef
					})) [
						visibility = JvmVisibility.PUBLIC
						final = true
						body = '''
							return ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().getAll(«resourceType.name».class);
						'''
					]

					builderClassType.members += resourceType.toMethod("getAccessible", typeRef(Collection, {
						typeRef
					})) [
						visibility = JvmVisibility.PUBLIC
						final = true
						body = '''
							return ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().getAccessible(«resourceType.name».class);
						'''
					]
				]
				
				members += resourceType.toMethod("erase", typeRef(void)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += overrideAnnotation()
					body = '''
						ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().eraseResource(this);
						ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(this,
								ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.ERASED);
					'''
				]
				
				
				for (param : resourceType.parameters) {
					members +=
						resourceType.toMethod("get" + param.declaration.name.toFirstUpper, param.declaration.parameterType) [
							body = '''
								return «param.declaration.name»;
							'''
						]
					members += resourceType.toMethod("set" + param.declaration.name.toFirstUpper, typeRef(void)) [
						parameters += resourceType.toParameter(param.declaration.name, param.declaration.parameterType)
						body = '''
							«resourceType.name» actual = this;
							
							if (isShallowCopy)
								actual = ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().copyOnWrite(this);
							
							actual.«param.declaration.name» = «param.declaration.name»;
							ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(actual,
									ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.ALTERED);
						'''
					]
				}
				
				
				members += resourceType.toMethod("checkEqual", typeRef(boolean)) [ m |
					m.visibility = JvmVisibility.PUBLIC
					m.parameters += resourceType.toParameter("other", typeRef)
					m.annotations += overrideAnnotation()
					m.body = '''
						«IF resourceType.parameters.isEmpty»
							return true;
						«ELSE»
							return «String.join(" && ", resourceType.parameters.map[ p |
						'''«IF p.declaration.parameterType.type instanceof JvmPrimitiveType
								»this.«p.declaration.name» == other.«p.declaration.name»«
							ELSE
								»this.«p.declaration.name».equals(other.«p.declaration.name»)«
							ENDIF»
						'''
					])»;
						«ENDIF»
					'''
				]
				
				members += resourceType.toMethod("deepCopy", typeRef) [
					visibility = JvmVisibility.PUBLIC
					annotations += overrideAnnotation
					body = '''
							«resourceType.name» copy = new «resourceType.name»(«SimulatorIdContract.SIMULATOR_ID_NAME»);	
							copy.setNumber(this.number);
							copy.setName(this.name);
							«FOR param : resourceType.parameters»
							copy.«param.declaration.name» = this.«param.declaration.name»;
							«ENDFOR»
						
							return copy;
					'''
				]
				
				
				members += resourceType.toMethod("getTypeName", typeRef(String)) [
					visibility = JvmVisibility.	PUBLIC
					final = true
					annotations += overrideAnnotation()
					body = '''
						return "«typeQualifiedName»";
					'''
				]

				members += resourceType.toMethod("serialize", typeRef(ByteBuffer)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += overrideAnnotation()

					var size = 0
					for (param : resourceType.parameters) {
						size = size + param.getSize()
					}
					val fixedWidthParametersSize = size
					val variableWidthParameters = resourceType.parameters.filter[!isFixedWidth]
					val fixedWidthParameters = resourceType.parameters.filter[isFixedWidth]

					body = '''
						int _totalSize = «fixedWidthParametersSize»;
						java.util.List<Integer> _positions = new java.util.ArrayList<>();
						
						int _currentPosition = «fixedWidthParametersSize + variableWidthParameters.size * DataType.INT.size»;
						«FOR param : variableWidthParameters»
							_positions.add(_currentPosition);
							String «param.declaration.name»Value = String.valueOf(«param.declaration.name»);
							byte[] «param.declaration.name»Bytes = «param.declaration.name»Value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
							int «param.declaration.name»Length = «param.declaration.name»Bytes.length;
							_currentPosition += «param.declaration.name»Length + «DataType.INT.size»;
							_totalSize += «param.declaration.name»Length + «2 * DataType.INT.size»;
						«ENDFOR»
						
						ByteBuffer buffer = ByteBuffer.allocate(_totalSize);
						
						«FOR param : fixedWidthParameters»
							buffer.«param.serializeAsFixedWidth»;
						«ENDFOR»
						
						java.util.Iterator<Integer> _it = _positions.iterator();
						«FOR param : variableWidthParameters»
							buffer.putInt(_it.next());
						«ENDFOR»
						
						«FOR param : variableWidthParameters»
							buffer.putInt(«param.declaration.name»Length);
							buffer.put(«param.declaration.name»Bytes);
						«ENDFOR»
						
						return buffer;
					'''
				]

			]

		]
	}

	def private static getSize(FieldDeclaration param) {
		return DataType.getByName(param.declaration.parameterType.simpleName).size
	}

	def private static isFixedWidth(FieldDeclaration param) {
		return param.getSize != 0
	}

	def private static serializeAsFixedWidth(FieldDeclaration param) {
		val type = DataType.getByName(param.declaration.parameterType.simpleName)
		switch type {
			case INT:
				return '''putInt(«param.declaration.name»)'''
			case DOUBLE:
				return '''putDouble(«param.declaration.name»)'''
			case BOOLEAN:
				return '''put(«param.declaration.name» ? (byte)1 : (byte)0)'''
			default:
				return '''/* INTERNAL ERROR: attempting to serialize type «param.declaration.parameterType.simpleName» as fixed width type */'''
		}
	}
}
