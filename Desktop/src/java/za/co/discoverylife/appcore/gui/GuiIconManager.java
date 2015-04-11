package za.co.discoverylife.appcore.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import za.co.discoverylife.desktop.util.PixelColor;

/**
 * Handles fetching of named icons, optionally using a cache.
 * Can also produce composite icons by over-laying 'sub-icons'.
 * eg main icon might be 'application' sub-icon could be one of 'add,remove,edit' etc
 * so 'application+#add' returns application.png with #add.png overlaid.
 * This allows for a more compact set of icons in the icon.jar
 * 
 * @author anton11
 */
public class GuiIconManager
{
  /** The cache of icons, disabled by {@link #stopCache()} which sets this to null */
  private static Map<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();

  /** Clears the icon cache - and leaves it active */
  public static void clearCache()
  {
    iconMap = new HashMap<String, ImageIcon>();
  }

  /** Starts the icon cache - (clean start) */
  public static void startCache()
  {
    clearCache();
  }

  /** Disables the icon cache (releasing any existing cache) */
  public static void stopCache()
  {
    iconMap = null;
  }

  /** returns an ImageIcon for the name provided */
  public static ImageIcon getIcon(String iconName)
  {
    ImageIcon icon = null;
    if ( iconName == null || iconName.length() == 0 )
    {
      return null;
    }
    // if cached use the cached version
    if ( iconMap != null )
    {
      icon = iconMap.get(iconName);
      if ( icon != null )
        return icon;
    }
    iconName = iconName.replace('+', '~');
    String[] sa = iconName.split("~");
    icon = retrieveIcon(sa[0]);
    if ( sa.length > 1 )
    {
      for (int i = 1; i < sa.length; i++)
      {
        ImageIcon iconMod = retrieveIcon(sa[i]);
        if ( iconMod != null )
        {
          icon = overLay(icon, iconMod);
        }
      }
    }
    if ( iconMap != null )
    {
      iconMap.put(iconName, icon);
    }
    return icon;
  }

  /** overlay the mainIcon with non-transparent parts of iconMod */
  private static ImageIcon overLay(ImageIcon iconMain, ImageIcon iconMod)
  {
    ImageIcon icon = new ImageIcon();
    Image mainimage = iconMain.getImage();
    int h = iconMain.getIconHeight();
    int w = iconMain.getIconWidth();
    // start by copying the main icon data
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.drawImage(mainimage, 0, 0, w, h, null);
    // get the over lay image data
    BufferedImage overlay = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gOver = overlay.createGraphics();
    gOver.drawImage(iconMod.getImage(), 0, 0, w, h, null);
    // scan image, where overlay is non-transparent, copy it over the main image
    for (int y = 0; y < h; y++)
    {
      for (int x = 0; x < w; x++)
      {
        int argb = overlay.getRGB(x, y);
        if ( (argb & 0xFF000000) != 0 )
        {
          PixelColor pcIcon = new PixelColor(image.getRGB(x, y));
          PixelColor pcOver = new PixelColor(argb);
          // pcOver.setAlpha(0xaa);
          image.setRGB(x, y, pcIcon.mix(pcOver));
        }
      }
    }
    g.dispose();
    gOver.dispose();
    // set edited image as the return icon image
    icon.setImage(image);
    return icon;
  }

  /**
   * Rotates an Icon (clockwise) in multiples of 90 degrees.
   * @param icon ImageIcon to be rotated
   * @param rotate 0=no rotation, 1=90CW, 2=180, 3=CCW
   * @return ImageIcon rotated.
   */
  public static ImageIcon rotateIcon(ImageIcon icon, int rotate)
  {
    rotate = rotate & 0x3;
    if ( rotate == 0 )
    {
      return icon;
    }
    Image mainimage = icon.getImage();
    int h = icon.getIconHeight();
    int w = icon.getIconWidth();
    int d = Math.max(h, w);
    h = d;
    w = d;
    d--;
    BufferedImage imageIn = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = imageIn.createGraphics();
    g.drawImage(mainimage, 0, 0, w, h, null);
    //-- output image
    BufferedImage imageOut = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gOver = imageOut.createGraphics();
    for (int y = 0; y < h; y++)
    {
      for (int x = 0; x < w; x++)
      {
        int argb = imageIn.getRGB(x, y);
        int ox = x;
        int oy = y;
        switch (rotate)
        {
          case 3 :// CCW 90
            ox = y;
            oy = d - x;
            break;
          case 2 :// CW 180
            ox = d - x;
            oy = d - y;
            break;
          case 1 :// CW 90
            ox = d - y;
            oy = x;
            break;
          case 0 :
          default :
            break;
        }
        imageOut.setRGB(ox, oy, argb);
      }
    }
    g.dispose();
    gOver.dispose();
    // set edited image as the return icon image
    icon.setImage(imageOut);
    return icon;
  }

