package za.co.discoverylife.appcore.gui.screens;

import za.co.discoverylife.appcore.field.IValidatable;
import za.co.discoverylife.appcore.field.MetaObjectInfo;
import za.co.discoverylife.appcore.field.ValueObject;
import za.co.discoverylife.appcore.gui.GuiBase;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.buttons.GuiButtonPanel;
import za.co.discoverylife.appcore.gui.controller.GuiController;
import za.co.discoverylife.appcore.task.MetaTask;

/**
 * Constructs a simple screen for editing an object. 
 * 
 * Shows labels and data fields for all editable fields, also adds OK and Cancel buttons.
 * 
 * @author Anton Schoultz
 */
public class GuiConfigScreen
    extends GuiScreen
{
  private static final long serialVersionUID = -8904869349626151668L;

  private ValueObject object;
  private ValueObject save;
  private GuiController ctrl = GuiBase.getGuiBase().getGuiController();
  private boolean flag = false;// false=Cancel, true =OK

  /**
   * CONSTRUCTOR which accepts the data object to edit, the title, the hint and a 
   * flag to indicate if it is to be a sub-screen.
   * 
   * @param dataObject The data object to be edited
   * @param title Panel title/name
   * @param hint ToolTip for the panel
   * @param asSubScreen true=as a sub-screen
   */
  public GuiConfigScreen(ValueObject dataObject, String title, String hint, boolean asSubScreen)
  {
    super(title, hint);
    this.object = dataObject;
    save = (ValueObject) object.clone();
    addLabelsAndFieldsForObject(this.object);
    if ( asSubScreen == false )
    {
      addOkCancelButtons();
      endPage();
    }
    else
    {
      titledBorder(title, 1);
    }
  }

  /**
   * CONSTRUCTOR which accepts the data object to edit.
   * The panel title and hint are taken from the MetaObjectInfo annotation on the object.
   * Title and hint are defaulted to data class name if there is no annotation
   * 
   * @param dataObject The data object to edit
   */
  public GuiConfigScreen(ValueObject dataObject)
  {
    this(dataObject, false);
  }

  /**
   * CONSTRUCTOR which accepts the data object to edit.
   * The panel title and hint are taken from the MetaObjectInfo annotation on the object.
   * Title and hint are defaulted to data class name if there is no annotation
   * 
   * @param dataObject The data object to edit
   * @param asSubScreen true=as a sub-screen
   */
  public GuiConfigScreen(ValueObject dataObject, boolean asSubScreen)
  {
    this(dataObject, dataObject.getClass().getSimpleName(), dataObject.getClass().getName(), asSubScreen);
    MetaObjectInfo meta = dataObject.getClass().getAnnotation(MetaObjectInfo.class);
    if ( meta != null )
    {
      if ( meta.label().trim().length() > 0 )
      {
        setName(meta.label());
      }
      if ( meta.hint().trim().length() > 0 )
      {
        setToolTipText(meta.hint());
      }
    }
  }

  /**
   * CONSTRUCTOR which accepts the data object to edit, the title and the hint.
   * The screen is assumed to be a top-level screen (Not a sub-panel)
   * 
   * @param dataObject The data object to be edited
   * @param title Panel title/name
   * @param hint ToolTip for the panel
   */
  public GuiConfigScreen(ValueObject dataObject, String title, String hint)
  {
    this(dataObject, title, hint, false);
  }

  /** Adds the Reset, OK and CANCEL buttons */
  public void addOkCancelButtons()
  {
    try
    {
      GuiButtonPanel br = addButtonPanel(this);
      br.tabPosition(W_LABEL_TEXT + H_GAP);
      br.addAllButtons();
      br.padAcross();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @MetaTask(seqId = 1, label = "Reset", hint = "Reset to default values", icon = "table+#new")
  public void reset()
  {
    object.reset();
    undo();
  }

  @MetaTask(seqId = 2, label = "Cancel", hint = "Cancel/Undo changes", icon = "arrow_undo")
  public void cancel()
  {
    save.copyTo(object);
    ctrl.undo();
    flag = false;
  }

  @MetaTask(seqId = 3, label = "OK", hint = "Commit changes", icon = "accept")
  public void ok()
  {
    commit();
    flag = true;
    try
    {
      if ( object instanceof IValidatable )
      {
        IValidatable vo = (IValidatable) object;
        vo.validate();
      }
      object.copyTo(save);
      ctrl.removePanel(getName());
      ctrl.undo();

      close();
      closeScreen();

    }
    catch (Exception e)
    {
      log.error("Data invalid:" + e.getMessage());
    }
  }

  public boolean isOK()
  {
    return flag;
  }

  /**
   * @return the object
   */
  public ValueObject getObject()
  {
    return object;
  }

}
