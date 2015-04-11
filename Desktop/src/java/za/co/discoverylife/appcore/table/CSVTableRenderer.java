package za.co.discoverylife.appcore.table;

/** Renderer to convert a DataTable to CSV format */
public class CSVTableRenderer implements ITableRenderer
{
  String name;
  StringBuilder sb = new StringBuilder();
  boolean first = true;

  public String toString()
  {
    return sb.toString();
  }

  public void beginTable(DataTable dataTable)
  {
    name = dataTable.getTitle();
    DataRow headings = dataTable.getColumnHeaders();
    beginRow();
    for (DataCell c : headings.getCells())
    {
      doCell(c);
    }
    endRow();
  }

  public void beginRow()
  {
    first = true;
  }

  public void doCell(DataCell cell)
  {
    if ( !first )
    {
      sb.append(",");
    }
    else
    {
      first = false;
    }
    if ( cell.isNumeric() )
    {
      sb.append(cell.getValue());
    }
    else
    {
      sb.append("\"").append(cell.getValue()).append("\"");
    }
  }

  public void endRow()
  {
    sb.append("\r\n");
  }

  public void endTable()
  {
    System.out.println("*** " + name + " ***");
    System.out.println(toString());
  }

}
