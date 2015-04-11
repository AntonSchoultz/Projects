package za.co.discoverylife.appcore.table;

import java.util.ArrayList;

/**
 * Represents a row of data in a table as a List of DataCells.
 * 
 * @author anton11
 */
public class DataRow
{
  private ArrayList<DataCell> cells = new ArrayList<DataCell>();

  /** CONSTRUCTOR */
  public DataRow()
  {
  }

  /** CONSTRUCTOR which creates a copy of another row */
  public DataRow(DataRow other)
  {
    for (DataCell dc : other.cells)
    {
      addCell((DataCell) dc.clone());
    }
  }

  /** CONSTRUCTOR which creates a data row, given headings and values */
  public DataRow(DataRow headings, String[] values)
  {
    int mx = headings.size();
    if ( values.length != mx )
    {
      throw new IndexOutOfBoundsException("Invalid number of data values");
    }
    for (int i = 0; i < mx; i++)
    {
      DataCell hdrCell = headings.cells.get(i);
      addCell(values[i], hdrCell.getWidth(), hdrCell.isNumeric());
    }
  }

  /** Returns sum of cell widths */
  public int getWidth()
  {
    int w = 0;
    for (DataCell dc : cells)
    {
      w += dc.getWidth();
    }
    return w;
  }

  /** Removes all cells from this row */
  public void clear()
  {
    cells.clear();
  }

  /** Adds a cell to this row. */
  public void addCell(DataCell cell)
  {
    cells.add(cell);
  }

  /** Adds a cell to this row.
   * 
   * @param value
   * @param width
   * @param isNumeric
   */
  public void addCell(String value, int width, boolean isNumeric)
  {
    cells.add(new DataCell(value, width, isNumeric));
  }

  /** Sets the string value for the given column number */
  public void setValueAt(int col, String value)
  {
    checkColumn(col);
    cells.get(col).setValue(value);
  }

  /** Returns the string value for the given column number */
  public String getValueAt(int col)
  {
    checkColumn(col);
    return cells.get(col).getValue();
  }

  /** Returns true if the given column number is a numeric column */
  public boolean isNumericAt(int col)
  {
    checkColumn(col);
    return cells.get(col).isNumeric();
  }

  /** Returns the cell at the given column number */
  public DataCell getCellAt(int col)
  {
    checkColumn(col);
    return cells.get(col);
  }

  /** 
   * Return column index for matching value
   * <br>Search is NOT case-sensitive.
   * @param value Column Name
   * @return index of item (-1 if not found)
   */
  public int indexOf(String value)
  {
    for (int i = 0; i < cells.size(); i++)
    {
      if ( cells.get(i).getValue().equalsIgnoreCase(value) )
      {
        return i;
      }
    }
    return -1;// column not found.
  }

  /** returns the number of columns in the data set */
  public int size()
  {
    return cells.size();
  }

  /** Returns true if there are no cells (row is empty) */
  public boolean isEmpty()
  {
    return cells.isEmpty();
  }

  /** Throws exception if column is out of range */
  private void checkColumn(int col)
  {
    if ( col < 0 || col > cells.size() )
      throw new IndexOutOfBoundsException("Invalid column index " + col);
  }

  /** Render this row via the given renderer */
  public void render(ITableRenderer renderer)
  {
    renderer.beginRow();
    for (DataCell cell : cells)
    {
      cell.render(renderer);
    }
    renderer.endRow();
  }

  /** Return ArrayList of the cells */
  public ArrayList<DataCell> getCells()
  {
    return cells;
  }

}
