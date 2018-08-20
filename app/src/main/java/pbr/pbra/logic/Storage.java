package pbr.pbra.logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import pbr.pbra.model.Assignment;
import pbr.pbra.model.Customer;
import pbr.pbra.model.Fulfillment;
import pbr.pbra.model.Order;

public enum Storage {
  INSTANCE, Storage;

  private static OrdersDbHelper dbHelper_;
  private static SQLiteDatabase db_;
  private static SQLiteDatabase r_;

  static final ArrayList<String> pairedDevices_ = new ArrayList<>();

  Storage() {
  }

  public static void init(Context c) {
    if (dbHelper_ == null) dbHelper_ = new OrdersDbHelper(c);
    if (db_ == null) db_ = dbHelper_.getWritableDatabase();
    if (r_ == null) r_ = dbHelper_.getReadableDatabase();
  }

  public static Storage instance(Context c) {
    if (r_ == null || dbHelper_ == null || db_ == null) {
      init(c);
    }
    return INSTANCE;
  }

  public static Cursor search(String s) {
    final String[] projection = {
        "rowid _id",
        "name",
        "email",
        "phone"
    };
    final String where = "email LIKE ? OR name LIKE ? OR name_search LIKE ? OR phone LIKE ?";
    String q = String.format("%%%s%%", s);
    String[] whereArgs = new String[]{q, q, q, q};
    return r_.query(
        "customers",  // The table to query
        projection,   // The columns to return
        where,        // The columns for the WHERE clause
        whereArgs,    // The values for the WHERE clause
        null,         // don't group the rows
        null,         // don't filter by row groups
        "email"       // The sort order
    );
  }

