package za.co.discoverylife.appcore.gui.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;

import za.co.discoverylife.appcore.field.FieldAccessException;
import za.co.discoverylife.appcore.gui.GuiField;
import za.co.discoverylife.appcore.gui.GuiRow;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.IScreenPlugin;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.task.Task;
import za.co.discoverylife.appcore.task.TaskEntry;
import za.co.discoverylife.appcore.task.TaskManager;

/**
 * Wraps button with their subject-objects
 * ActionKey is of the form 'Class@MethodName[?value]'
 * 
 * @author anton11
 */
public class GuiButtonPanel extends GuiRow implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = -4923154556425977448L;
	private Object subjectModel;
	private List<String> actionKeys;
	private TaskManager taskManager = TaskManager.getInstance();
	/** actions are stored according to seqID values */
	private List<GuiButton> btnList = new ArrayList<GuiButton>();
	List<TaskEntry> actionSet;

	private boolean isToolBar = false;

	/**
	 * Constructs a button panel which will act on subjectModel
	 * 
	 * @param subjectModel
	 * @throws Exception
	 */
	public GuiButtonPanel(Object subjectModel) throws Exception {
		this(subjectModel, false);
	}

	/**
	 * Constructs a button panel which will act on subjectModel
	 * 
	 * @param subjectModel
	 * @throws Exception
	 */
	public GuiButtonPanel(Object subjectModel, boolean isToolBar) throws Exception {
		super();
		this.isToolBar = isToolBar;
		this.subjectModel = subjectModel;
		if (subjectModel == null) {
			throw new Exception("May not create button panel for a null object");
		}
		Class<? extends Object> ak = subjectModel.getClass();
		actionSet = taskManager.listAvailable(ak);
		actionKeys = new ArrayList<String>();
		if (actionSet == null || actionSet.size() == 0) {
			// log.info("Screen built before model was registered - autoregistering ...");
			actionSet = taskManager.registerModel(ak);
			Collections.sort(actionSet);
		}
		for (TaskEntry ae : actionSet) {
			actionKeys.add(ae.getKey());
		}
	}

	/**
	 * Implemented for PropertyChangeListener, handles enable/disable of buttons
	 * based on the task being enabled/disabled in task manager
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String key = evt.getPropertyName();
		if (key != null) {
			TaskEntry te = (TaskEntry) evt.getNewValue();
			for (JButton btn : btnList) {
				if (key.equals(btn.getActionCommand())) {
					btn.setEnabled(te.isEnabled());
				}
			}
		}
	}

	/** enable/disable all buttons */
	public void setEnabled(boolean en) {
		for (GuiButton btn : btnList) {
			btn.setEnabled(en);
		}
	}

	/** Set edit value from the object field value */
	public void undo() {
		// set buttons to enabled/disabled based on task flag
		for (GuiButton btn : btnList) {
			btn.undo();
		}
	}

	/**
	 * Adds a button to this row. The button will action on the nearest containing
	 * screen, invoking a method of the form void doBtn{Name}()
	 * 
	 * @throws Exception
	 */
	public GuiButton addButton(String actionKey) {
		if (log == null) {
			log = LogManager.getLogger(getScreen().getClass());
		}
		if (!actionKeys.contains(actionKey)) {
			error("No action defined for button key=" + actionKey, null);
		}
		GuiButton btn = null;
		try {
			btn = GuiButton.create(actionKey);
			addGuiButton(btn);
			taskManager.addPropertyChangeListener(btn.getActionCommand(), this);
		} catch (Exception e) {
			error("Problem adding Button for button key=" + actionKey, e);
		}
		return btn;
	}

	/**
	 * Adds a GuiButton to this row (Button must have actionCommand set to
	 * actionKey)
	 */
	public void addGuiButton(GuiButton btn) {
		btn.addActionListener(this);
		btnList.add(btn);
		btn.setEnabled(true);
		addCell(btn);
	}

	/** Adds Buttons for all task methods detected */
	public void addAllButtons() throws Exception {
		int seq = -1;
		for (TaskEntry ae : actionSet) {
			int n = ae.getSequenceNo() % 100;
			if (seq < 0) { // first seq no found
				seq = n - 1;
				if (n >= 90) {
					seq = 0;
				}
			}
			seq++;// default next seq-no
			if (n > seq) { // break in sequence no means add bigger gap
				if (n >= 90) {// if seq no is>90 then right end
					tabPosition(W_LABEL_TEXT + H_GAP);
				} else {
					padAcross(H_GAP);
				}
				seq = n;
			}
			addButton(ae.getKey());
		}
	}

	/** Add a gap between button groups */
	public void addSeparator() {
		padAcross(H_GAP * 2);
	}

	/**
	 * Implements ActionListener to handle buttons by firing method given by
	 * actionCommand in it's own thread. This is done so that the GUI thread is
	 * not parked waiting for CMD to finish. The screen object's run is
	 * also queued, and GuiPanel uses this to do an undo() to refresh the screen.
	 */
	public void actionPerformed(ActionEvent e) {
		String actnKey = e.getActionCommand();
		GuiScreen screen = getScreen();
		GuiButton btn = (GuiButton) e.getSource();
		if (screen != null) {
			//log.report("Screen is " + screen.getClass().getName());
			screen.commit();
			screen.setBtnFired(btn);
		} else {
			//log.report("Screen not set");
		}
		if (subjectModel instanceof IScreenPlugin) {
			((IScreenPlugin) subjectModel).setActionContext(screen);
		}
		try {
			Task task = taskManager.getActionTask(actnKey, subjectModel, log);
			if (isToolBar) {
				taskManager.doTask(task, TASK_SPAWN);
			} else {
				taskManager.doTaskList(TASK_SPAWN, task, screen);
			}
		} catch (Exception e1) {
			log.error("Problem trying to action button " + e.getActionCommand(), e1);
		}
	}

	/**
	 * This allows for compact in-line buttons to emulate a tool bar input
	 */
	@Override
	public GuiField addLabelAndField(Object object, String fieldName) {
		try {
			GuiField gf = new GuiField(this, object, fieldName);
			addLabel(gf.getLabel(), gf.getToolTip(), 0);
			addGuiField(gf);
			addCell(gf.getComp());
			return gf;
		} catch (FieldAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
