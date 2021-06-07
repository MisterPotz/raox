package ru.bmstu.rk9.rao.ui.raoview;

import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.ui.UiContract;

public class ViewManager {
	protected final static Map<SimulatorId, SimulatorViews> simulatorViews = new HashMap<>();
	
	public static class SimulatorViews {
		private final Map<ViewType, RaoView> views = new HashMap<>();
	}
	
	public enum ViewType {
		CONSOLE(UiContract.ID_CONSOLE_VIEW),
		RESULTS(UiContract.ID_RESULTS_VIEW),
		TRACE(UiContract.ID_TRACE_VIEW),
		PLOT(UiContract.ID_PLOT_VIEW), 
		SERIALIZED(UiContract.ID_SERIALIZEDOBJS_VIEW),
		STATUS(UiContract.ID_STATUS_VIEW);

		private final String id;

		private ViewType(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}
	
	
	// TODO move the rao view creation here
}
