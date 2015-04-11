package za.co.discoverylife.appcore.gui;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.UIDefaults;
import javax.swing.plaf.metal.MetalLookAndFeel;

// import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;
// import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
// import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * Bob's look and feel is a slightly modified MetalLookAndFeel
 */
public class GuiLookAndFeel extends MetalLookAndFeel {
	private static final long serialVersionUID = 8681815415595784209L;
	public static int FontSize = -1;
	private static Font plain=null;
	private static Font italic=null;
	private static Font bold=null;

	@Override
	public UIDefaults getDefaults() {
		UIDefaults ret = super.getDefaults();
		getPlainFont();
		// Font italic = new Font("Dialog", Font.ITALIC, 10);
		ret.put("swing.boldMetal", Boolean.FALSE);
		ret.put("Label.font", plain);
		ret.put("Button.font", plain);
		ret.put("ToggleButton.font", plain);
		ret.put("ComboBox.font", plain);
		ret.put("TitledBorder.font", plain);
		ret.put("TextField.font", plain);
		ret.put("TextPane.font", plain);// console
		ret.put("EditorPane.font", plain);// log
		ret.put("TextArea.font", plain);
		ret.put("List.font", plain);
		ret.put("ProgressBar.font", plain);
		return ret;
	}

	public static Font getPlainFont() {
		if(plain==null){
			plain = new Font("Dialog", Font.PLAIN, FontSize);
		}
		return plain;
	}

	public static Font getItalicFont() {
		if(italic==null){
			italic = new Font("Dialog", Font.ITALIC, FontSize);
		}
		return italic;
	}

	public static Font getBoldFont() {
		if(bold==null){
			bold = new Font("Dialog", Font.BOLD, FontSize);
		}
		return bold;
	}

	public static void useFontSize(int newFontSize) {
		//System.err.println("GuiLookAndFeel.useFontsize() "+FontSize+" -> "+newFontSize);
		FontSize=newFontSize;
		plain=null;// force recreate of font
		getPlainFont();
		JLabel lbl = new JLabel("X");
		GuiPanel.initWidths( lbl.getFontMetrics(plain) );
	}

}

/*
L&F:List.font=javax.swing.plaf.FontUIResource
L&F:TableHeader.font=javax.swing.plaf.FontUIResource
L&F:Panel.font=javax.swing.plaf.FontUIResource
L&F:TextArea.font=javax.swing.plaf.FontUIResource
L&F:ToggleButton.font=javax.swing.plaf.FontUIResource
L&F:ComboBox.font=javax.swing.plaf.FontUIResource
L&F:ScrollPane.font=javax.swing.plaf.FontUIResource
L&F:Spinner.font=javax.swing.plaf.FontUIResource
L&F:RadioButtonMenuItem.font=javax.swing.plaf.FontUIResource
L&F:Slider.font=javax.swing.plaf.FontUIResource
L&F:EditorPane.font=javax.swing.plaf.FontUIResource
L&F:OptionPane.font=javax.swing.plaf.FontUIResource
L&F:ToolBar.font=javax.swing.plaf.FontUIResource
L&F:Tree.font=javax.swing.plaf.FontUIResource
L&F:CheckBoxMenuItem.font=javax.swing.plaf.FontUIResource
L&F:TitledBorder.font=javax.swing.plaf.FontUIResource
L&F:Table.font=javax.swing.plaf.FontUIResource
L&F:MenuBar.font=javax.swing.plaf.FontUIResource
L&F:PopupMenu.font=javax.swing.plaf.FontUIResource
L&F:DesktopIcon.font=javax.swing.plaf.FontUIResource
L&F:Label.font=javax.swing.plaf.FontUIResource
L&F:MenuItem.font=javax.swing.plaf.FontUIResource
L&F:MenuItem.acceleratorFont=javax.swing.plaf.FontUIResource
L&F:TextField.font=javax.swing.plaf.FontUIResource
L&F:TextPane.font=javax.swing.plaf.FontUIResource
L&F:CheckBox.font=javax.swing.plaf.FontUIResource 
*/
