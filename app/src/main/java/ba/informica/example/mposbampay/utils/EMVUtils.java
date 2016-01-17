package ba.informica.example.mposbampay.utils;

import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import java.util.ArrayList;

import ba.informica.example.mposbampay.reader.APDU;

/**
 * Created by nedim on 1/15/16.
 */
public final class EMVUtils {

    public static ArrayList<String> getRecords (Reader mReader, byte [] data) {
        ArrayList<String> al = new ArrayList<String>();
        if (data.length % 4 != 0) {
            Log.i("EMVUtils", "Exc in EMVUtils + data: " + HexUtils.toHexString(data));
            al.add("Wrong AFL data");
            return al;
        }
        Log.i("EMVUtils", "else..." + data.length);
        for (int i = 0; i < data.length / 4; i++) {
            int mSfi = data[i*4] >>> 3;
            mSfi = (mSfi << 3) | 4;
            Log.i("EMVUtils", "mSfi: " + mSfi);
            int startRec = data [i*4 + 1] & 0xFF;
            int endRec = data [i*4 + 2] & 0xFF;

            for (int record = startRec; record <= endRec; record++) {
                Log.i("EMVUtils", "Read record, sfi: " + mSfi + " , record: " + record);
                try {
                    String response = APDU.transmitByteCommand(mReader, new byte[] {(byte)0x00, (byte) 0xB2, (byte) record, (byte) mSfi, (byte) 0x00});
                    Log.i("EMVUtils", "response: " + response);
                    if (response.substring(response.length() - 4).equals("9000")) {
                        al.add(response);
                    }
                } catch (ReaderException e) {
                    e.printStackTrace();
                }
            }
        }
        return al;
    }
}
