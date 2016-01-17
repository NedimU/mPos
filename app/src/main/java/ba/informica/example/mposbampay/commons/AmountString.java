package ba.informica.example.mposbampay.commons;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by nedim on 12/10/15.
 */

/**
 * Formats string with the provided currency
 */
public class AmountString {
    private static final String BAM = "KM";
    private static final String EUR = "EUR";


    private final Currency mCurrency;
    private final BigDecimal mAmount;

    public AmountString(BigDecimal amount, Currency currency) {
        this.mCurrency = currency;
        this.mAmount = amount;
    }

    @Override
    public String toString() {
        NumberFormat formatter = getFormatter();
        return formatter.format(mAmount);
    }

    private NumberFormat getFormatter() {
        Locale locale = getLocaleFromCurrency(mCurrency);
        return NumberFormat.getCurrencyInstance(locale);
    }

    private Locale getLocaleFromCurrency(Currency currency) {
        if (BAM.equals(currency.getCurrencyCode())) {
            return (new Locale("sr", "BA"));
        } else if(EUR.equals(currency.getCurrencyCode())){
            return Locale.GERMANY;
        }

        throw new IllegalStateException("Tried to use unknown currency " +
                currency.getCurrencyCode());
    }
}