package com.shockdom.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by walterverdese on 13/12/14.
 */
public class AlertDialogs {

    public static ProgressDialog showProgressDialog(Context c, String title, String message) {
       return ProgressDialog.show(c, title, message, true, false);
    }

    public static AlertDialog showSimpleDialog(Context c, String title, String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder b = new AlertDialog.Builder(c);
        if (title != null) b.setTitle(title);
        if (message != null) b.setMessage(message);
        if (listener != null) b.setPositiveButton(android.R.string.ok, listener);
        else b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return b.show();
    }

    public static AlertDialog showSimpleChoiceDialog(Context c, String title, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener koListener) {
        AlertDialog.Builder b = new AlertDialog.Builder(c);
        if (title != null) b.setTitle(title);
        if (message != null) b.setMessage(message);
        if (okListener != null) b.setPositiveButton(android.R.string.ok, okListener);
        if (koListener != null) b.setNegativeButton(android.R.string.cancel, koListener);
        else b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return b.show();
    }

}
