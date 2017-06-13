package android.framework.entity;

import android.assist.Assert;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

@FindEntity(inject = false)
public class VersionEntity extends PullEntity implements Parcelable {
	private static final long serialVersionUID = 1L;

	/** 更新标记，例如子功能模块的标识 */
	@FindJNode
	public String model;

	/** 版本号 */
	@FindJNode
	public int verCode;

	/** 版本号名称 */
	@FindJNode
	public String verName;

	/** 描述 */
	@FindJNode
	public String description;

	/** 更新下载到url */
	@FindJNode
	public String url;

	@FindJNode
	public String md5;

	@FindJNode
	public long size;

	/** 标记，备用 */
	@FindJNode
	public int flag;

	public boolean notEmpty() {

		return verCode > 0 && Assert.notEmpty(verName) && Assert.notEmpty(url);
	}

	public boolean needToUpdate(int appVersionCode) {

		return result == VERSION_ERROR || verCode > appVersionCode;
	}

	@Override
	public VersionEntity parse(JSONObject jsonData) {
		super.parse(jsonData);

		return this;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		writeString(dest, model);
		dest.writeInt(verCode);
		writeString(dest, verName);
		writeString(dest, description);
		writeString(dest, url);
		writeString(dest, md5);
		dest.writeLong(size);
		dest.writeInt(flag);
	}

	@Override
	public VersionEntity readFromParcel(Parcel src) {
		super.readFromParcel(src);

		model = src.readString();
		verCode = src.readInt();
		verName = src.readString();
		description = src.readString();
		url = src.readString();
		md5 = src.readString();
		size = src.readLong();
		flag = src.readInt();

		return this;
	}

	public static final Parcelable.Creator<VersionEntity> CREATOR = new Parcelable.Creator<VersionEntity>() {

		@Override
		public VersionEntity createFromParcel(Parcel in) {

			return new VersionEntity().readFromParcel(in);
		}

		@Override
		public VersionEntity[] newArray(int size) {
			return new VersionEntity[size];
		}
	};
}
