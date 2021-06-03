package ru.bmstu.rk9.rao.lib.varconst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class VarConst {
	private double	start;
	private double	stop;
	private double	step;
	
	public abstract String getName();
	public abstract boolean checkValue(HashMap<String, Double> args);
	public abstract List<String> getAllDependencies();

	protected VarConst(double start, double stop, double step) {
		this.start = start;
		this.stop = stop;
		this.step = step;
	}

	public List<Double> getRangeList() {
		double tmp = this.getStart();
		List<Double> rangeList = new ArrayList<>();

		while (tmp < this.getStop()) {
			rangeList.add(tmp);
			tmp += this.getStep();
		}
		return rangeList;
	}

	public double getStart() {
		return this.start;
	}
	
	public double getStop() {
		return this.stop;
	}
	
	public double getStep() {
		return this.step;
	}
}
