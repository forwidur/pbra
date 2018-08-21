package pbr.pbra;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pbr.pbra.logic.Barcode;
import pbr.pbra.logic.Storage;
import pbr.pbra.model.Assignment;

/**
 * Created by mag on 8/23/17.
 */

public class ScannerActivity extends AppCompatActivity {
  private String acc = "";
  private static final Pattern KEYCODE_PATTERN = Pattern.compile("KEYCODE_(\\w)");
  private TextView scanCode;
  private TextView orderId;
  private TextView status;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scanner);
    scanCode = (TextView)findViewById(R.id.code);
    orderId = (TextView)findViewById(R.id.scanOrderId);
    status = (TextView)findViewById(R.id.scanStatus);
    // Allows collecting kbd input even if the screen is locked.
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
  }

  public void showMessage(String title, String message) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
        this);

    // set title
    alertDialogBuilder.setTitle(title);

    // set dialog message
    alertDialogBuilder
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
          }
        });

    // create alert dialog
    AlertDialog alertDialog = alertDialogBuilder.create();

    // show it
    alertDialog.show();
  }

  private void processCode(String c) {
    String code = Barcode.strip(c);
    Log.d("scan", code);
    scanCode.setText(code);

    Assignment a = Storage.instance(this).getAssignmentByBikeId(code);
    if (a == null) {
      status.setText("NOT FOUND");
      status.setTextColor(0xffff0000);
      return;
    }
    if (a.returned != 0) {
      status.setText("ALREADY RETURNED");
      status.setTextColor(0xffffffff);
      return;
    }

    a.returned =  (int) (System.currentTimeMillis()/1000);
    Storage.instance(this).updateAssignment(a);
    Storage.instance(this).queue(new Gson().toJson(a));
    status.setText("SUCCESS");
    status.setTextColor(0xff00ff00);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_ENTER) {
      Log.d("scan", "Scanned " + acc);
      processCode(acc);
      acc = "";
    } else {
      String key = KeyEvent.keyCodeToString(keyCode);

      Matcher matcher = KEYCODE_PATTERN.matcher(key);
      if (matcher.matches()) {
        acc = acc + matcher.group(1);
      }
    }

    return super.onKeyDown(keyCode, event);
  }
}
