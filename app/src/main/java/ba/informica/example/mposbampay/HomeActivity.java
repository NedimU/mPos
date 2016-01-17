package ba.informica.example.mposbampay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import ba.informica.example.mposbampay.payment.PaymentActivity;
import ba.informica.example.mposbampay.reader.MagStripeReader;

public class HomeActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        setupButtons();
        setVersionView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setVersionView() {
        TextView versionView = (TextView)findViewById(R.id.app_version_view);
        versionView.setText("example by Nedim");
    }

    private void setupButtons() {
        findViewById(R.id.payment_button).setOnClickListener(this);
        findViewById(R.id.refund_button).setOnClickListener(this);
        findViewById(R.id.terminals_button).setOnClickListener(this);
        findViewById(R.id.logout_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.payment_button:
                showPaymentScreen();
                break;
            case R.id.refund_button:
                showRefundScreen();
                break;
            case R.id.terminals_button:
                showDeviceManagementScreen();
                break;
            case R.id.logout_button:
                logout();

        }
    }

    private void showRefundScreen() {
        Intent intent = new Intent(this, MagStripeReader.class);
        startActivity(intent);
    }

    private void showDeviceManagementScreen() {
        /*Intent intent = new Intent(this, DeviceManagementActivity.class);
        startActivity(intent);*/
    }

    private void showPaymentScreen() {
        Log.i("MyActivity", "Payment activity ");
        Intent intent = new Intent(this, PaymentActivity.class);
        startActivity(intent);
    }

    private void logout() {
        //mLogoutController.logoutFrom(this);
    }
}