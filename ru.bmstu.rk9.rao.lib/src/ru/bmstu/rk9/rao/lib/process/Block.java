package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public interface Block {
	public BlockStatus check(SimulatorId simulatorId);
}
