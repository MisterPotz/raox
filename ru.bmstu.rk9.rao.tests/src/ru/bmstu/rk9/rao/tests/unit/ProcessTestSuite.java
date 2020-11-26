package ru.bmstu.rk9.rao.tests.unit;

import java.util.ArrayList;

import ru.bmstu.rk9.rao.lib.database.SerializationObjectsNames;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;

public class ProcessTestSuite {
	public static void initEmptySimulation() {
		SerializationObjectsNames.set(new ArrayList<String>());
		SimulatorPreinitializationInfo info = new SimulatorPreinitializationInfo();
		info.resourceClasses.add(TestResource.class);
		SimulatorWrapper simulatorWrapper = new SimulatorWrapper(new Simulator());
		simulatorWrapper.preinitialize(info);
	}
}
