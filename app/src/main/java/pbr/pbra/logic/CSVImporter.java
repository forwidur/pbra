package pbr.pbra.logic;

import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;

import pbr.pbra.model.Customer;
import pbr.pbra.model.Order;
import pbr.pbra.model.Fulfillment;

public class CSVImporter {
  private final Storage s_;
  static final SimpleDateFormat f_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public CSVImporter(Storage s) {
    s_ = s;
  }

  private static Customer makeCustomer(CSVRecord r) {
    Customer res = new Customer();

    res.email = r.get("Email").toLowerCase();
    res.name = r.get("Billing Name");
    res.name_search = r.get("Billing Name").toLowerCase();
    res.phone = r.get("Billing Phone").replaceAll("[\\+\\-\\(\\)\\s]", "");
    res.address = r.get("Billing Street");

    return res;
  }

  private static Order makeOrder(String id, CSVRecord r) {
    Order res = new Order();

    res.id = id;
    res.type = r.get("Lineitem name");
    res.quantity = Integer.parseInt(r.get("Lineitem quantity"));

    return res;
  }

  public int Process(String path) throws Exception {
    Reader in = new FileReader(path);
    Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

    s_.clean();

    int orders = 0;
    HashSet<String> seenCustomers = new HashSet<String>();
    HashSet<String> seenOrders = new HashSet<String>();
    int orderInc = 0;
    for (CSVRecord r : records) {
      Customer c = makeCustomer(r);
      if (!seenCustomers.contains(c.email)) {
        s_.insertCustomer(c);
        seenCustomers.add(c.email);
      } else {
      }

      String order = r.get("Name");
      if (!seenOrders.contains(order)) {
        seenOrders.add(order);
        orderInc = 0;
      } else {
        orderInc++;
      }
      String orderId = String.format("%s-%d", order, orderInc);
      s_.insertOrder(c.email, makeOrder(orderId, r));

      orders++;
    }

    return orders;
  }

  private int getDate(String s) {
    if (!s.isEmpty()) {
      try {
        return (int) (f_.parse(s).getTime() / 1000);
      } catch (ParseException e) {
        e.printStackTrace();
        return 1;
      }
    }
    return 0;
  }

  private Fulfillment makeFulfillment(CSVRecord r) {
    Fulfillment res = new Fulfillment();

    res.orderId = r.get("orderid");
    res.assignment = r.get("assignment");
    res.comment = r.get("comment");

    res.complete = getDate(r.get("complete"));
    res.returned = getDate(r.get("returned"));
    res.version = getDate(r.get("last_updated"));

    return res;
  }

  public int ProcessFul(String path) throws Exception {
    Reader in = new FileReader(path);
    Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

    int ful = 0;
    for (CSVRecord r : records) {
      Fulfillment f = makeFulfillment(r);
      s_.updateFulfillment(r.get("orderid"), f);
      ful++;
    }
    return ful;
  }
}