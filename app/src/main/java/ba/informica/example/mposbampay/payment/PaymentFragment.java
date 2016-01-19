package ba.informica.example.mposbampay.payment;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.acs.smartcard.Reader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import ba.informica.example.mposbampay.BamPayApplication;
import ba.informica.example.mposbampay.CurrencyProvider;
import ba.informica.example.mposbampay.CurrencySelectorDialog;
import ba.informica.example.mposbampay.R;
import ba.informica.example.mposbampay.view.AmountEntryView;
import ba.informica.example.mposbampay.view.NumberPadView;
import ba.informica.example.mposbampay.view.PaymentIdEntryView;
import ba.informica.example.mposbampay.view.SoapResponseView;

/**
 * Created by nedim on 12/10/15.
 */
public class PaymentFragment extends Fragment
        implements CurrencySelectorDialog.CurrencySelectorListener {

    private static final int MENU_ITEM_CHOOSE_CURRENCY = 100;

    private FragmentInteractionListener mFragmentInteractionListener;

    //injected
    private CurrencyProvider mCurrencyProvider;

    private Button mPayButton;
    private AmountEntryView mAmountView;
    private PaymentIdEntryView mPaymentIdEntryView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        View root = inflater.inflate(R.layout.activity_payment_fragment, container, false);

        setupActionBar();

        mPayButton = (Button) root.findViewById(R.id.pay_button);
        mPayButton.setOnClickListener(new OnPayButtonClicked());

        mAmountView = (AmountEntryView) root.findViewById(R.id.amount_entry_view);

        mPaymentIdEntryView = (PaymentIdEntryView) root.findViewById(R.id.external_id_entry_view);
        mPaymentIdEntryView.setOnChangedView(new ExternalIdTextWatcher());
        mPaymentIdEntryView.setButtonClickListener(new PaymentIdButtonListener());

        ((NumberPadView) root.findViewById(R.id.number_pad))
                .setNumberPadClickListener(new NumberPadClickListenerImpl());

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //fix inject function, remove mCurrencyProvider..
        //inject();
        mCurrencyProvider = new CurrencyProvider(this.getActivity().getBaseContext());
        Currency defaultCurrency = mCurrencyProvider.getCurrency();
        mAmountView.setCurrency(defaultCurrency);

        mManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

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
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getActivity().getBaseContext(), outputString, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });

        mPermissionIntent = PendingIntent.getBroadcast(getActivity().getBaseContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getActivity().registerReceiver(mReceiver, filter);

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
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mFragmentInteractionListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentInteractionListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_ITEM_CHOOSE_CURRENCY, 0, "Currency")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case MENU_ITEM_CHOOSE_CURRENCY:
                showCurrencySelectorDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onCurrencySelected(Currency currency) {
        mAmountView.setCurrency(currency);
        mCurrencyProvider.setCurrency(currency);
    }

    /**
     * This method may be substituted with the real injection mechanism like Dagger.
     */
    private void inject() {
        /*SampleApplication app = ((SampleApplication) getActivity().getApplication());
        mCurrencyProvider = app.getCurrencyProvider();*/
        BamPayApplication bamApp = (BamPayApplication) getActivity().getApplication();
        mCurrencyProvider = bamApp.getCurrencyProvider();
    }

    private void setupActionBar() {
        //enable action bar back arrow
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Display dialog with 2 different currencies which the user can possibly pay with
     */
    private void showCurrencySelectorDialog() {
        Currency latestUsedCurrency = mCurrencyProvider.getCurrency();
        CurrencySelectorDialog dialog = CurrencySelectorDialog.newInstance(latestUsedCurrency);
        dialog.setCurrencySelectorListener(this);
        dialog.show(this.getFragmentManager(), null);
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Reacts when a button from the number pad is clicked
     */
    private class NumberPadClickListenerImpl implements OnNumberPadClickListener {

        @Override
        public void onPadClicked(String value) {
            mPaymentIdEntryView.clearFocus();
            hideKeyboard();
            mAmountView.updateAmount(value);
            enableOrDisablePayButton(mAmountView.getAmount(),
                    mPaymentIdEntryView.getExternalId().length());
        }
    }

    /**
     * Reacts when a new character is introduced in the external id field
     */
    private class ExternalIdTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            enableOrDisablePayButton(mAmountView.getAmount(), s.length());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    /**
     * if external id is not empty and amount != 0 then enable pay button, otherwise disable
     *
     * @param totalAmount as double
     * @param length      of the external id
     */
    private void enableOrDisablePayButton(BigDecimal totalAmount, int length) {
        if (totalAmount.compareTo(BigDecimal.ONE) > -1 && length > 0) {
            mPayButton.setEnabled(true);
        } else {
            mPayButton.setEnabled(false);
        }
    }

    private String getPaymentUniqueId() {
        //This should be substituted with the real id from your database. This id can be used in
        // the future in order to match payleven payments to your records.
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    private class PaymentIdButtonListener implements PaymentIdEntryView.OnButtonClickListener {
        @Override
        public void onClick(PaymentIdEntryView view) {
            view.setText(getPaymentUniqueId());
        }
    }

    @Override
    public void onDestroy() {

        // Close reader
        mReader.close();

        // Unregister receiver
        getActivity().unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    /**
     * Start payment process when Pay button is clicked
     */
    private class OnPayButtonClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            /*BigDecimal amount = mAmountView.getAmount();
            Currency currency = mAmountView.getCurrency();
            String externalId = mPaymentIdEntryView.getExternalId();

           mFragmentInteractionListener.processPayment(amount, currency, externalId);*/

            /*Intent intent = new Intent(getActivity(), ConnectEMVReader.class);
            startActivity(intent);*/

            ArrayList<String> resp = new ArrayList<String>();
            if (mReader.isOpened() && mReader.getState(0) == 2) {
                Log.i("PaymentFragment", "Log in if statemant...!");
                /*ReaderManager rm = new ReaderManager(mReader);

                resp = rm.readCard();
                Toast toast = Toast.makeText(getActivity().getBaseContext(), "Resp: " + resp.get(resp.size() - 1), Toast.LENGTH_SHORT);
                toast.show();*/
            }
            else {
                Toast toast = Toast.makeText(getActivity().getBaseContext(), "Please insert card into reader!!!", Toast.LENGTH_SHORT);
                toast.show();
            }
            Log.i("PaymentFragment", "Log before start...!");

            Intent i = new Intent(getActivity().getBaseContext(), SoapResponseView.class);
            i.putExtra("card_data", resp);
            startActivity(i);

        }
    }
}
