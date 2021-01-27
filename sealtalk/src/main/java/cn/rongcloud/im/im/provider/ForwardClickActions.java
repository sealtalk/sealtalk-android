package cn.rongcloud.im.im.provider;

import io.rong.imlib.location.message.RealTimeLocationStartMessage;

/**
 * Created by zwfang on 2018/4/2.
 */

//todo
//public class ForwardClickActions implements IClickActions {
//
//    @Override
//    public Drawable obtainDrawable(Context context) {
//        return context.getResources().getDrawable(R.drawable.seal_selector_multi_forward);
//    }
//
//    @Override
//    public void onClick(Fragment curFragment) {
//        ConversationFragment fragment = (ConversationFragment) curFragment;
//        List<Message> messages = fragment.getCheckedMessages();
//        List<Message> forwardMessagesList = new ArrayList<>();
//        boolean allMessagesAllowForward = true;
//        for (Message message : messages) {
//            if (!allowForward(message)) {
//                allMessagesAllowForward = false;
//                break;
//            }
//            if (message.getContent() instanceof PublicServiceRichContentMessage) {
//                //公众号消息需要转为图文消息转发
//                if (((PublicServiceRichContentMessage) message.getContent()).getMessage() != null) {
//                    RichContentItem richContentItem = ((PublicServiceRichContentMessage) message.getContent()).getMessage();
//                    if (richContentItem != null)
//                        forwardMessagesList.add(getRichContentMessage(richContentItem, message));
//                }
//            } else if (message.getContent() instanceof PublicServiceMultiRichContentMessage) {
//                PublicServiceMultiRichContentMessage multiRichContentMessage = (PublicServiceMultiRichContentMessage) message.getContent();
//                if (multiRichContentMessage != null) {
//                    ArrayList<RichContentItem> richContentItemsList = multiRichContentMessage.getMessages();
//                    if (richContentItemsList != null) {
//                        for (RichContentItem richContentItem : richContentItemsList) {
//                            if (richContentItem != null)
//                                forwardMessagesList.add(getRichContentMessage(richContentItem, message));
//                        }
//                    }
//                }
//            } else {
//                forwardMessagesList.add(message);
//            }
//        }
//
//        if (forwardMessagesList.size() > 0 && allMessagesAllowForward) {
//            Intent intent = new Intent(curFragment.getActivity(), ForwardActivity.class);
//            intent.putParcelableArrayListExtra(IntentExtra.FORWARD_MESSAGE_LIST, (ArrayList<? extends Parcelable>) forwardMessagesList);
//            curFragment.getActivity().startActivity(intent);
//            fragment.resetMoreActionState();
//        } else if (!allMessagesAllowForward) {
//            new AlertDialog.Builder(curFragment.getActivity())
//                    .setTitle(R.string.seal_not_support_forward_pic)
//
//                    .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    }).show();
//        }
//    }
//
//    private Message getRichContentMessage(RichContentItem richContentItem, Message message) {
//        RichContentMessage richContentMessage = RichContentMessage.obtain(richContentItem.getTitle(), richContentItem.getDigest(),
//                richContentItem.getImageUrl(), richContentItem.getUrl());
//        Message forwardMessage = Message.obtain(message.getTargetId(), message.getConversationType(), richContentMessage);
//        return forwardMessage;
//    }
//
//    //不允许转发的消息类型：VoiceMessage(语音),JrmfRedPacketMessage（红包），JrmfRedPacketOpenedMessage(红包领取)
//    private boolean allowForward(Message message) {
//        if (message != null) {
//            MessageContent messageContent = message.getContent();
//            if (messageContent != null) {
//                if (messageContent instanceof VoiceMessage ||
////                        message.getObjectName().equals(RedpacketModule.jrmfOpenMessage) ||
////                        message.getObjectName().equals(RedpacketModule.jrmfMessage) ||
//                        messageContent instanceof RealTimeLocationStartMessage ||
//                        message.getSentStatus() == Message.SentStatus.FAILED ||
//                        message.getSentStatus() == Message.SentStatus.CANCELED) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//}
