package io.plactal.eoscommander.ui.buy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.di.component.ActivityComponent;
import io.plactal.eoscommander.ui.base.BaseFragment;
import io.plactal.eoscommander.util.UiUtils;

/**
 * Created by yangtao on 2018/11/6
 */
public class BuyFragment extends BaseFragment implements BuyMvpView {

    public static final String TAG = "yangtao";

    @Inject
    BuyPresenter mPresenter;

    private View mRootView;

    public static BuyFragment newInstance() {
        BuyFragment fragment = new BuyFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy_table, container, false);
        ActivityComponent component = getActivityComponent();
        if (component != null) {
            component.inject(this);
            mPresenter.attachView( this );
        }

        return view;
    }

    @Override
    protected void setUpView(View view) {

        mRootView = view;

        //  from, to, amount edit text
//        AutoCompleteTextView etFrom = view.findViewById(R.id.et_from);
        AutoCompleteTextView etTo = view.findViewById(R.id.et_to);
        EditText etAmount = view.findViewById(R.id.et_amount);

        // click handler
        view.findViewById(R.id.btn_transfer).setOnClickListener(v -> onSend() );

        etAmount.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (EditorInfo.IME_ACTION_SEND == actionId) {
                onSend();
                return true;
            }

            return false;
        });


        // account history
//        UiUtils.setupAccountHistory( etFrom, etTo );

    }

    private void onSend() {
        mPresenter.transfer("hoxhoxcreate", getTextFromEt(R.id.et_to), getTextFromEt(R.id.et_amount), getTextFromEt(R.id.et_memo));
    }


    private String getTextFromEt( int textEditId ) {
        EditText et = mRootView.findViewById( textEditId );
        if ( et == null ){
            return "";
        }

        return et.getText().toString();
    }



    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }
}
