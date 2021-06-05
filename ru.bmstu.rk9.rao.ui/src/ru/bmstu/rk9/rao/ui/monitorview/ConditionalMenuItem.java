package ru.bmstu.rk9.rao.ui.monitorview;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public abstract class ConditionalMenuItem extends MenuItem {

	public ConditionalMenuItem(TableViewer viewer, Menu parent, String name) {
		super(parent, SWT.CASCADE);
		setText(name);

		parent.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {
				SimulatorId simulator = (SimulatorId) viewer.getTable().getSelection()[0]
						.getData();
				setEnabled(isEnabled(simulator));
			}
		});

		addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				SimulatorId simulator = (SimulatorId) viewer.getTable().getSelection()[0]
						.getData();
				show(simulator);
			}
		});
	}
	@Override
	protected final void checkSubclass() {
	}

	abstract public boolean isEnabled(SimulatorId simulator);

	abstract public void show(SimulatorId simulator);
}

