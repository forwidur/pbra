package pbr.pbra;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
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

import pbr.pbra.logic.CSVImporter;
import pbr.pbra.logic.CustomerLoader;
import pbr.pbra.logic.AssCustomerLoader;
import pbr.pbra.logic.Storage;
import pbr.pbra.sync.Server;
import pbr.pbra.sync.Syncer;


public class AssignmentActivity extends ListActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {
  CursorAdapter adapter_;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_assignment);

    // Top input.
    EditText search = (EditText) findViewById(R.id.assSearch);
    search.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

    // Search adapter.
    String[] fromColumns = {"email", "assignment"};
    int[] toViews = {R.id.email, R.id.ass};
    adapter_ = new SimpleCursorAdapter(this,
        R.layout.customer_ass_item, null,
        fromColumns, toViews, 0);
    setListAdapter(adapter_);
  }

  public void onSearchClicked(View v) {
    EditText search = (EditText)findViewById(R.id.assSearch);
    issueSearch(search.getText().toString());
  }

  private void issueSearch(String s) {
    if (s.length() != 0) {
      Bundle b = new Bundle();
      b.putBoolean("empty", false);
      b.putString("q", s.toString());
      getLoaderManager().restartLoader(0, b, this);
    } else {
      Bundle b = new Bundle();
      b.putBoolean("empty", true);
      getLoaderManager().restartLoader(0, b, this);
    }
  }

  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    AssCustomerLoader res = new AssCustomerLoader(this);
    if (args.getBoolean("empty") == false) {
      String q = args.getString("q");
        res.search(q);
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
    b.putString("id", c.getString(c.getColumnIndexOrThrow("id")));
    b.putString("type", c.getString(c.getColumnIndexOrThrow("type")));
    b.putInt("quantity", c.getInt(c.getColumnIndexOrThrow("quantity")));
    Intent i = new Intent(this, FulfillmentActivity.class);
    i.putExtra("order", b);
    startActivity(i);
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
