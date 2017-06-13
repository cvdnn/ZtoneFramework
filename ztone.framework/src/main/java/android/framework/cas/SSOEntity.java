package android.framework.cas;

import android.framework.entity.FindJNode;
import android.framework.entity.PullEntity;

/**
 * Created by handy on 16-11-3.
 */

public class SSOEntity extends PullEntity {

    @FindJNode
    public String tgt;

    @FindJNode
    public String sso_id;

    @FindJNode
    public String st;

    @Override
    public boolean check() {

        return super.check();
    }
}