/*
 * ResultEntity.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework.entity;

import android.assist.Assert;
import android.assist.TextUtilz;
import android.framework.module.Validator;
import android.io.FileUtils;
import android.json.JNodeArray;
import android.json.JPath;
import android.json.JSONUtils;
import android.log.Log;
import android.os.Parcel;
import android.reflect.ClazzLoader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static android.framework.C.value.charset_encoding;
import static android.json.JSONUtils.optInt;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-8-31
 */
@FindEntity(inject = false)
public abstract class Entity implements Validator, Serializable {
    private static final String TAG = "Entity";

    private static final long serialVersionUID = 1L;

    public <E extends Entity> E parse(File jsonFile, boolean isFake) {
        String jsonText = FileUtils.read(jsonFile, charset_encoding);

        if (isFake) {
            jsonText = TextUtilz.fromFake(jsonText);
        }

        return parse(JSONUtils.from(jsonText));
    }

    public <E extends Entity> E parse(String json) {

        return parse(JSONUtils.from(json));
    }

    public <E extends Entity> E parse(JSONObject jsonData) {

        return Builder.parse(getClass(), (E) this, jsonData);
    }

    public JSONObject format() {

        return new Formatter().clazz(getClass()).entity(this).format();
    }

    @Override
    public boolean check() {

        return true;
    }

    public boolean check(JSONArray jsonArray) {
        return Assert.notEmpty(jsonArray);
    }

    public Entity readFromParcel(Parcel src) {

        return this;
    }

    public void writeToParcel(Parcel dest, int flags) {

    }

    protected void writeString(Parcel dest, String text) {
        if (dest != null) {
            dest.writeString(text == null ? "" : text);
        }
    }

    public int describeContents() {

        return 0;
    }

    public static final class Builder {
        private Class<? extends Entity> clazz;
        private JSONObject json;
        private Object external;

        public <E extends Entity> Builder clazz(Class<E> clazz) {
            this.clazz = clazz;

            return this;
        }

        public Builder json(JSONObject json) {
            this.json = json;

            return this;
        }

        public <O> Builder external(O o) {
            external = o;

            return this;
        }

        public <E extends Entity> E build() {
            E e = (E) build(clazz, external);
            if (e != null && json != null) {
                e.parse(json);
            }

            return e;
        }

        /**
         * 實例化
         */
        @SuppressWarnings({"unchecked", "unused"})
        private static <O> Entity build(@NonNull Class<? extends Entity> clazz, @Nullable O o) {
            Entity e = null;

            if (clazz != null) {
                try {
                    int modifier = clazz.getModifiers();
                    if (o == null || Assert.as(modifier, Modifier.STATIC)) {
                        Constructor<? extends Entity> constructor = clazz.getConstructor();
                        e = constructor != null ? constructor.newInstance() : clazz.newInstance();

                        // 内部类
                    } else if (o != null) {
                        Constructor<? extends Entity>[] constructors = (Constructor<? extends Entity>[]) clazz.getConstructors();
                        if (Assert.notEmpty(constructors) && constructors[0] != null) {

                            e = constructors[0].newInstance(o);
                        }
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "class: " + clazz.getName());
                    Log.e(TAG, t);
                }
            }

            return e;
        }

