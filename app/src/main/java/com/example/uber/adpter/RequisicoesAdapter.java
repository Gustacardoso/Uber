package com.example.uber.adpter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.uber.R;
import com.example.uber.model.Requisicao;
import com.example.uber.model.Usuario;

import java.util.List;

public class RequisicoesAdapter  extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List<Requisicao> requisicoes;
    private Context context;
    private Usuario motorista;

    public RequisicoesAdapter(List<Requisicao> requisicoes, Context context, Usuario motorista) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.motorista = motorista;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes,parent,false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

         Requisicao requisicao = requisicoes.get(position);
         Usuario passageiro =  requisicao.getPassageiro();

         holder.nome.setText(passageiro.getNome());
         holder.distancia.setText("1 km aproximadamente ");// depois vamos calcular a distancia do passageiro

    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nome, distancia;

        public MyViewHolder(View itemView){
            super(itemView);
            nome = itemView.findViewById(R.id.textRequisicaoNome);
            distancia = itemView.findViewById(R.id.textRequisicaoDistancia);
        }
    }

}
