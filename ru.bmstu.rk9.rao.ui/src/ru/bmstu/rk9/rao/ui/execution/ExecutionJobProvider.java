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
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;
import ru.bmstu.rk9.rao.lib.simulator.ReflectionUtils;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.SimulationStopCode;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.export.ExportTraceHandler;
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
				ConsoleView.clearConsoleText();
				ExportTraceHandler.reset();
				SerializationConfigView.initNames();
				SimulatorPreinitializationInfo preinitializationInfo = parser.getSimulatorPreinitializationInfo();

				
				/**
				 * TODO: maybe use static class methods and varconst array as a argument to generateCombinations method so 
				 * generating looks like -> combinations = VarConstManager.generateCombinations(parser.getVarConst())
				 * Now u need to use getCombinations method to get them
				*/
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
		ConsoleView.clearConsoleText();
		ExportTraceHandler.reset();
		SerializationConfigView.initNames();
		
		ISimulator simulator = new Simulator();
		SimulatorWrapper simulatorWrapper = new SimulatorWrapper(simulator);
		SimulatorManagerImpl.getInstance().addSimulator(simulator);

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


		display.syncExec(
				() -> AnimationView.initialize(readyParser.getAnimationFrames().stream().map(frameConstructor -> {
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
		StatusView.setStartTime(startTime);
		ConsoleView.addLine("Started model " + project.getName());

		SimulationStopCode simulationResult;

		try {
			simulationResult = simulatorWrapper.run();
		} catch (Throwable e) {
			e.printStackTrace();
			ConsoleView.addLine("Execution error\n");
			ConsoleView.addLine("Call stack:");
			ConsoleView.printStackTrace(e);
			simulatorWrapper.notifyError();

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

		ResultsView resultsView = ResultsView.getViewFor(simulator.getSimulatorId());

		display.asyncExec(() -> resultsView.update());

		ConsoleView.addLine("Time elapsed: " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");

		return Status.OK_STATUS;
	}
}
