package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;

import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class Transact extends ComparableResource<Transact> implements SimulatorDependent {
	private final SimulatorId simulatorId;

	@Override
	public SimulatorId getSimulatorId() {
		return simulatorId;
	}

	private static ISimulator getSimulator(SimulatorId simulatorId) {
		return SimulatorManagerImpl.getInstance().getSimulator(simulatorId);
	}

	private Transact(SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
	}

	@Override
	public ByteBuffer serialize() {
		return null;
	}

	@Override
	public String getTypeName() {
		return "Transact";
	}

	@Override
	public boolean checkEqual(Transact other) {
		return false;
	}

	public static void eraseTransact(Transact transact) {
		transact.erase();
	}

	public static Transact create(SimulatorId simulatorId) {
		Transact transact = new Transact(simulatorId);
		getSimulator(simulatorId).getModelState().addResource(transact);
		return transact;
	}

	@Override
	public void erase() {
		getSimulator(simulatorId).getModelState().eraseResource(this);
	}

	@Override
	public Transact deepCopy() {
		return new Transact(simulatorId);
	}
}
