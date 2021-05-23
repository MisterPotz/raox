package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;

public class SimulatorInitializationInfo {
	public final List<Function<Object, Runnable>> initList = new ArrayList<>();
	public final List<Function<Object, Supplier<Boolean>>> terminateConditions = new ArrayList<>();
	public final List<Function<Object, AbstractDecisionPoint>> decisionPoints = new ArrayList<>();
	public final List<Block> processBlocks = new ArrayList<>();
	public final List<Function<Object, AbstractResult<?>>> results = new ArrayList<>();
}
