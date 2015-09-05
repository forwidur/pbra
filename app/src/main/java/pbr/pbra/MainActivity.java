package pbr.pbra;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import pbr.pbra.logic.CustomerLoader;
import pbr.pbra.logic.Storage;
import pbr.pbra.sync.Server;
import pbr.pbra.sync.Syncer;


public class MainActivity extends ListActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
  CursorAdapter adapter_;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Storage.instance(this);

    // Top input.
    EditText search = (EditText) findViewById(R.id.search);
    search.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

    // Search adapter.
    String[] fromColumns = {"name", "email", "phone"};
    int[] toViews = {R.id.name, R.id.email, R.id.phone};
    adapter_ = new SimpleCursorAdapter(this,
            R.layout.customer_list_item, null,
            fromColumns, toViews, 0);
    setListAdapter(adapter_);

    // Binding.
    final LoaderManager.LoaderCallbacks<Cursor> lc = this;
    final MainActivity act = this;
    search.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) { }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        act.issueSearch(s.toString());
      }
    });

    startService(new Intent(this, Server.class));
    startService(new Intent(this, Syncer.class));
  }

  private void issueSearch(String s) {
    if (s.toString().equals("config")) {
      this.startActivity(new Intent(this, ConfigActivity.class));
    }
    if (s.length() > 2) {
      Bundle b = new Bundle();
      b.putBoolean("empty", false);
      b.putString("q", s.toString());
      getLoaderManager().restartLoader(0, b, this);
    }
    if (s.length() == 0) {
      Bundle b = new Bundle();
      b.putBoolean("empty", true);
      getLoaderManager().restartLoader(0, b, this);
    }

  }

  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    CustomerLoader res = new CustomerLoader(this);
    if (args.getBoolean("empty") == false) {
      String q = args.getString("q");
      if (q.equals("showworld")) {
        res.search("");
      } else {
        res.search(q);
      }
    } else {
      // TODO(mag): figure out a cleaner way.
      res.search("somebogusthing");
    }
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
    Bundle b = new Bundle();
    b.putString("name", c.getString(c.getColumnIndexOrThrow("name")));
    b.putString("email", c.getString(c.getColumnIndexOrThrow("email")));
    b.putString("phone", c.getString(c.getColumnIndexOrThrow("phone")));
    Intent i = new Intent(this, OrdersActivity.class);
    i.putExtra("customer", b);
    startActivity(i);

    startService(new Intent(this, Syncer.class));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
//    menu.add("0 waiting for sync");
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_ass) {
      startActivity(new Intent(this, AssignmentActivity.class));
      return true;
    }

    if (id == R.id.action_settings) {
      startActivity(new Intent(this, ConfigActivity.class));
      return true;
    }

    if (id == R.id.action_scan) {
      new IntentIntegrator(this).initiateScan();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scan =
        IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scan != null) {
      String cont = scan.getContents();
      ((EditText) findViewById(R.id.search)).setText(cont);
    }
  }
}
