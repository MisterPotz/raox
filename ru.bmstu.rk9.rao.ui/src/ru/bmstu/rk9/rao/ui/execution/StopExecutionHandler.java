package ru.bmstu.rk9.rao.ui.execution;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.ui.RaoActivatorExtension;

public class StopExecutionHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// todo https://www.notion.so/StopExecutionHandler-694e977a0f7d40608f8e5421431e40bd
//		RaoActivatorExtension.getTargetSimulatorManager().getTargetSimulatorWrapper().stopExecution();
		return null;
	}
}