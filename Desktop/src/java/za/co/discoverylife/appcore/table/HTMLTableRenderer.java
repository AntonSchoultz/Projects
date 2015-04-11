package za.co.discoverylife.appcore.table;

import za.co.discoverylife.appcore.html.HtmlTable;
import za.co.discoverylife.appcore.html.HtmlTag;

/** Renderer to convert a DataTable to an HTML table */
public class HTMLTableRenderer implements ITableRenderer
{
  HtmlTag pg;

  HtmlTable tbl;
  private boolean alternateColours = true;

  public HTMLTableRenderer(HtmlTag htmlPage)
  {
    pg = htmlPage;
  }

  public void beginTable(DataTable dataTable)
  {
    String title = dataTable.getTitle();
    DataRow headings = dataTable.getColumnHeaders();
    String id = dataTable.getId();
    tbl = pg.table(headings);
    tbl.setAlternateColours(alternateColours);
    tbl.setAttribute("width", "90%");
    tbl.setAttribute("border", "0");
    if ( id != null )
    {
      tbl.setAttribute("id", id);
    }
    tbl.heading(title);
  }
  public void beginRow()
  {
    tbl.newRow();
  }

  public void doCell(DataCell cell)
  {
    tbl.addCell(cell.getValue());
  }

  public void endRow()
  {
  }

  public void endTable()
  {
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
