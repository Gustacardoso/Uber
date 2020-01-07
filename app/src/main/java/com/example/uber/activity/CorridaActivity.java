package com.example.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.uber.R;
import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.UsuarioFirebase;
import com.example.uber.model.Requisicao;
import com.example.uber.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {

    //componente
    private Button buttonAceitarCorrida;

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;
    private LatLng localPassageiro;
    private Usuario motorista;
    private Usuario passageiro;
    private  String idRequisicao;
    private Requisicao requisicao;
    private DatabaseReference firebaseRef;
    private Marker marcadorMotorista;
    private  Marker marcadorPassageiro;
    private String statusRequisicao;
    private boolean requisicaoAtiva;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);

        inicializarComponentes();
        //Recupera dados do usuario
        if(getIntent().getExtras().containsKey("idRequisicao")
        && getIntent().getExtras().containsKey("motorista")){
            Bundle extras = getIntent().getExtras();
             motorista = (Usuario) extras.getSerializable("motorista");
             localMotorista = new LatLng(
                 Double.parseDouble(motorista.getLatitude()),
                         Double.parseDouble(motorista.getLongitude())
             );
             idRequisicao = extras.getString("idRequisicao");
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva");
             verificaStatusRequisicao();

        }

    }

    private void verificaStatusRequisicao() {

         DatabaseReference requisicoes = firebaseRef.child("requisicoes")
                 .child(idRequisicao);
         requisicoes.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 //recuperar requisicao
                 requisicao = dataSnapshot.getValue(Requisicao.class);
                 if (requisicao != null){
                     passageiro = requisicao.getPassageiro();
                     localPassageiro = new LatLng(
                             Double.parseDouble(passageiro.getLatitude()),
                             Double.parseDouble(passageiro.getLongitude())
                     );
                     statusRequisicao = requisicao.getStatus();
                     alteraInterfaceStatusRequisicao(statusRequisicao);
                 }

             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

    }
    private void alteraInterfaceStatusRequisicao(String status){
        switch (status ){
            case Requisicao.STATUS_AGUARDANDO:
                requisicaoAguardando();
                break;
            case Requisicao.STATUS_A_CAMINHO:
                requisicaoACaminho();
                break;
        }
    }
    private  void requisicaoAguardando(){
          buttonAceitarCorrida.setText("Aceitar Corrida");
        //exibe marcador do motorista
        adicionarMarcadorMotorista(localMotorista, motorista.getNome());

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(localMotorista,20)
        );
    }

    private void requisicaoACaminho(){
        buttonAceitarCorrida.setText("A Caminho do Passageiro");

        //exibe marcador do motorista
        adicionarMarcadorMotorista(localMotorista, motorista.getNome());
        //exibe marcados passageiro
        adicionarMarcadorPassageiro(localPassageiro, passageiro.getNome());
        //centralizar marcadores
        centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);
    }

    private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2) {
        // para exibir os marcadores que queremos exibir na tela
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(marcador1.getPosition());//para recuperar a posição
        builder.include(marcador2.getPosition());
         //bounds que sao  os limites entre os marcadores
        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20); //pegando  os 20% da tela
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds,largura,altura,espacoInterno
                )
        );

    }

    private void adicionarMarcadorMotorista(LatLng localizacao, String titulo) {

        if (marcadorMotorista != null)
            marcadorMotorista.remove();
      marcadorMotorista =   mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
        );



    }
    private void adicionarMarcadorPassageiro(LatLng localizacao, String titulo) {

        if (marcadorPassageiro != null)
            marcadorPassageiro.remove();
        marcadorPassageiro =   mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );

       /* mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(localizacao, 20)//definindo o local e o zoom

        );*/

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
        //vamos chamar o metodo Recuperar localização do usuario
        recuperarlocalizacaoUsuario();

        // Add a marker in Sydney and move the camera
     /*   LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }
    private void recuperarlocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //para recuperar a latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localMotorista = new LatLng(latitude, longitude);

                //atualizar Geofire
                UsuarioFirebase.atualizarDadosLocalizacao(latitude,longitude);

                alteraInterfaceStatusRequisicao(statusRequisicao);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //solicitar atualização de localização
        //as permissions tem que estar/ iguais
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(
                    locationManager.GPS_PROVIDER,
                    10000  ,//que vale a 10 segundo, 1000 mil milesegundos
                    10,//10 metros de distancia
                    locationListener

            );
            return;
        }

    }

    public void aceitarcorrida(View view){

        //configurar a requisicao
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao);
        requisicao.setMotorista(motorista);
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

        requisicao.atualizar();


    }

    private void inicializarComponentes(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Iniciar Corrida");

        buttonAceitarCorrida = findViewById(R.id.buttonAceitarCorrida);

        //Configuração inicial
       // autenticacao_paraDeslogarUsuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirabaseDataBase();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onSupportNavigateUp(){
        if (requisicaoAtiva){
            Toast.makeText(CorridaActivity.this,
                    "necessario encerrar a requisição atual!",
                    Toast.LENGTH_SHORT).show();
        }else
        {
            Intent intent = new Intent(CorridaActivity.this,RequisicoesActivity.class);
            startActivity(intent);
        }
        return  false;
    }

}
