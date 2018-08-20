package pbr.pbra.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Barcode {
  private static Pattern qr = Pattern.compile("HTTPSWWWBLUEGOGOCOMQRCODEHTMLNO00.*(\\d{7})");

  private static Pattern newBar = Pattern.compile("0{5}(\\d{4})");

  public static String strip(String c) {
    Matcher m = qr.matcher(c);
    if(m.matches()) {
      return m.group(1);
    }
    m = newBar.matcher(c);
    if(m.matches()) {
      return m.group(1);
    }
    return c;
  }
}
