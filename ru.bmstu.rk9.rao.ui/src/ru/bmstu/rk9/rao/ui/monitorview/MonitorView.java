package ru.bmstu.rk9.rao.ui.monitorview;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;
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
import java.util.List;
import java.util.Map.Entry;

public class MonitorView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.MonitorView1";
	
	private static TreeViewer treeViewer;
	private List<ConditionalMenuItem> conditionalMenuItems = new ArrayList<ConditionalMenuItem>();
	private FilterHelper filterHelper = new FilterHelper();

	private final Subscriber newPlannedSimulatorSubscriber = new Subscriber(){
		
		public boolean acceptsPayload() {
			return true;
		};
		
		@Override
		public void fireChangeWithPayload(Object object) {
			SimulatorId currenSimulatorId = (SimulatorId) object;

			SimulatorManagerImpl.getInstance().getSimulatorWrapper(currenSimulatorId).getExecutionStateNotifier().addSubscriber(new Subscriber(){

				@Override
				public void fireChange() {
					onChange(currenSimulatorId);
				}
			}, ExecutionState.EXECUTION_STARTED);

			SimulatorManagerImpl.getInstance().getSimulatorWrapper(currenSimulatorId).getExecutionStateNotifier().addSubscriber(new Subscriber(){

				@Override
				public void fireChange() {
					onChange(currenSimulatorId);
				}
			}, ExecutionState.EXECUTION_COMPLETED);
		}

		@Override
		public void fireChange() {}
	};
	
	public static void update() {
		treeViewer.setInput(SimulatorManagerImpl.getInstance().getAvailableIds());
	}

	void onChange(SimulatorId simulatorId) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				treeViewer.add(null, simulatorId);
			}
		});
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
		filterHelper.openDialog();
	}
	
	public static void clear() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				treeViewer.getTree().removeAll();
			}
		});
	}
	
	public static void addSimulator(SimulatorId simulatorId) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				treeViewer.add(new SimulatorId[] {simulatorId});
			}
		});
	}
	
	private void createViewer(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		createColumns(treeViewer);

		Tree tree = (Tree) treeViewer.getControl();
		
		tree.addSelectionListener(new SelectionListener() {		
			@Override
			public void widgetSelected(SelectionEvent e) {
			      TreeItem  item = (TreeItem) e.item;
			        if (item.getItemCount() > 0) {
			            item.setExpanded(!item.getExpanded());
			            // update the viewer
			            treeViewer.refresh();
			        }
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		Listener listener = new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
			      TreeItem treeItem = (TreeItem) event.item;
			      final TreeColumn[] treeColumns = treeItem.getParent().getColumns();
			      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
	
			         @Override
			         public void run() {
			            for (TreeColumn treeColumn : treeColumns)
			                 treeColumn.pack();
			         }
			      });
			}
		};

		tree.addListener(SWT.Expand, listener);
				
		createMenu(tree);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		treeViewer.setContentProvider(new ITreeContentProvider() {
			
			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof SimulatorId) {
					SimulatorId simulatorId = (SimulatorId) element;
					if (SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId).getVarConstSet() != null)
						return true;
				}
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				SimulatorWrapper simulatorWrapper = (SimulatorWrapper) element;
				return simulatorWrapper.getSimulatorId();
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				return ArrayContentProvider.getInstance().getElements(inputElement);
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				SimulatorId simulatorId = (SimulatorId) parentElement;
				
				ArrayList<String []> rowsArrayStrings = new ArrayList<>();
				
				for (Entry<String, Double> entry : SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId).getVarConstSet().entrySet()) {
					String[] row = new String[] {"", "", entry.getKey(), entry.getValue().toString()}; 
					rowsArrayStrings.add(row);
				}
				
				return rowsArrayStrings.toArray();
			}
		});
		
		getSite().setSelectionProvider(treeViewer);
		
		GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        treeViewer.getControl().setLayoutData(gridData);
	}
	
	private void createColumns(TreeViewer viewer) {
		String[] titles = {"Simulation", "Status", "Parameter", "Value"};
		int[] bounds = {90, 90, 100, 80};
		
//		First column creation
		TreeViewerColumn simulationViewerColumn = createTreeViewerColumn(viewer, titles[0], bounds[0], 0);
		simulationViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SimulatorId) {
					SimulatorId simulatorId = (SimulatorId) element;
					
					return simulatorId.toString();
				}
				return ((String []) element)[0];
			}
		});
		
