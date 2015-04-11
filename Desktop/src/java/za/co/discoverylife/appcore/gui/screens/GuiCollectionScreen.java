package za.co.discoverylife.appcore.gui.screens;

import java.security.InvalidParameterException;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import za.co.discoverylife.appcore.field.FieldAccessException;
import za.co.discoverylife.appcore.field.FieldAccessor;
import za.co.discoverylife.appcore.field.MetaFieldInfo;
import za.co.discoverylife.appcore.gui.GuiField;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.buttons.GuiButton;
import za.co.discoverylife.appcore.gui.buttons.GuiButtonPanel;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskManager;

/**
 * Sub screen that handles editing of collections
 * 
 * @author anton11
 */
public class GuiCollectionScreen extends GuiScreen {
	private static final long serialVersionUID = -3299436658345945696L;
	protected FieldAccessor fieldAccess;

	public int LIST_COLUMNS = 3;
	public int LIST_ROWS = 6;
	public int LIST_CELL_WIDTH = W_LABEL_TEXT / LIST_COLUMNS;

	DefaultListModel listModel;
	JList list;
	JScrollPane listScrollPane;
	JButton btnAdd;
	JButton btnRemove;
	JButton btnUp;
	JButton btnDown;

	@SuppressWarnings("rawtypes")
	Class itemsType;

	GuiButtonPanel bp;
	GuiField gfBuddy;
	Collection<String> items;

	String iconName;

	@MetaFieldInfo(label = "defaultBuddy", icon = "cup")
	String defaultBuddy = "";

	GuiButton edtbtn;

	GuiScreen actionScreen;

	/**
	 * CONSTRUCTOR
	 * 
	 * @param parent
	 *          - Parent screen
	 * @param selObj
	 *          - Object with selected value
	 * @param selFieldName
	 *          - Filed name of selected value
	 * @param itemObject
	 *          - object used to add an item
	 * @param itemFieldName
	 *          - items field name (buddy field)
	 * @param isTitled
	 *          - adds titled border if true.
	 * @throws FieldAccessException
	 */
	public GuiCollectionScreen(GuiScreen parent, Object selObj, String selFieldName, Object itemObject,
			String itemFieldName, boolean isTitled) throws FieldAccessException {
		this(parent, new FieldAccessor(selObj, selFieldName), itemObject, itemFieldName, isTitled);
	}

	/**
	 * CONSTRUCT a sub screen for editing a collection
	 * 
	 * @param parentScrn
	 *          Parent screen that this will form part of - add / remove / edit
	 *          are also routed to parent screen
	 * @param fldAccess
	 *          - Accessor for the collection field
	 * @param itemObject
	 *          - object used to add an item
	 * @param itemFieldName
	 *          - items field name (buddy field)
	 * @param isTitled
	 *          - adds titled border if true.
	 */
	@SuppressWarnings("unchecked")
	public GuiCollectionScreen(GuiScreen parentScrn, FieldAccessor fldAccess, Object itemObject, String itemFieldName,
			boolean isTitled) {
		super();
		fieldAccess = fldAccess;
		actionScreen = parentScrn;
		guiparent = parentScrn.getScreen();
		setName(fieldAccess.getLabel());

		gfBuddy = null;
		if (itemObject != null) {
			try {
				gfBuddy = new GuiField(guiparent, itemObject, itemFieldName);
			} catch (FieldAccessException e) {
				throw new InvalidParameterException("Buddy field specified is invalid:" + e.toString());
			}
		}
		items = (Collection<String>) fieldAccess.getFieldObject();
		try {
			itemsType = Class.forName(fieldAccess.getFieldInnerType());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(-1);
		list.setFixedCellWidth(LIST_CELL_WIDTH);
		listScrollPane = new JScrollPane(list);
		adjustSize(listScrollPane, LIST_CELL_WIDTH * LIST_COLUMNS + H_GAP, (V_TEXT_HEIGHT + V_GAP) * LIST_ROWS);
		newRow();
		row.addCell(listScrollPane);
		row.padAcross();
		//
		bp = addButtonPanel(this);
		bp.setParentGui(parentScrn);
		if (gfBuddy == null) {
			gfBuddy = bp.addField(this, "defaultBuddy");
		} else {
			bp.addCell(gfBuddy.getComp());
		}
		bp.addGuiField(gfBuddy);
		TaskManager.getInstance().registerModel(getClass());
		iconName = fieldAccess.getIconName();
		GuiButton btn;
		// add
		btn = new GuiButton("Add", iconName + "+#plus");
		btn.setAsMiniButton();
		btn.setActionCommand("GuiCollectionScreen@add");
		bp.addGuiButton(btn);
		// remove
		btn = new GuiButton("Remove", iconName + "+#minus");
		btn.setAsMiniButton();
		btn.setActionCommand("GuiCollectionScreen@remove");
		bp.addGuiButton(btn);
		// edit
		edtbtn = new GuiButton("Edit", iconName + "+#edit");
		edtbtn.setAsMiniButton();
		edtbtn.setActionCommand("GuiCollectionScreen@edit");
		edtbtn.setVisible(false);
		bp.addGuiButton(edtbtn);
		bp.padAcross();
		if (isTitled) {
			titledBorder(fieldAccess.getLabel(), 1);
		}
		setEnabled(!fieldAccess.isReadOnly());
		undo();
	}

	public void resize(int rows, int columns) {
		adjustSize(listScrollPane, LIST_CELL_WIDTH * columns + H_GAP, (V_TEXT_HEIGHT + V_GAP) * rows);
	}

	public void addEdit() {
		edtbtn.setVisible(true);
	}

	public void setEnabled(boolean enabled) {
		gfBuddy.setEnabled(enabled);
		bp.setEnabled(enabled);
	}

	public void undo() {
		gfBuddy.undo();
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

	@MetaTask(label = "Add")
	public void add() {
		gfBuddy.commit();
		String[] sa = gfBuddy.getFieldAsString().split(",");
		String last = "";
		for (String s : sa) {
			last = s.trim();
			items.add(last);
		}
		String method = fieldAccess.getFieldName() + "Add";
		actionScreen.fireMethod(method, false);
		gfBuddy.setFieldFromString("");
		gfBuddy.undo();
	}

	@MetaTask(label = "Remove")
	public void remove() {
		Object[] oa = list.getSelectedValues();
		for (Object o : oa) {
			gfBuddy.setFieldFromString(o.toString());
			items.remove(o);
		}
		gfBuddy.undo();
		String method = fieldAccess.getFieldName() + "Remove";
		actionScreen.fireMethod(method, false);
	}

	@MetaTask(label = "Edit")
	public void edit() {
		commit();
		gfBuddy.commit();
		String method = fieldAccess.getFieldName() + "Edit";
		Object[] oa = list.getSelectedValues();
		for (Object o : oa) {
			actionScreen.fireMethod(method, o, false);
		}
	}

}
