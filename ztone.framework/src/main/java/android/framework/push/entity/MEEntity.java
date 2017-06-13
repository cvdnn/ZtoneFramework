package android.framework.push.entity;

import android.framework.entity.Entity;
import android.os.Parcel;
import android.os.Parcelable;

public abstract class MEEntity extends Entity implements Parcelable {
	private static final long serialVersionUID = 1L;
	
	@Override
	public MEEntity readFromParcel(Parcel src) {
		super.readFromParcel(src);

		return this;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

	}
}