        private static <E extends Entity> E parse(Class<?> clazz, @NonNull E e, JSONObject json) {
            while (clazz != null && clazz != Object.class) {
                String clazzName = clazz.getSimpleName();
                Field[] fields = clazz.getDeclaredFields();
                if (Assert.notEmpty(fields)) {
                    for (Field field : fields) {
                        if (field != null) {
                            Annotation[] annArray = field.getAnnotations();
                            if (Assert.notEmpty(annArray)) {
                                for (Annotation ann : annArray) {
                                    if (ann instanceof FindJNode) {
                                        findEntityJNode(clazzName, field, (FindJNode) ann, e, json);

                                        break;
                                    } else if (ann instanceof FindJArray) {
                                        findEntityJArray(clazzName, field, (FindJArray) ann, e, json);

                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.v(TAG, "NN: class: " + clazzName + ", fields: null");
                }

                clazz = clazz.getSuperclass();
            }

            return e;
        }

        private static <E extends Entity> void findEntityJNode(String clazzName, Field field, FindJNode jNodeInject, E e, JSONObject json) {
            if (jNodeInject != null) {
                String jpath = jNodeInject.jpath();
                if (Assert.isEmpty(jpath)) {
                    jpath = JPath.SEPARATOR_VALUE + field.getName();
                }

                if (Assert.notEmpty(jpath)) {
                    Class<?> typeClazz = field.getType();
                    if (typeClazz == String.class) {
                        ClazzLoader.setFieldValue(e, field, JSONUtils.optString(json, jpath, jNodeInject.s()));

                    } else if (typeClazz == Integer.TYPE) {
                        ClazzLoader.setFieldValue(e, field, optInt(json, jpath, jNodeInject.i()));

                    } else if (typeClazz == Long.TYPE) {
                        ClazzLoader.setFieldValue(e, field, JSONUtils.optLong(json, jpath, jNodeInject.l()));

                    } else if (typeClazz == Boolean.TYPE) {
                        ClazzLoader.setFieldValue(e, field, JSONUtils.optBoolean(json, jpath, jNodeInject.b()));

                    } else if (typeClazz == Float.TYPE) {
                        ClazzLoader.setFieldValue(e, field, JSONUtils.optFloat(json, jpath, jNodeInject.f()));

                    } else if (typeClazz == Double.TYPE) {
                        ClazzLoader.setFieldValue(e, field, JSONUtils.optDouble(json, jpath, jNodeInject.d()));

                    } else if (Enum.class.isAssignableFrom(typeClazz)) {
                        Object o = null;

                        Method findEnumMethod = ClazzLoader.getSingleMethodByAnnotation(typeClazz, FindJEnum.class);
                        if (findEnumMethod != null) {
                            findEnumMethod.setAccessible(true);

                            try {
                                o = findEnumMethod.invoke(typeClazz, JSONUtils.optInt(json, jpath, jNodeInject.i()));
                            } catch (Exception exc) {
                                Log.d(TAG, exc);
                            }
                        }

                        ClazzLoader.setFieldValue(e, field, o);
                    }
                } else {
                    Log.i(TAG, clazzName + ": " + field.getName() + ": jpath is null");
                }
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static <E extends Entity> void findEntityJArray(String clazzName, Field field, FindJArray jArrayInject, E e, JSONObject json) {
            if (jArrayInject != null) {
                String jpath = jArrayInject.jpath();
                if (Assert.isEmpty(jpath)) {
                    jpath = field.getName();
                }

                if (Assert.notEmpty(jpath)) {
                    JSONArray jsonItems = JSONUtils.getJSONArray(json, jpath);
                    if (Assert.notEmpty(jsonItems)) {
                        Class<? extends Entity> metaClazz = jArrayInject.meta();

                        int size = jsonItems.length();
                        ArrayList dataList = new ArrayList(size);
                        for (int i = 0; i < size; i++) {
                            try {
                                Entity meta = ClazzLoader.newInstance(e, metaClazz);
                                if (meta != null) {
                                    meta.parse(jsonItems.getJSONObject(i));
                                }

                                dataList.add(meta);
                            } catch (Exception exc) {
                                Log.e(TAG, exc);
                            }
                        }

                        ClazzLoader.setFieldValue(e, field, dataList);
                    }
                } else {
                    Log.i(TAG, clazzName + ": " + field.getName() + ": jpath is null");
                }
            }
        }
    }

    public static final class Formatter {
        private final JSONObject mJsonObject = new JSONObject();

        private Class<? extends Entity> clazz;
        private Entity entity;

        public Formatter clazz(Class<? extends Entity> clazz) {
            this.clazz = clazz;

            return this;
        }

        public Formatter entity(Entity entity) {
            this.entity = entity;

            return this;
        }

        @NonNull
        public JSONObject format() {
            try {
                Class<?> tempClazz = clazz != null ? clazz : (entity != null ? entity.getClass() : null);
                while (Assert.isAssignable(Entity.class, tempClazz)) {
                    Field[] jNodeFields = tempClazz.getDeclaredFields();

                    if (Assert.notEmpty(jNodeFields)) {
                        for (Field fld : jNodeFields) {
                            if (fld != null) {
                                if (fld.isAnnotationPresent(FindJNode.class)) {
                                    setJNode(fld, mJsonObject);

                                } else if (fld.isAnnotationPresent(FindJArray.class)) {
                                    setJArray(fld, mJsonObject);
                                }
                            }
                        }
                    }

                    tempClazz = tempClazz.getSuperclass();
                }
            } catch (Exception e) {
                Log.e(TAG, e);
            }

            return mJsonObject;
        }

        private void setJNode(@NonNull Field fld, @NonNull JSONObject jsonObject) {
            FindJNode jNode = fld.getAnnotation(FindJNode.class);
            if (jNode != null) {
                JPath jpath = createJPath(fld.getName(), jNode.jpath());

                setValue(fld, attacheJSONObjectByJNode(jpath, jsonObject), jpath);
            }
        }

        private JSONObject attacheJSONObjectByJNode(JPath jpath, @NonNull JSONObject jsonObject) {
            JSONObject attacheJSON = jsonObject;

            if (jpath != null) {
                // 创建JSON节点
                JNodeArray jnodeArray = jpath.getJNodeArray();
                if (jnodeArray != null && !jnodeArray.isEmpty()) {
                    JSONObject jpathJSON = jsonObject;

                    int size = jnodeArray.length();
                    for (int i = 0; i < size; i++) {
                        String nodeName = jnodeArray.get(i);
                        JSONObject subJSON = jpathJSON.optJSONObject(nodeName);
                        if (subJSON == null) { // 添加新节点
                            subJSON = new JSONObject();

                            try {
                                jpathJSON.put(nodeName, subJSON);
                            } catch (Exception e) {
                                Log.d(TAG, e);
                            }
                        }

                        jpathJSON = subJSON;
                    }

                    attacheJSON = jpathJSON;
                }
            }

            return attacheJSON;
        }

        private void setJArray(@NonNull Field fld, @NonNull JSONObject jsonObject) {
            FindJArray jArrayInject = fld.getAnnotation(FindJArray.class);
            if (jArrayInject != null) {
                try {
                    Object dataObj = fld.get(entity);
                    if (dataObj instanceof List) {
                        List dataList = (List) dataObj;
                        if (Assert.notEmpty(dataList)) {
                            JPath jpath = createJPath(fld.getName(), jArrayInject.jpath());
                            if (jpath != null) {
                                JSONArray attacheArray = attacheJSONArrayByJNode(jpath, jsonObject);
                                if (attacheArray != null) {
                                    for (Object o : dataList) {
                                        if (o instanceof Entity) {
                                            Entity entity = (Entity) o;

                                            attacheArray.put(entity.format());
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, e);
                }

            }
        }

        private JSONArray attacheJSONArrayByJNode(JPath jpath, @NonNull JSONObject jsonObject) {
            JSONArray jsonArray = new JSONArray();

            if (jpath != null) {
                // 创建JSON节点
                JNodeArray jnodeArray = jpath.getJNodeArray();
                if (jnodeArray != null && !jnodeArray.isEmpty()) {
                    JSONObject jpathJSON = jsonObject;

                    String nodeName = null;

                    int size = jnodeArray.length();
                    if (size > 1) {
                        for (int i = 0; i < size - 1; i++) {
                            nodeName = jnodeArray.get(i);

                            JSONObject subJSON = jpathJSON.optJSONObject(nodeName);
                            if (subJSON == null) { // 添加新节点
                                subJSON = new JSONObject();

                                try {
                                    jpathJSON.put(nodeName, subJSON);
                                } catch (Exception e) {
                                    Log.d(TAG, e);
                                }
                            }

                            jpathJSON = subJSON;
                        }

                        // 最后一个作为Array节点的名称
                        nodeName = jnodeArray.get(size - 1);

                    } else {
                        nodeName = jnodeArray.get(0);
                    }

                    if (Assert.notEmpty(nodeName)) {
                        try {
                            jpathJSON.put(nodeName, jsonArray);
                        } catch (Exception e) {
                            Log.d(TAG, e);
                        }
                    }
                }
            }

            return jsonArray;
        }

        private JPath createJPath(@NonNull String nodeName, String strJPath) {

            return JPath.createJPath(Assert.isEmpty(strJPath) ? JPath.SEPARATOR_VALUE + nodeName : strJPath);
        }

        /**
         * 设置值
         *
         * @param fld
         * @param attacheJSON
         * @param jpath
         */
        private void setValue(@NonNull Field fld, @NonNull JSONObject attacheJSON, @NonNull JPath jpath) {
            if (jpath.isJValuePath()) {
                String key = jpath.getKey();

                Class<?> typeClazz = fld.getType();

                try {
                    if (typeClazz == String.class) {
                        attacheJSON.put(key, fld.get(entity));

                    } else if (typeClazz == Integer.TYPE) {
                        attacheJSON.put(key, fld.getInt(entity));

                    } else if (typeClazz == Long.TYPE) {
                        attacheJSON.put(key, fld.getLong(entity));

                    } else if (typeClazz == Boolean.TYPE) {
                        attacheJSON.put(key, fld.getBoolean(entity));

                    } else if (typeClazz == Float.TYPE) {
                        attacheJSON.put(key, fld.getFloat(entity));

                    } else if (typeClazz == Double.TYPE) {
                        attacheJSON.put(key, fld.getDouble(entity));

                    }
                } catch (Exception e) {
                    Log.d(TAG, e);
                }
            }
        }
    }
}
