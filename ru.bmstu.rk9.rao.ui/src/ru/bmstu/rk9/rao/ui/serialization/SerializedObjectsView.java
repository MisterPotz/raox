package ru.bmstu.rk9.rao.ui.serialization;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.IndexType;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.RaoActivatorExtension;
import ru.bmstu.rk9.rao.ui.graph.GraphPanel;
import ru.bmstu.rk9.rao.ui.notification.RealTimeSubscriberManager;
import ru.bmstu.rk9.rao.ui.plot.PlotView;
import ru.bmstu.rk9.rao.ui.simulation.SimulatorLifecycleListener;
import ru.bmstu.rk9.rao.ui.simulation.UiSimulatorDependent;

public class SerializedObjectsView extends ViewPart implements UiSimulatorDependent {

	static TreeViewer serializedObjectsTreeViewer;
	public static final String ID = "ru.bmstu.rk9.rao.ui.SerializedObjectsView";
	private List<ConditionalMenuItem> conditionalMenuItems = new ArrayList<ConditionalMenuItem>();
	private static SimulatorLifecycleListener listener = new SimulatorLifecycleListener();
	
	public abstract static class ConditionalMenuItem extends MenuItem {

		public ConditionalMenuItem(Menu parent, String name) {
			super(parent, SWT.CASCADE);
			setText(name);

			parent.addListener(SWT.Show, new Listener() {
				@Override
				public void handleEvent(Event event) {
					CollectedDataNode node = (CollectedDataNode) serializedObjectsTreeViewer.getTree().getSelection()[0]
							.getData();
					setEnabled(isEnabled(node));
				}
			});

			addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					CollectedDataNode node = (CollectedDataNode) serializedObjectsTreeViewer.getTree().getSelection()[0]
							.getData();
					show(node);
				}
			});
		}

		@Override
		protected final void checkSubclass() {
		}

		abstract public boolean isEnabled(CollectedDataNode node);

		abstract public void show(CollectedDataNode node);
	}

	@Override
	public void createPartControl(Composite parent) {
		serializedObjectsTreeViewer = new TreeViewer(parent);
		Tree serializedObjectsTree = serializedObjectsTreeViewer.getTree();
		serializedObjectsTree.setLayoutData(new GridLayout());
		serializedObjectsTree.setLinesVisible(true);

		serializedObjectsTreeViewer.setContentProvider(new RaoSerializedObjectsContentProvider());
		serializedObjectsTreeViewer.setLabelProvider(new RaoSerializedObjectsLabelProvider());

		Menu popupMenu = new Menu(serializedObjectsTreeViewer.getTree());
		serializedObjectsTree.setMenu(popupMenu);
		conditionalMenuItems.add(PlotView.createConditionalMenuItem(popupMenu));
		conditionalMenuItems.add(GraphPanel.createConditionalMenuItem(popupMenu));
		conditionalMenuItems.add(PlotView.createConditionalMenuItemExportCsv(popupMenu));
		conditionalMenuItems.add(PlotView.createConditionalMenuItemExportJson(popupMenu));
		serializedObjectsTreeViewer.getTree().setMenu(popupMenu);

		serializedObjectsTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				final TreeItem treeItem = serializedObjectsTreeViewer.getTree().getSelection()[0];
				final CollectedDataNode node = (CollectedDataNode) treeItem.getData();
				if (node == null)
					return;

				if (treeItem.getItemCount() == 0) {
					List<ConditionalMenuItem> enabledMenuItems = new ArrayList<ConditionalMenuItem>();
					for (ConditionalMenuItem conditionalMenuItem : conditionalMenuItems) {
						if (conditionalMenuItem.isEnabled(node))
							enabledMenuItems.add(conditionalMenuItem);
					}

					if (enabledMenuItems.size() == 1)
						enabledMenuItems.get(0).show(node);
				} else {
					if (serializedObjectsTreeViewer.getExpandedState(node)) {
						serializedObjectsTreeViewer.collapseToLevel(node, 1);
					} else {
						serializedObjectsTreeViewer.expandToLevel(node, 1);
					}
				}
			}
		});

		initializeSubscribers();
	}

	@Override
	public void dispose() {
		deinitializeSubscribers();
		super.dispose();
	}
	
	private final void initializeSubscribers() {
		listener.asSimulatorOnAndOnPostChange((event) -> {
			if (subscriberSubscriberManager == null) {
				subscriberSubscriberManager = new SimulatorSubscriberManager(getTargetSimulatorId());
			}
			if (realTimeSubscriberManager == null) {
				realTimeSubscriberManager = new RealTimeSubscriberManager();
			}
			subscriberSubscriberManager.initialize(
					Arrays.asList(new SimulatorSubscriberInfo(commonSubscriber, ExecutionState.EXECUTION_STARTED),
							new SimulatorSubscriberInfo(commonSubscriber, ExecutionState.EXECUTION_COMPLETED)));
			realTimeSubscriberManager.initialize(Arrays.asList(realTimeUpdateRunnable));
		});
		listener.asSimulatorPreOffAndPreChange((event) -> {
			deinitializeSubscribers();
		});
	}

	private final void deinitializeSubscribers() {
		if (subscriberSubscriberManager != null) {
			subscriberSubscriberManager.deinitialize();
		}
		if (realTimeSubscriberManager != null) {
			realTimeSubscriberManager.deinitialize();
		}
		subscriberSubscriberManager = null;
		realTimeSubscriberManager = null;
	}

	private SimulatorSubscriberManager subscriberSubscriberManager;
	private RealTimeSubscriberManager realTimeSubscriberManager;

	@Override
	public void setFocus() {
	}

	public final static boolean readyForInput() {
		return serializedObjectsTreeViewer != null && !serializedObjectsTreeViewer.getTree().isDisposed()
				&& serializedObjectsTreeViewer.getContentProvider() != null
				&& serializedObjectsTreeViewer.getLabelProvider() != null;
	}

	public static final Runnable realTimeUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			if (readyForInput()) {
				SerializedObjectsView.serializedObjectsTreeViewer.refresh();
			}
		}
	};

	public static final Subscriber commonSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			if (!readyForInput())
				return;

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!readyForInput())
						return;

					CollectedDataNode root = RaoActivatorExtension.getTargetSimulatorManager()
							.getTargetSimulatorWrapper().getDatabase().getIndexHelper().getTree();

					PlatformUI.getWorkbench().getDisplay().asyncExec(() -> serializedObjectsTreeViewer.setInput(root));
				}
			});
		}
	};
}

