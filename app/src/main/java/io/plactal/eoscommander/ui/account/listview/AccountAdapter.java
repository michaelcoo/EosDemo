package io.plactal.eoscommander.ui.account.listview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
        ViewHolder viewHolder = null;
        AccountBalance accountBalance = (AccountBalance) getItem(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder.mName = (TextView) convertView.findViewById(R.id.account_name);
            viewHolder.mBaleance = (TextView) convertView.findViewById(R.id.account_balance);
            viewHolder.mReceipt = (Button) convertView.findViewById(R.id.receipt);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mName.setText(accountBalance.getName());
        viewHolder.mBaleance.setText(accountBalance.getBalance());
        viewHolder.mReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReceiptClickListener.onReceiptClick(position);
            }
        });
        return convertView;
    }

    public interface onReceiptClickListener {
        void onReceiptClick(int i);
    }

    private onReceiptClickListener mReceiptClickListener;

    public void setonReceiptClickListener (onReceiptClickListener receiptClickListener) {
        this.mReceiptClickListener = receiptClickListener;
    }

    class ViewHolder {
        TextView mName;
        TextView mBaleance;
        Button mReceipt;
    }

}