//		Second column creation
		TreeViewerColumn statusViewerColumn = createTreeViewerColumn(viewer, titles[1], bounds[1], 1);
		statusViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SimulatorId) {
					SimulatorId simulatorId = (SimulatorId) element;
					
					return SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId).getExecutionState().toString();
				}
				return ((String []) element)[1];
			}
		});		
		
//		Third column creation
		TreeViewerColumn parameterViewerColumn = createTreeViewerColumn(viewer, titles[2], bounds[2], 2);
		parameterViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SimulatorId)
					return "";
				return ((String []) element)[2];
			}
		});		

//		Fourth column creation
		TreeViewerColumn valueViewerColumn = createTreeViewerColumn(viewer, titles[3], bounds[3], 3);
		valueViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SimulatorId)
					return "";
				return ((String []) element)[3];
			}
		});	
		
	}

	// TODO #refactor-0003 where do i choose column place? colIndex isnt used anywhere
	private TreeViewerColumn createTreeViewerColumn(TreeViewer treeViewer, String title, int bound, int colIndex) {
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.None);
		TreeColumn treeColumn = treeViewerColumn.getColumn();
		
		treeColumn.setText(title);
		treeColumn.setWidth(bound);
		
		return treeViewerColumn;
	}
	
	private void createMenu(Tree tree) {
		Menu menu = new Menu(tree);

		conditionalMenuItems.add(ResultsView.createConditionalMenuItem(treeViewer, menu, ViewType.RESULTS));
		conditionalMenuItems.add(TraceView.createConditionalMenuItem(treeViewer,  menu, ViewType.TRACE));
		conditionalMenuItems.add(SerializedObjectsView.createConditionalMenuItem(treeViewer, menu, ViewType.SERIALIZED));
		conditionalMenuItems.add(AnimationView.createConditionalMenuItem(treeViewer, menu, ViewType.ANIMATION));
		conditionalMenuItems.add(StatusView.createConditionalMenuItem(treeViewer, menu, ViewType.STATUS));
		conditionalMenuItems.add(ConsoleView.createConditionalMenuItem(treeViewer, menu, ViewType.CONSOLE));
		
		tree.setMenu(menu);
	}
	
	class FilterHelper {
		private MonitorFilterDialog currentDialog;
		private DialogState dialogState = DialogState.CLOSED;
		
		final void openDialog() {
			if (dialogState == DialogState.CLOSED) {
				currentDialog = new MonitorFilterDialog(treeViewer.getTree().getShell(), filterHelper);
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
	
		final FilterResult findStatus(ExecutionState status) {
			List<SimulatorId> simulatorIds = SimulatorManagerImpl.getInstance().getAvailableIds();
			List<SimulatorId> filteredSimulatorIds = new ArrayList<>();
			boolean showAll = (status == null);
			
			if (simulatorIds== null)
				return FilterResult.NOT_FOUND;
			
			for (SimulatorId simulatorId : simulatorIds) {
				SimulatorWrapper simulatorWrapper = (SimulatorWrapper) SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId);
				if (showAll || simulatorWrapper.getExecutionState() == status)
					filteredSimulatorIds.add(simulatorId);
			}
			
			treeViewer.setInput(filteredSimulatorIds);
			if (filteredSimulatorIds.size() == 0)
				return FilterResult.NOT_FOUND;
			
			return FilterResult.FOUND;
			
		}
	}
		
	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
}
