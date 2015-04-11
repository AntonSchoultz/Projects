package za.co.discoverylife.appcore.gui.screens;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.JTabbedPane;

import za.co.discoverylife.appcore.gui.GuiPanel;
import za.co.discoverylife.appcore.gui.GuiScreen;

/**
 * A Sub-screen that supports tabbed panes
 * 
 * @author anton11
 */
public class GuiTabScreen
		extends GuiScreen
{
	private static final long serialVersionUID = 248287312571847275L;

	JTabbedPane tabPane;

	private Set<GuiScreen> tabs = new TreeSet<GuiScreen>();

	/** CONSTRUCTOR */
	public GuiTabScreen() {
		tabPane = new JTabbedPane();
		newRow();
		row.add(tabPane);
	}

	/** Adds the provided screen as a tabbed panel */
	public void addTabPanel(GuiScreen panel) {
		tabPane.add(panel.getName(), panel);
		tabs.add(panel);
	}

	public void undo() {
		for (GuiPanel gp : tabs) {
			gp.undo();
		}
	}

	public void commit() {
		for (GuiPanel gp : tabs) {
			gp.commit();
		}
	}
}
