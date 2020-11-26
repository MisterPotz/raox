package ru.bmstu.rk9.rao.lib.simulatormanager;

/**
 * 
 * Unique simulator identifier
 *
 */
public class SimulatorId {
	private final long id;

	private SimulatorId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public static SimulatorId generateSimulatorId() {
		return new SimulatorId(SimulatorIdManager.getInstance().getNewId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimulatorId other = (SimulatorId) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
