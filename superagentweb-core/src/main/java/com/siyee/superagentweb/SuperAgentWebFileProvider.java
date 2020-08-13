package com.siyee.superagentweb;

import android.content.Context;
import android.content.pm.ProviderInfo;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

public class SuperAgentWebFileProvider extends FileProvider {

    @Override
    public void attachInfo(@NonNull Context context, @NonNull ProviderInfo info) {
        super.attachInfo(context, info);
    }

}
