package com.example.uber.model;

import com.example.uber.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Requisicao {

    private String id;
    private String status;
    private Usuario passageiro;
    private Usuario motorista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO =  "aguardando";
    public static final String STATUS_A_CAMINHO =  "acaminho";
    public static final String STATUS_VIAGEM =  "viagem";
    public static final String STATUS_FINALIZADA =  "finalizada";

    public Requisicao() {
    }

    public String getId() {
        return id;
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirabaseDataBase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        //recuperando  o id da requisição
        String idRequisicao = requisicoes.push().getKey();
        setId(idRequisicao);

        requisicoes.child(getId()).setValue(this);//assim salnado todos os dados da requisicao

    }

    public void atualizar (){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirabaseDataBase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("motorista", getMotorista());
        objeto.put("status" ,getStatus());

        requisicao.updateChildren(objeto);

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}
