package pbr.pbra.logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import pbr.pbra.model.Customer;
import pbr.pbra.model.Fulfillment;
import pbr.pbra.model.Order;

public enum Storage {
  INSTANCE, Storage;

  private static OrdersDbHelper dbHelper_;
  private static SQLiteDatabase db_;
  private static SQLiteDatabase r_;

  Storage() {
  }

  public static void init(Context c) {
    dbHelper_ = new OrdersDbHelper(c);
    db_ = dbHelper_.getWritableDatabase();
    r_ = dbHelper_.getReadableDatabase();
  }

  public static Storage instance()
  {
    return INSTANCE;
  }

  public static Cursor search(String s) {
    final String[] projection = {
        "rowid _id",
        "name",
        "email",
        "phone"
    };
    final String where = "email LIKE ? OR name LIKE ? OR phone LIKE ?";
    String q = String.format("%%%s%%", s);
    String[] whereArgs = new String[] { q, q, q };
    return r_.query(
        "customers",  // The table to query
        projection,   // The columns to return
        where,        // The columns for the WHERE clause
        whereArgs,    // The values for the WHERE clause
        null,         // don't group the rows
        null,         // don't filter by row groups
        null          // The sort order
    );
  }
  public static Cursor orders(String s) {
    final String[] projection = {
        "rowid _id",
        "id",
        "type",
        "quantity"
    };
    final String where = "email LIKE ?";
    String[] whereArgs = new String[] { s };
    return r_.query(
        "orders",  // The table to query
        projection,   // The columns to return
        where,        // The columns for the WHERE clause
        whereArgs,    // The values for the WHERE clause
        null,         // don't group the rows
        null,         // don't filter by row groups
        null          // The sort order
    );
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

    db_.insertWithOnConflict("fulfillment", null, v, SQLiteDatabase.CONFLICT_REPLACE);
  }

  public Fulfillment getFulfillment(String id) {
    if (db_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return null;
    }

    final String[] projection = {
        "assignment",
        "comment",
        "complete",
        "returned",
        "version"
    };
    final String where = "order_id LIKE ?";
    String[] whereArgs = new String[] { id };
    Cursor c = r_.query(
        "fulfillment",  // The table to query
        projection,   // The columns to return
        where,        // The columns for the WHERE clause
        whereArgs,    // The values for the WHERE clause
        null,         // don't group the rows
        null,         // don't filter by row groups
        null          // The sort order
    );

    if (c != null && c.getCount() != 0) {
      c.moveToFirst();
      Fulfillment res = new Fulfillment();
      res.orderId = id;
      res.assignment = c.getString(c.getColumnIndexOrThrow("assignment"));
      res.comment = c.getString(c.getColumnIndexOrThrow("comment"));
      res.complete = c.getInt(c.getColumnIndexOrThrow("complete"));
      res.returned = c.getInt(c.getColumnIndexOrThrow("returned"));
      res.version = c.getInt(c.getColumnIndexOrThrow("version"));
      return res;
    }
    return null;
  }

  public Cursor allFulfillments() {
    return r_.query("fulfillment_export", null, null, null, null, null, null);
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

      db.execSQL("CREATE VIEW fulfillment_export AS SELECT fulfillment.*, orders.* " +
          "FROM fulfillment, orders " +
          "WHERE fulfillment.order_id = orders.id");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS customers");
      db.execSQL("DROP TABLE IF EXISTS orders");
      db.execSQL("DROP TABLE IF EXISTS fulfillment");
      db.execSQL("DROP VIEW IF EXISTS fulfillment_export");
      onCreate(db);
    }
  }
}