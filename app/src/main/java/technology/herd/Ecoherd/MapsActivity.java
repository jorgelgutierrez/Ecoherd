package technology.herd.Ecoherd;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import technology.herd.Ecoherd.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<Marker> marcadores_camion = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void posicioncamiones() {
        //Esta función es llamada des de dentro del Timer
        //Para no provocar errores ejecutamos el Accion
        //Dentro del mismo Hilo
        this.runOnUiThread(actualizarposicion);
    }

    private Runnable actualizarposicion = new Runnable() {
        public void run() {

            new ConsultarCamiones().execute("Consultar_Localizacion_Camiones.php");

        }
    };

    //Ejecucion de operacion Consultar Camiones en un hilo separado de la interfaz del usuario....
    private class ConsultarCamiones extends AsyncTask<String, Void, String> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                Metodos obtenerjson = new Metodos();
                return obtenerjson.getJSONfromUrl((urls[0]));
            } catch (Exception e) {
                return "Problemas con la conexion a internet";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            JSONArray resultadoJSON = null;

            try {
                resultadoJSON = new JSONArray(result);

                //Removiendo los marcadores de camiones para poder moverlos de posicion...
                for (int x = 0; x <= marcadores_camion.size() - 1; x++) {
                    Marker marker = marcadores_camion.get(x);
                    marker.remove();
                }

                //Limpiando el array list de marcadores para poder volver a crear las posiciones nuevas de los camiones...
                marcadores_camion.clear();

                //Creando las nuevas posiciones de los camiones...
                for (int i = 0; i <= resultadoJSON.length() - 1; i++) {
                    JSONObject linea = resultadoJSON.getJSONObject(i);
                    if(linea.getString("Status").equals("true")) {
                        LatLng posicion_camion = new LatLng(Double.valueOf(linea.getString("Latitud")), Double.valueOf(linea.getString("Longitud")));
                        marcadores_camion.add(mMap.addMarker(new MarkerOptions()
                                .position(posicion_camion)
                                .title("Camion Basura " + linea.getString("Id_Camion"))
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_trash_truck))));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //Ejecucion de operacion Consultar Contenedores en un hilo separado de la interfaz del usuario....
    private class ConsultarContenedores extends AsyncTask<String, Void, String> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                Metodos obtenerjson = new Metodos();
                return obtenerjson.getJSONfromUrl((urls[0]));
            } catch (Exception e) {
                return "Problemas con la conexion a internet";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            JSONArray resultadoJSON = null;

            try {
                resultadoJSON = new JSONArray(result);

                for (int i = 0; i <= resultadoJSON.length() - 1; i++) {
                    JSONObject linea = resultadoJSON.getJSONObject(i);
                    LatLng posicion_camion = new LatLng(Double.valueOf(linea.getString("Latitud")), Double.valueOf(linea.getString("Longitud")));
                    mMap.addMarker(new MarkerOptions()
                            .position(posicion_camion)
                            .title("Contenedor " + linea.getString("Id_Contenedor"))
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_container)));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Posicionando la camara con el zoom...
        LatLngBounds posicionarguzman = new LatLngBounds(new LatLng(19.703648, -103.463000), new LatLng(19.703648, -103.463000));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionarguzman.getCenter(), 15));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        new ConsultarContenedores().execute("Consultar_Localizacion_Contenedores.php");
        Toast.makeText(getBaseContext(),"Localiza tu contenedor de basura mas cercano",Toast.LENGTH_LONG).show();
        //Timer para actualizar posicion del camion...
        Timer timer = new Timer();
        //Actuara cada 10 segundos...
        //Empezando des de el segundo 10
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Llamado ala funcion
                posicioncamiones();
            }
        }, 0, 10000);

    }
}
