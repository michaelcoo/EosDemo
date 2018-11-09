/*
 * Copyright (c) 2017-2018 PlayerOne.
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.plactal.eoscommander.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.di.component.ActivityComponent;
import io.plactal.eoscommander.ui.account.create.CreateEosAccountDialog;
import io.plactal.eoscommander.ui.account.listview.AccountAdapter;
import io.plactal.eoscommander.ui.account.listview.AccountBalance;
import io.plactal.eoscommander.ui.result.ShowResultDialog;
import io.plactal.eoscommander.ui.account.info.AccountInfoType;
import io.plactal.eoscommander.ui.account.info.InputAccountDialog;
import io.plactal.eoscommander.ui.base.BaseFragment;
import io.plactal.eoscommander.util.UiUtils;

public class AccountMainFragment extends BaseFragment
        implements AccountMainMvpView {

    @Inject
    AccountMainPresenter mPresenter;

    public static final String TAG = "yangtao";
    private ListView mListView;
    private Button mRefresh;
    private AccountAdapter mAccountAdapter;

    private List<AccountBalance> accountBalanceList = new ArrayList<AccountBalance>();
    private List<AccountBalance> temp = new ArrayList<AccountBalance>();
    private List<AccountBalance> temp1 = new ArrayList<AccountBalance>();
    private StringBuilder mNamesBuilder  = new StringBuilder();
    private StringBuilder mBalancesBuilder  = new StringBuilder();

    private Boolean isFirstBoot = false;


    public static AccountMainFragment newInstance() {
        Bundle args = new Bundle();
        AccountMainFragment fragment = new AccountMainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFirstBoot = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        ActivityComponent component = getActivityComponent();
        if (component != null) {
            component.inject(this);

            mPresenter.attachView( this );
        }

        return view;
    }


    @Override
    protected void setUpView(View view) {

        mListView = view.findViewById(R.id.display_list_view);
        mRefresh = view.findViewById(R.id.btn_refresh);
        mRefresh.setOnClickListener(v -> onClickRefresh());

        mAccountAdapter = new AccountAdapter(getContext(), R.layout.fragment_account_list_item, accountBalanceList);
        mListView.setAdapter(mAccountAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AccountBalance ab = (AccountBalance) adapterView.getItemAtPosition(i);
                Log.d(TAG, "onItemClick: ab.getName() " + ab.getName());
                mPresenter.getActions(ab.getName(), -1, -20);
            }
        });


        //  create account
        view.findViewById(R.id.btn_command_create_account).setOnClickListener(v -> mPresenter.onClickCreateAccount() );

        // eosc get account <account>
        view.findViewById(R.id.btn_get_account).setOnClickListener(v -> openInputAccountDialog( AccountInfoType.REGISTRATION));

        // eosc get transaction <account>
        view.findViewById(R.id.btn_get_actions).setOnClickListener(v -> openInputAccountDialog( AccountInfoType.ACTIONS));

        // eosc get servants <account>
        view.findViewById(R.id.btn_get_servants).setOnClickListener(v -> openInputAccountDialog( AccountInfoType.SERVANTS));
        if (isFirstBoot) {
            getDatafromPeference();
            isFirstBoot = false;
        }
    }

    private void getDatafromPeference() {
        Log.d(TAG, "getDatafromPeference: ");
        AccountBalance ab = mPresenter.getPreferenceHelper().getAccountsInfo();
        if (ab != null) {
            Log.d(TAG, "getDatafromPeference: ab != null");
            String[] names = ab.getName().split(",");
            String[] balances = ab.getBalance().split(",");
            Log.d(TAG, "getDatafromPeference: names.length " + names.length);



            for (int i = 0; i<names.length; i++) {
                AccountBalance  bc = new AccountBalance();
                bc.setName(names[i]);
                bc.setBalance(balances[i]);
                accountBalanceList.add(bc);
            }

            temp.addAll(accountBalanceList);

            Log.d(TAG, "getDatafromPeference: accountBalanceList "+accountBalanceList);

            mAccountAdapter.notifyDataSetChanged();
        }
    }

    public void onClickRefresh() {

        String name = UiUtils.getAccountName();
        Log.d(TAG, "onClickRefresh: name " + name);
        if (!"".equals(name) && name != null) {
            mPresenter.onGetBalance("hoxhoxhoxhox", name, "ACT");
        } else if (temp.size() >0) {
            Log.d(TAG, "accountBalanceList.size(): " + accountBalanceList.size() + " temp size = " + temp.size() );
            accountBalanceList.clear();
            temp1.addAll(temp);
            for (int i = 0; i < temp1.size(); i++) {
                Log.d(TAG, "temp.get(i).getName(): "+temp1.get(i).getName());
                mPresenter.onGetBalance("hoxhoxhoxhox", temp1.get(i).getName(), "ACT");
            }

            temp1.clear();
            mPresenter.putAccountsInfo(mNamesBuilder.toString(), mBalancesBuilder.toString());
        }


    }

    @Override
    public void refreshListView(String name, String balance) {
        if (!mNamesBuilder.toString().contains(name)) {
            mNamesBuilder.append(name).append(",");
            mBalancesBuilder.append(balance).append(",");
        }
        AccountBalance a = new AccountBalance(name, balance);
        accountBalanceList.add(a);

        temp.clear();

        temp.addAll(accountBalanceList);
        mAccountAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void openCreateAccountDialog() {
        CreateEosAccountDialog.newInstance()
                .show(getChildFragmentManager());
    }



    private void openInputAccountDialog( AccountInfoType infoType ) {
        InputAccountDialog.newInstance( infoType)
                .setCallback( mPresenter::loadAccountInfo)
                .show(getChildFragmentManager()) ;
    }


    @Override
    public void showAccountInfo(int titleRscId, String account, String info, String statusInfo) {
        String title = String.format( getString(R.string.account_info_title_fmt), getString(titleRscId), account);

        ShowResultDialog.newInstance( title, info, statusInfo).show( getChildFragmentManager());
    }
}