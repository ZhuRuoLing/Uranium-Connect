package net.zhuruoling.uraniumconnect.ui.gallery;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import net.zhuruoling.uraniumconnect.R;
import net.zhuruoling.uraniumconnect.Server;
import net.zhuruoling.uraniumconnect.client.Command;
import net.zhuruoling.uraniumconnect.client.Message;
import net.zhuruoling.uraniumconnect.databinding.FragmentGalleryBinding;
import net.zhuruoling.uraniumconnect.socket.EncryptedConnector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GalleryFragment extends Fragment  implements AdapterView.OnItemSelectedListener {

    private FragmentGalleryBinding binding;
    private String serverIP;
    private String key;
    private String cryptoKey;
    private int serverPort;
    private ExecutorService executerService;
    private String serverName = "";
    private Server server;
    private String operation = "";
    private String whitelist = "";
    private GalleryFragment self = this;
    Handler handler = null;
    @SuppressLint("DefaultLocale")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView textView = binding.textGallery;
        CacheDiskUtils cache = CacheDiskUtils.getInstance();
        serverIP = cache.getString("server_ip");
        key = cache.getString("key");
        cryptoKey = cache.getString("crypto_key");
        serverPort = Integer.parseInt(cache.getString("server_port"));
        serverName = cache.getString("server_name");
        handler = new Handler();
        textView.setText(String.format("%s@%s", serverName, serverIP));
        executerService = Executors.newCachedThreadPool();
        server = new Server(serverIP,serverPort,key,cryptoKey,serverName);
        String[] operations = {"Query","Get","Remove","Add"};
        Spinner spinner = binding.spinnerOperation;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(binding.getRoot().getContext(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1,operations);
        spinner.setAdapter(adapter);
        final Message[] message = new Message[1];
        self = this;
        executerService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(server.getServerIP(), server.getServerPort());
                    socket.setSoTimeout(60000);
                    EncryptedConnector connector = new EncryptedConnector(new BufferedReader(new InputStreamReader(socket.getInputStream())), new PrintWriter(new OutputStreamWriter(socket.getOutputStream())), server.getServerCryptoKey());
                    Command command = new Command("WHITELIST_LIST", new String[]{server.getServerKey()});
                    connector.println(new Gson().toJson(command));
                    String received = connector.readLine();
                    socket.close();
                    message[0] = new Gson().fromJson(received, Message.class);
                    ToastUtils.showShort(message[0].toString());
                    Spinner spinner = binding.spinnerWhitelistName;
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(binding.getRoot().getContext(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, message[0].getLoad());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setAdapter(adapter);
                        }
                    });
                }
                catch (Exception e){
                    ToastUtils.showShort(e.getMessage());
                }
            }
        });
        FloatingActionButton button = binding.floatingSendButton;
        Spinner spinnerOperation = binding.spinnerOperation;
        Spinner spinnerWhitelist = binding.spinnerWhitelistName;
        spinnerOperation.setOnItemSelectedListener(this);
        spinnerWhitelist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                whitelist = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                whitelist = message[0].getLoad()[0];
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText editText = binding.editPlayerName;
                String cmd = "";
                String operator = "";
                String[] load  = new String[4];
                switch (operation){
                    case "Get":
                        cmd = "WHITELIST_GET";
                        load[0] = whitelist;
                        load[1] = key;
                        break;
                    case "Remove":
                        cmd = "WHITELIST_EDIT";
                        operator = "REMOVE";
                        load[0] = whitelist;
                        load[1] = operator;
                        load[2] = editText.getText().toString();
                        if (StringUtils.isEmpty(load[2])){
                            Snackbar.make(view, "给👴空着干啥" ,Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            return;
                        }
                        load[3] = key;
                        break;
                    case "Add":
                        cmd = "WHITELIST_EDIT";
                        operator = "ADD";
                        load[0] = whitelist;
                        load[1] = operator;
                        load[2] = editText.getText().toString();
                        if (StringUtils.isEmpty(load[2])){
                            Snackbar.make(view, "给👴空着干啥" ,Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            return;
                        }
                        load[3] = key;
                        break;
                    case "Query":
                        cmd = "WHITELIST_QUERY";
                        load[0] = whitelist;
                        load[1] = editText.getText().toString();
                        load[2] = key;
                        if (StringUtils.isEmpty(load[1])){
                            Snackbar.make(view, "给👴空着干啥" ,Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            return;
                        }
                        break;
                    default:
                        return;
                }
                Command command = new Command(cmd,load);
                Snackbar.make(view, "Sending... " + command.toString(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                executerService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket(server.getServerIP(), server.getServerPort());
                            socket.setSoTimeout(60000);
                            EncryptedConnector connector = new EncryptedConnector(new BufferedReader(new InputStreamReader(socket.getInputStream())), new PrintWriter(new OutputStreamWriter(socket.getOutputStream())), server.getServerCryptoKey());
                            connector.println(new Gson().toJson(command));
                            String received = connector.readLine();
                            socket.close();
                            EditText output = binding.editOutput;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    output.setText(received);
                                }
                            });

                        }
                        catch (Exception e){
                            Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                });
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        operation = (String) adapterView.getItemAtPosition(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        operation = "Query";
    }
}