package ru.bmstu.rk9.rao.ui.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ru.bmstu.rk9.rao.lib.varconst.VarConst;

public class VarConstManager {

	private final List<VarConst> varconsts;
	private List< List<Double> > combinations;
	
	
	public VarConstManager(List<VarConst> varconsts) {
		this.varconsts = varconsts;
		this.combinations = new ArrayList<>();
	}
	
	public List< List<Double> > getCombinations() {
		return (combinations);
	}
	
	public void generateCombinations() {
		if (varconsts == null || varconsts.isEmpty()) return;
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
	
	public HashMap<String, Double> listToHashMap(List<Double> values) {
		HashMap<String, Double> hashMap = new HashMap<>();
		
	    IntStream.range(0, Math.min(values.size(), varconsts.size()))
	            .mapToObj(i -> Map.entry(varconsts.get(i).getName(), values.get(i)))
	            .forEach(entry -> hashMap.put(entry.getKey(), entry.getValue()));
        return hashMap;
	}
	
	private List< List<Double> > generateValuesMatrix() {
		List< List<Double> > values = new ArrayList<>();
		
		for (VarConst vc : varconsts)
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
		
		for (VarConst vc : varconsts) {
			if (vc.checkValue(set) == false)
				return (false);
		}
		
		return (true);
	}
	
	private List<String> getVCNames() {
		List<String> vcNames = new ArrayList<>();
		
		for (VarConst vc : varconsts)
			vcNames.add(vc.getName());
		
		return (vcNames);
	}
}
