package android.framework.pull;

import android.framework.entity.PullEntity;

/**
 * pulled数据过滤器
 *
 * @author handy
 */
public interface PulledFilter {

    boolean accept(PullEntity entity);
}
