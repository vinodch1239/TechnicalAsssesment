package com.perago.techtest;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * The object representing a diff.
 * Implement this class as you see fit. 
 *
 */
public class Diff<T extends Serializable> {

   private T holder;
   private List<ChangeLog> changeLogs = new LinkedList<ChangeLog>();

    public static class ChangeLog implements Serializable{
        private Status status;
        private String fieldName;
        private Object value;
        private boolean parent;
        private int depth;
        private String type ="";
        private Object oldValue;

        public ChangeLog(Status status,  String fieldName, String type,Object oldValue, Object value,int depth, boolean parent){
            this.status=status;
            this.fieldName = getSimpleName(fieldName);
            this.parent=parent;
            this.depth =depth;
            this.value =value;
            this.type=type;
            this.oldValue=oldValue;


        }

        public ChangeLog(Status status,  String fieldName, String type, int depth ,  boolean parent){
            this.status=status;
            this.fieldName = getSimpleName(fieldName);
            this.parent=parent;
            this.depth =depth;
            this.type=type;


        }

        public int getDepth() {
            return depth;
        }

        private static String getSimpleName(String fieldName){
            Objects.requireNonNull(fieldName);
            return fieldName.substring(fieldName.lastIndexOf(".")+1, fieldName.length());
        }

        public String getType() {
            return type;
        }

        public Status getStatus() {
            return status;
        }

        public boolean isParent() {
            return parent;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Object getValue() {
            return value;
        }


        public Object getOldValue() {
            return oldValue;
        }

        @Override
        public String toString() {
            return "ChangeLog{" +
                    "status=" + status +
                    ", fieldName='" + fieldName + '\'' +
                    ", parent=" + parent +
                    '}';
        }
    }



    public void addLog(ChangeLog changeLog){
        changeLogs.add(changeLog);
    }

    public void setHolder(T holder){
        this.holder = holder;
    }

   public T getHolder() {
       return holder;
   }

    public List<ChangeLog> getChangeLogs() {
        return changeLogs;
    }

    @Override
    public String toString() {
        return "Diff{" +
                "holder=" + holder +
                ", changeLogs=" + changeLogs +
                '}';
    }
}
