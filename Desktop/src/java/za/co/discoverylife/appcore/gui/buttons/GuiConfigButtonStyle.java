package za.co.discoverylife.appcore.gui.buttons;

import za.co.discoverylife.appcore.gui.GuiBase;
import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.task.MetaMenu;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskManager;

/** 
 * Provides configuration menu for setting the button style 
 * 
 * @author anton11
 *
 */
@MetaMenu(label = "_Buttons", hint = "Select button style")
public class GuiConfigButtonStyle
		implements Runnable, IButtonConstants
{
	ILogger log = LogManager.getLogger(getClass());

	/** CONSTRUCTOR */
	public GuiConfigButtonStyle() {
	}

	/** Runnable is queued into AWT event to repaint the buttons after a change */
	public void run() {
		GuiBase.undo();
	}

	/** The button style change is queued to execute in AWT thread */
	private void makeChange(int mask,int value) {
		int old = GuiButton.getButtonStyle();
		if( (old & mask) == value){
			return;// no change
		}
		int not = mask ^ 0xFFFF;
		int now = (old & not) | value;
		GuiButton.setButtonStyle(now);
		TaskManager.getInstance().doTask(this, TaskManager.TASK_AWT_EVENT);
	}

	@MetaTask(label = "_Icon Only", hint = "Use icons only in buttons", seqId = 0)
	public void doIconOnly() {
		makeChange(BUTTON_STYLE_MASK,BUTTON_STYLE_ICON_ONLY);
	}

	@MetaTask(label = "_Text Only", hint = "Use text only in buttons", seqId = 1)
	public void doTextOnly() {
		makeChange(BUTTON_STYLE_MASK,BUTTON_STYLE_TEXT_ONLY);
	}

	@MetaTask(label = "_Horizontal", hint = "Text next to Icon", seqId = 2)
	public void doHorizontal() {
		makeChange(BUTTON_STYLE_MASK,BUTTON_STYLE_ICON_AND_TEXT);
		makeChange(BUTTON_LAYOUT_MASK,BUTTON_LAYOUT_HORIZONTAL);
	}

	@MetaTask(label = "_Vertical", hint = "Text below icon", seqId = 3)
	public void doVertical() {
		makeChange(BUTTON_STYLE_MASK,BUTTON_STYLE_ICON_AND_TEXT);
		makeChange(BUTTON_LAYOUT_MASK,BUTTON_LAYOUT_VERTICAL);
	}

}
