package io.plactal.eoscommander.ui.result;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.uuzuche.lib_zxing.activity.CodeUtils;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.ui.base.BaseDialog;

/**
 * Created by yangtao on 2018/11/10
 */
public class ShowQRCodeDialog extends BaseDialog {
    private static final String TAG = ShowQRCodeDialog.class.getSimpleName();
    private static final String ARG_BITMAP_NAME = "show.bitmap.name";

    public static ShowQRCodeDialog newInstance(String name) {
        ShowQRCodeDialog fragment = new ShowQRCodeDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_BITMAP_NAME, name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_qr_code, container, false);
    }


    @Override
    protected void setUpView(View view) {
        Bundle args = getArguments();

        Bitmap bitmap = CodeUtils.createImage(args.getString(ARG_BITMAP_NAME), 600, 600, null);
        ((ImageView) view.findViewById(R.id.imageView)).setImageBitmap(bitmap);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, TAG);
    }
}
