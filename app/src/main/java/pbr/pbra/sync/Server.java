package pbr.pbra.sync;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.droidparts.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Server extends IntentService {
  private final BluetoothAdapter adapter_ = BluetoothAdapter.getDefaultAdapter();
  private BluetoothServerSocket server_;
  final private Thread t_ = new Thread(new Runnable() {
    @Override
    public void run() {
      Log.v("Server", "Started");
        while (true) {
          try {
            BluetoothSocket s = server_.accept();
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            String m;
            do {
              m = Util.readMessage(in);
              Util.writeMessage(out, "DONE");
              Log.v("Server", "Got: " + m);
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
    Log.v("Server", "Created");
//    adapter_.cancelDiscovery();
    if (adapter_.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
      Log.v("Server", "Disc");
    }
    try {
//      server_ = adapter_
 //             .listenUsingInsecureRfcommWithServiceRecord("pbra", Syncer.uuid);
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
