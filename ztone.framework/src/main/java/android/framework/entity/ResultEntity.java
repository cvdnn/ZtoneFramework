package android.framework.entity;

import android.os.Parcel;

import org.json.JSONObject;

@FindEntity(inject = false)
public final class ResultEntity extends PullEntity {
    private static final long serialVersionUID = 1L;

    public ResultEntity set(PullEntity entity) {
        if (entity != null) {
            result = entity.result;
            message = entity.message;
        }

        return this;
    }

    @Override
    public ResultEntity parse(JSONObject jsonData) {
        super.parse(jsonData);

        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

    }

    public ResultEntity readFromParcel(Parcel src) {
        super.readFromParcel(src);

        return this;
    }
}