class RaoSerializedObjectsContentProvider implements ITreeContentProvider {
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		CollectedDataNode root = (CollectedDataNode) inputElement;
		if (!root.hasChildren())
			return new Object[] {};
		return root.getChildren().values().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		CollectedDataNode parent = (CollectedDataNode) parentElement;
		if (!parent.hasChildren())
			return new Object[] {};
		return parent.getChildren().values().toArray();
	}

	@Override
	public Object getParent(Object element) {
		CollectedDataNode node = (CollectedDataNode) element;
		return node.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		CollectedDataNode node = (CollectedDataNode) element;
		return node.hasChildren();
	}
}

class RaoSerializedObjectsLabelProvider implements ILabelProvider, IColorProvider {
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object element) {
		final CollectedDataNode collectedDataNode = (CollectedDataNode) element;
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final ImageDescriptor img = ImageDescriptor.createFromURL(getImageUrl(collectedDataNode));

		return new Image(display, img.getImageData());
	}

	final private URL getImageUrl(final CollectedDataNode collectedDataNode) {
		final URL imageUrl;
		final Index index = collectedDataNode.getIndex();

		if (index == null) {
			imageUrl = FileLocator.find(Platform.getBundle("ru.bmstu.rk9.rao.ui"),
					new org.eclipse.core.runtime.Path("icons/cross-small-white.png"), null);
		} else if (index.getType() == IndexType.RESOURCE && ((ResourceIndex) index).isErased()) {
			imageUrl = FileLocator.find(Platform.getBundle("ru.bmstu.rk9.rao.ui"),
					new org.eclipse.core.runtime.Path("icons/cross.png"), null);
		} else {
			imageUrl = FileLocator.find(Platform.getBundle("ru.bmstu.rk9.rao.ui"),
					new org.eclipse.core.runtime.Path("icons/globe-small.png"), null);
		}

		return imageUrl;
	}

	@Override
	public String getText(Object element) {
		CollectedDataNode node = (CollectedDataNode) element;
		return node.getName();
	}

	@Override
	public Color getForeground(Object element) {
		CollectedDataNode node = (CollectedDataNode) element;
		Index index = node.getIndex();
		if (index != null && index.getType() == IndexType.RESOURCE && ((ResourceIndex) index).isErased()) {
			Display display = PlatformUI.getWorkbench().getDisplay();
			return new Color(display, 0x88, 0x88, 0x88);
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}
}
