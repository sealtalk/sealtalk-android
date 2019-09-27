package cn.rongcloud.im.model;

/**
 * 获取接收戳一下消息状态结果
 */
public class GetPokeResult {
    /**
     * 接收戳一下消息状态:
     * 0 不允许
     * 1 允许
     */
    private int pokeStatus;

    public int getPokeStatus() {
        return pokeStatus;
    }

    public void setPokeStatus(int pokeStatus) {
        this.pokeStatus = pokeStatus;
    }

    /**
     * 是否接受戳一下消息
     *
     * @return
     */
    public boolean isReceivePokeMessage() {
        return pokeStatus == 1;
    }
}
