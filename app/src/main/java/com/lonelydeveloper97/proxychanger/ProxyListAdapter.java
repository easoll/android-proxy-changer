package com.lonelydeveloper97.proxychanger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by easoll on 17-5-27.
 */

public class ProxyListAdapter extends BaseAdapter {
    private Context mContext;
    private List<ProxyModel> mProxyModels;
    private LayoutInflater mLayoutInflater;

    public ProxyListAdapter(Context context, List<ProxyModel> proxyModels){
        mContext = context;
        mProxyModels = proxyModels;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mProxyModels.size();
    }

    @Override
    public Object getItem(int position) {
        return mProxyModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.view_item_proxy, parent, false);
            holder.tvProxyServer = (TextView)convertView.findViewById(R.id.tv_proxy);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        ProxyModel model = (ProxyModel)getItem(position);
        String address = model.server + ":" + model.port;
        holder.tvProxyServer.setText(address);

        return convertView;
    }

    private class ViewHolder{
        public TextView tvProxyServer;
    }
}
