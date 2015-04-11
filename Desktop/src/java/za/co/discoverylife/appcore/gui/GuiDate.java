package za.co.discoverylife.appcore.gui;

import java.util.Date;

import za.co.discoverylife.appcore.field.MetaFieldInfo;
import za.co.discoverylife.appcore.gui.buttons.GuiButton;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskManager;
import za.co.discoverylife.appcore.util.DateTime;

/**
 * A GUI component to display and edit a java.util.Date field.
 * 
 * @author Anton Schoultz - 2013
 */
public class GuiDate extends GuiRow
{
  private static final long serialVersionUID = -4624638091881118213L;

  // This holds the date being edited
  private DateTime dtm = new DateTime();

  @MetaFieldInfo(label = "Date", hint = "Date", width = 7)
  private String dateString;

  private GuiField gfDate;
  private GuiButton gfBtn;

  /**
   * CONSTRUCTOR
   */
  public GuiDate()
  {
    TaskManager.getInstance().registerModel(getClass());
    gfDate = addField(this, "dateString");
    gfBtn = addButton(this, "GuiDate@pick");
    gfBtn.setAsMiniButton();
  }

  @MetaTask(seqId = 10, label = "...", hint = "Select date", icon = "date+#edit")
  public void pick()
  {
    dateString = new GuiDatePicker(null, dtm.getAsDate()).setPickedDate();
    dtm.parse(dateString);
    undo();
  }

  /** Returns string form of the date as yyyy/mm/dd */
  public String toString()
  {
    return dtm.toStringYmd();
  }

  public void setToDate(Date date)
  {
    dtm.setToDate(date);
  }

  /** Sets the editable flag for this field */
  public void setEditable(boolean editable)
  {
    gfDate.setEnabled(editable);
    gfBtn.setEnabled(editable);
  }

  @Override
  public void commit()
  {
    super.commit();
    dtm.parse(dateString);
  }

  @Override
  public void undo()
  {
    dateString = dtm.toStringYmd();
    super.undo();
  }

  @Override
  public void reset()
  {
    super.reset();
  }

}
