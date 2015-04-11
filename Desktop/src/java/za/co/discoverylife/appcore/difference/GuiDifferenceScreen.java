package za.co.discoverylife.appcore.difference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskManager;

/**
 * Displays a visual comparison of two lists of objects.
 * The ListComparatorData object provides the data and the adaptors
 * required to sort , match and compare items.
 * It also provides the guts for committing changes. 
 * 
 * @author anton11
 *
 * @param <T>
 */
public class GuiDifferenceScreen<T> extends GuiScreen
{
  private static final long serialVersionUID = -5488313653394453237L;

  private ListComparatorData<T> adaptor;
  private List<T> lhs;
  private List<T> rhs;
  private MatchComparator<T> comparator;

  private ArrayList<GuiDifferenceRow<T>> list = new ArrayList<GuiDifferenceRow<T>>();

  //private T t=null;

  /**
   * CONSTRUCTOR
   * 
   * @throws Exception
   */
  public GuiDifferenceScreen(GuiScreen host, ListComparatorData<T> adaptor) throws Exception
  {
    super(host.getName(), adaptor.titleLeft + " vS " + adaptor.titleRight);
    this.adaptor = adaptor;
    this.lhs = adaptor.getLhs();
    this.rhs = adaptor.getRhs();
    boolean hasHeadings = false;
    TaskManager.getInstance().registerModel(getClass());
    // == sort both lists by match String
    comparator = new MatchComparator<T>(adaptor);
    Collections.sort(lhs, comparator);
    Collections.sort(rhs, comparator);
    Iterator<T> lix = lhs.iterator();
    Iterator<T> rix = rhs.iterator();
    T left = null;
    T right = null;

    //wr("Left len=" + lhs.size() + ", Right len=" + rhs.size());
    //int rn = 0;

    GuiDifferenceRow<T> guirow = null;
    while (lix.hasNext() | rix.hasNext())
    {
      // wr("--------------------------- rn=" + rn);
      //rn++;
      if ( left == null & lix.hasNext() )
      {
        left = lix.next();
        //wr("fetch left:" + String.valueOf(left));
      }
      if ( right == null & rix.hasNext() )
      {
        right = rix.next();
        //wr("fetch right:" + String.valueOf(right));
      }
      guirow = null;
      // -ve if left is less-than/before right     : left -
      // 0 if left=right                           : left = right
      // +ve if left is greater-than/after right   :      - right
      int diff = adaptor.compareMatch(left, right);
      if ( diff < 0 )
      {
        guirow = new GuiDifferenceRow<T>(adaptor, left, null);
        left = null;
        //wr(" L :   ");
      }
      if ( diff == 0 )
      {
        guirow = new GuiDifferenceRow<T>(adaptor, left, right);
        left = null;
        right = null;
        //wr(" L = R ");
      }
      if ( diff > 0 )
      {
        guirow = new GuiDifferenceRow<T>(adaptor, null, right);
        right = null;
        //wr("   : R ");
      }
      if ( guirow != null )
      {
        if ( !hasHeadings )
        {
          // == headings
          newRow();
          row.addCell(GuiDifferenceRow.createHeadingRow(this, adaptor));
          row.padAcross();
          hasHeadings = true;
        }
        list.add(guirow);
        newRow();
        row.addCell(guirow);
        row.padAcross();
      }
    }
    // flush last row info
    if ( left != null | right != null )
    {
      guirow = new GuiDifferenceRow<T>(adaptor, left, right);
      list.add(guirow);
      newRow();
      row.addCell(guirow);
      row.padAcross();
    }

  }

  /** Removes all items on the left side */
  @MetaTask(seqId = 1, label = "leftRemoveAll", hint = "Delete all from Left", icon = "delete")
  public void leftRemoveAll()
  {
    for (GuiDifferenceRow<T> gdr : list)
    {
      gdr.leftRemove();
    }
  }

  /** Copies all items from the left side to the right */
  @MetaTask(seqId = 2, label = "leftToRightAll", hint = "Copy all from Left to Right", icon = "btn_forward")
  public void leftToRightAll()
  {
    for (GuiDifferenceRow<T> gdr : list)
    {
      gdr.leftToRight();
    }
  }

  /** Copies all items from the right side to the left */
  @MetaTask(seqId = 3, label = "rightToLeftAll", hint = "Copy all from Right to Left", icon = "btn_back")
  public void rightToLeftAll()
  {
    for (GuiDifferenceRow<T> gdr : list)
    {
      gdr.rightToLeft();
    }
  }

  /** Removes all items on the right side */
  @MetaTask(seqId = 4, label = "rightRemoveAll", hint = "Delete all from Right", icon = "delete")
  public void rightRemoveAll()
  {
    for (GuiDifferenceRow<T> gdr : list)
    {
      gdr.rightRemove();
    }
  }

  /** Rebuilds the lists from the screen rows and commits the changes via the adaptor*/
  @MetaTask(seqId = 5, label = "ok", hint = "Save", icon = "accept")
  public void ok()
  {
    commit();
  }

  /** Applies the changes by rebuilding the lists based on the visual diff state */
  public void commit()
  {
    lhs.clear();
    rhs.clear();
    for (GuiDifferenceRow<T> difrow : list)
    {
      if ( difrow.hasLeft() )
      {
        lhs.add(difrow.getLeft());
      }
      if ( difrow.hasRight() )
      {
        rhs.add(difrow.getRight());
      }
    }
    adaptor.accept(lhs, rhs);
  }

}
