package com.example.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.uber.R;
import com.example.uber.adpter.RequisicoesAdapter;
import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.RecyclerItemClickListener;
import com.example.uber.helper.UsuarioFirebase;
import com.example.uber.model.Requisicao;
import com.example.uber.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequisicoesActivity extends AppCompatActivity {

    //componentes
    private RecyclerView recyclerRequisicoes;
    private TextView textResultado;

    FirebaseAuth autenticacao_paraDeslogarUsuario;
    private DatabaseReference firebaseRef;
    private List<Requisicao> listaRequisicoes = new ArrayList<>();
    private RequisicoesAdapter adapter;
    private Usuario motorista;

    private LocationManager locationManager;
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes);
        inicializarComponentes();

        //vamos chamar o metodo Recuperar localização do usuario
        recuperarlocalizacaoUsuario();
    }

    @Override
    protected  void onStart(){
        super.onStart();
        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao(){
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
         DatabaseReference firabaseRef = ConfiguracaoFirebase.getFirabaseDataBase();

         DatabaseReference requisicoes = firabaseRef.child("requisicoes");

         Query requisicoesPesquisa = requisicoes.orderByChild("motorista/id")
                 .equalTo(usuarioLogado.getId());

         requisicoesPesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 for (DataSnapshot ds: dataSnapshot.getChildren()){
                     Requisicao requisicao = ds.getValue(Requisicao.class);

                     if (requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO)
                     || requisicao.getStatus().equals(Requisicao.STATUS_VIAGEM)){

                         abrirTelaCorrida(requisicao.getId(), motorista, true);

                     }

                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
    }

    private void recuperarlocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //para recuperar a latitude e longitude
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                //atualizar Geofire
                UsuarioFirebase.atualizarDadosLocalizacao(location.getLatitude()
                        ,location.getLongitude());

                if (!latitude.isEmpty() && !longitude.isEmpty()){
                    motorista.setLatitude(latitude);
                    motorista.setLongitude(longitude);
                    //precisamos da localização para calcular a distancia
                    locationManager.removeUpdates(locationListener);
                    adapter.notifyDataSetChanged();
                }

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
                    0,
                    0,
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

    public void abrirTelaCorrida(String idRequisicao, Usuario motorista, boolean requisicaoAtiva){

        Intent i  = new Intent(RequisicoesActivity.this,CorridaActivity.class);
        i.putExtra("idRequisicao", idRequisicao);
        i.putExtra("motorista", motorista);
        i.putExtra("requisicaoAtiva",requisicaoAtiva );
        startActivity(i);
    }

    private void inicializarComponentes() {

        getSupportActionBar().setTitle("Requisições");

        //configura componentes
        recyclerRequisicoes = findViewById(R.id.RecicleViewRequicicoes);
        textResultado = findViewById(R.id.text_resultado);


        //Configuração inicial
        motorista = UsuarioFirebase.getDadosUsuarioLogado();
        autenticacao_paraDeslogarUsuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirabaseDataBase();

        //configurar RecyclerView
        adapter = new RequisicoesAdapter(listaRequisicoes, getApplicationContext(),motorista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerRequisicoes.setLayoutManager(layoutManager);
        recyclerRequisicoes.setHasFixedSize(true);
        recyclerRequisicoes.setAdapter(adapter);

        //Adiciona evento de clique  no recycler
        recyclerRequisicoes.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerRequisicoes,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Requisicao requisicao = listaRequisicoes.get(position);
                                abrirTelaCorrida(requisicao.getId(), motorista, false);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        recupararRequisicoes();
    }

    private void recupararRequisicoes() {

        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        Query requisicaoPesquisa = requisicoes.orderByChild("status")
                .equalTo(Requisicao.STATUS_AGUARDANDO);//para listar os status aquardado
        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()> 0 ) {
                    textResultado.setVisibility(View.GONE);//ára esconder os text view
                    recyclerRequisicoes.setVisibility(View.VISIBLE);
                }else {
                    //agora vamos fazer ao contrario
                    textResultado.setVisibility(View.VISIBLE);
                    recyclerRequisicoes.setVisibility(View.GONE);
                }
                listaRequisicoes.clear();
                //recuperar a listagem de itens
                for (DataSnapshot ds: dataSnapshot.getChildren() ){
                    Requisicao requisicao = ds.getValue(Requisicao.class);
                    listaRequisicoes.add(requisicao);
                }

                adapter.notifyDataSetChanged(); // para mostrar as requisições
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
