package net.zhuruoling.uraniumconnect;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;

import net.zhuruoling.uraniumconnect.client.Command;
import net.zhuruoling.uraniumconnect.client.Message;
import net.zhuruoling.uraniumconnect.socket.EncryptedConnector;
import net.zhuruoling.uraniumconnect.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Executable;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ExecutorService executerService = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CacheDiskUtils.getInstance().clear();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executerService = Executors.newCachedThreadPool();
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MANAGE_EXTERNAL_STORAGE"
        };
        try {
            int perm = ActivityCompat.checkSelfPermission(this,permissions[2]);
            if (perm != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,permissions,1);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Button buttonChooseFile = findViewById(R.id.buttonLoad);
        buttonChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }
        });
        Button buttonConnect = findViewById(R.id.buttonLogin);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               executerService.execute(new Runnable() {
                   @Override
                   public void run() {
                       TextView ipText = findViewById(R.id.editServerIP);
                       TextView portText = findViewById(R.id.editServerPort);
                       TextView keyText = findViewById(R.id.editServerKey);
                       TextView cryptoKeyText = findViewById(R.id.editCryptoKey);
                       String ip = ipText.getText().toString();
                       int port = 0;
                       boolean parseFail = false;
                       try {
                           port = Integer.parseInt(portText.getText().toString());
                       }
                       catch (Exception ignored){
                           parseFail = true;
                       }
                       String key = keyText.getText().toString();
                       String cryptoKey = cryptoKeyText.getText().toString();
                       boolean check = (TextUtils.isEmpty(ip) || TextUtils.isEmpty(key) || TextUtils.isEmpty(cryptoKey) || parseFail);
                       if (check){
                           ToastUtils.showShort("给👴空着干啥");
                           return;
                       }
                       try {
                           Socket socket = new Socket(ip,port);
                           socket.setSoTimeout(60000);
                           EncryptedConnector connector = new EncryptedConnector(new BufferedReader(new InputStreamReader( socket.getInputStream())),new PrintWriter(new OutputStreamWriter(socket.getOutputStream())),cryptoKey);
                           Command command = new Command("PING",new String[]{key});
                           connector.println(new Gson().toJson(command));
                           String received = connector.readLine();
                           socket.close();
                           Message message = new Gson().fromJson(received,Message.class);

                           if (message.getMsg().equals("OK")){
                               int finalPort = port;
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       ToastUtils.showShort("Connected.");
                                       // TODO: 2022/1/27 start activity
                                        Intent intent = new Intent(MainActivity.this,CommandActivity.class);
                                        Bundle options = new Bundle();
                                        options.putString("server_ip",ip);
                                        options.putInt("server_port", finalPort);
                                        options.putString("key",key);
                                        options.putString("crypto_key", cryptoKey);
                                        options.putString("server_name",message.getLoad()[0]);
                                        intent.putExtras(options);
                                        startActivity(intent);
                                   }
                               });

                           }
                           else {
                               if (message.getMsg().equals("BAD_KEY")){
                                   runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           ToastUtils.showShort("Wrong Login key");
                                       }
                                   });

                               }
                               else {
                                   ToastUtils.showShort("Unknown message:" + message.toString());
                               }
                           }
                       }
                       catch (Exception e){
                           Log.i("Connector",e.getMessage());
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   ToastUtils.showShort("Can't connect to server.\n" + e.getMessage());
                               }
                           });
                           e.printStackTrace();
                       }
                   }

               });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileUtil.getFilePathByUri(this,uri);
                    if (path != null) {
                        File file = FileUtils.getFileByPath(path);
                        if (FileUtils.isFileExists(file)){
                            String jString = FileIOUtils.readFile2String(file);
                            jString = jString.replace('\n',' ');
                            Gson gson = new Gson();
                            Server server = gson.fromJson(jString,Server.class);
                            TextView ip = findViewById(R.id.editServerIP);
                            TextView port = findViewById(R.id.editServerPort);
                            TextView key = findViewById(R.id.editServerKey);
                            TextView cryptoKey = findViewById(R.id.editCryptoKey);
                            ip.setText(server.getServerIP());
                            port.setText(new String(String.valueOf(server.getServerPort())));
                            key.setText(server.getServerKey());
                            cryptoKey.setText(server.getServerCryptoKey());
                            return;
                        }
                    }
                }
            }

        }
    }
}