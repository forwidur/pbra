package pbr.pbra.sync;

import android.app.IntentService;
import android.app.backup.FullBackupDataOutput;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import pbr.pbra.logic.Storage;
import pbr.pbra.model.Fulfillment;

public class Server extends IntentService {
  private final BluetoothAdapter adapter_ = BluetoothAdapter.getDefaultAdapter();
  private BluetoothServerSocket server_;
  final private Thread t_ = new Thread(new Runnable() {
    @Override
    public void run() {
      Gson gson = new Gson();
      Log.v("Server", "Started");
        while (true) {
          try {
            BluetoothSocket s = server_.accept();
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            String m;
            do {
              m = Util.readMessage(in);
              Log.d("Server", "Got: " + m);

              if (m.equals("FINISH")) break;

              Fulfillment f = gson.fromJson(m, Fulfillment.class);
              Storage.instance(Server.this).updateFulfillmentIfNewer(f);

              Util.writeMessage(out, "DONE");
            } while (!m.equals("FINISH"));
            s.close();
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
    }
  });

  public Server() {
    super("Server");
    Log.d("Server", "Created");
    try {
      Method m = adapter_.getClass().getMethod("listenUsingInsecureRfcommOn", new Class[]{int.class});
      server_ = (BluetoothServerSocket) m.invoke(adapter_, 1);
    }  catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (!t_.isAlive()) {
      t_.start();
    }
    return START_STICKY;
  }

  @Override
  protected void onHandleIntent(Intent intent) {
  }
}
