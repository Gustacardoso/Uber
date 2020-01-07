package com.example.uber.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.uber.R;
import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.UsuarioFirebase;
import com.example.uber.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private Switch switchTipoUsuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //inicializar os componentes

        campoNome = findViewById(R.id.idcadastroNome);
        campoEmail = findViewById(R.id.idcadastroEmail);
        campoSenha = findViewById(R.id.idcadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);
    }

    public void ValidarCadastroUsuario(View view){ //validando cadastro do usuario antes de salvar.
        //recuperar os textos do  campos
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        //verificando  se o texto nome nao esta vazio
        if (!textoNome.isEmpty()){
            if (!textoEmail.isEmpty()){
                if (!textoSenha.isEmpty()){
                    //instanciar classe usuario
                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);
                    usuario.setTipo(verificaTipoUsuario() );
                    //criar o metodo cadastrar usuario
                    cadrastrarUsuario(usuario);


                }else {
                    Toast.makeText(getApplicationContext(),"Preencha a senha!!",Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(getApplicationContext(),"Preencha o Email!!",Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Preencha o nome!!",Toast.LENGTH_LONG).show();
        }
    }

    private void cadrastrarUsuario(final Usuario usuario) {
        //vamos salvar o usuario dentro do  firebase

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //verificando se foi possivel fazer o cadastro do usuario
                if (task.isSuccessful()){
                   try {
                       String  idUsuario = task.getResult().getUser().getUid();
                       usuario.setId(idUsuario);
                       usuario.salvar();

                       //atualizar nome no user profile
                       UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                       //redireciona o usuario com base no seu  tipo
                       // se o  usuario  for passageiro chama activity maps
                       //senao chama a actvity  requisiçoes

                       if (verificaTipoUsuario() == "P"){
                           startActivity(new Intent(CadastroActivity.this, PassageiroActivity.class));
                           finish();
                           Toast.makeText(getApplicationContext(),"Sucesso ao cadastrar o passageiro",Toast.LENGTH_LONG).show();
                       }else {
                           startActivity(new Intent(CadastroActivity.this,RequisicoesActivity.class));
                           finish();
                           Toast.makeText(getApplicationContext(),"Sucesso ao cadastrar Motorista",Toast.LENGTH_LONG).show();
                       }
                   }catch (Exception e ){
                       e.printStackTrace();
                   }


                   // Toast.makeText(getApplicationContext(),"Sucesso",Toast.LENGTH_LONG).show();

                }
                else{
                    String excecao = "";
                    try{
                        throw  task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte !";
                    }catch (FirebaseAuthInvalidCredentialsException e ){
                        excecao = "Por Favor digite um email valido";
                    }catch (FirebaseAuthUserCollisionException e ){
                        excecao = "esta conta ja foi cadastrada";
                    }catch (Exception e ){
                        excecao = "Erro ao  cadastrar usuario:" +e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(),excecao,Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public String verificaTipoUsuario (){ //esse metodo  vai retornar um tipo de usuario
        //vamos utilizar um operador ternario, dessa forma que ele vi funcionar
       //return switchTipoUsuario.isChecked() ? "true" : "false"; // aqui esta verificando  se é verdadeiro ou falso
        //para entender melho caso seja true sera o motorista caso nao marque sera passageiro
        return switchTipoUsuario.isChecked() ? "M":"P";

    }
}
