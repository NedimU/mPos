package ba.informica.example.mposbampay.payment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import java.math.BigDecimal;
import java.util.Currency;

import ba.informica.example.mposbampay.R;

public class PaymentActivity extends AppCompatActivity implements FragmentInteractionListener {

    private boolean mIsBackEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        setupActionBar();

        if (savedInstanceState == null) {
            showFragment(new PaymentFragment());
        }

    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        ft.commit();
    }

    @Override
    public void processPayment(BigDecimal amount, Currency currency, String externalId) {
        //showFragment(PaymentProcessingFragment.newInstance(amount, currency, externalId));
    }

    @Override
    public void showPaymentFragment() {
        showFragment(new PaymentFragment());
    }

    @Override
    public void setBackNavigationEnabled(boolean enabled) {
        mIsBackEnabled = enabled;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.payment));
            actionBar.setElevation(3);
        }
    }
}
