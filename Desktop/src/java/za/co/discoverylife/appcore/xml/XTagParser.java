package za.co.discoverylife.appcore.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Parses a file / text into an XML document tree.
 * 
 * @author anton11
 */
public class XTagParser
{

  // read buffer - part of error message
  private final int mxc = 200;
  private char[] cycle = new char[mxc];
  private int cx = 0;
  private int lineNo = 0;
  private int charNo = 0;

  // parser characters
  private int pc = 0;
  private int ch = 0;

  // Reader to get xml doc from for parsing
  private Reader in;

  /** CONSTRUCTOR - private */
  private XTagParser()
  {

  }

  /** parse the provided file into a doc tree */
  public static XTag parseFile(File fXmlDoc) throws Exception
  {
    return parseFile(fXmlDoc.getAbsolutePath());
  }

  /** parse the named file into a doc tree */
  public static XTag parseFile(String path) throws Exception
  {
    XTagParser parser = new XTagParser();
    FileReader fr = new FileReader(path);
    XTag doc = parser.parseStream(fr);
    fr.close();
    return doc;
  }

  /**
   * parse a piece of the input stream -
   * ( decides if we have text, comment or a tag )
   */
  private XTag parse() throws Exception
  {
    skipWhite();
    if ( ch == '<' )
    {
      // Tag or comment
      if ( getChar() == '!' )
      {
        getChar();
        if ( ch == '-' )
        {
          return parseComment();
        }
        if ( ch == '[' )
        {
          return parseCData();
        }
        // DOCTYPE
        return parseOther("<!");
      }
      if ( ch == '?' )
      {
        return parseInstruction();
      }
      // end tag ?
      if ( ch == '/' )
      {
        return null;
      }
      else
      {
        return parseTag();
      }
    }
    else
    {
      return parseText();
    }
  }

  /** Parse the XML body */
  private XTag parseBody() throws Exception
  {
    skipWhite();
    return parse();
  }

