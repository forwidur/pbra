package pbr.pbra.logic;

import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.HashSet;

import pbr.pbra.model.Customer;
import pbr.pbra.model.Order;

public class CSVImporter {
  private final Storage s_;

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

    Storage s = Storage.instance();
    s.clean();

    int orders = 0;
    HashSet<String> seenCustomers = new HashSet<String>();
    HashSet<String> seenOrders = new HashSet<String>();
    int orderInc = 0;
    for (CSVRecord r: records) {
      Customer c = makeCustomer(r);
      if (!seenCustomers.contains(c.email)) {
        s.insertCustomer(c);
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
      s.insertOrder(c.email, makeOrder(orderId, r));

      orders++;
    }

    return orders;
  }
}
