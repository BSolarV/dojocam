package com.pinneapple.dojocam_app.objets;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class DataHolder {
    Map<String, WeakReference<Object>> data = new HashMap<String, WeakReference<Object>>();

    void save(String id, Object object) {
        data.put(id, new WeakReference<Object>(object));
    }

    Object retrieve(String id) {
        WeakReference<Object> objectWeakReference = data.get(id);
        assert objectWeakReference != null;
        return objectWeakReference.get();
    }
}
