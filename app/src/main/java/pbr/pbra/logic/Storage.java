package pbr.pbra.logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import pbr.pbra.model.Customer;
import pbr.pbra.model.Fulfillment;
import pbr.pbra.model.Order;

public enum Storage {
  INSTANCE, Storage;

  private static OrdersDbHelper dbHelper_;
  private static SQLiteDatabase db_;

  Storage() {
  }

  public static void init(Context c) {
    dbHelper_ = new OrdersDbHelper(c);
    db_ = dbHelper_.getWritableDatabase();
  }

  public static Storage instance()
  {
    return INSTANCE;
  }

  public static void clean() {
    if (db_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }
    db_.execSQL("DELETE FROM customers;");
    db_.execSQL("DELETE FROM orders;");
  }

  public static void insertCustomer(Customer c) {
    if (db_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }

    ContentValues v = new ContentValues();
    v.put("name", c.name);
    v.put("email", c.email);
    v.put("phone", c.phone);
    v.put("address", c.address);

    db_.insertOrThrow("customers", null, v);
  }

  public static void insertOrder(String email, Order o) {
    if (db_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }

    ContentValues v = new ContentValues();
    v.put("id", o.id);
    v.put("email", email);
    v.put("type", o.type);
    v.put("quantity", o.quantity);
    db_.insertOrThrow("orders", null, v);
  }

  public static void updateFulfillment(String orderId, Fulfillment f) {
    if (db_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }

    ContentValues v = new ContentValues();
    v.put("order_id", orderId);
    v.put("assignment", f.assignment);
    v.put("complete", f.complete);
    v.put("returned", f.returned);
    v.put("comment", f.comment);
    v.put("version", f.version);
    String[] sel = {orderId};
    db_.update("fulfillement", v, "order_id LIKE ?", sel);
  }

  public static class OrdersDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "PBROrders.db";

    public OrdersDbHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE customers " +
          "(email TEXT PRIMARY KEY, name TEXT, phone TEXT, address TEXT)");
      db.execSQL("CREATE INDEX customer_composite_idx ON customers(email, name, phone);");

      db.execSQL("CREATE TABLE orders " +
          "(email TEXT, id TEXT PRIMARY KEY, type TEXT, quantity INTEGER)");
      db.execSQL("CREATE INDEX order_email_idx ON orders(email);");

      db.execSQL("CREATE TABLE fulfillment " +
          "(order_id TEXT PRIMARY KEY, assignment TEXT, comment TEXT, " +
          " complete INTEGER, returned INTEGER, version INTEGER)");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS customers");
      db.execSQL("DROP TABLE IF EXISTS orders");
      db.execSQL("DROP TABLE IF EXISTS fulfillment");
      onCreate(db);
    }
  }
}