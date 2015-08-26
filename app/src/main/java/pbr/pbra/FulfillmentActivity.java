package pbr.pbra;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import pbr.pbra.logic.Storage;
import pbr.pbra.model.Fulfillment;
import pbr.pbra.sync.Syncer;

public class FulfillmentActivity extends AppCompatActivity {
  private String id_;
  private Fulfillment f_;
  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_fulfillement);

    Bundle o = getIntent().getBundleExtra("order");
    ((TextView)findViewById(R.id.orderType)).setText(o.getString("type"));
    ((TextView)findViewById(R.id.orderQuantity)).setText(Integer.toString(o.getInt("quantity")));
    id_ = o.getString("id");
    ((TextView)findViewById(R.id.orderId)).setText(id_);

    f_ = Storage.instance(this).getFulfillment(id_);
    if (f_ != null) {
      ((EditText) findViewById(R.id.fulAssignment)).setText(f_.assignment);
      ((EditText) findViewById(R.id.fulComment)).setText(f_.comment);
      ((CheckBox) findViewById(R.id.fulComplete)).setChecked(f_.complete != 0);
      ((CheckBox) findViewById(R.id.fulReturned)).setChecked(f_.returned != 0);
    } else {
      f_ = new Fulfillment();
    }
  }

  public void onSaveClicked(View view) {
    Fulfillment n = new Fulfillment();
    n.orderId = id_;
    n.assignment = ((EditText) findViewById(R.id.fulAssignment)).getText().toString();
    n.comment = ((EditText) findViewById(R.id.fulComment)).getText().toString();
    int ts = (int) (System.currentTimeMillis()/1000);
    n.version = ts;
    if (((CheckBox) findViewById(R.id.fulComplete)).isChecked()) {
      n.complete = f_.complete != 0 ? f_.complete : ts;
    }
    if (((CheckBox) findViewById(R.id.fulReturned)).isChecked()) {
      n.returned = f_.returned != 0 ? f_.returned : ts;
    }

    if (!f_.equals(n)) { // Update is in order.
      Storage.updateFulfillment(id_, n);
      Syncer.sendAll(this, new Gson().toJson(n));
    }

    finish();
  }

  public void onCancelClicked(View view) {
    finish();
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scan =
        IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scan != null) {
      String cont = scan.getContents();
      ((EditText) findViewById(R.id.fulAssignment)).setText(cont);
    }
  }

  public void onScanClicked(View view) {
    new IntentIntegrator(this).initiateScan();
  }
}