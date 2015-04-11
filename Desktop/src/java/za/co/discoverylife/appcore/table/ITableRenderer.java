package za.co.discoverylife.appcore.table;

/** defines methods required to render a table */
public interface ITableRenderer
{
  /** Start of table with title and column headers */
  public void beginTable(DataTable dataTable);

  /** Start of a row of data */
  public void beginRow();

  /** Renders a cell */
  public void doCell(DataCell cell);

  /** End of data row */
  public void endRow();

  /** End of table */
  public void endTable();

}
