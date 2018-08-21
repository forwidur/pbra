package pbr.pbra.model;

public class Assignment {
  public String id = "";
  public String orderId = "";
  public int bikeId = 0;
  public int returned = 0;
  public int version;

  // Needed by Firebase.
  public Assignment() {}

  public Assignment(String id) {
    this.id = id;
    this.version = (int) (System.currentTimeMillis()/1000);
  }

  public Assignment(String orderId, int num) {
    this.id = makeId(orderId, num);
    this.orderId = orderId;
    this.version = (int) (System.currentTimeMillis()/1000);
  }

  public Assignment(String id, String orderId, int bikeId, int returned) {
    this.id = id;
    this.orderId = orderId;
    this.bikeId = bikeId;
    this.returned = returned;
    this.version = (int) (System.currentTimeMillis()/1000);
  }

  static public String makeId(String oid, int num) {
    return String.format("%s-%d", oid, num);
  }
}
