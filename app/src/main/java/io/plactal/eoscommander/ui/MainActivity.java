package io.plactal.eoscommander.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.data.EoscDataManager;
import io.plactal.eoscommander.data.remote.model.types.TypeSymbol;
import io.plactal.eoscommander.ui.base.BaseActivity;
import io.plactal.eoscommander.ui.settings.SettingsActivity;
import io.plactal.eoscommander.util.StringUtils;
import timber.log.Timber;


public class MainActivity extends BaseActivity {
    private static final int REQ_OPEN_CONNECTION_INFO = 10;
    private static final int REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION = 2;

    @Inject
    EoscDataManager mDataManager;

    @Inject
    CmdPagerAdapter mPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // di
        getActivityComponent().inject(this);

        setContentView(R.layout.activity_main);
        setToolbarConfig(R.id.toolbar, false);
        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText( R.string.wallet));
        tabLayout.addTab(tabLayout.newTab().setText( R.string.account ));
        tabLayout.addTab(tabLayout.newTab().setText( R.string.transfer ));
//        tabLayout.addTab(tabLayout.newTab().setText( R.string.push ));
//        tabLayout.addTab(tabLayout.newTab().setText( R.string.get_table));
        tabLayout.addTab(tabLayout.newTab().setText( R.string.buy_table));
//        tabLayout.addTab(tabLayout.newTab().setText( R.string.currency ));
        tabLayout.setTabGravity(TabLayout.MODE_SCROLLABLE);

        mViewPager =findViewById(R.id.container);
        mPagerAdapter.setTabCount( tabLayout.getTabCount());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if ( null != mPagerAdapter.getFragment( position ) ) {
                    mPagerAdapter.getFragment( position ).onSelected();
                }
            }

            @Override public void onPageScrollStateChanged(int state) {}
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {
                hideKeyboard();
            }

            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });


        mDataManager.clearAbiObjects();

        if (StringUtils.isEmpty( mDataManager.getPreferenceHelper().getNodeosConnInfo( null, null)) ) {
            mDataManager.getPreferenceHelper().putNodeosConnInfo("http", "172.20.160.41",8001);
            openSettingsActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void openSettingsActivity() {
        startActivityForResult( new Intent(this, SettingsActivity.class), REQ_OPEN_CONNECTION_INFO);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettingsActivity();
            return true;
        }else if (id == R.id.action_about) {
            startActivity( new Intent( this, InfoActivity.class) );
            return true;
        } else if(id == R.id.action_qr_code) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            } else {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //通过requestCode来识别是否同一个请求
        if (requestCode == CAMERA_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //用户同意，执行操作
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }else{
                //用户不同意，向用户展示该权限作用
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    new AlertDialog.Builder(this)
                            .setMessage("应用需要相机的权限，请给予便于使用扫一扫功能")
                            .setPositiveButton("OK", (dialog1, which) ->
                                    ActivityCompat.requestPermissions(this,
                                            new String[]{Manifest.permission.CAMERA},
                                            CAMERA_PERMISSION))
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == REQ_OPEN_CONNECTION_INFO ){
            return;
        } else if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    mViewPager.setCurrentItem(2);
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    showTransferFragment(result);
                    Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showTransferFragment(String data) {
        mPagerAdapter.getFragment(2).setSpecialData(data);
    }
}
