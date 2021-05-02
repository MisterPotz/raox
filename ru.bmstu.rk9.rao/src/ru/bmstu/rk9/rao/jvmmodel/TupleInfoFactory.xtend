package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.RelevantResourceTuple
import org.eclipse.xtext.common.types.impl.TypesFactoryImpl
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import ru.bmstu.rk9.rao.jvmmodel.TupleInfoManager.TupleInfo
import ru.bmstu.rk9.rao.jvmmodel.TupleInfoManager.GenericTupleInfo
import ru.bmstu.rk9.rao.jvmmodel.TupleInfoManager.TupleElementInfo
import ru.bmstu.rk9.rao.jvmmodel.TupleInfoManager.GenericTupleElementInfo
import java.util.List
import java.util.Map
import java.util.HashMap
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

class TupleInfoFactory extends RaoEntityCompiler {
	
	new(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations) {
		super(jvmTypesBuilder, jvmTypeReferenceBuilder, associations)
	}
	
	def Map<RelevantResourceTuple, TupleInfo> createTuplesInfo(List<RelevantResourceTuple> tuples) {
		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			val tupleInfoMap = new HashMap<RelevantResourceTuple, TupleInfo>()
			val tupleInfoManager = new TupleInfoManager

			for (tuple : tuples) {
				val tupleSize = tuple.names.size
				var tupleName = "Tuple"
				for (name : tuple.names) {
					tupleName = tupleName + name.toFirstUpper
				}

				var GenericTupleInfo genericTupleInfo = new GenericTupleInfo(tupleSize)
				var boolean isUnique

				if (!tupleInfoManager.uniqueGenericTupleSizes.contains(tupleSize)) {
					genericTupleInfo = new GenericTupleInfo(tupleSize)
					tupleInfoManager.uniqueGenericTupleSizes.add(tupleSize)
					isUnique = true
				} else {
					isUnique = false
				}

				var tupleInfo = new TupleInfo(tupleName, genericTupleInfo, isUnique)

				for (name : tuple.names) {
					val index = tuple.names.indexOf(name)
					val paramName = name
					val typeParameter = createTypeParameter(name)
					val staticTypeParameter = createTypeParameter(name)
					tupleInfo.genericTupleInfo.typeReferencesArray.set(index, typeRef(typeParameter))
					tupleInfo.genericTupleInfo.staticTypeReferencesArray.set(index, typeRef(staticTypeParameter))
					tupleInfo.tupleElementsInfo +=
						new TupleElementInfo(paramName, new GenericTupleElementInfo(typeParameter, staticTypeParameter))
				}

				tupleInfoMap.put(tuple, tupleInfo)
			}

			return tupleInfoMap

		]

	}

	def createTypeParameter(String name) {
		return apply [ extension jvmTypesBuilder, extension jvmTypeReferenceBuilder |
			val typeParameter = TypesFactoryImpl.eINSTANCE.createJvmTypeParameter
			val constraint = TypesFactoryImpl.eINSTANCE.createJvmUpperBound
			constraint.typeReference = typeRef(ru.bmstu.rk9.rao.lib.resource.ComparableResource, {
				typeRef(typeParameter)
			})
			typeParameter.name = ru.bmstu.rk9.rao.jvmmodel.TupleInfoFactory.createTupleGenericTypeName(name)
			typeParameter.constraints += constraint

			return typeParameter

		]
	}

	def static createTupleGenericTypeName(String name) {
		return "__TupleType" + name.toFirstUpper
	}
}
