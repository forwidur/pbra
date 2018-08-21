package pbr.pbra.logic;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Barcode {
  private static ArrayList<Pattern> ps = new ArrayList<Pattern>() {{
    add(Pattern.compile(".*ODEHTMLNO00.*(\\d{7})"));
    add(Pattern.compile("https://www\\.bluegogo\\.com/qrcode\\.html\\?no=00.*(\\d{7})"));
    add(Pattern.compile("0+(\\d{4})"));}};

  public static String strip(String c) {
    for (Pattern p : ps) {
      Matcher m = p.matcher(c);
      if(m.matches()) {
        return m.group(1);
      }
    }
    return c;
  }
}
