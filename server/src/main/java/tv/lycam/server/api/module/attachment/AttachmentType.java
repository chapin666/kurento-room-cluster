package tv.lycam.server.api.module.attachment;

/**
 * Created by lycamandroid on 2017/6/26.
 */
public enum AttachmentType {

    Word(1, "Word"), PDF(2, "Pdf");

    private int code;
    private String value;
    AttachmentType(int code, String value) {
        this.code = code;
        this.value = value;
    }


    public int getCode() {
        return this.code;
    }

    public static AttachmentType getValueByCode(int code) {
        for (AttachmentType a : values()) {
            if (a.getCode() == code) {
                return a;
            }
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }


    @Override
    public String toString() {
        return String.valueOf(this.code);
    }
}
