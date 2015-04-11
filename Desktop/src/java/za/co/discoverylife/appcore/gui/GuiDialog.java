package za.co.discoverylife.appcore.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.IGuiScreenCloser;

/**
 * Wraps a GuiScreen as a modal dialogue which exits when the screen closes
 * itself.
 * 
 * @author Anton
 * 
 */
public class GuiDialog extends JDialog
		implements IGuiScreenCloser
{
	private static final long serialVersionUID = -236161534091748298L;
	private static GuiDialog dialog;
	private static String value = "";

	private GuiScreen guiDialogueScreen;

	/**
	 * Set up and show the dialog. The first Component argument determines which
	 * frame the dialog depends on; it should be a component in the dialog's
	 * controlling frame. The second Component argument should be null if you want
	 * the dialog to come up with its left corner in the center of the screen;
	 * otherwise, it should be the component on top of which the dialog should
	 * appear.
	 */
	public static String showDialog(Component frameComp,
				GuiScreen guiPanel) {
		Frame frame = JOptionPane.getFrameForComponent(frameComp);
		dialog = new GuiDialog(frame, guiPanel);
		dialog.setVisible(true);
		return value;
	}

	public GuiDialog(Frame frame, GuiScreen guiScreen) {
		super(frame, guiScreen.getName(), true);
		this.guiDialogueScreen = guiScreen;
		guiScreen.setDialog(this);
		guiDialogueScreen.setGuiCloser(this);
		JScrollPane panelScroller = new JScrollPane(guiDialogueScreen);
		Dimension dim = guiDialogueScreen.getMaximumSize();
		panelScroller.setPreferredSize(dim);
		panelScroller.setMaximumSize(dim);
		panelScroller.setMinimumSize(dim);
		panelScroller.setAlignmentX(LEFT_ALIGNMENT);
		// Create a container so that we can add a title around
		// the scroll pane. Can't add a title directly to the
		// scroll pane because its background would be white.
		// Lay out the label and scroll pane from top to bottom.
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("labelText");
		label.setLabelFor(guiDialogueScreen);
		listPane.add(label);
		listPane.add(Box.createRigidArea(new Dimension(0, 5)));
		listPane.add(panelScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		Container contentPane = getContentPane();
		contentPane.add(guiDialogueScreen, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(frame);
		guiDialogueScreen.requestFocus();
	}

	public void closeGuiScreen(GuiScreen scrn) {
		GuiDialog.value = scrn.getName();
		GuiDialog.dialog.setVisible(false);
		GuiDialog.dialog.validate();
		GuiDialog.dialog.dispose();
	}

}
