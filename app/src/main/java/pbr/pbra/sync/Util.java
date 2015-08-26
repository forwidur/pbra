package pbr.pbra.sync;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {
  public static void writeMessage(OutputStream s, String m) throws IOException {
    byte[] msg = m.getBytes();
    byte[] buf = new byte[msg.length + 1];
    buf[msg.length] = 0;
    System.arraycopy(msg, 0, buf, 0, msg.length);
    s.write(buf);
  }

  public static String readMessage(InputStream s) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    while (true) {
      int b = s.read();
      if (b == 0 || b == -1) break;
      out.write(b);
    }
    return out.toString();
  }
}
