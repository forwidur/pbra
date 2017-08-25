package pbr.pbra.sync;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pbr.pbra.logic.Storage;

public class Syncer extends Service {
  final BluetoothAdapter adapter_ = BluetoothAdapter.getDefaultAdapter();
  Set<BluetoothDevice> paired_;

  final private Thread t_ = new Thread(new Runnable() {
    @Override
    public void run() {
      Log.d("Syncer", "Loop started");
      for (BluetoothDevice d: paired_) {
        Log.d("server", "Pushing to " + d.getName());
      }

      while (true) {
        try {
          for (BluetoothDevice d: paired_) {
            String a = d.getAddress();
            Map<Integer, String> messages = Storage.instance(Syncer.this).getQueue(a);
            if (!messages.isEmpty()) {
              Log.d("Syncer", "Connecting to " + d.getName() + " " + d.getAddress());
              BluetoothSocket s = connect(a);
              if (s == null) continue;

              OutputStream out = s.getOutputStream();
              InputStream in = s.getInputStream();

              for (Map.Entry<Integer, String> entry : messages.entrySet()) {
                Integer id = entry.getKey();
                String m = entry.getValue();

                Log.d("Syncer", a + ": " + m);

                Util.writeMessage(out, m);
                if (Util.readMessage(in).equals("DONE")) {
                  Storage.instance(Syncer.this).deleteQueue(id);
                }
              }

              Util.writeMessage(out, "FINISH");
              // Server closes the connection.
            }
          }
          // Stops the loop from spinning like crazy and creating garbage.
          Thread.sleep(5000);
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    }
  });

  public Syncer() {
  }

  @Override
  public void onCreate() {
    paired_ = new HashSet<BluetoothDevice>();
    for (BluetoothDevice d: adapter_.getBondedDevices()) {
      if (d.getName().startsWith("pbr")) {
        paired_.add(d);
        Storage.instance(this).addPaired(d.getAddress());
      }
    }

    t_.start();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  // This is horrible.
  private BluetoothSocket connect(String address) throws IOException {
    BluetoothDevice d = adapter_.getRemoteDevice(address);
    BluetoothSocket s = null;
    try {
      Method m = d.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
      s = (BluetoothSocket) m.invoke(d, 1);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    if (adapter_.isDiscovering()) {
      adapter_.cancelDiscovery();
    }

    try {
      s.connect();
    } catch (IOException e) {
      Log.d("Syncer", "Connection failed to " + address);
      return null;
    }
    return s;
  }
}
