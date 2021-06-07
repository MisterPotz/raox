package ru.bmstu.rk9.rao.ui.execution;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.utils.SimulatorReflectionUtils;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;
import ru.bmstu.rk9.rao.lib.simulator.ReflectionUtils;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.SimulationStopCode;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.export.ExportTraceHandler;
import ru.bmstu.rk9.rao.ui.raoview.ViewManager;
import ru.bmstu.rk9.rao.ui.raoview.ViewManager.ViewType;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;

@SuppressWarnings("restriction")
public class ExecutionJobProvider {
	private final IResourceSetProvider resourceSetProvider;
	private final IProject project;
	private final IBatchTypeResolver typeResolver;
	
	public ExecutionJobProvider(final IProject project, IResourceSetProvider resourceSetProvider,
			IBatchTypeResolver typeResolver) {
		this.project = project;
		this.resourceSetProvider = resourceSetProvider;
		this.typeResolver = typeResolver;
	}

	public final Job createExecutionJob() {
		final Job executionJob = new Job(project.getName() + " execution") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final ModelInternalsParser parser = new ModelInternalsParser(project, resourceSetProvider,
						typeResolver);
				try {
					parser.parse();
				} catch (Exception e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Model parsing failed", e);
				} finally {
					parser.closeClassLoader();
				}
				SerializationConfigView.initNames();

				VarConstManager varconsts = new VarConstManager(parser.getVarConsts());
				varconsts.generateCombinations();
								
				for (List<Double> iterable_element : varconsts.getCombinations().subList(0, 2)) {
					IStatus runningResult = runSeparateSimulator(
							varconsts.listToHashMap(iterable_element),
							parser);
					if (IStatus.ERROR == runningResult.getCode()) {
						return runningResult;
					}
				}
				return Status.OK_STATUS;

			}

			@Override
			public boolean belongsTo(Object family) {
				return ("rao_model_run").equals(family);
			}
		};

		executionJob.setPriority(Job.LONG);
		return executionJob;
	}
	
	private IStatus runSeparateSimulator(
			HashMap<String, Double> combination,
			ModelInternalsParser readyParser
			) {	
		final Display display = PlatformUI.getWorkbench().getDisplay();
		
		ISimulator simulator = new Simulator();
		SimulatorWrapper simulatorWrapper = new SimulatorWrapper(simulator);
		SimulatorManagerImpl.getInstance().addSimulator(simulator);

		ConsoleView consoleView = ViewManager.getViewFor(simulator.getSimulatorId(), ViewType.CONSOLE);
		consoleView.clearConsoleText();
		
		// TODO move to dependency from a simulator
		ExportTraceHandler.reset();
		
		// TODO this is where we must plan the creation of model instances and run the simulations
		try {
			/**
			 * change state of static context of model via running resourcePreinitializers
			 */
			simulatorWrapper.preinitialize(readyParser.getSimulatorPreinitializationInfo());
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Simulator preinitialization failed", e);
		}
		
		Object initializationScopeInstance = SimulatorReflectionUtils
				.getInitializationField(simulator.getModelInstance(), readyParser
						.getSimulatorPreinitializationInfo()
						.getSimulatorCommonModelInfo()) ;


		AnimationView animationView = ViewManager.getViewFor(simulator.getSimulatorId(), ViewType.ANIMATION);
		
		display.syncExec(
				() -> animationView.initialize(readyParser.getAnimationFrames().stream().map(frameConstructor -> {
					return ReflectionUtils.safeNewInstance(AnimationFrame.class, frameConstructor, initializationScopeInstance);
				}).collect(Collectors.toList())));

		try {
			/** launch init#run */
			simulatorWrapper.initialize(readyParser.getSimulatorInitializationInfo());
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Simulator initialization failed", e);
		}

		final long startTime = System.currentTimeMillis();
		StatusView statusView = ViewManager.getViewFor(simulator.getSimulatorId(), ViewType.STATUS);
		statusView.setStartTime(startTime);
		consoleView.addLine("Started model " + project.getName());

		SimulationStopCode simulationResult;

		try {
			simulationResult = simulatorWrapper.run();
		} catch (Throwable e) {
			e.printStackTrace();
			consoleView.addLine("Execution error\n");
			consoleView.addLine("Call stack:");
			consoleView.printStackTrace(e);
			simulatorWrapper.notifyError();

			if (e instanceof Error)
				throw e;

			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Execution failed", e);
		} finally {
			display.syncExec(() -> animationView.deinitialize());
		}

		switch (simulationResult) {
			case TERMINATE_CONDITION:
				consoleView.addLine("Stopped by terminate condition");
				break;
			case USER_INTERRUPT:
				consoleView.addLine("Model terminated by user");
				break;
			case NO_MORE_EVENTS:
				consoleView.addLine("No more events");
				break;
			default:
				consoleView.addLine("Runtime error");
				break;
			}

		ResultsView resultsView = ViewManager.getViewFor(simulator.getSimulatorId(), ViewType.RESULTS);

		display.asyncExec(() -> resultsView.update());

		consoleView.addLine("Time elapsed: " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");

		return Status.OK_STATUS;
	}
}
