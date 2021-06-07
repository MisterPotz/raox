package ru.bmstu.rk9.rao.ui.raoview;

import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.ui.UiContract;

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
	
	public enum ViewType {
		CONSOLE(UiContract.ID_CONSOLE_VIEW),
		RESULTS(UiContract.ID_RESULTS_VIEW),
		TRACE(UiContract.ID_TRACE_VIEW),
		PLOT(UiContract.ID_PLOT_VIEW), 
		SERIALIZED(UiContract.ID_SERIALIZEDOBJS_VIEW),
		STATUS(UiContract.ID_STATUS_VIEW),
		ANIMATION(UiContract.ID_ANIMATION_VIEW);

		private final String id;

		private ViewType(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}
}
