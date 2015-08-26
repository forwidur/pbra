package pbr.pbra.logic;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

public class OrderLoader extends CursorLoader {
  private String q_;
  public OrderLoader(Context context) {
    super(context);
  }

  public void search(String q) {
    q_ = q;
  }

  @Override
  public Cursor loadInBackground() {
    return Storage.instance(null).orders(q_);
  }
}
