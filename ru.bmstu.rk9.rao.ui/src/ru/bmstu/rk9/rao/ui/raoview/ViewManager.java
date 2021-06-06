package ru.bmstu.rk9.rao.ui.raoview;

import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public class ViewManager {
	protected final static Map<SimulatorId, SimulatorViews> simulatorViews = new HashMap<>();
	
	public static class SimulatorViews {
		private final Map<ViewType, RaoView> views = new HashMap<>();
		
		
	}
	
	public enum ViewType {
		CONSOLE, RESULTS, TRACE, PLOT, SERIALIZED, STATUS;
	}
	
	
	// TODO move the rao view creation here
}
