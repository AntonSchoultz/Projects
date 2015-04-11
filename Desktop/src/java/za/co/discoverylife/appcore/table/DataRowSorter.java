/**
 * DataRowSorter.java
 */
package za.co.discoverylife.appcore.table;

import java.util.Comparator;

/**
 *
 * @author Anton Schoultz (2013)
 */
public class DataRowSorter implements Comparator<DataRow>
{

  private int[] sortColumns;

  /**
   * Constructs a sorter for column numbers provided.
   * <p>Column numbers are given in base 1,
   * <br>negative values indicate descending order.-
   * @param sortColumns
   */
  public DataRowSorter(int... sortColumnNumbers)
  {
    super();
    this.sortColumns = sortColumnNumbers;
  }

  public int compare(DataRow a, DataRow b)
  {
    int delta = 0;
    if ( sortColumns == null || sortColumns.length == 0 )
    {
      sortColumns = new int[]{1};
    }
    for (int i = 0; i < sortColumns.length; i++)
    {
      int spec = sortColumns[i];
      int direction = 1;
      int n = spec;
      if ( spec < 0 )
      {
        direction = -1;
        n = -n;
      }
      DataCell dca = a.getCellAt(n - 1);
      DataCell dcb = b.getCellAt(n - 1);
      if ( dca.isNumeric() )
      {
        delta = (int) (dca.getNumericValue() - dcb.getNumericValue());
      }
      else
      {
        delta = dca.getValue().compareTo(dcb.getValue());
      }
      if ( delta != 0 )
      {
        return delta * direction;
      }
    }// next key column
    return 0;// exact match for keys given
  }
}
