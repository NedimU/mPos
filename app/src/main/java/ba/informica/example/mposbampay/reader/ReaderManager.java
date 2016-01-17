package ba.informica.example.mposbampay.reader;

import android.util.Log;

import com.acs.smartcard.Reader;

import java.util.ArrayList;

import ba.informica.example.mposbampay.utils.EMVUtils;
import ba.informica.example.mposbampay.utils.HexUtils;

/**
 * Created by nedim on 1/5/16.
 */
public class ReaderManager {

    public ArrayList<String> readCard () {

        ArrayList<String> responses = new ArrayList<String>();
        String command = null;
        String response = null;

        Log.i("RManager", " protocol: " + mReader.getProtocol(0));


        byte[] atr = null;
        try {
            atr = mReader.power(0, Reader.CARD_WARM_RESET);
            if (atr != null) {
                responses.add(HexUtils.toHexString(atr));
            }

        } catch (Exception e) {

            responses.add("Error in ATR");

            //nothing...
        }

        try {
            mReader.setProtocol(0, Reader.PROTOCOL_T1);
        }catch (Exception e) {
            Log.i("RManager", " Error setting protocol T1");
            try {
                mReader.setProtocol(0, Reader.PROTOCOL_T0);
            } catch (Exception ex) {
                Log.i("RManager", " Error setting protocol T0");
            }
        }
        Log.i("RManager", " protocol after: " + mReader.getProtocol(0));

        try {
            command = "00A404000E315041592E5359532E444446303100";
            response = APDU.transmitCommand(mReader, command);
            if (!response.equals(noAppInstalled)) responses.add(response);
            Log.i("RManager", " comm: PSE" + "; resp: " + response);

        } catch (Exception e) {
            Log.i("ReaderManager", "Error asking for: " + command);
        }

        try {
            command = "00A4040007A000000004101000";
            response = APDU.transmitCommand(mReader, command);
            if (!response.equals(noAppInstalled)) responses.add(response);
            Log.i("RManager", " comm: MC" + "; resp: " + response);

        } catch (Exception e) {
            Log.i("ReaderManager", "Error asking for: " + command);
        }

        try {
            command = "00A4040007A000000004306000";
            response = APDU.transmitCommand(mReader, command);
            if (!response.equals(noAppInstalled)) responses.add(response);
            Log.i("RManager", " comm: MA" + "; resp: " + response);

        } catch (Exception e) {
            Log.i("ReaderManager", "Error asking for: " + command);
        }

        try {
            command = "00A4040007A000000003101000";
            response = APDU.transmitCommand(mReader, command);
            if (!response.equals(noAppInstalled)) responses.add(response);
            Log.i("RManager", " comm: VIS" + "; resp: " + response);

        } catch (Exception e) {
            Log.i("ReaderManager", "Error asking for: " + command);
        }

        try {
            command = "00A4040007A000000003201000";
            response = APDU.transmitCommand(mReader, command);
            if (!response.equals(noAppInstalled)) responses.add(response);
            Log.i("RManager", " comm: VISEl" + "; resp: " + response);
        } catch (Exception e) {
            Log.i("ReaderManager", "Error asking for: " + command);
        }

        try {
            //Get processing options
            command = "80A8000002830000";
            response = APDU.transmitCommand(mReader, command);
            if (response.substring(response.length() - 4).equals(SW1SW2_OK)) responses.add(response);
            Log.i("RManager", " comm: VISEl" + "; resp: " + response);
        } catch (Exception e) {
            Log.i("ReaderManager", "Error in get processing options: " + command);
        }

        if (response.substring(0,2).equals("77")) {
            String AIP = response.substring(4,12);
            Log.i("ReaderManager", "AIP: " + AIP);
            String AFL = response.substring(16, response.length() - 4);
            Log.i("ReaderManager", "AFL: " + AFL);
            ArrayList<String> recs = new ArrayList<String>();
            recs = EMVUtils.getRecords(mReader, HexUtils.toByteArray(AFL));
            responses.addAll(recs);

        } else if (response.substring(0,2).equals("80")) {
            String AIP = response.substring(4,8);
            Log.i("ReaderManager", "AIP: " + AIP);
            String AFL = response.substring(8, response.length() - 4);
            Log.i("ReaderManager", "AFL: " + AFL);
            ArrayList<String> recs = new ArrayList<String>();
            recs = EMVUtils.getRecords(mReader, HexUtils.toByteArray(AFL));
            responses.addAll(recs);

        } else {
            Log.i("ReaderManager", "Error in get processing options response: ");
            responses.add("Error in get processing options response");
        }

        return responses;
    }
    private final static String noAppInstalled = "6A82";

    private final static String SW1SW2_OK = "9000";

    private Reader mReader;

    public ReaderManager(Reader reader) {
        mReader = reader;
    }
}
