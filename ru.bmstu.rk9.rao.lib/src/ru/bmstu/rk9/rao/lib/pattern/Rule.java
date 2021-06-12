package ru.bmstu.rk9.rao.lib.pattern;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public abstract class Rule extends Pattern {
	
	public Rule(SimulatorId simulatorId) {
		super(simulatorId);
	}
	@Override
	public final void run() {
		execute();
		finish();
	}

	protected void execute() {
	}
}
