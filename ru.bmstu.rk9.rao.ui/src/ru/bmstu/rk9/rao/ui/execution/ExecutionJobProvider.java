package ru.bmstu.rk9.rao.ui.execution;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.SimulationStopCode;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.export.ExportTraceHandler;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessParsingException;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;

@SuppressWarnings("restriction")
public class ExecutionJobProvider {
	public ExecutionJobProvider(final IProject project, IResourceSetProvider resourceSetProvider,
			IBatchTypeResolver typeResolver) {
		this.project = project;
		this.resourceSetProvider = resourceSetProvider;
		this.typeResolver = typeResolver;
	}

	private final IResourceSetProvider resourceSetProvider;
	private final IProject project;
	private final IBatchTypeResolver typeResolver;

	public final Job createExecutionJob() {
		final Job executionJob = new Job(project.getName() + " execution") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final Display display = PlatformUI.getWorkbench().getDisplay();
				ArrayList<Simulator> simulators = new ArrayList<>();
				ArrayList<ModelInternalsParser> parsers = new ArrayList<>();
				
				for (int i =0; i < 4; i++) {
					Simulator simulator = simulators.get(i);
					ModelInternalsParser parser = parsers.get(i);
					simulators.add(simulator);
					parsers.add(parser);
				}
				// TODO mess directly below, generalize in some way?
				IStatus currentStatus = Status.OK_STATUS;
				
				for (int i =0; i < simulators.size(); i++) {
					Simulator simulator = simulators.get(i);
					ModelInternalsParser parser = new ModelInternalsParser(project, resourceSetProvider,
							typeResolver);
					IStatus newStatus =  runSeparateModel(simulator, parser);
					if (!newStatus.isOK() && currentStatus.isOK()) {
						currentStatus = newStatus;
					}
				}

				return currentStatus;
			}

			@Override
			public boolean belongsTo(Object family) {
				return ("rao_model_run").equals(family);
			}
		};

		executionJob.setPriority(Job.LONG);
		return executionJob;
	}

	private IStatus runSeparateModel(Simulator simulator, ModelInternalsParser modelInternalParser) {
		ConsoleView.clearConsoleText();

		final Display display = PlatformUI.getWorkbench().getDisplay();

		try {
			modelInternalParser.parse();
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Model parsing failed", e);
		} finally {
			modelInternalParser.closeClassLoader();
		}

		try {
			simulator.preinitilize(modelInternalParser.getSimulatorPreinitializationInfo());
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Simulator preinitialization failed", e);
		}

		try {
			modelInternalParser.postprocess();
		} catch (ProcessParsingException e) {
			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "invalid block parameter", e);
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Model postprocessing failed", e);
		}

		display.syncExec(() -> AnimationView.initialize(modelInternalParser.getAnimationFrames()));

		try {
			SimulatorWrapper.initialize(modelInternalParser.getSimulatorInitializationInfo());
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Simulator initialization failed", e);
		}

		final long startTime = System.currentTimeMillis();
		StatusView.setStartTime(startTime);
		ConsoleView.addLine("Started model " + project.getName());

		SimulationStopCode simulationResult;

		try {
			simulationResult = SimulatorWrapper.run();
		} catch (Throwable e) {
			e.printStackTrace();
			ConsoleView.addLine("Execution error\n");
			ConsoleView.addLine("Call stack:");
			ConsoleView.printStackTrace(e);
			SimulatorWrapper.notifyError();

			if (e instanceof Error)
				throw e;

			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Execution failed", e);
		} finally {
			display.syncExec(() -> AnimationView.deinitialize());
		}

		switch (simulationResult) {
		case TERMINATE_CONDITION:
			ConsoleView.addLine("Stopped by terminate condition");
			break;
		case USER_INTERRUPT:
			ConsoleView.addLine("Model terminated by user");
			break;
		case NO_MORE_EVENTS:
			ConsoleView.addLine("No more events");
			break;
		default:
			ConsoleView.addLine("Runtime error");
			break;
		}
		
		ExportTraceHandler.reset();
		SerializationConfigView.initNames();

		display.asyncExec(() -> ResultsView.update());

		ConsoleView.addLine("Time elapsed: " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");


		return Status.OK_STATUS;
	}
}
