package io.plactal.eoscommander.ui.buy;

import com.google.gson.JsonObject;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.data.EoscDataManager;
import io.plactal.eoscommander.ui.base.BasePresenter;
import io.plactal.eoscommander.ui.base.RxCallbackWrapper;
import io.plactal.eoscommander.util.Utils;

/**
 * Created by yangtao on 2018/11/6
 */
public class BuyPresenter extends BasePresenter<BuyMvpView> {

    @Inject
    EoscDataManager mDataManager;

    @Inject
    public BuyPresenter(){
    }

    public void transfer( String from, String to, String amount, String memo){
        long amountAsLong = Utils.parseLongSafely( amount, 0);
        if ( amountAsLong < 0 ) {
            getMvpView().onError( R.string.invalid_amount);
            return;
        }

        getMvpView().showLoading( true );

        addDisposable( mDataManager
                .transfer( from, to, amountAsLong, memo)
                .doOnNext( jsonObject -> mDataManager.addAccountHistory( from, to ))
                .subscribeOn( getSchedulerProvider().io())
                .observeOn( getSchedulerProvider().ui())
                .subscribeWith( new RxCallbackWrapper<JsonObject>( this){
                    @Override
                    public void onNext(JsonObject result) {

                        if ( ! isViewAttached() ) return;

                        getMvpView().showLoading( false );

                        getMvpView().showResult( Utils.prettyPrintJson(result), "OK");
                    }
                })

        );

    }
}
