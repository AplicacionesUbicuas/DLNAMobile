package org.unicauca.dlnamobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.ParserException;
import org.cybergarage.xml.parser.JaxpParser;
import org.unicauca.dlnamobile.adapter.DevicesAdapter;
import org.unicauca.dlnamobile.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SearchActivity extends Activity {
    private static final String TAG = "SearchingActivity";
    private static String SLDEVICEID = "sec001";
    private static final String _CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz";
    private static final int SEARCHING_DIALOG = 0;
    private static final int SAERCHING_ERROR_DIALOG = 1;
    private static final int PAIRING_DIALOG = 2;
    private static final int PAIRING_ERROR_DIALOG = 3;
    private static final int PAIRING_REACH_LIMIT = 4;
    private ProgressDialog searchingDialog;
    private ProgressDialog pairingDialog;
    private AlertDialog alertDialog;
    private ControlPoint controlPoint;
    private ArrayList<String> deviceNames = null;
    private ArrayList<String> deviceAddresses = null;
    private String connectTarget = "";
    private String queueTarget = "";
    private String pollingTarget = "";
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    private String ipconf;

    /**
     * Clic sobre un item en la lista de dispotivos encontrados
     * <p/>
     * Inicia el emparejamiento
     */
    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            connectTarget = "http://" + deviceAddresses.get(position)
                    + Constants.CONNECTION_PATH;
            queueTarget = "http://" + deviceAddresses.get(position)
                    + Constants.QUEUE_PATH;
            pollingTarget = "http://" + deviceAddresses.get(position)
                    + Constants.POLLING_PATH;
            editor = prefs.edit();
            editor.putString("ip", deviceAddresses.get(position));
            editor.commit();
            startPairing(connectTarget);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search_layout);
        deviceNames = new ArrayList<String>();
        deviceAddresses = new ArrayList<String>();
        prefs = getSharedPreferences("Preferencias",
                getApplicationContext().MODE_PRIVATE);
        prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        SLDEVICEID = getRandomString();
        editor = prefs.edit();
        editor.putString("SLDEVICEID", SLDEVICEID);
        editor.commit();
        Log.i(TAG, "Generated SLDEVICEID for this device " + SLDEVICEID);

        startSearch();
    }

    public void updateSearch(View v){
        startSearch();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
    }

    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
    }

    private void startSearch() {
        new SearchAsyncTask().execute("");
    }

    private void startPairing(String target) {
        new PairingAsyncTask().execute(target);
    }

    public String getRandomString() {
        StringBuffer randStr = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            Random r = new Random();
            int number = r.nextInt(_CHAR.length());
            char ch = _CHAR.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SEARCHING_DIALOG:
                searchingDialog = ProgressDialog.show(this, "",
                        getString(R.string.searching_message));
                return searchingDialog;

            case SAERCHING_ERROR_DIALOG:
                alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(R.string.searching_error);
                alertDialog.setMessage(getString(R.string.searching_error_msg));
                alertDialog.setCanceledOnTouchOutside(true);
                return alertDialog;

            case PAIRING_DIALOG:
                pairingDialog = ProgressDialog.show(this, "",
                        getString(R.string.pairing_message));
                return pairingDialog;

            case PAIRING_ERROR_DIALOG:
                alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(R.string.pairing_error);
                alertDialog.setMessage(getString(R.string.pairing_error_msg));
                return alertDialog;

            case PAIRING_REACH_LIMIT:
                alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(R.string.pairing_error);
                alertDialog.setMessage(getString(R.string.pairing_error_limit));
                return alertDialog;

            default:
                return super.onCreateDialog(id);
        }
    }

    private void displaySearchResult(HashMap<String, String> devices) {
        Log.i(TAG, "Dispositivos encontrados " + devices.size());
        if (devices.size() > 0) {
            deviceNames = new ArrayList<String>(devices.keySet());
            deviceAddresses = new ArrayList<String>(devices.values());
            ListView listView = (ListView) findViewById(R.id.ListDevices);
            DevicesAdapter adap = new DevicesAdapter(this,
                    R.layout.list_item_devices, deviceNames);
            listView.setAdapter(adap);
            listView.setOnItemClickListener(itemClickListener);
        } else {
            showDialog(SAERCHING_ERROR_DIALOG);
            deviceNames = new ArrayList<String>();
            DevicesAdapter da = new DevicesAdapter(this,
                    R.layout.list_item_devices, deviceNames);
            ListView listView = (ListView) findViewById(R.id.ListDevices);
            listView.setAdapter(da);
            listView.setOnItemClickListener(itemClickListener);

        }

    }

    private void displayPairingResult(Integer responseCode) {
        Log.i(TAG, "Pairing terminado con codigo " + responseCode);
        switch (responseCode) {

            // OK
            case HttpURLConnection.HTTP_OK:
                Log.i(TAG, "Conexion establecida");
                Toast.makeText(getApplicationContext(), "conectado con exito", Toast.LENGTH_LONG).show();
                onDestroy();
                startActivity(new Intent(getApplicationContext(), ViewPageActivity.class));
                break;

            // Unauthorized, not found, not responded
            case HttpURLConnection.HTTP_NOT_FOUND:
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Log.i(TAG, "Conexion rechazada por el Dispositivo");
                showDialog(PAIRING_ERROR_DIALOG);
                break;

            // Full connections pool
            case HttpURLConnection.HTTP_UNAVAILABLE:
                Log.i(TAG, "No se puede establecer mas conexiones al Dispositivo");
                showDialog(PAIRING_REACH_LIMIT);
                break;

            case HttpURLConnection.HTTP_BAD_REQUEST:
                Log.i(TAG, "Error 400 Bad Request... Verificar cabeceras HTTP");
                break;

            case -1:
                showDialog(PAIRING_ERROR_DIALOG);
                break;
        }

    }

    /**
     * Esta clase AsyncTask realiza la peticion HTTP para el emparejamiento con
     * el Smart TV seleccionado de la lista de encontrados.
     *
     * @author Jaime
     */
    private class PairingAsyncTask extends AsyncTask<String, Integer, Integer> {

        HttpURLConnection connection;

        @Override
        protected Integer doInBackground(String... arg0) {
            int responseCode = -1;

            try {

                System.setProperty("http.keepAlive", "false");
                System.setProperty("http.maxConnections", "" + 10);
                CookieHandler.setDefault(new CookieManager(null,
                        CookiePolicy.ACCEPT_ALL));

                URL url = new URL(arg0[0]);
                connection = (HttpURLConnection) url.openConnection();

                // SET REQUEST INFO
                connection.setRequestMethod("GET");
                connection.setDoOutput(false);
                connection.setConnectTimeout(50000);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.connect();
                responseCode = connection.getResponseCode();
                Log.i(TAG, "Connection Response Code " + responseCode);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            pairingDialog.dismiss();
            displayPairingResult(responseCode);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(PAIRING_DIALOG);
        }

    }

    /**
     * Esta clase AsyncTask realizar la busqueda de dispositivos Upnp en un
     * hilo distino al UIThread
     *
     * @author Jaime
     */
    private class SearchAsyncTask extends
            AsyncTask<String, Integer, HashMap<String, String>> {
        private HashMap<String, String> devices = new HashMap<String, String>();

        @Override
        protected HashMap<String, String> doInBackground(String... arg0) {
            controlPoint = new ControlPoint();
            controlPoint
                    .addSearchResponseListener(new SearchResponseListener() {

                        @Override
                        public void deviceSearchResponseReceived(
                                SSDPPacket ssdpPacket) {
                            String location = ssdpPacket.getLocation(); // location
                            String ip = ssdpPacket.getRemoteAddress(); // IP
                            // Address
                            int rp = ssdpPacket.getRemotePort();
                            Log.d(TAG, "location = " + location);
                            Log.d(TAG, "IP " + ip);
                            Log.d(TAG, "Remote Port " + rp);
                            HttpGet httpget = new HttpGet(location);
                            DefaultHttpClient client = new DefaultHttpClient();
                            HttpResponse resp = null;
                            try {
                                resp = client.execute(httpget); // http GET for
                                // Location.
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // Try to find friendly name in XML
                            if (resp != null
                                    && resp.getStatusLine().getStatusCode() == 200) {
                                JaxpParser parser = null;
                                try {
                                    parser = new JaxpParser();
                                    InputStream is = resp.getEntity()
                                            .getContent();
                                    Node deviceNode = parser.parse(is).getNode(
                                            "device");
                                    if (deviceNode != null) {
                                        String friendly = deviceNode
                                                .getNodeValue("friendlyName"); // friendly
                                        // name
                                        Log.d(TAG, "friendly : " + friendly);
                                        devices.put(friendly, ip);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ParserException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (resp != null) {
                                    Log.i(TAG, "RESPONSE CODE "
                                            + resp.getStatusLine()
                                            .getStatusCode());
                                } else {
                                    Log.i(TAG, "Respuesta es null");
                                }
                            }
                        }
                    });

            controlPoint.start(); // start control point using search
            // target
            Log.i(TAG, "iniciando busqueda de uPnP");
            long endTime = System.currentTimeMillis() + 10 * 1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }

            Log.i(TAG, "terminado busqueda de uPnP");
            controlPoint.stop();
            return devices;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(SEARCHING_DIALOG);
        }

        @Override
        protected void onPostExecute(HashMap<String, String> result) {
            super.onPostExecute(result);
            searchingDialog.dismiss();
            displaySearchResult(result);
        }

    }
}
