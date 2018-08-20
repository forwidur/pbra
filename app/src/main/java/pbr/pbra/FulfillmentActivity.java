package pbr.pbra;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import pbr.pbra.logic.Barcode;
import pbr.pbra.logic.Storage;
import pbr.pbra.model.Assignment;
import pbr.pbra.model.Fulfillment;

public class FulfillmentActivity extends AppCompatActivity {
  private String id_;
  private Fulfillment f_;
  private int quantity_ = 0;
  private ArrayList<AssUpdater> ups_ = new ArrayList<>();
  private EditText scanTargetEdit;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_fulfillement);

    Bundle o = getIntent().getBundleExtra("order");
    ((TextView)findViewById(R.id.orderType)).setText(o.getString("type"));
    quantity_ = o.getInt("quantity");
    ((TextView)findViewById(R.id.orderQuantity)).setText(Integer.toString(quantity_));
    id_ = o.getString("id");
    ((TextView)findViewById(R.id.orderId)).setText(id_);

    LinearLayout asses = ((LinearLayout)findViewById(R.id.fulAssignment));
    for(int i=0; i<quantity_; ++i) {
      ups_.add(addAss(asses, id_, i));
    }

    f_ = Storage.instance(this).getFulfillment(id_);
    if (f_ != null) {
//      ((EditText) findViewById(R.id.fulAssignment)).setText(f_.assignment);
      ((EditText) findViewById(R.id.fulComment)).setText(f_.comment);
//      ((CheckBox) findViewById(R.id.fulComplete)).setChecked(f_.complete != 0);
//      ((CheckBox) findViewById(R.id.fulReturned)).setChecked(f_.returned != 0);
    } else {
      f_ = new Fulfillment();
    }
  }

  private class AssUpdater {
    private final CheckBox c_;
    private String oid_;
    private EditText edit_;
    private Assignment oldValue_;

    public AssUpdater(String oid, EditText edit, CheckBox c, Assignment oldValue) {
      this.oid_ = oid;
      this.edit_ = edit;
      this.oldValue_ = oldValue;
      this.c_ = c;
    }

    public boolean update(Activity act) {
      String str = edit_.getText().toString();
      if (str.isEmpty() && oldValue_.bikeId == 0) {
        return true;
      }

      int val;
      try {
        val = Integer.parseInt(str);
      } catch (NumberFormatException e) {
        Log.d("ass", String.valueOf(e));
        return false;
      }

      boolean ret = c_.isChecked();
      boolean wasReturned = oldValue_.returned == 0 ? false : true;

      if (val == oldValue_.bikeId && wasReturned == ret) {
        return true;
      }

      // Only update returned ts if was actually changed.
      int finalRet = oldValue_.returned;
      if (wasReturned != ret) {
        int ts = (int) (System.currentTimeMillis()/1000);
        finalRet = ret ? ts : 0;
      }

      Assignment a = new Assignment(oldValue_.id, oid_, val, finalRet);
      Storage.instance(act).updateAssignment(a);
      Storage.instance(act).queue(new Gson().toJson(a));

      return true;
    }

    public String getValue() {
      return edit_.getText().toString();
    }
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult sr = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (sr == null) return;

    String c = Barcode.strip(sr.getContents());
    Log.d("code", c);

    scanTargetEdit.setText(c);
  }

  private AssUpdater addAss(LinearLayout r, String oid, int i) {
    LinearLayout l = new LinearLayout(this);
    final EditText e = new EditText(this);
    e.setWidth(200);
    e.setInputType(InputType.TYPE_CLASS_NUMBER);
    l.addView(e);

    Assignment a = Storage.instance(this).getAssignment(oid, i);
    if (a == null) {
      a = new Assignment(oid, i);
    }
    String val = a.bikeId == 0 ? "" : Integer.toString(a.bikeId);
    e.setText(val);

    Button scan = new Button(this);
    scan.setText("Scan");
    final Activity act = this;
    scan.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        IntentIntegrator i = new IntentIntegrator(act);
        scanTargetEdit = e;
        i.initiateScan();
      }
    });
    l.addView(scan);

    CheckBox c = new CheckBox(this);
    l.addView(c);
    if (a.returned != 0) {
      c.setChecked(true);
    }

    final LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    r.addView(l, rp);

    return new AssUpdater(oid, e, c, a);
  }

  private void showAlert(String m) {
    AlertDialog alertDialog = new AlertDialog.Builder(FulfillmentActivity.this).create();
    alertDialog.setTitle("Error parsing assignment");
    alertDialog.setMessage(m);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    alertDialog.show();
  }

  public void onSaveClicked(View view) {
    Fulfillment n = new Fulfillment();
    n.orderId = id_;
//    n.assignment = ((EditText) findViewById(R.id.fulAssignment)).getText().toString();
    n.comment = ((EditText) findViewById(R.id.fulComment)).getText().toString();
    int ts = (int) (System.currentTimeMillis()/1000);
    n.version = ts;
//    if (((CheckBox) findViewById(R.id.fulComplete)).isChecked()) {
//      n.complete = f_.complete != 0 ? f_.complete : ts;
//    }
//    if (((CheckBox) findViewById(R.id.fulReturned)).isChecked()) {
//      n.returned = f_.returned != 0 ? f_.returned : ts;
//  }

    for (AssUpdater u: ups_) {
      if (!u.update(this)) {
        showAlert("Not a valid bike tag: " + u.getValue());
        return;
      }
    }

    if (!f_.equals(n)) { // Update is in order.
      Storage.instance(this).updateFulfillment(id_, n);
      Storage.instance(this).queue(new Gson().toJson(n));
    }

    finish();
  }

  public void onCancelClicked(View view) {
    finish();
  }
}