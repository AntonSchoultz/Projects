package za.co.discoverylife.appcore.gui.screens;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.progress.IProgressListener;

/**
 * Small single-row screen which goes at the bottom and is used to display
 * status messages.
 * <br>Implements IProgressListener
 * 
 * @author Anton Schoultz
 */
public class GuiStatus extends GuiScreen implements IProgressListener
{
  private static final long serialVersionUID = -4236499253437216747L;

  private static final int BAR_WIDTH = 100;

  private JLabel status;
  private JProgressBar bar;
  long barMax = 100;
  long barMin = 0;
  long barValue = 0;
  String barTip;
  String barText;

  public GuiStatus()
  {
    super("GuiStatus", "StatusBar");
    newRow(0);
    row.addLabelRight("Status:", "");
    status = row.addLabel(" ", "Status", W_LABEL_TEXT);
    status.setForeground(Color.RED);
    bar = new JProgressBar(0, 100);
    setBarText(null, "");
    bar.setIndeterminate(false);
    bar.setBorderPainted(true);
    bar.setMaximumSize(new Dimension(BAR_WIDTH, V_TEXT_HEIGHT));
    bar.setMinimum(0);
    setToDoCount(100);
    setDoneCount(0);
    row.padAcross();
    row.add(bar);
    row.padAcross(H_GAP);
    newRow();
  }

  /**
   * Sets the message to be displayed as the 'Status' (use null to clear the
   * message)
   * 
   * @param message
   *            String to be displayed (or null to clear)
   */
  public void setStatus(String message)
  {
    status.setText(message == null ? " " : "<html><b>" + message);
    update();
  }

  /**
   * Sets text for status bar progress
   * 
   * @param text
   */
  public void setBarText(String text, String hint)
  {
    barText = text;
    barTip = hint;
    update();
  }

  /** 
   * Initialise the status bar with textm hint and max todo
   * 
   * @param text Status text (can be html)
   * @param tip Tool tip
   * @param max ToDo count
   */
  public void startBar(String text, String tip, int max)
  {
    setBarText(text, tip);
    setToDoCount(max);
    update();
  }

  /** Sets maximum for bar */
  public void setToDoCount(long max)
  {
    barMax = max;
    barValue = 1;
    update();
  }

  /** Update progress with how many are done */
  public void setDoneCount(long n)
  {
    barValue = n;
    update();
  }

  /** 
   * Indicates that all items are done.
   */
  public void setAllDone()
  {
    barValue = barMax;
  }

  public void barClear()
  {
    setBarText(null, "");
    barValue = 0;
    update();
  }

  public void undo()
  {
    if ( barText != null )
    {
      bar.setString(barText);
      bar.setStringPainted(true);
    }
    else
    {
      bar.setString("");
      bar.setStringPainted(false);
    }
    if ( barTip == null )
    {
      bar.setToolTipText(barValue + " / " + barMax);
    }
    else
    {
      bar.setToolTipText(barTip);
    }
    bar.setMaximum((int) barMax);
    bar.setValue((int) barValue);
  }

  public JProgressBar getBar()
  {
    return bar;
  }

}
