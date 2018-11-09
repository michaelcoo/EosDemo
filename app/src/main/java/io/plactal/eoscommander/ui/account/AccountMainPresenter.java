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

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import org.json.JSONArray;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.data.EoscDataManager;
import io.plactal.eoscommander.data.prefs.PreferencesHelper;
import io.plactal.eoscommander.data.remote.model.api.EosChainInfo;
import io.plactal.eoscommander.ui.account.info.AccountInfoType;
import io.plactal.eoscommander.ui.base.BasePresenter;
import io.plactal.eoscommander.ui.base.RxCallbackWrapper;
import io.plactal.eoscommander.util.StringUtils;
import io.plactal.eoscommander.util.Utils;
import io.reactivex.Completable;

/**
 * Created by swapnibble on 2017-11-16.
 */

public class AccountMainPresenter extends BasePresenter<AccountMainMvpView> {

    public static final String TAG = "yangtao";

    @Inject
    EoscDataManager mDataManager;

    @Inject
    public AccountMainPresenter(){
    }

    public void onClickCreateAccount(){
        // check any opened wallet
        if ( mDataManager.getWalletManager().listWallets( false ).size() <= 0 ) {
            getMvpView().showToast( R.string.no_wallet_unlocked_or_selected);
            return ;
        }

        getMvpView().openCreateAccountDialog();
    }

    public void loadAccountInfo(String account, int position, int offset, AccountInfoType infoType ){
        if (StringUtils.isEmpty(account)) {
            return;
        }

        switch ( infoType ) {
            case REGISTRATION:
                getRegistrationInfo( account );
                break;

            case TRANSACTIONS:
                getMvpView().showToast("Not implemented, coming soon.");
                break;

            case SERVANTS:
                getServents( account );
                break;

            case ACTIONS:
                getActions( account, position, offset );
                break;
        }
    }

    private void showResult( int titleId, String account, String result ) {
        addDisposable(
                Completable.fromAction( () -> mDataManager.addAccountHistory( account ))
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn( getSchedulerProvider().ui())
                    .doOnComplete(() -> getMvpView().showAccountInfo( titleId, account, result, null) )
                    .subscribe()
        );
    }

    private void getRegistrationInfo( String account) {
        getMvpView().showLoading( true );

        addDisposable( mDataManager
                .readAccountInfo( account )
                .subscribeOn(getSchedulerProvider().io())
                .observeOn( getSchedulerProvider().ui())
                .subscribeWith( new RxCallbackWrapper<JsonObject>( this) {
                    @Override
                    public void onNext(JsonObject result) {
                        if ( ! isViewAttached() ) return;

                        getMvpView().showLoading( false );

                        Log.d(TAG, "get account string: " + Utils.prettyPrintJson( result));

                        showResult( AccountInfoType.REGISTRATION.getTitleId(), account, Utils.prettyPrintJson( result) );
                        //getMvpView().showAccountInfo( AccountInfoType.REGISTRATION.getTitleId(), account,  Utils.prettyPrintJson( result) );
                    }

                })
        );
    }

    public void getActions(String account, int pos, int offset ) {
        getMvpView().showLoading( true );

        addDisposable( mDataManager
                .getActions( account, pos, offset )
                .subscribeOn(getSchedulerProvider().io())
                .observeOn( getSchedulerProvider().ui())
                .subscribeWith( new RxCallbackWrapper<JsonObject>( this) {
                    @Override
                    public void onNext(JsonObject result) {
                        if ( ! isViewAttached() ) return;

                        getMvpView().showLoading( false );

                        String a = getDataFromJsonObject(result, account);

                        getMvpView().showAccountInfo(AccountInfoType.TRANSACTIONS.getTitleId(), account, a, null);

//                        showResult( AccountInfoType.TRANSACTIONS.getTitleId(), account, Utils.prettyPrintJson( result) );
                        //getMvpView().showAccountInfo( AccountInfoType.TRANSACTIONS.getTitleId(), account, Utils.prettyPrintJson(result ) );
                    }

                })
        );
    }

    private String getDataFromJsonObject(JsonObject jsonObject, String accountName) {
        StringBuilder stringBuilder = new StringBuilder();
        JsonArray jaActions = jsonObject.getAsJsonArray("actions");
        Log.d(TAG, "getDataFromJsonObject: jaActions " + jaActions.size());
        for (int i=0; i<jaActions.size(); i++) {
            JsonObject jo = jaActions.get(i).getAsJsonObject();
            JsonObject joTrace = jo.getAsJsonObject("action_trace");
            JsonObject joReceipt = joTrace.getAsJsonObject("receipt");
            String name = joReceipt.get("receiver").getAsString();
            Log.d(TAG, "getDataFromJsonObject: name ....." +name);
            if (name.equals(accountName)) {
                JsonObject joAct = joTrace.getAsJsonObject("act");
                JsonObject joData = joAct.getAsJsonObject("data");
                String result = joData.toString();
                stringBuilder.append(result);
            }
        }
        Log.d(TAG, "getDataFromJsonObject: stringBuilder "+ stringBuilder.toString());
        return stringBuilder.toString();
    }

    private void getServents( String account ) {
        getMvpView().showLoading( true );

        addDisposable( mDataManager
                .getServants( account )
                .subscribeOn(getSchedulerProvider().io())
                .observeOn( getSchedulerProvider().ui())
                .subscribeWith( new RxCallbackWrapper<JsonObject>( this) {
                    @Override
                    public void onNext(JsonObject result) {
                        if ( ! isViewAttached() ) return;

                        getMvpView().showLoading( false );

                        showResult( AccountInfoType.SERVANTS.getTitleId(), account, Utils.prettyPrintJson( result) );
                        //getMvpView().showAccountInfo( AccountInfoType.SERVANTS.getTitleId(), account, Utils.prettyPrintJson(result ) );
                    }

                })
        );
    }

    public void onGetBalance(String contract, String account, String symbol){
        getMvpView().showLoading( true );

        addDisposable(
                mDataManager.getCurrencyBalance( contract, account, symbol )
//                        .doOnNext( balanceResult -> mDataManager.addAccountHistory( contract, account ))
                        .subscribeOn( getSchedulerProvider().io())
                        .observeOn( getSchedulerProvider().ui())
                        .subscribeWith( new RxCallbackWrapper<String>( this){
                            @Override
                            public void onNext(String result) {

                                if ( ! isViewAttached() ) return;

                                getMvpView().showLoading( false );
                                Log.d(TAG, "get balance string: "+result);

//                                getMvpView().showResult( result, null);
                                getMvpView().refreshListView(account, result);
                            }
                        })
        );
    }

    public void putAccountsInfo(String names, String balances) {
        Log.d(TAG, "putAccountsInfo: name " + names + " balance = " + balances);
        mDataManager.getPreferenceHelper().putAccountsInfo(names, balances);
    }

    public PreferencesHelper getPreferenceHelper(){
        return mDataManager.getPreferenceHelper();
    }
}
