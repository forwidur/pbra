package pbr.pbra.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BarcodeTest {
  @Test
  public void testSmoke() {
    assertEquals(Barcode.strip("1234"), "1234");
    assertEquals(Barcode.strip("12345"), "12345");
  }

  @Test
  public void testQr() {
    assertEquals(Barcode.strip("HTTPSWWWBLUEGOGOCOMQRCODEHTMLNO002001234"), "2001234");
  }
}