package ru.bmstu.rk9.rao.lib.pattern;

import ru.bmstu.rk9.rao.lib.database.SerializationConstants;
import ru.bmstu.rk9.rao.lib.event.Event;

public abstract class Operation extends Pattern {
	
	@Override
	public final void run() {
		begin();
		planEnd();
	}

	protected void begin() {
	}

	protected void end() {
	}

	protected double duration() {
		return 0;
	}

	private final void planEnd() {
		getSimulator().pushEvent(new OperationEvent(getSimulator().getTime() + duration()));
	}

	private class OperationEvent extends Event {
		OperationEvent(double time) {
			this.time = time;
			setSimulatorId(getSimulatorId());
		}

		@Override
		public String getName() {
			return Operation.this.getTypeName() + "_endEvent";
		}

		@Override
		protected void execute() {
			Operation.this.end();
			getSimulator().getDatabase().addOperationEndEntry(Operation.this);
			getSimulator().getDatabase().addMemorizedResourceEntries(
					Operation.this.getTypeName() + "." + SerializationConstants.CREATED_RESOURCES, null, null);
			finish();
		}
	}
}
