package pbr.pbra;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import pbr.pbra.logic.OrderLoader;
import pbr.pbra.logic.Storage;

public class OrdersActivity extends ListActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
  CursorAdapter adapter_;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_orders);

    Bundle customer = getIntent().getBundleExtra("customer");

    String[] fromColumns = {"type"};
    int[] toViews = {android.R.id.text1};
    adapter_ = new SimpleCursorAdapter(
        this, android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
    setListAdapter(adapter_);

    getLoaderManager().restartLoader(0, customer, this);
  }

  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    OrderLoader res = new OrderLoader(this);
    res.search(args.getString("email"));
    return res;
  }

  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    adapter_.swapCursor(data);
  }

  public void onLoaderReset(Loader<Cursor> loader) {
    adapter_.swapCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Cursor c = (Cursor) getListAdapter().getItem(position);
    Log.d("", c.getString(c.getColumnIndexOrThrow("id")));
    Bundle b = new Bundle();
    b.putString("id", c.getString(c.getColumnIndexOrThrow("id")));
    b.putString("type", c.getString(c.getColumnIndexOrThrow("type")));
    b.putInt("quantity", c.getInt(c.getColumnIndexOrThrow("quantity")));
    Intent i = new Intent(this, FulfillmentActivity.class);
    i.putExtra("order", b);
    startActivity(i);
  }
}
