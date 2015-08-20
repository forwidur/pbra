package pbr.pbra.logic;

import android.database.Cursor;
import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class CSVExporter {
  static final SimpleDateFormat f_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static String dateConv(int ts) {
    if (ts == 0) {
      return "";
    }
    return f_.format(new java.util.Date(((long)ts) * 1000));
  }

  public static int exportFulfillments(String path, Cursor c) throws IOException {
    int cnt = 0;

    final int id = c.getColumnIndexOrThrow("id");
    final int email = c.getColumnIndexOrThrow("email");
    final int type = c.getColumnIndexOrThrow("type");
    final int assignment = c.getColumnIndexOrThrow("assignment");
    final int comment = c.getColumnIndexOrThrow("comment");
    final int complete = c.getColumnIndexOrThrow("complete");
    final int returned = c.getColumnIndexOrThrow("returned");
    final int version = c.getColumnIndexOrThrow("version");

    final FileWriter f = new FileWriter(path);
    final CSVPrinter p = new CSVPrinter(f, CSVFormat.EXCEL.withHeader(
        "email", "orderid", "type", "assignment", "comment", "complete", "returned", "last_updated"));

    if (c.moveToFirst()) {
      do {
        p.printRecord(
            c.getString(email),
            c.getString(id),
            c.getString(type),
            c.getString(assignment),
            c.getString(comment),
            dateConv(c.getInt(complete)),
            dateConv(c.getInt(returned)),
            dateConv(c.getInt(version))
        );
        cnt++;
      } while(c.moveToNext());
    }

    p.close();
    f.close();

    return cnt;
  }
}
