package top.wonderheng.common;

/**
 * @BelongsProject: photo-storage-system-boot
 * @BelongsPackage: top.wonderheng.common
 * @Author: WonderHeng
 * @CreateTime: 2018-12-27 20:06
 */
public class Binary {

    private final byte[] data;

    public Binary(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }


    public long length() {
        return data == null ? 0 : data.length;
    }
}
