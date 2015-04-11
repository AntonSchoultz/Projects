package za.co.discoverylife.appcore.difference;

import java.awt.Color;

import javax.swing.JLabel;

import za.co.discoverylife.appcore.gui.GuiRow;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.buttons.GuiButton;
import za.co.discoverylife.appcore.gui.buttons.GuiButtonPanel;
import za.co.discoverylife.appcore.task.MetaTask;

/**
 * Handles the display of a single pair of items.
 * Caters for delete left/right and copy form one side to the other.
 * 
 * The ListComparatorData object provides the data and the adaptors
 * required to match and compare items.
 * It also provides the guts for committing changes (fired from GuiDifferenceScreen).
 * 
 * @author anton11
 *
 * @param <T>
 */
public class GuiDifferenceRow<T> extends GuiRow
{
	private static final long serialVersionUID = 427523243336544848L;

	private ListComparatorData<T> adaptor;
	private T left;
	private T right;

	private GuiButtonPanel btns;

	private JLabel leftDisplay;
	private GuiButton leftRemove;
	private GuiButton leftToRight;

	private GuiButton leftVsRight;

	private GuiButton rightToLeft;
	private GuiButton rightRemove;
	private JLabel rightDisplay;

	static Color L_GREEN = new Color(0xDD, 0xFF, 0xDD);
	static Color L_BLUE = new Color(0xDD, 0xFF, 0xFF);
	static Color L_RED = new Color(0xFF, 0xDD, 0xDD);
	static Color L_YELLOW = new Color(0xFF, 0xFF, 0xCC);
	
	private static Color[] bgColors = {null, L_BLUE, L_RED, L_GREEN};

	private static int w = 0;

	/**
	 * CONSTRUCTS a GuiRow for the pair of objects
	 * 
	 * @throws Exception
	 */
	public GuiDifferenceRow(ListComparatorData<T> adaptor, T left, T right) throws Exception {
		super();
		this.adaptor = adaptor;
		this.left = left;
		this.right = right;
		w = adaptor.getColWidth() * W_CHAR;
		if(w<=0){
			w = W_TEXT;
		}
		btns = new GuiButtonPanel(this);
		btns.setParentGui(this);
		leftDisplay = btns.addLabelRight(adaptor.getDisplayString(left), adaptor.getToolTipString(left), w);
		leftRemove = btns.addButton(this, "GuiDifferenceRow@leftRemove").setAsMiniButton(1);
		leftToRight = btns.addButton(this, "GuiDifferenceRow@leftToRight").setAsMiniButton(1);
		leftVsRight = btns.addButton(this, "GuiDifferenceRow@leftVsRight").setAsMiniButton(1);
		rightToLeft = btns.addButton(this, "GuiDifferenceRow@rightToLeft").setAsMiniButton(1);
		rightRemove = btns.addButton(this, "GuiDifferenceRow@rightRemove").setAsMiniButton(1);
		rightDisplay = btns.addLabel(adaptor.getDisplayString(right), adaptor.getToolTipString(right), w);
		add(btns);
		adjustHeight(this, V_TEXT_HEIGHT);
	}

	/**
	 * inserts left and right hand side headings into the row
	 * 
	 * @throws Exception
	 */
	public static GuiRow createHeadingRow(GuiScreen scrn,ListComparatorData<?> adaptor) throws Exception {
		GuiRow row = new GuiRow();
		GuiButtonPanel btns = new GuiButtonPanel(row);
		btns.setParentGui(scrn);
		btns.addLabelRight("<html><b>" + adaptor.getTitleLeft(), adaptor.getTitleLeft(), w);
		btns.addButton(scrn, "GuiDifferenceScreen@leftRemoveAll").setAsMiniButton(1);
		btns.addButton(scrn, "GuiDifferenceScreen@leftToRightAll").setAsMiniButton(1);
		btns.padAcross(W_ICON+H_GAP+H_GAP);
		btns.addButton(scrn, "GuiDifferenceScreen@rightToLeftAll").setAsMiniButton(1);
		btns.addButton(scrn, "GuiDifferenceScreen@rightRemoveAll").setAsMiniButton(1);
		btns.addLabel("<html><b>" + adaptor.getTitleRight(), adaptor.getTitleRight(), w);
		row.add(btns);
		adjustHeight(row, V_TEXT_HEIGHT);
		return row;
	}

	/** Updated the enable flags based on pair state */
	@Override
	public void undo() {
		int st = 0;
		// left
		boolean en = hasLeft();
		if (en) {
			st += 1;
		}
		leftRemove.setEnabled(en);
		leftDisplay.setText(adaptor.getDisplayString(left));
		leftDisplay.setEnabled(en);
		leftToRight.setEnabled(en);
		// right
		en = hasRight();
		if (en) {
			st += 2;
		}
		rightRemove.setEnabled(en);
		rightDisplay.setText(adaptor.getDisplayString(right));
		rightDisplay.setEnabled(en);
		rightToLeft.setEnabled(en);
		// detailed difference
		boolean equal = (adaptor.compare(left, right) == 0);
		String iconName = equal ? "btn_equal" : "btn_not_equal";
		leftVsRight.loadIcon(iconName);
		leftVsRight.setEnabled(hasLeft() && hasRight());
		leftVsRight.setDisplayStyle(GuiButton.BUTTON_STYLE_ICON_ONLY);
		// color line based on state
		Color fg = getForeground();
		if (!equal) {
			fg = Color.RED;
		}
		btns.setColors(fg, bgColors[st]);
		super.undo();
	}

	/** Imports a project from the team list into Bob */
	@MetaTask(seqId = 21, label = "Remove", hint = "Remove from Left Hand Side", icon = "delete")
	public void leftRemove() {
		left = null;
	}

	/** Imports a project from the team list into Bob */
	@MetaTask(seqId = 21, label = "Export", hint = "Copy form left to right", icon = "btn_play")
	public void leftToRight() {
		right = left;
		undo();
	}

	/** Imports a project from the team list into Bob */
	@MetaTask(seqId = 21, label = "Compare", hint = "Detailed compare", icon = "btn_reverse")
	public void leftVsRight() {
		adaptor.drillDown(left, right);
	}

	/** Imports a project from the team list into Bob */
	@MetaTask(seqId = 21, label = "Import", hint = "Copy from rigth to left", icon = "btn_reverse")
	public void rightToLeft() {
		left = right;
	}

	/** Imports a project from the team list into Bob */
	@MetaTask(seqId = 21, label = "Remove", hint = "Remove from Right Hand Side", icon = "delete")
	public void rightRemove() {
		right = null;
	}

	/** Returns the item on the left */
	public T getLeft() {
		return left;
	}

	/** Returns true if the item on the left is not null */
	public boolean hasLeft() {
		return left != null;
	}

	/** Returns the item on the right */
	public T getRight() {
		return right;
	}

	/** Returns true if the item on the right is not null */
	public boolean hasRight() {
		return right != null;
	}

}
