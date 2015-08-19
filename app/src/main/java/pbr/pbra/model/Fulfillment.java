package pbr.pbra.model;

public class Fulfillment {
  public String orderId = "";
  public String assignment = "";
  public String comment = "";
  public int complete;
  public int returned;
  public int version;

  @Override
  public boolean equals(Object rhs) {
    if (!(rhs instanceof Fulfillment)) {
      return false;
    }
    Fulfillment r = (Fulfillment) rhs;
    return orderId.equals(r.orderId) &&
        assignment.equals(r.assignment) &&
        comment.equals(r.comment) &&
        complete == r.complete &&
        returned == r.returned &&
        version == r.version;
  }
}
