package pilecka.paulina.btreader;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    public static final String TAG = "BT_READER_LOGGING";
    private final int REQUEST_CODE = 1000;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mDevice;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        initViews();
        initBluetooth();
        removeResults();
    }

    private void removeResults() {
        FileHelper helper = new FileHelper(this);
        helper.removeFile();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission not granted! Bye bye!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void initViews() {
        progressBar = this.findViewById(R.id.progress_bar);

        Button connectButton = this.findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAvailableDevices();
            }
        });


        Button stopButton = this.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mConnectThread!=null){
                    mConnectThread.cancel();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        });

        Button showChart = this.findViewById(R.id.chart);
        showChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ActivityScatterChart.class);
                startActivity(i);

            }
        });
    }


    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, 1);
        }
    }

    private void displayAvailableDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices != null) {
            if (pairedDevices.size() > 0) {
                showAlertDialogWithPairedDevices(pairedDevices);
            } else {
                showNoPairedDevicesAlertDialog();
            }
        }
    }

    private void showAlertDialogWithPairedDevices(Set<BluetoothDevice> deviceSet) {

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.device_name);
        final ArrayList<BluetoothDevice> deviceArray = new ArrayList<>();

        for (BluetoothDevice device : deviceSet) {
            String deviceName = device.getName();
            Log.d(TAG, "Adding the device: " + deviceName + " to the adapter");

            adapter.add(deviceName);
            deviceArray.add(device);
        }

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.paired_devices);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                mDevice = deviceArray.get(i);

                communicateWithDevice(mDevice);

            }
        });

        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

    private void showNoPairedDevicesAlertDialog() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.paired_devices);
        builder.setMessage(R.string.no_paired_devices);

        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }


    private void communicateWithDevice(BluetoothDevice device) {
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }


    private class ConnectThread extends Thread {

        private final BluetoothSocket mSocket;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "socket = " + tmp.toString());
            } catch (IOException e) {
                Log.d(TAG, "connectThread exception: " + e.getMessage());
            }
            mSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run in connectThread");
            bluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
                mConnectedThread = new ConnectedThread(mSocket);
                mConnectedThread.start();
                Log.d(TAG, "ConnectThread: socked connection successfull");
            } catch (IOException connectException) {
                Log.d(TAG, "connectException exception: " + connectException.getMessage());
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.d(TAG, "closeException exception: " + closeException.getMessage());
                }
                return;
            }

        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
            }
        }


    }

    private class ConnectedThread extends Thread {

        private static final int MESSAGE_CODE = 1111;

        private final BluetoothSocket mSocket;
        private final InputStream mInStream;

        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
                Log.d(TAG, "ConnectedThread: input stream: " + tmpIn.toString());
            } catch (IOException e) {
                Log.d(TAG, "connectedException: " + e.getMessage());
            }
            mInStream = tmpIn;
        }

        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true) {
                try {
                    bytes = mInStream.read(buffer);
                    Log.d(TAG, "bytes read: " + bytes);
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "bytes read message: " + readMessage);
                    mHandler.obtainMessage(MESSAGE_CODE, readMessage).sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "bytes read exception: " + e.getMessage());
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
            }
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;

            switch (msg.what) {
                case ConnectedThread.MESSAGE_CODE:
                    FileHelper fileHelper = new FileHelper(MainActivity.this);
                    fileHelper.writeToFile(message);
                    break;
            }
        }
    };

}
