package pbr.pbra;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import pbr.pbra.logic.CSVExporter;
import pbr.pbra.logic.CSVImporter;
import pbr.pbra.logic.Storage;

public class ConfigActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_config);
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

  public void onImportClicked(View view) {
    String file = Environment.getExternalStorageDirectory() +"/sales.csv";

    try {
      showMessage("Import successful", String.format("Imported %d orders.",
          new CSVImporter(Storage.instance(this)).Process(file)));
    } catch (Exception e) {
      showMessage("Import failed", e.getMessage());
    }
  }

  public void onFulImportClicked(View view) {
    String file = Environment.getExternalStorageDirectory() +"/fulfillments.csv";

    try {
      showMessage("Import successful", String.format("Imported %d fulfillements.",
          new CSVImporter(Storage.instance(this)).ProcessFul(file)));
    } catch (Exception e) {
      showMessage("Import failed", e.getMessage());
    }
  }

  public void onExportClicked(View view) {
    String file = Environment.getExternalStorageDirectory() +"/fulfillments.csv";

    try {
      showMessage("Export successful", String.format("Exported %d records.",
          CSVExporter.exportFulfillments(file, Storage.instance(this).allFulfillments())));
    } catch (Exception e) {
      showMessage("Export failed", e.getMessage());
    }
  }
}
