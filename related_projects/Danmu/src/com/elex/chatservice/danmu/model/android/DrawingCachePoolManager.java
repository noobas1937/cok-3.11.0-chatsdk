
package com.elex.chatservice.danmu.model.android;

import com.elex.chatservice.danmu.model.objectpool.PoolableManager;

public class DrawingCachePoolManager implements PoolableManager<DrawingCache> {

    @Override
    public DrawingCache newInstance() {
        return null;
    }

    @Override
    public void onAcquired(DrawingCache element) {

    }

    @Override
    public void onReleased(DrawingCache element) {

    }

}
