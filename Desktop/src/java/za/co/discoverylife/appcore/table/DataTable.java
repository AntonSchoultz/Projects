package za.co.discoverylife.appcore.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import za.co.discoverylife.appcore.field.FieldAccessException;
import za.co.discoverylife.appcore.field.FieldAccessor;
import za.co.discoverylife.appcore.field.ValueObject;
import za.co.discoverylife.appcore.util.DateTime;

/**
 * Represents a table of data rows
 * 
 * @author anton11
 * 
 */
public class DataTable extends ValueObject
{
  /** title for this table */
  private String title = " ";

  /** maps heading to field name */
  private HashMap<String, String> headVsName = new HashMap<String, String>();

  /** list of column headings */
  private ArrayList<String> headList = new ArrayList<String>();

  /** row of column headers */
  private DataRow columnHeaders = new DataRow();

  /** Vector of data rows */
  private Vector<DataRow> rows = new Vector<DataRow>();

  /** Id for table when rendered as html */
  private String id = null;

  /** Map heading to a look-up Map */
  private HashMap<String, Map<String, String>> translations = new HashMap<String, Map<String, String>>();

  /**
   * CONSTRUCTOR
   */
  public DataTable()
  {
  }

  /**
   * CONSTRUCTOR which accepts a table title
   */
  public DataTable(String title)
  {
    this.title = title;
  }

  /**
   * CONSTRUCTOR which accepts a table title and a sample object for row data
   * @throws FieldAccessException 
   */
  public DataTable(String title, ValueObject sampleRowObject) throws FieldAccessException
  {
    this.title = title;
    autoAddColumns(sampleRowObject);
  }

  /**
   * CONSTRUCTOR which accepts a table title and a sample object for row data
   * @throws Exception 
   */
  public DataTable(String title, Collection<? extends ValueObject> data) throws Exception
  {
    this.title = title;
    loadTable(data);
  }

  /** 
   * Automatically define columns as the annotated fields in the supplied object
   * 
   * @param sampleRowObject
   * @throws FieldAccessException 
   */
  public void autoAddColumns(ValueObject sampleRowObject) throws FieldAccessException
  {
    List<FieldAccessor> lst = FieldAccessor.listAccessorsForObject(sampleRowObject);
    for (FieldAccessor fa : lst)
    {
      if ( fa.hasAnnotation() )
      {
        addColumnMap(fa.getFieldName(), fa.getLabel());
      }
    }
    // columnHeaders.clear();
    // rows.clear();
    for (String head : headList)
    {
      String fieldName = headVsName.get(head);
      FieldAccessor fa = new FieldAccessor(sampleRowObject, fieldName);
      DataCell colHdrCell = new DataCell(head, (fa.isNumeric() ? 10 : 30), fa.isNumeric());
      columnHeaders.addCell(colHdrCell);
    }
  }

  /** Adds a mapping for column heading vs field name */
  public void addColumnMap(String fieldName, String columnHeading)
  {
    headVsName.put(columnHeading, fieldName);
    headList.add(columnHeading);
  }

  /** Adds a mapping for column heading vs field name */
  public void addColumnMap(String fieldName)
  {
    addColumnMap(fieldName, fieldName);
  }

  /** creates column headings based on a value object's fields */
  public void extractColumnHeaders(ValueObject vo) throws Exception
  {
    if ( headList.size() <= 0 )
    {
      autoAddColumns(vo);
      return;
    }
    columnHeaders.clear();
    rows.clear();
    for (String head : headList)
    {
      String fieldName = headVsName.get(head);
      FieldAccessor fa = new FieldAccessor(vo, fieldName);
      DataCell colHdrCell = new DataCell(head, (fa.isNumeric() ? 10 : 30), fa.isNumeric());
      addColumn(colHdrCell);
    }
  }

  /** Add a translation map for a column (key is heading)
   * 
   * @param heading Text of the heading (key to select the map)
   * @param map Map of code:translated-value
   */
  public void addTranslation(String heading, Map<String, String> map)
  {
    translations.put(heading, map);
  }

  protected void makeColumnText(String heading)
  {
    int cx = columnHeaders.indexOf(heading);
    DataCell cellHdr = columnHeaders.getCellAt(cx);
    cellHdr.setNumeric(false);
    cellHdr.setWidth(30);
  }

  /** 
   * Add a translation map for the provided array of strings,
   * the key is the index. (base 0)
   * @param heading Text of the heading (key to select the map)
   * @param values String[] of values for "0","1","2","3"...
   */
  public void addTranslation(String heading, String... values)
  {
    addTranslation(heading, buildMap(values));
  }

  /** 
   * Extracts a data row for the give value object,
   * (column mappings must be defined)
   *
   * @param vo
   * @throws Exception 
   */
  public DataRow extractDataRow(ValueObject vo) throws Exception
  {
    if ( headList.size() <= 0 )
    {
      autoAddColumns(vo);
    }
    if ( columnHeaders.isEmpty() )
    {
      extractColumnHeaders(vo);
    }
    DataRow row = new DataRow();
    for (String head : headList)
    {
      String fieldName = headVsName.get(head);
      FieldAccessor fa = new FieldAccessor(vo, fieldName);
      String fieldString = fa.getStringFromField();
      boolean isnumeric = fa.isNumeric();
      Map<String, String> m = translations.get(head);
      if ( m != null )
      {
        // translated column
        String text = (String) m.get(fieldString);
        if ( text != null )
        {
          fieldString = text;
          isnumeric = false;
        }
        if ( rows.size() == 0 )
        {
          makeColumnText(head);
        }
      }
      else
      {
        if ( isnumeric &&
            (fieldName.toLowerCase().startsWith("time")
            || fieldName.toLowerCase().endsWith("time")) )
        {
          long tick = Long.parseLong(fieldString);
          DateTime dtm = new DateTime();
          dtm.setTimeInMillis(tick);
          fieldString = dtm.toStringYmdHms();
          isnumeric = false;
          if ( rows.size() == 0 )
          {
            makeColumnText(head);
          }
        }
      }
      //row.addCell(fieldString, (isnumeric ? 10 : 30), isnumeric);
      row.addCell(fieldString, 0, isnumeric);
    }
    return row;
  }

