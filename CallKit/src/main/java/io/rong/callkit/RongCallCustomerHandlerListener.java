package io.rong.callkit;


import android.content.Context;
import android.content.Intent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;

public interface RongCallCustomerHandlerListener {

    List<String> handleActivityResult(int requestCode, int resultCode, Intent data);

    void addMember(Context context, ArrayList<String> currentMemberIds);

    void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType);

    void onCallConnected(RongCallSession callSession, SurfaceView localVideo);

    void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason);

}
