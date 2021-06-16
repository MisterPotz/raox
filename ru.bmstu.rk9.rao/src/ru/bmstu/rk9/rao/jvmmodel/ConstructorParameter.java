package ru.bmstu.rk9.rao.jvmmodel;

import org.eclipse.xtext.common.types.JvmFormalParameter;

public class ConstructorParameter {
	private final JvmFormalParameter parameter;
	private final boolean useHiddenName; 
	private final boolean initializeInConstructor;
	private final boolean addToSuperInitialization;
	private final String substitutionValue;
	private final boolean dontAddAsParam;
	
	private ConstructorParameter(
			JvmFormalParameter parameter, 
			boolean useHiddenName, 
			boolean initializeInConstructor, 
			boolean addToSuperInitialization,
			boolean dontAddAsParam,
			String substitutionValue) {
		super();
		this.parameter = parameter;
		this.useHiddenName = useHiddenName;
		this.initializeInConstructor = initializeInConstructor;
		this.addToSuperInitialization = addToSuperInitialization;
		this.substitutionValue = substitutionValue; 
		this.dontAddAsParam = dontAddAsParam;
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
	public boolean isDontAddAsParam() {
		return dontAddAsParam;
	}

	public String getSubstitutionValue() {
		if (substitutionValue == null) {
			return parameter.getName();
		}
		return substitutionValue;
	}

	public static class Builder {
		private JvmFormalParameter parameter;
		private boolean useHiddenName = false; 
		private boolean initializeInConstructor = false;
		private boolean addToSuperInitialization = false;
		private String substitutionValue = null;
		private boolean dontAddAsParam;

		public Builder parameter(JvmFormalParameter parameter) {
			this.parameter = parameter;
			return this;
		}
		public Builder useHiddenName() {
			this.useHiddenName = true;
			return this;
		}
		public Builder initializeInConstructor() {
			this.initializeInConstructor = true;
			return this;
		}
		public Builder addToSuperInitialization() {
			this.addToSuperInitialization = true;
			return this;
		}
		public Builder substituteValue(String substitutionValue) {
			this.substitutionValue = substitutionValue;
			return this;
		}
		
		public Builder dontAddAsParam() {
			this.dontAddAsParam = true;
			return this;
		}
		
		public ConstructorParameter build() {
			return new ConstructorParameter(parameter, useHiddenName, initializeInConstructor, addToSuperInitialization, dontAddAsParam, substitutionValue);
		}

	}
}
