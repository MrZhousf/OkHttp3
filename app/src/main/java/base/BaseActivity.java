package base;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import butterknife.ButterKnife;

public abstract class BaseActivity extends ActionBarActivity {

    protected abstract int initLayout();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initLayout());
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }
}
