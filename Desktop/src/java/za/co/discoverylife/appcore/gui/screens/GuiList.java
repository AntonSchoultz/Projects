package za.co.discoverylife.appcore.gui.screens;

import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import za.co.discoverylife.appcore.field.FieldAccessor;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.task.TaskManager;

/**
 * Screen that shows a list of items
 * 
 * @author Anton Schoultz (2012)
 */
public class GuiList extends GuiScreen {
	private static final long serialVersionUID = 9066790228110111065L;

	private int LIST_COLUMNS = 3;
	private int LIST_ROWS = 6;
	private int LIST_CELL_WIDTH = W_LABEL_TEXT / LIST_COLUMNS;

	private DefaultListModel listModel;
	private JList list;
	private JScrollPane listScrollPane;
	private Collection<String> items;

	private Object data;
	private FieldAccessor fieldAccess;

	/**
	 * Screen that shows a list of items
	 * 
	 * @param name
	 *          Screen name
	 * @param hint
	 *          Hint text
	 */
	public GuiList(String name, String hint) {
		super(name, hint);
	}

	/**
	 * Build up the screen
	 * 
	 * @param dataObject
	 *          object where data is
	 * @param collectionFieldName
	 *          name of field that holds the collection to be shown
	 * @param rows
	 *          no of rows
	 * @param cols
	 *          no of columns
	 * @param width
	 *          width of screen in pixels
	 */
	@SuppressWarnings("unchecked")
	public void buildScreen(Object dataObject, String collectionFieldName, int rows, int cols, int width) {
		removeAll();
		data = dataObject == null ? this : dataObject;
		fieldAccess = FieldAccessor.getAccessor(data, collectionFieldName);
		LIST_COLUMNS = cols;
		LIST_ROWS = rows;
		LIST_CELL_WIDTH = (width / LIST_COLUMNS) - H_GAP;
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(-1);
		list.setFixedCellWidth(LIST_CELL_WIDTH);
		listScrollPane = new JScrollPane(list);
		resize(LIST_ROWS, LIST_COLUMNS);
		items = (Collection<String>) fieldAccess.getFieldObject();
		newRow();
		row.addCell(listScrollPane);
		row.padAcross();
		undo();
	}

	/**
	 * Resize the screen
	 */
	public void resize(int rows, int columns) {
		adjustSize(listScrollPane, LIST_CELL_WIDTH * columns + H_GAP, (V_TEXT_HEIGHT + V_GAP) * rows);
	}

	/**
	 * Trigger a Repaint the screen
	 */
	public void undo() {
		TaskManager.getInstance().doTask(this, TASK_AWT_EVENT);
	}

	/** perform list update from AWT thread */
	public void run() {
		setEnabled(!fieldAccess.isReadOnly());
		list.setIgnoreRepaint(true);
		listModel.clear();
		if (items != null) {
			for (Object o : items) {
				listModel.addElement(o);
			}
		}
		list.setIgnoreRepaint(false);
		super.undo();
	}

	/** Store the edited list into the object */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void commit() {
		Collection collection = (Collection) fieldAccess.getFieldObject();
		// clear the data collection
		collection.clear();
		// populate it from the model
		int mx = listModel.getSize();
		for (int i = 0; i < mx; i++) {
			Object item = listModel.getElementAt(i);
			if (item != null && !item.toString().trim().equals("")) {
				collection.add(item);
			}
		}
		super.commit();
	}

}
