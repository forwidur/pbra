package pbr.pbra.logic;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

public class AssCustomerLoader extends CursorLoader {
  private String q_;
  public AssCustomerLoader(Context context) {
    super(context);
  }

  public void search(String q) {
    q_ = q;
  }

  @Override
  public Cursor loadInBackground() {
    return Storage.instance(null).searchAss(q_);
  }
}
