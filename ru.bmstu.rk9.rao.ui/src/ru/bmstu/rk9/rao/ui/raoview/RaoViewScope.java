package ru.bmstu.rk9.rao.ui.raoview;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.ui.raoview.ViewManager.ViewType;

public class RaoViewScope {
	private final static HashMap< ViewType, ArrayList<Action> > plannedActionsMap = new HashMap<>(); 
	
	public static void plan(Action plannedAction, ViewType viewType) {
		if (!plannedActionsMap.containsKey(viewType)) {
			plannedActionsMap.put(viewType, new ArrayList<>());
		}
		plannedActionsMap.get(viewType).add(plannedAction);
	}
	
	public static <T extends RaoView> void applyCommandsTo(T receiver, ViewType viewType) {
		for (Action action : plannedActionsMap.get(viewType)) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					action.apply(receiver);					
				}
			});
		}
	}
	
	public interface Action {
		public void apply(RaoView receiver);
	}
}
