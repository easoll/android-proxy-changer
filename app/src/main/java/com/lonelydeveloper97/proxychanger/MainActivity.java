package com.lonelydeveloper97.proxychanger;


import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitbucket.lonelydeveloper97.wifiproxysettingslibrary.proxy_change_realisation.wifi_network.WifiProxyChanger;
import com.bitbucket.lonelydeveloper97.wifiproxysettingslibrary.proxy_change_realisation.wifi_network.WifiProxyInfo;
import com.bitbucket.lonelydeveloper97.wifiproxysettingslibrary.proxy_change_realisation.wifi_network.exceptions.ApiNotSupportedException;
import com.bitbucket.lonelydeveloper97.wifiproxysettingslibrary.proxy_change_realisation.wifi_network.exceptions.NullWifiConfigurationException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String PREFERENCE_KEY_PROXY = "proxy";
    private ListView mListView;
    private List<ProxyModel> mProxyModels;
    private ProxyListAdapter mAdapter;
    private View mProxyInfoWrapper;
    private View mAddProxyWrapper;
    private Button mBtnAddProxy;
    private Button mBtnCancelProxy;
    private Button mBtnConfirmAddProxy;
    private Button mBtnCancelAddProxy;
    private TextView mTvProxyInfo;
    private EditText mEtProxyServer;
    private EditText mEtProxyPort;
    private SharedPreferences mProxyInfoPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProxyInfoPreference = getSharedPreferences("proxy_info", MODE_PRIVATE);

        mProxyInfoWrapper = findViewById(R.id.proxy_info_wrapper);
        mAddProxyWrapper = findViewById(R.id.add_proxy_wrapper);
        mBtnAddProxy = (Button)findViewById(R.id.btn_add_proxy);
        mBtnAddProxy.setOnClickListener(this);
        mBtnCancelProxy = (Button)findViewById(R.id.btn_cancel_proxy);
        mBtnCancelProxy.setOnClickListener(this);
        mBtnConfirmAddProxy = (Button)findViewById(R.id.btn_confirm);
        mBtnConfirmAddProxy.setOnClickListener(this);
        mBtnCancelAddProxy = (Button)findViewById(R.id.btn_cancel);
        mBtnCancelAddProxy.setOnClickListener(this);
        mTvProxyInfo = (TextView)findViewById(R.id.tv_proxy_info);
        mEtProxyServer = (EditText)findViewById(R.id.et_proxy_server);
        mEtProxyPort = (EditText)findViewById(R.id.et_proxy_port);

        mListView = (ListView)findViewById(R.id.lv_proxy);
        //changeProxySettings("myhost.com", 12345);
        registerForContextMenu(mListView);
        String proxyStr = mProxyInfoPreference.getString(PREFERENCE_KEY_PROXY, "[]");
        mProxyModels = new Gson().fromJson(proxyStr,  new TypeToken<List<ProxyModel>>() {}.getType());
        mAdapter = new ProxyListAdapter(this, mProxyModels);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateProxyInfo();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add_proxy:
                mProxyInfoWrapper.setVisibility(View.GONE);
                mAddProxyWrapper.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_cancel_proxy:
                cancelProxy();
                break;
            case R.id.btn_confirm:
                addProxy();
                break;
            case R.id.btn_cancel:
                mAddProxyWrapper.setVisibility(View.GONE);
                mProxyInfoWrapper.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void cancelProxy(){
        try {
            WifiProxyChanger.clearProxySettings(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateProxyInfo();
    }

    private void updateProxyInfo(){
        String proHost = null ;
        int proPort = -1;

        try{
            proHost = WifiProxyInfo.getHost(this);
            proPort = WifiProxyInfo.getPort(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(proHost != null && !"".equals(proHost) && proPort != -1){
            mTvProxyInfo.setText(proHost + ":" + proPort);
        }else {
            mTvProxyInfo.setText("无代理");
        }
    }

    private void addProxy(){
        try{
            String proxyServer = mEtProxyServer.getText().toString();
            if("".equals(proxyServer)){
                throw new NullPointerException();
            }
            String proxyPort = mEtProxyPort.getText().toString();
            ProxyModel model = new ProxyModel();
            model.server = proxyServer;
            model.port = Integer.parseInt(proxyPort);
            mProxyModels.add(model);
            persistenceProxyInfo();
            mAdapter.notifyDataSetChanged();
            mAddProxyWrapper.setVisibility(View.GONE);
            mProxyInfoWrapper.setVisibility(View.VISIBLE);
        }catch (Exception e){
            Toast.makeText(this, "输入信息有误", Toast.LENGTH_SHORT).show();
        }
    }

    private void persistenceProxyInfo(){
        String proxyStr = new Gson().toJson(mProxyModels);
        SharedPreferences.Editor editor = mProxyInfoPreference.edit();
        editor.putString(PREFERENCE_KEY_PROXY, proxyStr);
        editor.apply();
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        //Toast.makeText(this, "" + position, Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mProxyModels.remove(position);
                persistenceProxyInfo();
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_set_proxy:
                ProxyModel model = mProxyModels.get(position);
                changeProxySettings(model);
                updateProxyInfo();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    void changeProxySettings(ProxyModel model) {
        try {
            WifiProxyChanger.changeWifiStaticProxySettings(model.server, model.port, this);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException | NoSuchFieldException | IllegalAccessException | NullWifiConfigurationException | ApiNotSupportedException e) {
            e.printStackTrace();
        }
    }
}

