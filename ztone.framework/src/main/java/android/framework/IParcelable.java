package android.framework;


import android.assist.Assert;
import android.os.Parcel;
import android.os.Parcelable;

public abstract class IParcelable implements Parcelable {

	@Override
	public abstract int describeContents();

	@Override
	public abstract void writeToParcel(Parcel dest, int flags);

	public final void writeString(Parcel dest, String val) {
		if (dest != null) {
			dest.writeString(Assert.notEmpty(val) ? val : "");
		}
	}
}
