package ru.bmstu.rk9.rao.jvmmodel;

import org.eclipse.xtext.common.types.JvmFormalParameter;

public class ConstructorParameter {
	private final JvmFormalParameter parameter;
	private final boolean useHiddenName; 
	private final boolean initializeInConstructor;
	private final boolean addToSuperInitialization;
	
	public ConstructorParameter(JvmFormalParameter parameter, boolean useHiddenName, 
			boolean initializeInConstructor, boolean addToSuperInitialization) {
		super();
		this.parameter = parameter;
		this.useHiddenName = useHiddenName;
		this.initializeInConstructor = initializeInConstructor;
		this.addToSuperInitialization = addToSuperInitialization; 
	}
	public JvmFormalParameter getParameter() {
		return parameter;
	}
	public boolean isUseHiddenName() {
		return useHiddenName;
	}
	public boolean isInitializeInConstructor() {
		return initializeInConstructor;
	}
	public boolean isAddToSuperInitialization() {
		return addToSuperInitialization;
	}
	
	
}
