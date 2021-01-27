package io.rong.recognizer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;

import io.rong.imkit.conversation.extension.InputMode;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.RongExtensionViewModel;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imkit.conversation.extension.component.plugin.IPluginRequestPermissionResultCallback;
import io.rong.imkit.manager.AudioPlayManager;
import io.rong.imkit.utils.PermissionCheckUtil;


public class RecognizePlugin implements IPluginModule, IPluginRequestPermissionResultCallback {

    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_recognizer_voice_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_plugin_recognize);
    }

    @Override
    public void onClick(Fragment currentFragment, final RongExtension extension, int index) {
        String[] permissions = {Manifest.permission.RECORD_AUDIO};
        if (PermissionCheckUtil.checkPermissions(currentFragment.getActivity(), permissions)) {
            startRecognize(currentFragment, extension);
        } else {
            extension.requestPermissionForPluginResult(permissions, IPluginRequestPermissionResultCallback.REQUEST_CODE_PERMISSION_PLUGIN, this);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void startRecognize(Fragment fragment, final RongExtension extension) {
        if (AudioPlayManager.getInstance().isPlaying()) {
            AudioPlayManager.getInstance().stopPlay();
        }
        RongExtensionViewModel mExtensionViewModel = new ViewModelProvider(fragment).get(RongExtensionViewModel.class);
        mExtensionViewModel.getInputModeLiveData().postValue(InputMode.RecognizeMode);

        Recognizer recognizerView = new Recognizer(extension.getContext());
        recognizerView.setResultCallBack(new IRecognizedResult() {
            @Override
            public void onResult(String data) {
                Editable editable = extension.getInputEditText().getText();
                String str = editable.toString() + data;
                extension.getInputEditText().setText(str);
                extension.getInputEditText().setSelection(str.length());
            }

            @Override
            public void onClearClick() {
                extension.getInputEditText().setText("");
            }
        });
        extension.addPluginPager(recognizerView);
        recognizerView.startRecognize();
    }

    @Override
    public boolean onRequestPermissionResult(Fragment fragment, RongExtension extension, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionCheckUtil.checkPermissions(fragment.getActivity(), permissions)) {
            startRecognize(fragment, extension);
        } else {
            PermissionCheckUtil.showRequestPermissionFailedAlter(fragment.getContext(), permissions, grantResults);
        }
        return true;
    }
}
