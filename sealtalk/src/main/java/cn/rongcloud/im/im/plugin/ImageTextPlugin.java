package cn.rongcloud.im.im.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imlib.model.Message;
import io.rong.message.RichContentMessage;

public class ImageTextPlugin implements IPluginModule {

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ext_plugin_poke_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_reference_link);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension, int index) {
        RichContentMessage richContentMessage =
                new RichContentMessage(
                        "图文",
                        "this is",
                        "https://image.baidu.com/search/detail?ct=503316480&z=0&ipn=d&word=%E5%AF%BC%E5%BC%B9&step_word=&hs=0&pn=0&spn=0&di=36740&pi=0&rn=1&tn=baiduimagedetail&is=0%2C0&istype=2&ie=utf-8&oe=utf-8&in=&cl=2&lm=-1&st=-1&cs=2405501551%2C1872029435&os=1056869731%2C2271147540&simid=3477634566%2C568554732&adpicid=0&lpn=0&ln=1676&fr=&fmq=1638449136165_R&fm=result&ic=&s=undefined&hd=&latest=&copyright=&se=&sme=&tab=0&width=&height=&face=undefined&ist=&jit=&cg=&bdtype=0&oriquery=&objurl=https%3A%2F%2Fgimg2.baidu.com%2Fimage_search%2Fsrc%3Dhttp%3A%2F%2Fqqpublic.qpic.cn%2Fqq_public%2F0%2F0-2953606467-0168C2BA1D90075896C875237663BE2B%2F0%3Ffmt%3Djpg%26size%3D23%26h%3D549%26w%3D900%26ppv%3D1.jpg%26refer%3Dhttp%3A%2F%2Fqqpublic.qpic.cn%26app%3D2002%26size%3Df9999%2C10000%26q%3Da80%26n%3D0%26g%3D0n%26fmt%3Djpeg%3Fsec%3D1641041144%26t%3Da05d408a775676e82bba284c707a2dfa&fromurl=ippr_z2C%24qAzdH3FAzdH3Fh7wtkw5_z%26e3Bqq_z%26e3Bv54AzdH3FfAzdH3Fda8lal8cwat0z8aa%3F6juj6%3Dfrt1j6&gsm=1&rpstart=0&rpnum=0&islist=&querylist=&nojc=undefined&dyTabStr=MCw0LDEsNiw1LDMsNyw4LDIsOQ%3D%3D",
                        "http://www.baidu.com");
        Message message =
                Message.obtain(rongExtension.getConversationIdentifier(), richContentMessage);
        RongIM.getInstance().sendMessage(message, null, null, null);
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {}
}
