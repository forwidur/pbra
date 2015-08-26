package pbr.pbra.sync;

import android.app.Activity;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import pbr.pbra.logic.Storage;

public class Syncer extends IntentService {
  final BluetoothAdapter adapter_ = BluetoothAdapter.getDefaultAdapter();
  final Set<BluetoothDevice> paired_ = adapter_.getBondedDevices();
  CountDownLatch l_ = new CountDownLatch(1);

  final private Thread t_ = new Thread(new Runnable() {
    @Override
    public void run() {
      Log.d("Syncer", "Loop started");
      while (true) {
        try {
          for (BluetoothDevice d: paired_) {
            l_ = new CountDownLatch(1);
            l_.await(60, TimeUnit.SECONDS);

            String a = d.getAddress();
            Map<Integer, String> messages = Storage.instance().getQueue(a);
            if (!messages.isEmpty()) {
              BluetoothSocket s = connect(a);
              OutputStream out = s.getOutputStream();
              InputStream in = s.getInputStream();

              for (Map.Entry<Integer, String> entry : messages.entrySet()) {
                Integer id = entry.getKey();
                String m = entry.getValue();

                Log.d("Syncer", a + ": " + m);

                Util.writeMessage(out, m);
                if (Util.readMessage(in).equals("DONE")) {
                  Storage.instance().deleteQueue(id);
                }
              }

              Util.writeMessage(out, "FINISH");
              // Server closes the connection.
            }
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    }
  });

  public Syncer() {
    super("Syncer");
    t_.start();
  }

  @Override
  protected void onHandleIntent(Intent i) {
    String m = i.getStringExtra("message");
    if (m != null) {
      all(m);
    }
  }

  public static void sendAll(Activity v, String message) {
    Intent i = new Intent(v, Syncer.class);
    i.putExtra("message", message);
    v.startService(i);
  }

  private void all(String message) {
    for (BluetoothDevice d: paired_) {
      addQueue(d.getAddress(), message);
    }
    l_.countDown();
  }

  private void addQueue(String address, String message) {
    Storage.instance().insertQueue(address, message);
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

    s.connect();
    return s;
  }
}