  /** skip over white space (blank, return, new line, tab) */
  private void skipWhite()
  {
    while (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t')
    {
      getChar();
    }
  }

  /** Handle parsing of a CDATA tag <![CDATA[ ... ]]> */
  private XTag parseCData() throws Exception
  {
    StringBuffer sb = new StringBuffer();
    expect("[CDATA[");
    getChar();
    while (!((ch == ']') && (pc == ']')) && ch >= 0)
    {
      sb.append((char) pc);
      getChar();
    }
    if ( getChar() != '>' )
    {
      syntax("Missing > for CDATA"); // >
    }
    XTag x = new XTag("[", sb.toString().trim());
    getChar();
    return x;
  }

  /** Handle parsing of a comment tag */
  private XTag parseComment() throws Exception
  {
    StringBuffer sb = new StringBuffer();
    expect("--");
    getChar(); // prime
    while (!((ch == '-') && (pc == '-')))
    {
      sb.append((char) pc);
      getChar();
    }
    if ( getChar() != '>' )
    {
      syntax("Missing > for comment"); // >
    }
    XTag x = new XTag("!", sb.toString().trim());
    getChar();
    return x;
  }

  /**
   * Initiates the parsing process from the given reader, returning the xml
   * tree's root node
   */
  private XTag parseStream(Reader inp) throws Exception
  {
    in = inp;
    ch = pc = 0;
    lineNo = 1;
    charNo = 0;
    getChar();
    XTag tb = parseBody();
    while (tb.isInstruction())
    {
      XTag ti = tb;
      tb = parseBody();
      while (tb.getName().equalsIgnoreCase("") || tb.getName().equalsIgnoreCase("!"))
      {
        tb = parseBody();
      }
      tb.addInstruction(ti);
    }
    return tb;
  }

  /**
   * Parse characters up to a stop character
   * and return string
   */
  private String parseStringTo(char endChar) throws Exception
  {
    StringBuffer sb = new StringBuffer();
    while (ch != endChar && ch > 0 && ch != '>' && ch != '<')
    {
      sb.append((char) ch);
      getChar();
    }
    if ( ch > 0 )
    {
      expect(endChar);
    }
    return sb.toString();
  }

  /** parse Instruction */
  private XTag parseInstruction() throws Exception
  {
    // get the tag name ?xxxx an dattributes
    boolean done = false;
    XTag x = parseTagName();
    done = parseTagAttributes(x);
    if ( !done )
    {
      syntax("Error parsing instruction tag");
    }
    return x;
  }

  /**
   * Parse other (unrecognised tags <!....>)
   * These will be handled as in-line text
   */
  private XTag parseOther(String start) throws Exception
  {
    StringBuffer sb = new StringBuffer();
    sb.append(start);
    while (ch >= 0 && ch != '>')
    {
      sb.append((char) ch);
      getChar();
    }
    expect('>');
    sb.append('>');
    return new XTag("", sb.toString());
  }

  /**
   * Parse characters up to a stop character
   * and return string
   * 
   * @param endChar
   * @return
   * @throws Exception
   */
  private String parseQuoteString(char endChar) throws Exception
  {
    StringBuffer sb = new StringBuffer();
    while (ch != endChar && ch > 0)
    {
      sb.append((char) ch);
      getChar();
    }
    if ( ch > 0 )
    {
      expect(endChar);
    }
    return sb.toString();
  }

  /** parse Tag */
  private XTag parseTag() throws Exception
  {
    // start normal tag
    boolean done = false;
    XTag x = parseTagName();
    done = parseTagAttributes(x);
    if ( done )
    {
      return x;
    }
    expect('>');
    // into contents of this tag
    while (ch >= 0)
    {
      XTag child = parse();
      if ( child == null )
      {
        break; // close tag '</.....
      }
      if ( !child.isEmpty() )
      {
        x.addChild(child);
      }
    }
    expect('/');
    // check for matching closing tag
    String clsTag = parseStringTo('>');
    String xmlTag = x.getName();
    if ( !clsTag.toString().equalsIgnoreCase(xmlTag) )
    {
      syntax("Closing tag '" + clsTag.toString() + "' does not match opening tag '" + xmlTag + "'");
    }
    return x;
  }

  /** parse the attributes and add them to the tag */
  private boolean parseTagAttributes(XTag x) throws Exception
  {
    boolean done = false;
    while (ch != '>')
    {
      while (ch <= ' ' && ch > 0)
      {
        getWhite();
      }
      if ( ch == '/' || ch == '?' )
      {
        // empty tag (or instruction) so we're done
        getChar();
        expect('>');
        done = true;
        break;
      }
      // attributes ..
      String name = parseStringTo('=');
      if ( ch != '\'' && ch != '"' )
      {
        syntax("Expected quoted string");
      }
      char qt = (char) ch;
      getChar(); // skip open quote
      String value = parseQuoteString(qt);
      x.setAttribute(name.trim(), value);
    }
    return done;
  }

  /** Parse the tagname */
  private XTag parseTagName() throws Exception
  {
    StringBuffer sb = new StringBuffer();
    while (ch != ' ' && ch != '>' && ch != '/')
    {
      sb.append((char) ch);
      getWhite();
    }
    String xmlTag = sb.toString();
    XTag x = new XTag(xmlTag);
    return x;
  }

  /** handle parsing of plain text */
  private XTag parseText() throws Exception
  {
    StringBuffer sb = new StringBuffer();
    while (ch != '<' && ch > 0)
    {
      sb.append((char) ch);
      getWhite();
    }
    String txt = removeWhiteSpace(sb.toString()).trim();
    XTag x = new XTag("", txt);
    return x;
  }

  /** throws a syntax error giving the line and column references */
  private void syntax(String hint) throws Exception
  {
    throw new Exception(hint + " at " + lineNo + ":" + charNo + "\r\n" + tail());
  }

  /** return most recently read bit of text */
  private String tail()
  {
    StringBuffer sb = new StringBuffer();
    cx = (++cx) % mxc;
    for (int i = 0; i < mxc; i++)
    {
      sb.append(cycle[cx]);
      cx = (++cx) % mxc;
    }
    return sb.toString();
  }

  /**
   * checks that the required character has been read, if not, throws a syntax
   * exception
   */
  private void expect(char c) throws Exception
  {
    if ( ch != c )
    {
      syntax("Expected '" + c + "' got '" + ((char) ch) + "' instead.");
    }
    getChar(); // get next char
  }

  /** Check for a sequence of expected characters */
  private void expect(String str) throws Exception
  {
    int mx = str.length();
    for (int ix = 0; ix < mx; ix++)
    {
      char chr = str.charAt(ix);
      expect(chr);
    }
  }

  /** reads a character from the input reader - tracks line and column number */
  private int getChar()
  {
    if ( ch < 0 )
    {
      return ch;
    }
    pc = ch;
    try
    {
      ch = in.read();
    }
    catch (IOException ex)
    {
      ch = -1;
    }
    if ( ch == '\n' )
    {
      lineNo++;
      charNo = 0;
    }
    charNo++;
    cycle[(++cx) % mxc] = (char) ch;
    return ch;
  }

  /** reads white space */
  private int getWhite()
  {
    getChar();
    if ( ch < ' ' && ch >= 0 )
    {
      ch = ' ';
      if ( pc == ' ' )
      {
        getWhite();
      }
    }
    return ch;
  }

  /** Removes white space from text */
  private String removeWhiteSpace(String inString)
  {
    char prevChar = 0;
    StringBuffer sb = new StringBuffer();
    int mx = inString.length();
    for (int ix = 0; ix < mx; ix++)
    {
      char chr = inString.charAt(ix);
      if ( chr < ' ' )
      {
        chr = ' ';
      }
      if ( chr != ' ' )
      {
        prevChar = chr;
        sb.append(chr);
      }
      else
      {
        if ( prevChar != ' ' )
        {
          prevChar = ' ';
          sb.append(' ');
        }
      }
    }
    return sb.toString();
  }

}
