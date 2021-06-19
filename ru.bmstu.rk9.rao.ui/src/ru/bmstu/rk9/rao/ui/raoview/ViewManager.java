package ru.bmstu.rk9.rao.ui.raoview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.ui.UiContract;


// TODO refactor-0005 we never use this class except for ViewType enum
public class ViewManager {
	protected final static Map<SimulatorId, SimulatorViews> simulatorViews = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends RaoView> T getViewFor(SimulatorId simulatorId, ViewType viewType) {
		if (!simulatorViews.containsKey(simulatorId)) {
			SimulatorViews newSimulatorViews = new SimulatorViews(simulatorId);

			simulatorViews.put(simulatorId, newSimulatorViews);
		}
		
		T view = (T) simulatorViews.get(simulatorId).getViewFor(viewType);
		// view.initialize(simulatorId, viewType.getId());
		return view;
	}
	
	public static <T extends RaoView> ArrayList<T> getAvailableViews(ViewType viewType) {
		ArrayList<T> views = new ArrayList<>();
		
		for (Map.Entry<SimulatorId, SimulatorViews> entry : simulatorViews.entrySet()) {
			views.add(entry.getValue().getViewFor(viewType));
		}
		
		return views;
	}

	public static class SimulatorViews {
		private final Map<ViewType, RaoView> views = new HashMap<>();
		private final SimulatorId simulatorId;


		public SimulatorViews(SimulatorId simulatorId) {
			this.simulatorId = simulatorId;
		}
		
		@SuppressWarnings("unchecked")
		public <T extends RaoView> T getViewFor(ViewType viewType) {
			if (!views.containsKey(viewType)) {
				// TODO: fix-0005 - opens view - shouldn't
				
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						try {
							RaoView newRaoView = (RaoView) PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow()
									.getActivePage()
									.showView(viewType.getId(), simulatorId.toString(), IWorkbenchPage.VIEW_CREATE);
							views.put(viewType, newRaoView);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				});
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
