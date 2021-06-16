package ru.bmstu.rk9.rao.ui.export;

import java.io.PrintWriter;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.lib.result.AbstractResult;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.ui.RaoActivatorExtension;
import ru.bmstu.rk9.rao.ui.results.ResultsParser;

public class ExportResultsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!ready())
			return null;

		exportResults();

		return null;
	}

	public final static void exportResults() {
		if (!ready())
			return;
		// TODO fix-0002
//		List<AbstractResult<?>> results = RaoActivatorExtension.getTargetSimulatorManager().getTargetSimulatorWrapper().getResults();
//
//		PrintWriter writer = ExportPrintWriter.initializeWriter(".res");
//		if (writer == null)
//			return;
//
//		writer.println(ResultsParser.parseAsString(results));
//		writer.close();
//
//		try {
//			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
//		} catch (CoreException e) {
//		}
	}

	private final static boolean ready() {
		return false;
		// TODO fix-0002
//		return RaoActivatorExtension.getTargetSimulatorManager().getTargetSimulatorWrapper().isInitialized() && !RaoActivatorExtension.getTargetSimulatorManager().getTargetSimulatorWrapper().getDatabase().getAllEntries().isEmpty();
	}
}
