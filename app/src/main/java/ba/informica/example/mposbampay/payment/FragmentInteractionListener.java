package ba.informica.example.mposbampay.payment;

/**
 * Created by nedim on 12/10/15.
 */

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Defines method that should be implemented by activity in order to communicate with fragments
 */
public interface FragmentInteractionListener {
    void processPayment(BigDecimal amount, Currency currency, String externalId);
    void showPaymentFragment();
    void setBackNavigationEnabled(boolean enabled);
}