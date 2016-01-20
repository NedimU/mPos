package ba.informica.example.mposbampay.view;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ba.informica.example.mposbampay.R;

public class SoapResponseView extends AppCompatActivity {

    String TAG = "Response";
    Button bt;
    EditText celcius;
    String getCel;
    SoapPrimitive resultString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap_response_view);
        Log.i("SoapResponse", "In the onCreate: ");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //String cardData = extras.getString("card_data");
        }

        bt = (Button) findViewById(R.id.bt);
        celcius = (EditText) findViewById(R.id.cel);


        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getCel = celcius.getText().toString();
                AsyncCallWS task = new AsyncCallWS();
                task.execute();
            }
        });
    }

    private class AsyncCallWS extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "doInBackground");
            calculate();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
            Toast.makeText(getBaseContext(), "Response" + resultString.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    public void calculate() {
        String SOAP_ACTION = "";//"WSInterface";
        String METHOD_NAME = "hello";
        String NAMESPACE = "http://mPosWSExample.ws.informica.ba/";
        String URL = "http://192.168.0.106:8080/mPosWSExample/WSInterface?wsdl";
        Log.i("SoapResponse", "In the calculate method: ");

        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            Request.addProperty("helloTo", getCel);

            /*PropertyInfo p = new PropertyInfo();
            p.setName("helloTo");
            p.setValue(getCel);
            p.setType(String.class);
            Request.addProperty(p);*/

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            soapEnvelope.setOutputSoapObject(Request);

            HttpTransportSE transport = new HttpTransportSE(URL);

            transport.call(SOAP_ACTION, soapEnvelope);
            resultString = (SoapPrimitive) soapEnvelope.getResponse();

            Log.i(TAG, "Result: " + resultString);
        } catch (Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
        }
    }
}