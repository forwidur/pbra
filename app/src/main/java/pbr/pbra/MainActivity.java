package pbr.pbra;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.List;

import pbr.pbra.logic.Storage;
import pbr.pbra.model.Customer;


public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Storage.instance().init(this);

    EditText search = (EditText) findViewById(R.id.search);
    search.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

    search.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) { }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d("SEARCH", s.toString());
        if (s.length() > 2) {
          issueSearch(s.toString());
        }
      }
    });
  }

  private void issueSearch(String q) {
    List<Customer> res = Storage.instance().search(q);

    for (Customer c : res) {
      Log.e("FOUND", c.email);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add("0 waiting for sync");
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

    return super.onOptionsItemSelected(item);
  }
}
