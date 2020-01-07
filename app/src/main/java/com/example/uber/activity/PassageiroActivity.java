package com.example.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.uber.R;
import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.UsuarioFirebase;
import com.example.uber.model.Destino;
import com.example.uber.model.Requisicao;
import com.example.uber.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    //componentes
    private EditText editDestino;
    private LinearLayout linearLayoutDestino;
    private Button buttonChamarUber;

    private GoogleMap mMap;
    FirebaseAuth autenticacao_paraDeslogarUsuario;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPassageiro;
    private boolean uberChamado = false;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);

        inicializarComponentes();

        //adiciona listener para status da requisição
        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao() {

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaopesquisa = requisicoes.orderByChild("passageiro/id")
            .equalTo(usuarioLogado.getId());

        requisicaopesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Requisicao> lista = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                       lista.add(ds.getValue(Requisicao.class));
                }
                Collections.reverse(lista);
                if (lista!= null && lista.size()>0){
                    requisicao = lista.get(0);

                    switch (requisicao.getStatus()){ // aplicando os requisitos de status e aplicando as formatação graficas
                        case Requisicao.STATUS_AGUARDANDO:
                            linearLayoutDestino.setVisibility(View.GONE);
                            buttonChamarUber.setText("Cancelar Uber");
                            uberChamado = true;
                            break;
                }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    public void chamarUber(View view) {
        if (!uberChamado) {// uber nao foi chamado
            //inicio

            String enderecoDestino = editDestino.getText().toString(); //para recuperar o endereço

            if (!enderecoDestino.equals("") || enderecoDestino != null) { //verificando se o editext nao esta vazio

                Address addressDestino = recupararEndereco(enderecoDestino);

                if (addressDestino != null) {
                    final Destino destino = new Destino();
                    destino.setCidade(addressDestino.getSubAdminArea());
                    destino.setCep(addressDestino.getPostalCode());
                    destino.setBairro(addressDestino.getSubLocality());
                    destino.setRua(addressDestino.getThoroughfare());
                    destino.setNumero(addressDestino.getFeatureName());
                    destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                    destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("Cidade: " + destino.getCidade());
                    mensagem.append("\nRua: " + destino.getRua());
                    mensagem.append("\nBairro: " + destino.getBairro());
                    mensagem.append("\nNumero: " + destino.getNumero());
                    mensagem.append("\nCep: " + destino.getCep());

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu Endereço")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //salvar requisicao
                                    salvarRequisicao(destino);
                                    uberChamado = true;
                                }
                            }).setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();//para exibir


                } else
                    Toast.makeText(this, "Nao foi possivel Recuperar o endereço", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Informe o endereço de destino!", Toast.LENGTH_LONG).show();

            }
            //fim
        } else{
            uberChamado = false;
            //Cancelar a requisição
        }

    }

    public  void salvarRequisicao(Destino destino){

        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario usuarioPassageiro = UsuarioFirebase.getDadosUsuarioLogado();
        usuarioPassageiro.setLatitude(String.valueOf(localPassageiro.latitude));
        usuarioPassageiro.setLongitude(String.valueOf(localPassageiro.longitude));

                requisicao.setPassageiro(usuarioPassageiro);
                requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
                requisicao.salvar();
                linearLayoutDestino.setVisibility( View.GONE);
                buttonChamarUber.setText("Cancelar Uber");//isso para quando  o usuario estiver aquardado  o uber ele possa cancelar a viagem
               // assim o setText iara mudar de chamr uber para cancelar uber



    }

    private Address recupararEndereco(String endereco) {
     //feito isso podemos recuperar os dados basiado no endereco  do usuario
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEndereco = geocoder.getFromLocationName(endereco,1);
            if (listaEndereco != null && listaEndereco.size() > 0){
                Address address = listaEndereco.get(0);

                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }

    private void recuperarlocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //para recuperar a latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPassageiro = new LatLng(latitude, longitude);

                //atualizar Geofire
             UsuarioFirebase.atualizarDadosLocalizacao(latitude,longitude);
                mMap.clear();//para limpar o mapa

                mMap.addMarker(
                        new MarkerOptions()
                                .position(localPassageiro)
                                .title("Meu Local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                );

                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localPassageiro, 15)//definindo o local e o zoom

                );

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
                    1000,//que vale a 10 segundo, 1000 mil milesegundos
                    10,//10 metros de distancia
                    locationListener

            );
            return;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //para deslogar o  usuario
        switch (item.getItemId()){
            case R.id.menuSair:
                autenticacao_paraDeslogarUsuario.signOut();
                finish();// para finalizar a interface
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Iniciar uma viagem");
        setSupportActionBar(toolbar);

        //incializar os componentes
        editDestino = findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        buttonChamarUber = findViewById(R.id.buttonChamarUber);



        //Configuração inicial
        autenticacao_paraDeslogarUsuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirabaseDataBase();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}
