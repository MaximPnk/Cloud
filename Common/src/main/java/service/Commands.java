package service;

public enum Commands {
    UPLOAD ((byte) 10),
    DOWNLOAD ((byte) 11),
    MKDIR ((byte) 12),
    TOUCH ((byte) 13),
    REMOVE ((byte) 14),
    GET((byte) 15),
    CD ((byte) 16),
    LOG ((byte) 17),
    UPLOAD_COMPLETED ((byte) 18),
    DOWNLOAD_COMPLETED ((byte) 19),
    AUTH ((byte) 20),
    REG ((byte) 21);

    private final byte bt;

    Commands(byte bt) {
        this.bt = bt;
    }

    public static Commands getCommand(byte bt) {
        if (bt == UPLOAD.bt) {
            return UPLOAD;
        } else if (bt == DOWNLOAD.bt) {
            return DOWNLOAD;
        } else if (bt == MKDIR.bt) {
            return MKDIR;
        } else if (bt == TOUCH.bt) {
            return TOUCH;
        } else if (bt == REMOVE.bt) {
            return REMOVE;
        } else if (bt == GET.bt) {
            return GET;
        } else if (bt == CD.bt) {
            return CD;
        } else if (bt == LOG.bt) {
            return LOG;
        } else if (bt == UPLOAD_COMPLETED.bt) {
            return UPLOAD_COMPLETED;
        } else if (bt == DOWNLOAD_COMPLETED.bt) {
            return DOWNLOAD_COMPLETED;
        } else if (bt == AUTH.bt) {
            return AUTH;
        } else {
            return REG;
        }
    }

    public byte getBt() {
        return bt;
    }
}
