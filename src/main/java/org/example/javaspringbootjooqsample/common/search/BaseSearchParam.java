package org.example.javaspringbootjooqsample.common.search;

import io.micrometer.common.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Objects;

public class BaseSearchParam {
    public boolean isNotEmpty(String... fieldNames) {
        try {
            boolean result = true;

            for (String fieldName : fieldNames) {
                Field field = this.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                Class<?> clazz = field.getType();

                if (clazz == String.class) {
                    String val = (String) field.get(this);
                    result = result && StringUtils.isNotEmpty(val);
                } else {
                    Object obj = field.get(this);

                    result = result && !Objects.isNull(obj);
                }
            }

            return result;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    public boolean isEmpty(String... fieldNames) {
        return !this.isNotEmpty(fieldNames);
    }

    public boolean isNotEqualsString(String... fieldNames) {
        try {
            boolean result = true;

            for (String fieldName : fieldNames) {
                Field field = this.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                Class<?> clazz = field.getType();

                if (clazz == String.class) {
                    String val = (String) field.get(this);
                    result = result && StringUtils.isNotEmpty(val);
                }
            }

            return result;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    public boolean isEqualsString(String... fieldNames) {
        return !this.isNotEqualsString(fieldNames);
    }

    public String appendLike(String fieldNm) {
        try {
            Field field = this.getClass().getDeclaredField(fieldNm);
            field.setAccessible(true);
            String val = (String) field.get(this);
            val = StringUtils.isNotEmpty(val) ? val.toLowerCase() : "";

            return val + "%";
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return "";
        }
    }

    public String appendLikeNoLowerCase(String fieldNm) {
        try {
            Field field = this.getClass().getDeclaredField(fieldNm);
            field.setAccessible(true);
            String val = (String) field.get(this);
            val = StringUtils.isNotEmpty(val) ? val : "";

            return val + "%";
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return "";
        }
    }
}
