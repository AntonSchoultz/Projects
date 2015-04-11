package za.co.discoverylife.appcore.gui;

import java.awt.Rectangle;

import javax.swing.JScrollPane;

import za.co.discoverylife.appcore.gui.screens.ISaveable;

/**
 * Wraps the provided GuiPanel in a JScrollPanel
 * 
 * @author Anton Schoultz 
 */
public class GuiScrollPanel
		extends GuiScreen implements ISaveable{
	private static final long serialVersionUID = 5548094623782491803L;

	private GuiPanel content;

	private JScrollPane scrollPane;

	/**
	 * CONSTRUCT a scrolling panel wrapper for the provided panel.
	 * 
	 * @param panel GuiPanel to be scrolled.
	 */
	public GuiScrollPanel(GuiPanel panel) {
		if (panel == null)
			throw new IllegalArgumentException("ScrollPanel can not wrap a null panel");
		content = panel;
		// mimic enclosed panel's details
		this.guiparent = content.guiparent;
		this.closeable = content.closeable;
		setName(content.getName());
		// wrap panel and show it
		content.guiparent = this;
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(content);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(V_TEXT_HEIGHT * 3);
		add(scrollPane);
	}

	/** Returns the wrapped panel (content of scrolling panel) */
	public GuiPanel getContent() {
		return content;
	}

	// --- Implement IEditable by delegating to the enclosed panel

	@Override
	public void commit() {
		super.commit();
		content.commit();
	}

	@Override
	public void undo() {
		super.undo();
		content.undo();
	}

	@Override
	public void reset() {
		super.reset();
		content.reset();
	}

	/** Scroll so that the given rectangle is visible */
	public void setViewTo(Rectangle rect) {
		scrollPane.getViewport().scrollRectToVisible(rect);
	}

	public void saveChanges() {
		if( content instanceof ISaveable){
			((ISaveable)content).saveChanges();
		}else{
			log.error("File Save is not applicable to '"+content.getName()+"' screen");
		}
	}

}
