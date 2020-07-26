package com.perago.techtest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Created by paballo on 2017/08/19.
 */
public class DiffEngineImpl implements DiffEngine {


    public <T extends Serializable> T apply(T original, Diff<?> diff) throws DiffException {

        T t = null;
        try {
            if (Objects.isNull(diff)) {
                throw new DiffException("Diff cannot be null");
            }
            if (Objects.isNull(original)) {
                return null;
            }
            if (Objects.isNull(diff.getHolder())) {
                return null;
            }

            t = (T)BeanUtils.cloneBean(original);
            transform(diff, t, 0);

        } catch (IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return t;
    }

    public <T extends Serializable> Diff<T> calculate(T original, T modified) throws DiffException {

        if (Objects.nonNull(original) && Objects.nonNull(modified)) {
            if (!original.getClass().equals(modified.getClass())) {
                throw new DiffException("Objects not of the same type");
            }
        }
        Diff<T> diff = new Diff<>();
        T t;
        try {
            if (Objects.nonNull(modified) && Objects.isNull(original)) {
                t = (T) BeanUtils.cloneBean(modified);
            } else {
                Class c = original.getClass().getClassLoader().loadClass(original.getClass().getName());
                t = (T) c.newInstance();
            }
            diff.setHolder(t);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        try {
            generateChangeLogs(diff, original, modified, 0);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return diff;
    }

    private <T extends Serializable> void generateChangeLogs(final Diff<T> diff, final T original, final T modified, int depth) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Field[] children;
        if (Objects.nonNull(modified)) {
            children = modified.getClass().getDeclaredFields();
        } else {
            children = original.getClass().getDeclaredFields();
        }
        if (Objects.isNull(original)) {
            diff.addLog(new Diff.ChangeLog(Status.CREATE, modified.getClass().getName(), null, depth, true));
            for (Field child : children) {
                if (java.lang.reflect.Modifier.isStatic(child.getModifiers())) {
                    continue;
                }
                if (child.getType().equals(modified.getClass())) {
                    diff.addLog(new Diff.ChangeLog(Status.CREATE, child.getName(), child.getType().getSimpleName(), depth, false));
                } else {
                    child.setAccessible(true);
                    Object modifiedInstance = child.get(modified);
                    diff.addLog(new Diff.ChangeLog(Status.CREATE, child.getName(), child.getType().getSimpleName(), null, modifiedInstance, depth, false));
                }
                if (child.getType().equals(modified.getClass())) {
                    child.setAccessible(true);
                    T value = (T) child.get(modified);
                    if (value != null) {
                        generateChangeLogs(diff, null, value, depth + 1);
                    }
                }
            }

        } else if (Objects.isNull(modified)) {
            diff.addLog(new Diff.ChangeLog(Status.DELETE, original.getClass().getName(), null, depth, true));
            diff.setHolder(null);
        } else {
            diff.addLog(new Diff.ChangeLog(Status.UPDATE, modified.getClass().getName(), null, depth, true));
            for (Field child : children) {
                if (java.lang.reflect.Modifier.isStatic(child.getModifiers())) {
                    continue;
                }
                boolean isCollection = false;
                //collections are proving somewhat difficult probably due to referencing(pointers)
                if (Objects.nonNull(original) || Objects.nonNull(modified)) {
                    child.setAccessible(true);
                    Object instance = (Objects.nonNull(original)) ? child.get(original) : child.get(modified);
                    isCollection = instance instanceof Collection;
                }
                if ((Objects.isNull(BeanUtils.getProperty(original, child.getName())) &&
                        Objects.isNull(BeanUtils.getProperty(modified, child.getName())))) {
                    continue;
                }
                if (child.getType().equals(original.getClass())) {
                    diff.addLog(new Diff.ChangeLog(Status.UPDATE, child.getName(), child.getType().getSimpleName(), depth, false));
                    child.setAccessible(true);

                    T fieldInOriginal = (T) child.get(original);
                    T fieldInModified = (T) child.get(modified);

                    if (Objects.isNull((fieldInModified)) &&
                            Objects.nonNull(fieldInOriginal)) {
                        diff.addLog(new Diff.ChangeLog(Status.DELETE, child.getName(), child.getType().getSimpleName(), depth, false));
                        continue;
                    }
                    generateChangeLogs(diff, fieldInOriginal, fieldInModified, depth + 1);
                    continue;

                }
                if (Objects.isNull(BeanUtils.getProperty(original, child.getName()))
                        && Objects.nonNull(BeanUtils.getProperty(modified, child.getName()))) {
                    child.setAccessible(true);
                    Object modifiedInstance = child.get(modified);
                    diff.addLog(new Diff.ChangeLog(Status.UPDATE, child.getName(), child.getType().getSimpleName(), null, modifiedInstance, depth, false));

                }
                if (Objects.nonNull(BeanUtils.getProperty(original, child.getName())) &&
                        Objects.nonNull(BeanUtils.getProperty(modified, child.getName()))) {

                    if (!isCollection && (BeanUtils.getProperty(original, child.getName()).equals(BeanUtils.getProperty(modified, child.getName())))) {
                        continue;
                    } else {
                        child.setAccessible(true);
                        Object modifiedInstance = child.get(modified);
                        Object originalInstance = child.get(original);
                        if (isCollection) {
                            if (CollectionUtils.isEqualCollection((Collection) originalInstance, (Collection) modifiedInstance)) {
                                continue;
                            }
                        }
                        diff.addLog(new Diff.ChangeLog(Status.UPDATE, child.getName(), child.getType().getSimpleName(),
                                originalInstance, modifiedInstance, depth, false));
                    }
                }
                if (Objects.isNull(BeanUtils.getProperty(modified, child.getName())) &&
                        Objects.nonNull(BeanUtils.getProperty(original, child.getName()))) {
                    diff.addLog(new Diff.ChangeLog(Status.DELETE, child.getName(), child.getType().getTypeName(), depth, false));
                }

            }

        }
    }

    private static boolean setField(Object targetObject, String fieldName, Object fieldValue) {
        Field field;
        try {
            field = targetObject.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = null;
        }
        Class superClass = targetObject.getClass().getSuperclass();
        while (field == null && superClass != null) {
            try {
                field = superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                superClass = superClass.getSuperclass();
            }
        }
        if (field == null) {
            return false;
        }
        field.setAccessible(true);
        try {
            field.set(targetObject, fieldValue);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private <T extends Serializable> void transform(Diff<?> diff, T copy, int depth) throws InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Objects.requireNonNull(diff);
        Objects.requireNonNull(copy);

        List<Diff.ChangeLog> changeLogs = diff.getChangeLogs();
        int level = depth;
        //in case copy is set to null
        T referenceCopy = (T) BeanUtils.cloneBean(copy);
        for (Diff.ChangeLog changeLog : changeLogs) {
            if (changeLog.getDepth() > depth) {
                level = changeLog.getDepth();
            }
            if (changeLog.getDepth() == depth) {
                switch (changeLog.getStatus()) {
                    case DELETE:
                        if (changeLog.getFieldName().equals(copy.getClass().getSimpleName())) {
                            copy = null;
                            break;
                        } else {
                            setField(copy, changeLog.getFieldName(), changeLog.getValue());
                            break;
                        }
                    case CREATE:
                        if (Objects.isNull(copy)) copy = referenceCopy;
                        setField(copy, changeLog.getFieldName(), changeLog.getValue());
                    case UPDATE:
                        setField(copy, changeLog.getFieldName(), changeLog.getValue());
                        break;
                        default:
                            break;
                }
            }
            if (depth < level) {
                Field[] fields = copy.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().getName().equals(copy.getClass().getName())) {
                        field.setAccessible(true);
                        T object = (T) field.get(copy);
                        if (object == null) {
                            Class c = copy.getClass().getClassLoader().loadClass(copy.getClass().getName());
                            object = (T) c.newInstance();
                            setField(copy, field.getName(), object);
                        }
                        transform(diff, object, depth + 1);
                    }
                }
            }
        }

    }

}


