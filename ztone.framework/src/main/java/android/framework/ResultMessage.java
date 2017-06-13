package android.framework;

public class ResultMessage {
    public static final int RST_FAIL = 0;
    public static final int RST_SUCCESS = 1;

    public int result;
    public String message;

    public Object tag;

    protected ResultMessage(int result, String message) {
        this.result = result;
        this.message = message;
    }

    public static ResultMessage create(int result) {

        return new ResultMessage(result, "");
    }

    public static ResultMessage create(int result, String message) {

        return new ResultMessage(result, message);
    }

    public static ResultMessage create(ResultMessage result) {
        int r = RST_FAIL;
        String m = "";

        if (result != null) {
            r = result.result;
            m = result.message;
        }

        return new ResultMessage(r, m);
    }
}
