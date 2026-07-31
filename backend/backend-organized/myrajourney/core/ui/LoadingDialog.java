package com.example.myrajourney.core.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.example.myrajourney.R;

public class LoadingDialog {
    private Dialog dialog;
    private Context context;
    
    public LoadingDialog(Context context) {
        this.context = context;
    }
    
    public void show(String message) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_loading, null);
        
        TextView messageText = view.findViewById(R.id.loading_message);
        messageText.setText(message);
        
        builder.setView(view);
        builder.setCancelable(false);
        
        dialog = builder.create();
        dialog.show();
    }
    
    public void show() {
        show("Loading...");
    }
    
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}






