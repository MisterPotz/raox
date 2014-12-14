package ru.bmstu.rk9.rdo.ui.contributions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import ru.bmstu.rk9.rdo.generator.RDONaming;
import ru.bmstu.rk9.rdo.lib.DecisionPointSearch.SerializationLevel;
import ru.bmstu.rk9.rdo.lib.ModelStructureHelper;
import ru.bmstu.rk9.rdo.lib.TraceConfig;
import ru.bmstu.rk9.rdo.lib.TraceConfig.TraceNode;
import ru.bmstu.rk9.rdo.rdo.DecisionPoint;
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch;
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource;
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource;
import ru.bmstu.rk9.rdo.rdo.Pattern;
import ru.bmstu.rk9.rdo.rdo.RDOModel;
import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration;
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration;
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource;

public class RDOTraceConfigView extends ViewPart
{
	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOTraceConfigView";

	private static CheckboxTreeViewer traceTreeViewer;

	private static TraceConfig traceConfig = new TraceConfig();
	private static TraceConfigurator traceConfigurator =
		new TraceConfigurator();

	@Override
	public void createPartControl(Composite parent)
	{
		traceTreeViewer = new CheckboxTreeViewer(parent);
		Tree traceTree = traceTreeViewer.getTree();
		traceTree.setLayoutData(new GridLayout());
		traceTree.setLinesVisible(true);

		traceTreeViewer.setContentProvider(
			new RDOTraceConfigContentProvider());
		traceTreeViewer.setLabelProvider(
			new RDOTraceConfigLabelProvider());
		traceTreeViewer.setCheckStateProvider(
			new RDOTraceConfigCheckStateProvider());

		traceTreeViewer.addCheckStateListener(
			new ICheckStateListener()
			{
				@Override
				public void checkStateChanged(CheckStateChangedEvent event)
				{
					TraceNode node = (TraceNode) event.getElement();
					node.setTraceState(event.getChecked());
					if (event.getChecked())
					{
						traceTreeViewer.setSubtreeChecked(
							event.getElement(), true);
						node.traceVisibleChildren(true);
					}
				}
			}
		);

		IPartService service =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService();

		service.addPartListener(
			new IPartListener2()
			{
				@Override
				public void partVisible(IWorkbenchPartReference partRef) {}

				@Override
				public void partOpened(IWorkbenchPartReference partRef) {}

				@Override
				public void partInputChanged(IWorkbenchPartReference partRef) {}

				@Override
				public void partHidden(IWorkbenchPartReference partRef) {}

				@Override
				public void partDeactivated(IWorkbenchPartReference partRef) {}

				@Override
				public void partClosed(IWorkbenchPartReference partRef) {}

				@Override
				public void partBroughtToTop(IWorkbenchPartReference partRef) {}

				@Override
				public void partActivated(IWorkbenchPartReference partRef)
				{
					if (partRef.getId().equals("ru.bmstu.rk9.rdo.RDO"))
					{
						IEditorPart editor = partRef.getPage().getActiveEditor();
						IXtextDocument document =
							((XtextEditor) editor).getDocument();

						if (documents.contains(document))
							return;

						documents.add(document);

						RDOModel model =
							(RDOModel) document.readOnly(
							new IUnitOfWork<XtextResource, XtextResource>()
							{
								public XtextResource exec(XtextResource state)
								{
									return state;
								}
							}
						).getContents().get(0);

						updateInput(model.eResource());

						document.addModelListener(
							new IXtextModelListener() {
								@Override
								public void modelChanged(XtextResource resource)
								{
									updateInput(resource);
								}
							}
						);
					}
				}

				private final HashSet<IXtextDocument> documents =
					new HashSet<IXtextDocument>();
			}
		);

		traceConfigurator.initCategories(traceConfig.getRoot());
		traceTreeViewer.setInput(traceConfig);
	}

	public static void updateInput(Resource model)
	{
		traceConfig.setModelName(RDONaming.getResourceName(model));
		traceConfigurator.fillCategories(model, traceConfig.getRoot());
		if (RDOTraceConfigView.traceTreeViewer == null)
			return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(
			new Runnable()
				{
					@Override
					public void run()
					{
						RDOTraceConfigView.traceTreeViewer.refresh();
					}
				}
		);
	}

