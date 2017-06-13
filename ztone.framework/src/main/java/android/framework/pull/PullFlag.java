package android.framework.pull;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by handy on 17-1-20.
 */

public class PullFlag implements Parcelable {
    public int flag;

    public PullFlag(int flag) {
        this.flag = flag;
    }

    public PullFlag(Parcel src) {
        readFromParcel(src);
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeInt(flag);
        }
    }

    public void readFromParcel(Parcel src) {
        if (src != null) {
            flag = src.readInt();
        }
    }

    public static final Creator<PullFlag> CREATOR = new Creator<PullFlag>() {

        @Override
        public PullFlag createFromParcel(Parcel source) {

            return new PullFlag(source);
        }

        @Override
        public PullFlag[] newArray(int size) {

            return new PullFlag[size];
        }
    };
}
