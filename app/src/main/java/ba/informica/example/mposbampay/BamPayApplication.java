package ba.informica.example.mposbampay;

import android.app.Application;

/**
 * Created by nedim on 12/16/15.
 */
public class BamPayApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //mPaylevenProvider = new PaylevenProvider(this);
        //You may want to substitute the real implementation with the stubbed one for testing
        // purposes.
        //For example:
        //mPaylevenProvider = StubPaylevenProvider.alwaysApprovedPaylevenWithSignature(this);
    }

    public CurrencyProvider getCurrencyProvider() {
        return new CurrencyProvider(this);
    }
}
