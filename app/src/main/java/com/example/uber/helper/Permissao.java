package com.example.uber.helper;

import android.app.Activity;
import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {
    public static boolean validaPermissoes(String[] permissoes, Activity activity, int requestCode){


        if (Build.VERSION.SDK_INT >= 23){
            List<String> listapermissoes = new ArrayList<String>();

            /*percorre as permissoes passadas, verificando  uma a uma
            se ja tem permissao liberada
             */
            for (String permissao:permissoes){

                boolean validaPermissao = ContextCompat.checkSelfPermission(activity,permissao) == PackageManager.PERMISSION_GRANTED;
                if (!validaPermissao) listapermissoes.add(permissao); //caso essa lista nao seja validade ela quarda para a lista
                                                                      // da permissao
            }
            //caso  a lista esteja vazia, noa é necessario solicitar permissão
            if (listapermissoes.isEmpty())return true;
               String[] novasPermissoes = new String[listapermissoes.size()]; //tivemos que criar um array de String, para utiliza no requestPermission
               listapermissoes.toArray(novasPermissoes);
            //solicita permissao
            ActivityCompat.requestPermissions(activity,novasPermissoes,requestCode);
        }
        return true;
    }
}