  public static HashMap<String, String> buildMap(String[] ary)
  {
    HashMap<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < ary.length; i++)
    {
      map.put(String.valueOf(i), ary[i]);
    }
    return map;
  }

  /** Loads Rows from a collection of objects */
  public void loadTable(Collection<? extends ValueObject> data) throws Exception
  {
    for (ValueObject vo : data)
    {
      addDataRow(vo);
    }
  }

  /**
   * Extracts a data row for the give value object,
   * (column mappings must be defined)
   * and then adds it to the table.
   *
   * @param vo
   * @throws Exception
   */
  public void addDataRow(ValueObject vo) throws Exception
  {
    rows.add(extractDataRow(vo));
  }

  /**
   * Adds a column definition to this table. All columns must be defined before
   * any data is added.
   * 
   * @param columnDef
   *          DataColumn object to define the column
   * @throws Exception
   *           If there is data already.
   */
  public void addColumn(DataCell columnDef) throws Exception
  {
    if ( rows.size() > 0 )
    {
      throw new Exception("Table must be empty to add columns");
    }
    columnHeaders.addCell(columnDef);
  }

  /** returns the number of columns in the data set */
  public int getColumnCount()
  {
    return columnHeaders.size();
  }

  /**
   * returns the column heading for the supplied column number (or null if
   * invalid)
   */
  public String getHeader(int colNo)
  {
    return columnHeaders.getValueAt(colNo);
  }

  /**
   * Returns the column number for the supplied column heading (or -1 if not
   * found)
   * <br>Search is NOT case-sensitive.
   */
  public int getColumnIndex(String headerName)
  {
    return columnHeaders.indexOf(headerName);
  }

  /** Adds a row to the table */
  public void addRow(DataRow row)
  {
    rows.add(row);
  }

  /**
   * Add a row of data to the table (as String representations) No of item must
   * match the no of columns,
   * 
   * @param row
   *          String[]
   */
  public void addRowData(String[] row)
  {
    rows.add(new DataRow(columnHeaders, row));
  }

  /** Adds a data row by extracting data fields matching the column headers */
  public void addDataObject(ValueObject vo)
  {
    Properties p = vo.getAsProperties();
    DataRow row = new DataRow();
    for (DataCell dch : columnHeaders.getCells())
    {
      String value = String.valueOf(p.getProperty(dch.getValue()));
      row.addCell(value, (dch.isNumeric() ? 10 : 30), dch.isNumeric());
    }
  }

  /**
   * Returns the specified row as a String[].
   * 
   * @param rowNumber
   *          index of row 0-max
   * @return DataRow
   */
  public DataRow getRow(int rowNumber)
  {
    checkRow(rowNumber);
    return rows.get(rowNumber);
  }

  /** 
   * Sort the data rows according to the column specifiers (base:1,-ve=descend)
   * 
   * @param colNos
   */
  public void sortBy(int... colNos)
  {
    DataRowSorter sorter = new DataRowSorter(colNos);
    Collections.sort(rows, sorter);
  }

  /**
   * Sort data rows by the headings given.
   * <p>all lower-case is descending order.
   * @param sortkeys
   */
  public void sortBy(String sortkeys)
  {
    String[] keys = sortkeys.split(",");
    int[] sort = new int[keys.length];
    for (int i = 0; i < keys.length; i++)
    {
      String key = keys[i];
      int n = getColumnIndex(key);
      if ( n < 0 )
      {
        throw new InvalidColumnException(key);
      }
      n++;
      // all lower is descending
      if ( key.toLowerCase().compareTo(key) == 0 )
      {
        n = -n;
      }
      sort[i] = n;
    }
    sortBy(sort);
  }

  /**
   * Searches for the given value in the specified column. returns the row index
   * if found otherwise returns -1
   */
  public int lookUp(String headerName, String value)
  {
    int ix = -1;
    int col = getColumnIndex(headerName);
    int mx = rows.size();
    for (int rx = 0; rx < mx; rx++)
    {
      DataRow row = rows.get(rx);
      String data = row.getValueAt(col);
      if ( data != null && data.equalsIgnoreCase(value) )
      {
        ix = rx;
        break;
      }
    }
    return ix;
  }

  /** Throws exception if column is out of range */
  private void checkRow(int row)
  {
    if ( row < 0 || row > rows.size() )
      throw new IndexOutOfBoundsException("Invalid row index " + row);
  }

  /** Sets this table's title */
  public void setTitle(String title)
  {
    this.title = title != null ? title : " ";
  }

  /** Render this table via the given renderer */
  public ITableRenderer render(ITableRenderer renderer)
  {
    renderer.beginTable(this);
    for (DataRow row : rows)
    {
      renderer.beginRow();
      for (DataCell cell : row.getCells())
      {
        renderer.doCell(cell);
      }
      renderer.endRow();
    }
    renderer.endTable();
    return renderer;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getTitle()
  {
    return title;
  }

  public DataRow getColumnHeaders()
  {
    return columnHeaders;
  }

}
