package ba.informica.example.mposbampay.reader;

import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import java.util.Arrays;

import ba.informica.example.mposbampay.utils.HexUtils;

/**
 * Created by nedim on 1/11/16.
 */
public final class APDU {

    public static String transmitCommand (Reader mReader, String command) throws ReaderException {

        byte [] byteCommand = HexUtils.toByteArray(command);
        byte [] response = new byte[300];
        int responseLength = 0;

        try {
            if (mReader.getProtocol(0) == Reader.PROTOCOL_T0) {
                byteCommand = Arrays.copyOf(byteCommand, byteCommand.length - 1);
                responseLength = mReader.transmit(0, byteCommand, byteCommand.length, response, response.length);
                if (response[0] == (byte)0x61 || response[0] == (byte)0x6C) {
                    byte [] newCommandbytes = new byte[]{(byte) 0, (byte)0xC0, (byte) 0, (byte) 0, response[1]};
                    responseLength = mReader.transmit(0, newCommandbytes, newCommandbytes.length, response, response.length);
                }
            }
            else {
                responseLength = mReader.transmit(0, byteCommand, byteCommand.length, response, response.length);
            }
        } catch (ReaderException | IllegalArgumentException e) {
            Log.i("APDU", " cach: ", e);
            throw e;
        }

        if (responseLength > 0) {
            response = Arrays.copyOf(response, responseLength);
        }

        return HexUtils.toHexString(response);
    }

    public static String transmitByteCommand (Reader mReader, byte [] command) throws ReaderException{
        byte [] response = new byte[300];
        int responseLength = 0;
        Log.i("APDU", " byte Comm: " + HexUtils.toHexString(command));
        try {
            if (mReader.getProtocol(0) == Reader.PROTOCOL_T0) {
                command = Arrays.copyOf(command, command.length - 1);
                responseLength = mReader.transmit(0, command, command.length, response, response.length);

                if (response[0] == (byte)0x61) {
                    byte [] newCommandbytes = new byte[]{(byte) 0, (byte)0xC0, (byte) 0, (byte) 0, response[1]};
                    responseLength = mReader.transmit(0, newCommandbytes, newCommandbytes.length, response, response.length);
                }
                else if (response[0] == (byte)0x6C) {
                    byte [] newCommandbytes = Arrays.copyOf(command, command.length + 1);
                    newCommandbytes [command.length] = response[1];
                    responseLength = mReader.transmit(0, newCommandbytes, newCommandbytes.length, response, response.length);
                }

            } else {
                responseLength = mReader.transmit(0, command, command.length, response, response.length);
            }

        } catch (ReaderException | IllegalArgumentException e) {
            Log.i("APDU", " cach: ", e);
            throw e;
        }

        if (responseLength > 0) {
            response = Arrays.copyOf(response, responseLength);
        }

        return HexUtils.toHexString(response);
    }
}
