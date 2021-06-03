package ru.bmstu.rk9.rao.ui.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ru.bmstu.rk9.rao.lib.varconst.VarConst;

public class VarConstManager {

	private final List<VarConst> vcs;
	private List< List<Double> > combinations;
	
	
	public VarConstManager(List<VarConst> vcs) {
		this.vcs = vcs;
		this.combinations = new ArrayList<>();
	}
	
	public List< List<Double> > getCombinations() {
		return (combinations);
	}
	
	public void generateCombinations() {
		List< List<Double> > values = generateValuesMatrix();
		
		generateCombinations(values, 0, new ArrayList<>());
	}
	
	private void generateCombinations(	List< List<Double> > values,
										int depth,
										List<Double> combination) {
		if (depth == values.size()) {
			if (isValidCombination(combination))
				combinations.add(new ArrayList<>(combination));
			return ;
		} 

		for (Double number : values.get(depth)) {
			combination.add(number);
			generateCombinations(values, depth + 1, combination);
			combination.remove(combination.size() - 1);
		}
	}
	
	private List< List<Double> > generateValuesMatrix() {
		List< List<Double> > values = new ArrayList<>();
		
		for (VarConst vc : vcs)
			values.add(vc.getRangeList());
		
		return (values);
	}
	
	private boolean isValidCombination(List<Double> combination) {
		HashMap<String, Double> set = new HashMap<String, Double>() {{
			
			List<String> vcNames = getVCNames();
			
			for (int i = 0; i < vcNames.size(); i++) {
				put(vcNames.get(i), combination.get(i));
			}
		}};
		
		for (VarConst vc : vcs) {
			if (vc.checkValue(set) == false)
				return (false);
		}
		
		return (true);
	}
	
	private List<String> getVCNames() {
		List<String> vcNames = new ArrayList<>();
		
		for (VarConst vc : vcs)
			vcNames.add(vc.getName());
		
		return (vcNames);
	}
}