	public static void onModelSave()
	{
		traceConfig.getRoot().removeHiddenChildren();
	}

	public static final void initNames()
	{
		traceConfig.initNames();
	}

	@Override
	public void setFocus()
	{}
}

class TraceConfigurator
{
	public enum TraceCategory
	{
		RESOURCES("Resources", ResourceDeclaration.class),
		PATTERNS("Patterns", Pattern.class),
		DECISION_POINTS("Decision points", DecisionPoint.class),
		RESULTS("Results", ResultDeclaration.class);

		TraceCategory(String name, Class<?> cateforyClass)
		{
			this.name = name;
			this.categoryClass = cateforyClass;
		}

		private final String name;

		public final String getName()
		{
			return name;
		}

		//TODO doesn't seem like a good way to work with Class type
		public final Class getCategoryClass()
		{
			return categoryClass;
		}

		private final Class<?> categoryClass;
	}

	public final void fillCategories(Resource model, TraceNode node)
	{
		//TODO see comment to category.getCategoryClass()
		for (TraceCategory category : TraceCategory.values())
			fillCategory(
				node.getChildren().get(category.ordinal()),
				model.getAllContents(),
				category.getCategoryClass()
			);
	}

	private final <T extends EObject> void fillCategory(
		TraceNode category,
		TreeIterator<EObject> allContents,
		Class<T> categoryClass
	)
	{
		category.hideChildren();
		final ArrayList<T> categoryList =
			new ArrayList<T>();
		Iterator<T> filter = Iterators.<T>filter(allContents, categoryClass);
		Iterable<T> iterable = IteratorExtensions.<T>toIterable(filter);
		Iterables.addAll(categoryList, iterable);

		//TODO why don't name include model name here already?
		//the way it is now it differs from the names we get from database
		for (T c : categoryList)
		{
			TraceNode child = category.addChild(RDONaming.getNameGeneric(c));
			if (categoryClass == Pattern.class)
			{
				for (EObject relRes : c.eContents())
				{
					if (relRes instanceof RuleRelevantResource ||
						relRes instanceof EventRelevantResource ||
						relRes instanceof OperationRelevantResource)
					child.addChild(
						child.getName() + "." + RDONaming.getNameGeneric(relRes));
				}
			}
			else if (c instanceof DecisionPointSearch)
			{
				for (SerializationLevel type : SerializationLevel.values())
					child.addChild(child.getName() + "." + type.toString());
			}
		}
	}

	public final void initCategories(TraceNode node)
	{
		for (TraceCategory category : TraceCategory.values())
			node.addChild(category.getName());
	}
}

class RDOTraceConfigCheckStateProvider implements ICheckStateProvider
{
	@Override
	public boolean isChecked(Object element)
	{
		TraceNode node = (TraceNode) element;
		return node.isTraced();
	}

	@Override
	public boolean isGrayed(Object element)
	{
		return false;
	}
}

class RDOTraceConfigContentProvider implements ITreeContentProvider
{
	public void dispose() {}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	public Object[] getElements(Object inputElement)
	{
		TraceConfig traceConfig = (TraceConfig) inputElement;
		if (!traceConfig.getRoot().hasChildren())
			return null;
		return traceConfig.getRoot().getChildren().toArray();
	}

	public Object[] getChildren(Object parentElement)
	{
		TraceNode traceNode = (TraceNode) parentElement;
		if (!traceNode.hasChildren())
			return null;
		return traceNode.getChildren().toArray();
	}

	public Object getParent(Object element)
	{
		TraceNode traceNode = (TraceNode) element;
		return traceNode.getParent();
	}

	public boolean hasChildren(Object element)
	{
		TraceNode traceNode = (TraceNode) element;
		return traceNode.hasChildren();
	}
}

class RDOTraceConfigLabelProvider implements ILabelProvider
{
	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		TraceNode traceNode = (TraceNode) element;
		return ModelStructureHelper.getRelativeName(traceNode.getName());
	}

}