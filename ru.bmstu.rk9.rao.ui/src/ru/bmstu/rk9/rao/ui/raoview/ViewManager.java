package ru.bmstu.rk9.rao.ui.raoview;

import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public class ViewManager {
	protected final static Map<SimulatorId, SimulatorViews> simulatorViews = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends RaoView> T getViewFor(SimulatorId simulatorId, ViewType viewType) {
		if (!simulatorViews.containsKey(simulatorId)) {
			SimulatorViews newSimulatorViews = new SimulatorViews();

			simulatorViews.put(simulatorId, newSimulatorViews);
		}
		return (T) simulatorViews.get(simulatorId).getViewFor(viewType);
	}

	public enum ViewType {
		CONSOLE, RESULTS, TRACE, PLOT, SERIALIZED_OBJECTS, STATUS, ANIMATION ;
	}

	public static class SimulatorViews {
		private final Map<ViewType, RaoView> views = new HashMap<>();
		
		@SuppressWarnings("unchecked")
		public <T extends RaoView> T getViewFor(ViewType viewType) {
			if (!views.containsKey(viewType)) {
				RaoView newRaoView = /* TODO create the necessary RaoView here */ new RaoView();
	
				views.put(viewType, newRaoView);
			}
			return (T) views.get(viewType);
		} 
	}
}
