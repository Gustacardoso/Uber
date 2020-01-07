package com.example.uber.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //inicializar componentes
        campoEmail = findViewById(R.id.edit_loginEmail);
        campoSenha = findViewById(R.id.edit_loginSenha);
    }
     public void validarLoginUsuario (View view){
          //recuperar textos dos campos
         String textoEmail = campoEmail.getText().toString();
         String textoSenha = campoSenha.getText().toString();

         if (!textoEmail.isEmpty()){
             if (!textoSenha.isEmpty()){
                 Usuario usuario = new Usuario();
                 usuario.setEmail(textoEmail);
                 usuario.setSenha(textoSenha);
                 //criar um metodo
                 logarUsuario(usuario);
             }else{
                 Toast.makeText(LoginActivity.this,"preencha o senha!",Toast.LENGTH_LONG).show();
             }
         }else{
             Toast.makeText(LoginActivity.this,"preencha o email!",Toast.LENGTH_LONG).show();
         }
     }

    private void logarUsuario(Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful() ){
                     //verificar o tipo  de usuario logado
                    //motorista ou passageiro
                    UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);
                }else{
                    String excececao = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excececao = "usuario não esta cadastrado.";
                    }catch (FirebaseAuthInvalidCredentialsException e ){
                        excececao = "E-mail e senha não correspondendem a uma ";
                    }catch (Exception e ){
                        excececao = "Erro ao cadastrar usuario" + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this,excececao,Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
