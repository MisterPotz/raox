package ru.bmstu.rk9.rao.jvmmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;

/**
 * @author algor
 *
 */
public class ProxyBuilderHelper {
	private final EObject sourceElement;
	private final boolean targetClassStatic;
	private EObject jvmClassElement;
	private final JvmTypesBuilder jvmTypesBuilder;
	private final IJvmModelAssociations associations;
	private final ProxyBuilderHelperUtil util;
	private JvmTypeReferenceBuilder jTRB;
	private final boolean createProxyBuilder;
	private final ArrayList<JvmOperation> delegateOperations = new ArrayList<JvmOperation>();

	public ProxyBuilderHelper(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jTRB,
			IJvmModelAssociations associations, EObject sourceElement, boolean isStatic, boolean createProxyBuilder) {
		this.sourceElement = sourceElement;
		this.targetClassStatic = isStatic;
		this.associations = associations;
		this.jvmTypesBuilder = jvmTypesBuilder;
		this.createProxyBuilder = createProxyBuilder;
		this.jTRB = jTRB;
		this.util = new ProxyBuilderHelperUtil(jvmTypesBuilder, targetClassStatic);
	}

	public JvmConstructor createConstructor(JvmFormalParameter... givenParams) {
		return jvmTypesBuilder.toConstructor(sourceElement, p -> {
			for (JvmFormalParameter param : givenParams) {
				p.getParameters().add(param);
			}

			if (isTargetClassStatic()) {
				p.getParameters().add(RaoEntityCompiler.createSimulatorIdParameter(sourceElement));
			}

			jvmTypesBuilder.setBody(p, util.createConstructorBody(p));
		});
	}

	public List<JvmField> createFields(JvmFormalParameter... givenParams) {
		List<JvmField> s = Arrays.asList(givenParams).stream().map(it -> {
			return jvmTypesBuilder.toField(sourceElement, it.getName(), it.getParameterType());
		}).collect(Collectors.toList());

		ArrayList<JvmField> toRet = new ArrayList<JvmField>();
		toRet.addAll(s);
		if (isTargetClassStatic()) {
			toRet.add(RaoEntityCompiler.createSimulatorIdField(sourceElement));
		}
		return toRet;
	}

	/**
	 * Must be run after all related functions are added to <members> of the
	 * sourceClass that this helper is related to
	 * 
	 * @param namesOfFunctionsToProxy names of functions that should be delegated
	 *                                from builder class to the original one
	 */
	public void rememberFunctionsToProxy(String... namesOfFunctionsToProxy) {
		// must get all operations currently associated with the object
		List<String> names = Arrays.asList(namesOfFunctionsToProxy);

		List<JvmOperation> operations = associations.getJvmElements(sourceElement).stream().filter(jvmEl -> {
			return (jvmEl instanceof JvmOperation);
		}).filter(jvmEl -> {
			String simpleName = ((JvmOperation) jvmEl).getSimpleName();
			return names.contains(simpleName);
		}).map(jv -> ((JvmOperation) jv)).collect(Collectors.toList());

		delegateOperations.clear();
		delegateOperations.addAll(operations);
	}

	// TODO create field for proxifying of source class
	// TODO create initialization lines for the proxifyment field and fields that
	// are required by source
	// classes to be in model scope (like ResourceDeclaration), so remember them and
	// then initialize in
	// the constructor of the model

	public EObject getJvmClassElement() {
		return jvmClassElement;
	}

	public void setJvmClassElement(EObject jvmClassElement) {
		this.jvmClassElement = jvmClassElement;
	}

	public EObject getSourceElement() {
		return sourceElement;
	}

	public boolean isTargetClassStatic() {
		return targetClassStatic;
	}
}
