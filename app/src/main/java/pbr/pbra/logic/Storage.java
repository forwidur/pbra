package pbr.pbra.logic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import pbr.pbra.model.Customer;
import pbr.pbra.model.Order;

public enum Storage {
  INSTANCE;

  private static OrdersDbHelper dbHelper_;

  Storage() {
  }

  public static void init(Context c) {
    dbHelper_ = new OrdersDbHelper(c);
  }

  public static Storage instance()
  {
    return INSTANCE;
  }


  public static void insertCustomer(Customer customer) {
    if (dbHelper_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }

  }

  public static void insertOrder(String email, Order order) {
    if (dbHelper_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }

  }

  public static void updateFulfillment(String orderId, Order fulfillment) {
    if (dbHelper_ == null) {
      Log.e("Storage", "DB op but helper not initialized.");
      return;
    }

  }

 public static class OrdersDbHelper extends SQLiteOpenHelper {
   public static final int DATABASE_VERSION = 1;
   public static final String DATABASE_NAME = "PBROrders.db";

   public OrdersDbHelper(Context context) {
     super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }
   public void onCreate(SQLiteDatabase db) {
     db.execSQL("CREATE TABLE customers " +
                "(name TEXT, email TEXT, phone TEXT, address TEXT)");
   }
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     db.execSQL("DROP TABLE IF EXISTS customers");
     onCreate(db);
    }
 }

}