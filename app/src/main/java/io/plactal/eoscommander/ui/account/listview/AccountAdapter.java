package io.plactal.eoscommander.ui.account.listview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.plactal.eoscommander.R;

/**
 * Created by yangtao on 2018/11/7
 */
public class AccountAdapter extends ArrayAdapter {

    private final int resourceId;
    private List<AccountBalance> mDatas;

    public AccountAdapter(@NonNull Context context, int resource, @NonNull List<AccountBalance> objects) {
        super(context, resource, objects);
        resourceId = resource;
        mDatas = objects;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AccountBalance accountBalance = (AccountBalance) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView name = (TextView) view.findViewById(R.id.account_name);
        TextView balance = (TextView) view.findViewById(R.id.account_balance);
        name.setText(accountBalance.getName());
        balance.setText(accountBalance.getBalance());
        return view;
    }
}
