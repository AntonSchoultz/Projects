package za.co.discoverylife.appcore.table;

/**
 * Represents a cell of data in a table. Holds a string representation of the
 * value, and a flag to indicate if the value is numeric (which should be right
 * justified)
 * 
 * @author anton11
 * 
 */
public class DataCell
{

  private String value = "";// data value/heading
  private boolean numeric = false;// is data numeric? (right justify)
  private int width = 0;

  public DataCell()
  {
    value = "";
    numeric = false;
  }

  public DataCell(String value, int width, boolean isNumeric)
  {
    this.value = value;
    this.width = width;
    this.numeric = isNumeric;
  }

  public boolean isNumeric()
  {
    return numeric;
  }

  public void setNumeric(boolean numeric)
  {
    this.numeric = numeric;
  }

  public String getValue()
  {
    return value;
  }

  public double getNumericValue()
  {
    return Double.parseDouble(value);
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  @Override
  public String toString()
  {
    return "DataCell[" + (numeric ? "#" : "$") + value + "]";
  }

  @Override
  protected DataCell clone()
  {
    return new DataCell(new String(value), width, numeric);
  }

  /** Render this row via the given renderer */
  public void render(ITableRenderer renderer)
  {
    renderer.doCell(this);
  }

  public int getWidth()
  {
    return width;
  }

  public void setWidth(int width)
  {
    this.width = width;
  }

}
