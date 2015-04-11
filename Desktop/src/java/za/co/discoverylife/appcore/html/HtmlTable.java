package za.co.discoverylife.appcore.html;

import za.co.discoverylife.appcore.table.DataCell;
import za.co.discoverylife.appcore.table.DataRow;
import za.co.discoverylife.appcore.table.InvalidColumnException;

/**
 * Wraps an HTML table.
 * 
 * @author Anton Schoultz 
 */
public class HtmlTable extends HtmlTag
{
  private String heading = null;
  private DataRow tableHeader = null;

  private HtmlTag tr = null;
  private int col = 0;
  private int colMax = -1;
  private boolean odd = true;
  private boolean alternateColours = true;

  /** CONSTRUCTS a basic table */
  public HtmlTable()
  {
    super("TABLE");
    tableHeader = new DataRow();
  }

  /** CONSTRUCTOR which accepts a table header definition */
  public HtmlTable(DataRow headerRow)
  {
    super("table");
    tableHeader = new DataRow(headerRow);
  }

  /** Add a column heading */
  public HtmlTable addColumnHeader(String heading, int widthWeighting, boolean rightJustify)
  {
    if ( colMax > 0 )
      throw new UnsupportedOperationException("May not add columns once data is started");
    tableHeader.addCell(heading, widthWeighting, rightJustify);
    return this;
  }

  /** 
   * Create new data row - outputs headings etc if needed
   * Alternate data rows are marked as class="odd"/"even".
   * newRow
   *
   * @return
   */
  public HtmlTag newRow()
  {
    // check if this is the first one, if so do headings etc
    if ( colMax < 0 )
    {
      colMax = tableHeader.size();
      if ( colMax < 1 )
      {
        throw new InvalidColumnException("Must define headings before adding data");
      }
      // table heading
      if ( heading != null )
      {
        tr = add(new HtmlTag("Tr"));
        tr.add(new HtmlTag("th"))
            .setAttribute("colspan", colMax)
            .setBody(heading);
      }
      // column headings
      int totWidth = tableHeader.getWidth();
      tr = add(new HtmlTag("Tr"));
      for (DataCell colHdr : tableHeader.getCells())
      {
        String align = colHdr.isNumeric() ? "right" : "left";
        HtmlTag tdh = tr.add(new HtmlTag("th", colHdr.getValue()));
        tdh.setAttribute("align", align);
        if ( totWidth > 0 )
        {
          int pct = colHdr.getWidth() * 100 / totWidth;
          tdh.setAttribute("width", pct + "%");
        }
      }
      odd = false;;
    }
    if ( alternateColours )
    {
      odd = !odd;
    }
    col = 0;
    tr = add(new HtmlTag("Tr"));

    String rowStyle = odd ? "odd" : "even";
    tr.setAttribute("class", rowStyle);
    return tr;
  }

  /** 
   * Adds the provided tag to the table row
   * 
   * @param cellBody
   * @return the TD tag
   */
  public HtmlTag addCell(HtmlTag cellBody)
  {
    return addCell(cellBody.toString());
  }

  /** 
   * Adds the string value of the provided number to the table row.
   * 
   * @param number
   * @return the TD tag
   */
  public HtmlTag addCell(long number)
  {
    return addCell(String.valueOf(number));
  }

  /**
   * Adds the supplied body text as a new table cell.
   *
   * @param value
   * @return the TD tag
   */
  public HtmlTag addCell(String value)
  {
    if ( tr == null )
    {
      newRow();
    }
    if ( col >= colMax )
    {
      newRow();
    }
    DataCell colHdr = tableHeader.getCellAt(col++);
    String align = colHdr.isNumeric() ? "right" : "left";
    HtmlTag td = tr.add(new HtmlTag("td", value));
    td.setAttribute("align", align);
    return td;
  }

  /**
   * Adds a TD cell to table and returns it
   *
   * @param value
   * @return the TD tag
   */
  public HtmlTag td()
  {
    return addTdTh("td");
  }

  /**
   * Adds a TH cell to table and returns it
   *
   * @param value
   * @return the TD tag
   */
  public HtmlTag th()
  {
    return addTdTh("th");
  }

  // adds <tag> applying justification as needed, returns the tag
  private HtmlTag addTdTh(String tag)
  {
    if ( tr == null )
    {
      newRow();
    }
    if ( col >= colMax )
    {
      newRow();
    }
    DataCell colHdr = tableHeader.getCellAt(col++);
    String align = colHdr.isNumeric() ? "right" : "left";
    HtmlTag td = tr.add(new HtmlTag(tag));
    td.setAttribute("align", align);
    return td;
  }

  /** sets the table's border attribute to the value given */
  public HtmlTable border(int n)
  {
    setAttribute("border", n);
    return this;
  }

  /** sets the table's width attribute as a percentage to the value given */
  public HtmlTable widthPct(int n)
  {
    return (HtmlTable) setAttribute("width", n + "%");
  }

  /** sets the table's width attribute to the value given */
  public HtmlTable width(int n)
  {
    return (HtmlTable) setAttribute("width", n);
  }

  /** sets the table's spacing attribute to the value given */
  public HtmlTable spacing(int n)
  {
    return (HtmlTable) setAttribute("spacing", n);
  }

  /** sets the table's padding attribute to the value given */
  public HtmlTable padding(int n)
  {
    return (HtmlTable) setAttribute("padding", n);
  }

  /** Sets a main heading for the table */
  public HtmlTable heading(String heading)
  {
    this.heading = heading;
    return this;
  }

  /** Adds the provides cells, wraps to new row if needed */
  public void addCells(String... values)
  {
    for (String v : values)
    {
      addCell(v);
    }
  }

  public void resetRowCount()
  {
    odd = true;
  }

  public boolean isAlternateColours()
  {
    return alternateColours;
  }

  public void setAlternateColours(boolean alternateColours)
  {
    this.alternateColours = alternateColours;
  }

}
