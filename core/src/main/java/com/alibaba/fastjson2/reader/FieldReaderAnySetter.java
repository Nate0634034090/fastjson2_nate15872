package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

class FieldReaderAnySetter<T>
        extends FieldReaderObject<T> {
    FieldReaderAnySetter(
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            JSONSchema schema,
            Method method) {
        super("$$any$$", fieldType, fieldClass, ordinal, features, format, null, null, schema, method, null, null);
    }

    @Override
    public ObjectReader getItemObjectReader(JSONReader jsonReader) {
        if (itemReader != null) {
            return itemReader;
        }
        return itemReader = jsonReader.getObjectReader(fieldType);
    }

    @Override
    public void accept(T object, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processExtra(JSONReader jsonReader, Object object) {
        String name = jsonReader.getFieldName();

        ObjectReader itemObjectReader = getItemObjectReader(jsonReader);
        Object value = itemObjectReader.readObject(jsonReader, fieldType, fieldName, 0);

        try {
            method.invoke(object, name, value);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("any set error"), e);
        }
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (initReader == null) {
            initReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }

        Object value;
        if (jsonReader.isJSONB()) {
            value = initReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        } else {
            value = initReader.readObject(jsonReader, fieldType, fieldName, features);
        }

        accept(object, value);
    }
}
