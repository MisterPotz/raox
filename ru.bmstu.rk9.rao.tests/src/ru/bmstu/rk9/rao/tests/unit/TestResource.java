package ru.bmstu.rk9.rao.tests.unit;

import java.nio.ByteBuffer;

import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;

public class TestResource extends ComparableResource<TestResource> {

	@Override
	public boolean checkEqual(TestResource other) {
		return false;
	}

	@Override
	public String getTypeName() {
		return "TestResource";
	}

	@Override
	public void erase() {
		SimulatorWrapper.getModelState().eraseResource(this);
	}

	public static TestResource create() {
		TestResource resource = new TestResource();
		SimulatorWrapper.getModelState().addResource(resource);
		return resource;
	}

	@Override
	public ByteBuffer serialize() {
		return null;
	}

	@Override
	public TestResource deepCopy() {
		return null;
	}
}
