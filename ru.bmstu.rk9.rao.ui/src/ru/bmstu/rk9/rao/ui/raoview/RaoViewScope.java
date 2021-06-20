package ru.bmstu.rk9.rao.ui.raoview;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.ui.raoview.ViewManager.ViewType;

public class RaoViewScope {
	private final static HashMap<SimulatorId, SimulatorActions> plannedActions = new HashMap<>();
	
	/*
	 * if simulatorId == null we think its commmon actions for all simulators
	 */
	public static void plan(Action plannedAction, ViewType viewType, SimulatorId simulatorId) {
		if (!plannedActions.containsKey(simulatorId)) {
			SimulatorActions simulatorActions = new SimulatorActions(simulatorId);
			plannedActions.put(simulatorId, simulatorActions);
		}
		
		plannedActions.get(simulatorId).plan(plannedAction, viewType);
	}
	
	public static void applyAllCommandsTo(RaoView view, ViewType viewType, SimulatorId simulatorId) {
		applyCommandsTo(view, viewType, null);
		applyCommandsTo(view, viewType, simulatorId);
	}
	
	public static void applyCommandsTo(RaoView view, ViewType viewType, SimulatorId simulatorId) {
		plannedActions.get(simulatorId).applyCommandsTo(view, viewType);
	}
	
	public static class SimulatorActions {
		private final HashMap<ViewType, ArrayList<Action>> plannedActions = new HashMap<>();
		private final SimulatorId simulatorId;
		
		
		public SimulatorActions(SimulatorId simulatorId) {
			this.simulatorId = simulatorId;
		}
	
		public void plan(Action plannedAction, ViewType viewType) {
			if (!plannedActions.containsKey(viewType)) {
				plannedActions.put(viewType, new ArrayList<>());
			}
			plannedActions.get(viewType).add(plannedAction);
		}
		
		public <T extends RaoView> void applyCommandsTo(T receiver, ViewType viewType) {
			if (plannedActions.containsKey(viewType)) {
				for (Action action : plannedActions.get(viewType)) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							action.apply(receiver);					
						}
					});
				}
			}
		}
	}	
	
	public interface Action {
		public void apply(RaoView receiver);
	}	
}
