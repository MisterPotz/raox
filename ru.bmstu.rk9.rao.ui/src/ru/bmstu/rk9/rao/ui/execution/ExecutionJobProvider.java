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
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.utils.SimulatorReflectionUtils;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;
import ru.bmstu.rk9.rao.lib.simulator.ReflectionUtils;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.SimulationStopCode;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.export.ExportTraceHandler;
import ru.bmstu.rk9.rao.ui.monitorview.MonitorView;
import ru.bmstu.rk9.rao.ui.raoview.RaoViewScope;
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

	public enum SystemSimulatorEvent {
		ADDED_NEW
	}

	public static Notifier<SystemSimulatorEvent> systemSimulatorNotifier = new Notifier<>(SystemSimulatorEvent.class);

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
				
				MonitorView.clear();
				VarConstManager varconsts = new VarConstManager(parser.getVarConsts());
				varconsts.generateCombinations(); 
								
				for (List<Double> iterable_element : varconsts.getCombinations().subList(0, Math.min(2, varconsts.getCombinations().size()))) {
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
		simulatorWrapper.setVarConstSet(combination);
		SimulatorManagerImpl.getInstance().addSimulatorWrapper(simulatorWrapper);
		
		MonitorView.addSimulator(simulator.getSimulatorId());
		
		RaoViewScope.plan(view -> {
			((ConsoleView) view).clearConsoleText();
		}, ViewType.CONSOLE, simulator.getSimulatorId());
		
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


//		AnimationView animationView = ViewManager.getViewFor(simulator.getSimulatorId(), ViewType.ANIMATION);
		
//		TODO fix-0005 - exception - frames == null
//		display.syncExec(
//				() -> animationView.initialize(readyParser.getAnimationFrames().stream().map(frameConstructor -> {
//					return ReflectionUtils.safeNewInstance(AnimationFrame.class, frameConstructor, initializationScopeInstance);
//				}).collect(Collectors.toList())));

		try {
			/** launch init#run */
			simulatorWrapper.initialize(readyParser.getSimulatorInitializationInfo());
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Simulator initialization failed", e);
		}
		
		systemSimulatorNotifier.notifySubscribers(SystemSimulatorEvent.ADDED_NEW, simulator.getSimulatorId());

		final long startTime = System.currentTimeMillis();
		RaoViewScope.plan(view -> {
			((StatusView) view).setStartTime(startTime);
		}, ViewType.STATUS, simulator.getSimulatorId());
		
		RaoViewScope.plan(view -> {
			((ConsoleView) view).addLine("Started model " + project.getName());
		}, ViewType.CONSOLE, simulator.getSimulatorId());
		

		SimulationStopCode simulationResult;

		try {
			simulationResult = simulatorWrapper.run();
		} catch (Throwable e) {
			e.printStackTrace();
			
			RaoViewScope.plan(view -> {
				ConsoleView consoleView = (ConsoleView) view;
				consoleView.addLine("Execution error\n");
				consoleView.addLine("Call stack:");
				consoleView.printStackTrace(e);
				
			}, ViewType.CONSOLE, simulator.getSimulatorId());
			
			simulatorWrapper.notifyError();

			if (e instanceof Error)
				throw e;

			return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Execution failed", e);
		} finally {
//			display.syncExec(() -> animationView.deinitialize());
		}

		RaoViewScope.plan(view -> {
			((ConsoleView) view).addLine(simulationResult.toString());
		}, ViewType.CONSOLE, simulator.getSimulatorId());
		
		RaoViewScope.plan(view -> {
			display.asyncExec(() -> ((ResultsView) view).update());
		}, ViewType.RESULTS, simulator.getSimulatorId());
		
				
		long endTime = System.currentTimeMillis();
		RaoViewScope.plan(view -> {
			((ConsoleView) view).addLine("Time elapsed: " + String.valueOf(endTime - startTime) + "ms");
		}, ViewType.CONSOLE, simulator.getSimulatorId());
 
		return Status.OK_STATUS;
	}
}
