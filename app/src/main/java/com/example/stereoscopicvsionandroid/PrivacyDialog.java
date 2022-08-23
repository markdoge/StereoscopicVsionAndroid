package com.example.stereoscopicvsionandroid;

import android.app.Dialog;
import android.content.Context;

public class PrivacyDialog extends Dialog {

    public PrivacyDialog(Context context) {
        super(context);

        setContentView(R.layout.dialog_privacy);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}
