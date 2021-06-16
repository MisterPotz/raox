package ru.bmstu.rk9.rao.ui.monitorview;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.builder.clustering.CurrentDescriptions;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;
import ru.bmstu.rk9.rao.ui.UiContract;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.execution.ExecutionJobProvider;
import ru.bmstu.rk9.rao.ui.execution.ExecutionJobProvider.SystemSimulatorEvent;
import ru.bmstu.rk9.rao.ui.raoview.ViewManager.ViewType;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializedObjectsView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;
import ru.bmstu.rk9.rao.ui.trace.TraceView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

public class MonitorView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.MonitorView";
	
	private static TableViewer viewer;
	private List<ConditionalMenuItem> conditionalMenuItems = new ArrayList<ConditionalMenuItem>();
	private FilterHelper filterHelper = new FilterHelper();

	private final Subscriber newPlannedSimulatorSubscriber = new Subscriber(){
		@Override
		public void fireChangeWithPayload(Object object) {
			SimulatorId currenSimulatorId = (SimulatorId) object;

			SimulatorManagerImpl.getInstance().getSimulatorWrapper(currenSimulatorId).getExecutionStateNotifier().addSubscriber(new Subscriber(){

				@Override
				public void fireChange() {
					onChange(currenSimulatorId, "Running");
				}
			}, ExecutionState.EXECUTION_STARTED);

			SimulatorManagerImpl.getInstance().getSimulatorWrapper(currenSimulatorId).getExecutionStateNotifier().addSubscriber(new Subscriber(){

				@Override
				public void fireChange() {
					onChange(currenSimulatorId, "Finished");
				}
			}, ExecutionState.EXECUTION_COMPLETED);
		}

		@Override
		public void fireChange() {}
	};

	void onChange(SimulatorId simulatorId, String executionState) {
		TableItem currentRow = Arrays.stream(viewer.getTable().getItems())
		.filter(row -> row.getText(0).equals(simulatorId.toString())).collect(Collectors.toList()).get(0);

		currentRow.setText(1, executionState);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		initializeSubscribers();

		createViewer(parent);
		configureToolBar();
	}

	private void initializeSubscribers() {
		ExecutionJobProvider.systemSimulatorNotifier.addSubscriber(newPlannedSimulatorSubscriber, SystemSimulatorEvent.ADDED_NEW);
	}
	
	private final void configureToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		toolBarManager.add(new Action() {
			ImageDescriptor image;
			{
				image = ImageDescriptor.createFromURL(FileLocator.find(Platform.getBundle("ru.bmstu.rk9.rao.ui"),
						new org.eclipse.core.runtime.Path("icons/search.gif"), null));
				setImageDescriptor(image);
				setText("Filter by Status");
			}
			
			@Override
			public void run() {
				showFilterDialog();
			}
		});
	}
	
	private final void showFilterDialog() {
//		filterHelper.openDialog();
	}
	
	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		createColumns(viewer);

		Table table = viewer.getTable();
		createMenu(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(SimulatorManagerImpl.getInstance().getAvailableIds());
		getSite().setSelectionProvider(viewer);
		
		GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(gridData);
	}
	
	private void createColumns(TableViewer viewer) {
		String[] titles = {"Model ID", "Model Status"};
		int[] bounds = {100, 100};
		
//		First column creation
		TableViewerColumn column = createTableViewerColumn(viewer, titles[0], bounds[0], 0);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// TODO Auto-generated method stub
				SimulatorId simulatorId = (SimulatorId) element;
				
				return String.valueOf(simulatorId);
			}
		});
		
//		Second column creation
		column = createTableViewerColumn(viewer, titles[1], bounds[1], 1);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				SimulatorId simulatorId = (SimulatorId) element;			
				
				// TODO: make feature to get simulator status
				String simulatorState = new String("Not started");
				
				return simulatorState;
			}
		});		
	}

	// where do i choose column place? colIndex isnt used anywhere

	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound, int colIndex) {
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.None);
		TableColumn column = viewerColumn.getColumn();
		
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		
		return viewerColumn;
	}
	
	private void createMenu(Table table) {
		Menu menu = new Menu(table);

		conditionalMenuItems.add(ResultsView.createConditionalMenuItem(viewer, menu, ViewType.RESULTS));
		conditionalMenuItems.add(TraceView.createConditionalMenuItem(viewer,  menu, ViewType.TRACE));
		conditionalMenuItems.add(SerializedObjectsView.createConditionalMenuItem(viewer, menu, ViewType.SERIALIZED));
		conditionalMenuItems.add(AnimationView.createConditionalMenuItem(viewer, menu, ViewType.ANIMATION));
		conditionalMenuItems.add(StatusView.createConditionalMenuItem(viewer, menu, ViewType.STATUS));
		conditionalMenuItems.add(ConsoleView.createConditionalMenuItem(viewer, menu, ViewType.CONSOLE));
		
		table.setMenu(menu);
	}
	
	class FilterHelper {
		private MonitorFilterDialog currentDialog;
		private DialogState dialogState = DialogState.CLOSED;
		
		final void openDialog() {
			if (dialogState == DialogState.CLOSED) {
				currentDialog = new MonitorFilterDialog(viewer.getTable().getShell(), filterHelper);
				currentDialog.setBlockOnOpen(false);
				currentDialog.open();
				dialogState = DialogState.OPENED;
			} else {
				currentDialog.getShell().setFocus();
			}
		}
		
		final void closeDialog() {
			dialogState = DialogState.CLOSED;
		}
	
		final FilterResult findStatus(/* SimulatorStatus */Integer status) {
			List<SimulatorId> simulatorIds = SimulatorManagerImpl.getInstance().getAvailableIds();
			List<SimulatorId> filteredSimulatorIds = new ArrayList<>();
			boolean showAll = (status == null);
			
			if (simulatorIds== null)
				return FilterResult.NOT_FOUND;
			
			for (SimulatorId simulatorId : simulatorIds) {
				Simulator simulator = (Simulator) SimulatorManagerImpl.getInstance().getSimulator(simulatorId);
				if (showAll || /* simulator.getStatus() == status */ true)
					filteredSimulatorIds.add(simulatorId);
			}
			
			viewer.setInput(filteredSimulatorIds);
			if (filteredSimulatorIds.size() == 0)
				return FilterResult.NOT_FOUND;
			
			return FilterResult.FOUND;
			
		}
	}
		
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