  public static Cursor searchAss(String s) {
    final String[] projection = {
        "rowid _id",
        "email",
        "id",
        "type",
        "quantity",
        "bike_id",
        "assid"
    };
    final String where = "bike_id = ?";
    String q = String.format("%s", s);
    String[] whereArgs = new String[]{q};
    return r_.query(
        "assignments_orders",  // The table to query
        projection,   // The columns to return
        where,        // The columns for the WHERE clause
        whereArgs,    // The values for the WHERE clause
        null,         // don't group the rows
        null,         // don't filter by row groups
        "id"       // The sort order
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
    String[] whereArgs = new String[]{s};
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
    v.put("name_search", c.name_search);
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
    String[] whereArgs = new String[]{id};
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

  public boolean updateFulfillmentIfNewer(Fulfillment f) {
    Fulfillment old = getFulfillment(f.orderId);
    if (old == null || f.version > old.version) {
      updateFulfillment(f.orderId, f);
      return true;
    }
    return false;
  }

  public Cursor allFulfillments() {
    return r_.query("fulfillment_export", null, null, null, null, null, null);
  }

  public Cursor allAssignments() {
    return r_.query("assignments_orders", null, null, null, null, null, null);
  }

  public void insertQueue(String address, String message) {
    if (db_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }

    ContentValues v = new ContentValues();
    v.put("address", address);
    v.put("message", message);

    db_.insertOrThrow("queue", null, v);
  }

  public void deleteQueue(Integer id) {
    db_.delete("queue", "rowid" + "=" + id, null);
  }

  public Map<Integer, String> getQueue(String address) {
    Map<Integer, String> res = new TreeMap<>();
    final String[] projection = {
        "rowid",
        "message"
    };
    final String where = "address LIKE ?";
    String[] whereArgs = new String[]{address};
    Cursor c = r_.query(
        "queue",      // The table to query
        projection,   // The columns to return
        where,        // The columns for the WHERE clause
        whereArgs,    // The values for the WHERE clause
        null,         // don't group the rows
        null,         // don't filter by row groups
        null          // The sort order
    );

    int idIdx = c.getColumnIndexOrThrow("rowid");
    int messageIdx = c.getColumnIndexOrThrow("message");
    if (c.moveToFirst()) {
      while (c.isAfterLast() == false) {
        res.put(c.getInt(idIdx), c.getString(messageIdx));
        c.moveToNext();
      }
    }
    return res;
  }

  public Assignment getAssignment(String oid, int i) {
    return getAssignment(Assignment.makeId(oid, i));
  }

  public Assignment getAssignment(String id) {
    if (db_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return null;
    }

    if (id.isEmpty()) {
      return null;
    }

    final String[] projection = {
        "id",
        "order_id",
        "bike_id",
        "returned",
        "version"
    };

    final String where = "id LIKE ?";
    String[] whereArgs = new String[] { id };
    Cursor c = r_.query(
        "assignments",  // The table to query
        projection,   // The columns to return
        where,        // The columns for the WHERE clause
        whereArgs,    // The values for the WHERE clause
        null,         // don't group the rows
        null,         // don't filter by row groups
        null          // The sort order
    );

    if (c != null && c.getCount() != 0) {
      c.moveToFirst();
      Assignment res = new Assignment(id);

      res.orderId = c.getString(c.getColumnIndexOrThrow("order_id"));
      res.bikeId = c.getInt(c.getColumnIndexOrThrow("bike_id"));
      res.returned = c.getInt(c.getColumnIndexOrThrow("returned"));
      res.version = c.getInt(c.getColumnIndexOrThrow("version"));

      return res;
    }
    Log.d("ass", "Not found");
    return null;
  }

  public Assignment getAssignmentByBikeId(String id) {
    Cursor c = searchAss(id);
    if (c != null && c.getCount() != 0) {
      c.moveToFirst();
      String assId = c.getString(c.getColumnIndexOrThrow("assid"));
      return getAssignment(assId);
    }
    return null;
  }

  // TODO: implement
  public void updateAssignment(Assignment a) {
    if (db_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }

    ContentValues v = new ContentValues();
    v.put("id", a.id);
    v.put("order_id", a.orderId);
    v.put("bike_id", a.bikeId);
    v.put("returned", a.returned);
    v.put("version", a.version);

    db_.insertWithOnConflict("assignments", null, v, SQLiteDatabase.CONFLICT_REPLACE);
  }

  public boolean updateAssignmentIfNewer(Assignment a) {
    Assignment old = getAssignment(a.id);

    Log.d("assold", String.valueOf(old));
    Log.d("assnew", String.valueOf(a));

    if (old == null || a.version > old.version) {
      updateAssignment(a);
      return true;
    }
    return false;
  }

  public void addPaired(String address) {
    pairedDevices_.add(address);
  }

  public void queue(String message) {
    for (String a: pairedDevices_) {
      insertQueue(a, message);
    }
  }


  public static class OrdersDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "PBROrders.db";

    public OrdersDbHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE customers " +
          "(email TEXT PRIMARY KEY, name TEXT, name_search TEXT, phone TEXT, address TEXT)");
      db.execSQL("CREATE INDEX customer_composite_idx ON customers(email, name, name_search, phone);");

      db.execSQL("CREATE TABLE orders " +
          "(email TEXT, id TEXT PRIMARY KEY, type TEXT, quantity INTEGER)");
      db.execSQL("CREATE INDEX order_email_idx ON orders(email);");

      db.execSQL("CREATE TABLE fulfillment " +
          "(order_id TEXT PRIMARY KEY, assignment TEXT, comment TEXT, " +
          " complete INTEGER, returned INTEGER, version INTEGER)");

      db.execSQL("CREATE TABLE assignments " +
          "(id TEXT PRIMARY KEY, order_id TEXT," +
          " bike_id INTEGER, returned INTEGER, version INTEGER)");

      db.execSQL("CREATE VIEW fulfillment_export AS SELECT fulfillment.*, orders.* " +
          "FROM fulfillment, orders " +
          "WHERE fulfillment.order_id = orders.id");

      db.execSQL("CREATE VIEW assignments_orders AS SELECT " +
          "assignments.bike_id, assignments.returned, assignments.id AS assid, orders.* " +
          "FROM assignments, orders " +
          "WHERE assignments.order_id = orders.id");

      db.execSQL("CREATE TABLE queue (address STRING, message STRING)");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS customers");
      db.execSQL("DROP TABLE IF EXISTS orders");
      db.execSQL("DROP TABLE IF EXISTS fulfillment");
      db.execSQL("DROP VIEW IF EXISTS fulfillment_export");
      db.execSQL("DROP TABLE IF EXISTS queue");
      db.execSQL("DROP TABLE IF EXISTS assignmentes");
      onCreate(db);
    }
  }
}