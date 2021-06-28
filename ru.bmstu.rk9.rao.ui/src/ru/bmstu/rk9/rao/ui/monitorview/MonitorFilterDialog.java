package ru.bmstu.rk9.rao.ui.monitorview;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.ExecutionState;

class MonitorFilterDialog extends Dialog {
	
	private MonitorView.FilterHelper filterHelper;
	
	private Combo statusCombo;
	private Button filterButton;
	private Label statusLabel;
	// TODO fix-0003
	private ExecutionState filterStatus;
	
	private final void saveInputStatus() {
		String status = statusCombo.getText();
		// TODO fix-0003
		Optional<ExecutionState> selected = Arrays.asList(ExecutionState.values()).stream().filter(val -> val.toString().equals(status)).findAny();
		if (selected.isPresent()) {
			filterStatus = selected.get();
		} else {
			filterStatus = null;
		}
	}
	
	public MonitorFilterDialog(Shell parentShell, MonitorView.FilterHelper filterHelper) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		this.filterHelper = filterHelper;
	}

	@Override
	public void create() {
		super.create();
		getButton(IDialogConstants.OK_ID).setText("Close");
		getButton(IDialogConstants.OK_ID).setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		getButton(IDialogConstants.CANCEL_ID).dispose();
	}
	
	@Override
	public boolean close() {
		boolean returnValue = super.close();
		filterHelper.findStatus(null);
		filterHelper.closeDialog();
		return returnValue;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Filter by Status");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		createDialogContents(area);
		
		return area;
	}
	
	private final void createDialogContents(Composite parent) {
		Composite area = new Composite(parent, SWT.FILL);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(new GridLayout(2, false));

		Label comboTitle = new Label(area, SWT.NONE);
		comboTitle.setText("Select status: ");
		comboTitle.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));
		
		statusCombo = new Combo(area, SWT.NONE);
		statusCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
//		TODO fix-0003
		statusCombo.setItems(new String[] {"Not started", "In process", "Finished"});
		
		filterButton = new Button(area, SWT.PUSH);
		filterButton.setText("Filter");
		filterButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
					
		statusLabel = new Label(area, SWT.NONE);
		statusLabel.setText("Wrapped search");
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
		
		filterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
	
				saveInputStatus();
				if (filterHelper.findStatus(filterStatus) == FilterResult.NOT_FOUND)
					statusLabel.setText("Models with chosen status not found");
				else
					statusLabel.setText("Wrapped search");
			}
		});
	}

	
	@Override
	protected boolean isResizable() {
		return true;
	}
}