  /** Swap colour nibbles from 'rgb' to the specified sequence */
  public static ImageIcon recolour(ImageIcon iconMain, String rgb)
  {
    rgb = rgb.toUpperCase();
    if ( rgb.equalsIgnoreCase("RGB") )
    {
      return iconMain;
    }
    int rx = rgb.indexOf("R");
    int gx = rgb.indexOf("G");
    int bx = rgb.indexOf("B");
    if ( rx < 0 || gx < 0 || bx < 0 )
      return iconMain;
    if ( rx > 2 || gx > 2 || bx > 2 )
      return iconMain;

    rx = (2 - rx) * 8;
    gx = (2 - gx) * 8;
    bx = (2 - bx) * 8;

    ImageIcon icon = new ImageIcon();
    Image mainimage = iconMain.getImage();
    int h = iconMain.getIconHeight();
    int w = iconMain.getIconWidth();
    // start by copying the main icon data
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();
    g2d.drawImage(mainimage, 0, 0, w, h, null);
    // get the over lay image data
    BufferedImage overlay = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gOver = overlay.createGraphics();
    // scan image, where overlay is non-transparent, copy it over the main image
    for (int y = 0; y < h; y++)
    {
      for (int x = 0; x < w; x++)
      {
        int argb = image.getRGB(x, y);

        int a = argb & 0xFF000000;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb) & 0xFF;
        int outRgb = a + (r << rx) + (g << gx) + (b << bx);
        overlay.setRGB(x, y, outRgb);
      }
    }
    g2d.dispose();
    gOver.dispose();
    // set edited image as the return icon image
    icon.setImage(overlay);
    return icon;
  }

  /** Constructs the icon specified, then saves it in the given File */
  public static void saveIcon(String iconRef, File file) throws IOException
  {
    ImageIcon icon = getIcon(iconRef);
    saveIcon(icon, file);
  }

  /** Saves the provided icon in the given File */
  public static void saveIcon(ImageIcon icon, File file) throws IOException
  {
    Image mainimage = icon.getImage();
    int h = icon.getIconHeight();
    int w = icon.getIconWidth();
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.drawImage(mainimage, 0, 0, w, h, null);
    ImageIO.write(image, "PNG", file);
  }

  /** Retrieve the named icon */
  protected static ImageIcon retrieveIcon(String iconName)
  {
    ImageIcon icon = null;
    String key = iconName;
    // if cached use the cached version
    if ( iconMap != null )
    {
      icon = iconMap.get(key);
      if ( icon != null )
        return icon;
    }
    int rotate = 0;
    while (iconName.endsWith("/"))
    {
      rotate++;
      iconName = iconName.substring(0, iconName.length() - 1);
    }
    int cx = iconName.indexOf("^");
    String rgb = "rgb";
    if ( cx > 0 )
    {
      rgb = iconName.substring(cx + 1);
      iconName = iconName.substring(0, cx);
    }

    //System.out.println("### GuiIconManager : " + iconName + " rotate=" + rotate);
    String iconFileName = ("images/icons/" + iconName + ".png").toLowerCase();
    try
    {
      InputStream is = ClassLoader.getSystemResourceAsStream(iconFileName);
      if ( is != null )
      {
        byte[] ba = new byte[8000];
        is.read(ba);
        icon = new ImageIcon(ba);
      }
      else
      {
        System.err.println("Could not find resource for icon " + iconName + " @ " + iconFileName);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    //--
    icon = rotateIcon(icon, rotate);
    icon = recolour(icon, rgb);
    iconMap.put(key, icon);
    return icon;
  }

}
