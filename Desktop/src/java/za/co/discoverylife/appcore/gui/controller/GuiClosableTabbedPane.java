package za.co.discoverylife.appcore.gui.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTabbedPane;

import za.co.discoverylife.appcore.gui.GuiPanel;

/**
 * Adds a 'close' function to any panels added.
 * The close may be inhibited by making the panel disabled. [ setEnable(true/false) ]
 * 
 * @author anton11
 */
public class GuiClosableTabbedPane
		extends JTabbedPane {
	private static final long serialVersionUID = -8780277756866281333L;
	private TabCloseUI closeUI = new TabCloseUI(this);

	/** CONSTRUCTOR - default */
	public GuiClosableTabbedPane() {
		super();
	}

	/** CONSTRUCTS a close able tabbed pane with specified tab placement and layout policy */
	public GuiClosableTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
	}

	/** CONSTRUCTS a close able tabbed pane with specified tab placement */
	public GuiClosableTabbedPane(int tabPlacement) {
		super(tabPlacement);
	}

	/** add painting of button to the paint routine */
	public void paint(Graphics g) {
		try {
			super.paint(g);
			closeUI.paint(g);
		} catch (Exception e) {
		}
	}

	/** Adds a tab pane for the provided component */
	public void addTab(String title, GuiPanel component) {
		super.addTab(title + "  ", component);
	}

	/** Return the title for the n'th pane */
	public String getTabTitleAt(int index) {
		return super.getTitleAt(index).trim();
	}

	/** Return the tool tip for the n'th pane */
	public String getToolTipAt(int selectedTab) {
		try {
			GuiPanel pan = (GuiPanel) super.getComponentAt(selectedTab);
			return pan.getToolTipText();
		} catch (Exception e) {
			e.printStackTrace();
			return "Could not get tool tip";
		}
	}

	/** Returns true if the n'th tab is closeable */
	public boolean isClosableAt(int selectedTab) {
		try {
			GuiPanel pan = (GuiPanel) super.getComponentAt(selectedTab);
			boolean mayClose = pan.isCloseable();
			return mayClose;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	/** Confirms if close is permitted */
	public boolean tabAboutToClose(int tabIndex) {
		return isClosableAt(tabIndex);
	}

	private class TabCloseUI
			implements MouseListener, MouseMotionListener {
		private GuiClosableTabbedPane tabbedPane;
		private int closeX = 0, closeY = 0, meX = 0, meY = 0;
		private int selectedTab;
		private final int width = 8, height = 8;
		private Rectangle rectangle = new Rectangle(0, 0, width, height);

		// private TabCloseUI(){}

		public TabCloseUI(GuiClosableTabbedPane pane) {
			tabbedPane = pane;
			tabbedPane.addMouseMotionListener(this);
			tabbedPane.addMouseListener(this);
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseExited(MouseEvent me) {
		}

		public void mousePressed(MouseEvent me) {
		}

		public void mouseClicked(MouseEvent me) {
		}

		public void mouseDragged(MouseEvent me) {
		}

		public void mouseReleased(MouseEvent me) {
			if (closeUnderMouse(me.getX(), me.getY())) {
				boolean isToCloseTab = tabAboutToClose(selectedTab);
				if (isToCloseTab && selectedTab > -1) {
					GuiPanel pan = (GuiPanel) getComponentAt(selectedTab);
					pan.closeScreen();
//					try {
//						tabbedPane.removeTabAt(selectedTab);
//					} catch (Exception e) {
//					}
				}
				try {
					selectedTab = tabbedPane.getSelectedIndex();
				} catch (Exception e) {
				}
			}
		}

		public void mouseMoved(MouseEvent me) {
			meX = me.getX();
			meY = me.getY();
			if (mouseOverTab(meX, meY)) {
				controlCursor();
				tabbedPane.repaint();
			}
		}

		private void controlCursor() {
			if (tabbedPane.getTabCount() > 0)
				if (closeUnderMouse(meX, meY)) {
					tabbedPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
					if (selectedTab > -1 && isClosableAt(selectedTab))
						tabbedPane.setToolTipTextAt(selectedTab, "Close " + tabbedPane.getTitleAt(selectedTab));
				} else {
					tabbedPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					if (selectedTab > -1)
						tabbedPane.setToolTipTextAt(selectedTab, tabbedPane.getToolTipAt(selectedTab));
				}
		}

		private boolean closeUnderMouse(int x, int y) {
			rectangle.x = closeX;
			rectangle.y = closeY;
			return rectangle.contains(x, y);
		}

		public void paint(Graphics g) {
			int tabCount = tabbedPane.getTabCount();

			for (int j = 0; j < tabCount; j++) {
				if (tabbedPane.getComponent(j).isShowing()) {
					int x = tabbedPane.getBoundsAt(j).x + tabbedPane.getBoundsAt(j).width - width - 5;
					int y = tabbedPane.getBoundsAt(j).y + 5;
					if (tabbedPane.isClosableAt(j)) {
						drawClose(g, x, y);
					}
					break;
				}
			}
			if (mouseOverTab(meX, meY)) {
				if (tabbedPane.isClosableAt(selectedTab)) {
					drawClose(g, closeX, closeY);
				}
			}
		}

		private void drawClose(Graphics g, int x, int y) {
			if (tabbedPane != null && tabbedPane.getTabCount() > 0) {
				Graphics2D g2 = (Graphics2D) g;
				drawColored(g2, isUnderMouse(x, y) ? Color.RED : Color.WHITE, x, y);
			}
		}

		private void drawColored(Graphics2D g2, Color color, int x, int y) {
			g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
			g2.setColor(Color.BLACK);
			g2.drawLine(x, y, x + width, y + height);
			g2.drawLine(x + width, y, x, y + height);
			g2.setColor(color);
			g2.setStroke(new BasicStroke(2, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
			g2.drawLine(x, y, x + width, y + height);
			g2.drawLine(x + width, y, x, y + height);
		}

		private boolean isUnderMouse(int x, int y) {
			if (Math.abs(x - meX) < width && Math.abs(y - meY) < height)
				return true;
			return false;
		}

		private boolean mouseOverTab(int x, int y) {
			int tabCount = tabbedPane.getTabCount();
			for (int j = 0; j < tabCount; j++) {
				if (tabbedPane.getBoundsAt(j).contains(meX, meY)) {
					selectedTab = j;
					closeX = tabbedPane.getBoundsAt(j).x + tabbedPane.getBoundsAt(j).width - width - 5;
					closeY = tabbedPane.getBoundsAt(j).y + 5;
					return true;
				}
			}
			return false;
		}

	}

}
