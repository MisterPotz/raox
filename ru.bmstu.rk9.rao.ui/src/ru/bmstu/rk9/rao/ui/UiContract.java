package ru.bmstu.rk9.rao.ui;

import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.plot.PlotView;
import ru.bmstu.rk9.rao.ui.raoview.RaoView;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializedObjectsView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;
import ru.bmstu.rk9.rao.ui.trace.TraceView;

public class UiContract {
	private UiContract() {
		
	}
	
	public static String ID_CONSOLE_VIEW = RaoView.getIdForView(ConsoleView.class);
	public static String ID_RESULTS_VIEW = RaoView.getIdForView(ResultsView.class);
	public static String ID_TRACE_VIEW = RaoView.getIdForView(TraceView.class);
	public static String ID_SERIALIZEDOBJS_VIEW = RaoView.getIdForView(SerializedObjectsView.class);
	public static String ID_PLOT_VIEW = RaoView.getIdForView(PlotView.class);
	public static String ID_STATUS_VIEW = RaoView.getIdForView(StatusView.class);
	public static String ID_ANIMATION_VIEW = RaoView.getIdForView(AnimationView.class);

}
