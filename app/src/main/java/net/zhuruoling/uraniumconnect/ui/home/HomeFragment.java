package net.zhuruoling.uraniumconnect.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;

import net.zhuruoling.uraniumconnect.R;
import net.zhuruoling.uraniumconnect.Server;
import net.zhuruoling.uraniumconnect.client.Command;
import net.zhuruoling.uraniumconnect.client.Message;
import net.zhuruoling.uraniumconnect.databinding.FragmentHomeBinding;
import net.zhuruoling.uraniumconnect.socket.EncryptedConnector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private String serverIP;
    private String key;
    private String cryptoKey;
    private String serverName;
    private int serverPort;
    private ExecutorService executerService;
    private Server server = null;
    @SuppressLint("DefaultLocale")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView textView = binding.textHome;
        CacheDiskUtils cache = CacheDiskUtils.getInstance();
        serverIP = cache.getString("server_ip");
        key = cache.getString("key");
        cryptoKey = cache.getString("crypto_key");
        serverPort = Integer.parseInt(cache.getString("server_port"));
        serverName = cache.getString("server_name");
        server = new Server(serverIP,serverPort,key,cryptoKey,serverName);
        textView.setText(String.format("%s@%s", serverName, serverIP));
        executerService = Executors.newCachedThreadPool();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}