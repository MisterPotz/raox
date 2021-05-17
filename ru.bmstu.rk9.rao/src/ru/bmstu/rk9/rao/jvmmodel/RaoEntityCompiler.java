package ru.bmstu.rk9.rao.jvmmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;

import ru.bmstu.rk9.rao.rao.ResourceType;

public class RaoEntityCompiler {
	public static Boolean isSimulatorIdOn = false; 
	public static List<String> resourceTypes = new ArrayList<>();
	public static HashMap<String, JvmGenericType> entitiesToClasses = new HashMap<>();

	protected final JvmTypesBuilder jvmTypesBuilder;
	protected final JvmTypeReferenceBuilder jvmTypeReferenceBuilder;
	protected final IJvmModelAssociations associations;
	
	public RaoEntityCompiler(JvmTypesBuilder jvmTypesBuilder,
	 JvmTypeReferenceBuilder jvmTypeReferenceBuilder, IJvmModelAssociations associations) {
		this.jvmTypesBuilder = jvmTypesBuilder;
		this.jvmTypeReferenceBuilder = jvmTypeReferenceBuilder;
		this.associations = associations;
	}

	protected interface Extensioner<T> {
		T apply(JvmTypesBuilder b, JvmTypeReferenceBuilder tB);
	}
	
	protected <T> T apply(Extensioner<T> extensioner) {
		return extensioner.apply(jvmTypesBuilder, jvmTypeReferenceBuilder);
	}

	protected JvmAnnotationReference overrideAnnotation() {
		return this.apply(((JvmTypesBuilder b, JvmTypeReferenceBuilder tB) -> {
			JvmAnnotationReference anno = TypesFactory.eINSTANCE.createJvmAnnotationReference();
			JvmAnnotationType annoType = (JvmAnnotationType) tB.typeRef(Override.class).getType();
			anno.setAnnotation(annoType);
			return anno;
		}));
	}

	public static void rememberResourceType(ResourceType resourceType) {
		resourceTypes.add(resourceType.getName());
	}

	public static void addResourceClass(ResourceType resourceType, JvmGenericType genericType) {
		ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.entitiesToClasses.put(resourceType.getName(), genericType);
	}

	public static void cleanCachedResourceTypes() {
		resourceTypes.clear();
		ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.entitiesToClasses.clear();
	}
}
