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
import java.util.Set;

public class Syncer extends IntentService {
  final BluetoothAdapter adapter_ = BluetoothAdapter.getDefaultAdapter();
  final Set<BluetoothDevice> paired_ = adapter_.getBondedDevices();

  public Syncer() {
    super("Syncer");
  }

  @Override
  protected void onHandleIntent(Intent i) {
    String m = i.getStringExtra("message");
    if (m != null) {
      Log.v("Syncer", "Message: " + m);
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
      sendMessage(d.getAddress(), message);
    }
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

  private void sendMessage(String address, String message) {
    Log.v("Syncer", "Sending to " + address);
    try {
      synchronized (this) {
        BluetoothSocket s = connect(address);
        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();

        Util.writeMessage(out, message);
        Log.v("Sync", Util.readMessage(in));
        Util.writeMessage(out, "FINISH");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
