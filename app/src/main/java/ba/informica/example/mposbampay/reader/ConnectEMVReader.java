package ba.informica.example.mposbampay.reader;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.acs.smartcard.Reader;

import java.util.ArrayList;
import java.util.List;

import ba.informica.example.mposbampay.R;

public class ConnectEMVReader extends AppCompatActivity {

    private static final String[] stateStrings = { "Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific" };
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private List<String> mReaderAdapter;
    private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {

                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (device != null) {

                            // Open reader
                            mReader.open(device);
                            Toast toast = Toast.makeText(context, "Opened device: " + device.getDeviceName(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    } else {
                        Toast toast = Toast.makeText(context, "Cant acces to device...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                synchronized (this) {

                    // Update reader list
                    mReaderAdapter.clear();
                    for (UsbDevice device : mManager.getDeviceList().values()) {
                        if (mReader.isSupported(device)) {
                            mReaderAdapter.add(device.getDeviceName());
                        }
                    }

                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {
                        mReader.close();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_reader);

        mManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        // Initialize reader
        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {

            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                // Create output string
                final String outputString = "Slot " + slotNum + ": "
                        + stateStrings[prevState] + " -> "
                        + stateStrings[currState];

                // Show output
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getBaseContext(), outputString, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);

        mReaderAdapter = new ArrayList<String>();

        mReaderAdapter.clear();
        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                mReaderAdapter.add(device.getDeviceName());
            }
        }

        String deviceName = null;
        if (mReaderAdapter.size() > 0) {
            deviceName = mReaderAdapter.get(0);
        }

        if (deviceName != null) {

            // For each device
            for (UsbDevice device : mManager.getDeviceList().values()) {

                // If device name is found
                if (deviceName.equals(device.getDeviceName())) {

                    // Request permission
                    mManager.requestPermission(device,
                            mPermissionIntent);
                    break;
                }
            }
        }

        final Button button = (Button) findViewById(R.id.readCard);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startReader();
            }
        });

    }

    private void startReader() {

        try {

            byte[] atr = mReader.power(0, Reader.CARD_WARM_RESET);
            if (atr != null) {
            }

        } catch (Exception e) {

            Toast toast = Toast.makeText(getBaseContext(), "power exception: + " + mReader.isOpened(), Toast.LENGTH_SHORT);
            toast.show();

            //nothing...
        }

        byte[] atr = null;
        if (mReader.isOpened()) {
            atr = mReader.getAtr(0);
        }
        // Show ATR
        if (atr != null) {

            Toast toast = Toast.makeText(getBaseContext(), toHexString(atr), Toast.LENGTH_SHORT);
            toast.show();

        } else {
            Toast toast = Toast.makeText(getBaseContext(), "No ATR...", Toast.LENGTH_SHORT);
            toast.show();
        }

        String comm = "00A4040007A000000004101000";
        byte [] command = toByteArray(comm);
        byte [] response = new byte[300];
        try {
            int resp = mReader.transmit(0, command, command.length, response, response.length);
            Toast toast = Toast.makeText(getBaseContext(), "Resp: " + toHexString(response), Toast.LENGTH_SHORT);
            toast.show();

        } catch (Exception e) {
            Toast toast = Toast.makeText(getBaseContext(), "Error in APDU command...", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    private String toHexString(byte[] buffer) {

        String bufferString = "";

        for (int i = 0; i < buffer.length; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        return bufferString;
    }

    private byte[] toByteArray(String hexString) {

        int hexStringLength = hexString.length();
        byte[] byteArray = null;
        int count = 0;
        char c;
        int i;

        // Count number of hex characters
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        boolean first = true;
        int len = 0;
        int value;
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[len] = (byte) (value << 4);

                } else {

                    byteArray[len] |= value;
                    len++;
                }

                first = !first;
            }
        }

        return byteArray;
    }

    @Override
    protected void onDestroy() {

        // Close reader
        mReader.close();

        // Unregister receiver
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }
}
