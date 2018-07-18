package io.rong.callkit.util;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import io.rong.callkit.R;

/**
 * Created by dengxudong on 2018/5/18.
 */

public class GlideUtils {

    public static void showBlurTransformation(Context context, ImageView imageView ,Uri val){
        if(val==null){return;}
        Glide.with(context)
                .load(val)
                .apply(RequestOptions.bitmapTransform(new GlideBlurformation(context)))
                .apply(new RequestOptions().centerCrop())
                .into(imageView);
    }


    public static void showRemotePortrait(Context context, ImageView imageView ,Uri val){
        RequestOptions requestOptions=new RequestOptions();
        requestOptions.transform(new GlideRoundTransform());
        requestOptions.priority(Priority.HIGH);
        requestOptions.placeholder(R.drawable.rc_default_portrait);
        if(val==null){
            Glide.with(context)
                    .load(R.drawable.rc_default_portrait)
                    .apply(requestOptions)
                    .into(imageView);
        }else{
            Glide.with(context)
                    .load(val)
                    .apply(requestOptions)
                    .into(imageView);
        }
    }
}
