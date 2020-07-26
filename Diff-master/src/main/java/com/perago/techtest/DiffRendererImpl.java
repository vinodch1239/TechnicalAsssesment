package com.perago.techtest;

import java.util.List;
import java.util.Objects;

/**
 * Created by paballo on 2017/08/20.
 */
public class DiffRendererImpl implements DiffRenderer {



    public String render(Diff<?> diff) throws DiffException {

      Objects.requireNonNull(diff);

        Class aClass;
        aClass = (Objects.nonNull(diff.getHolder()))?diff.getHolder().getClass():null;
        List<Diff.ChangeLog> changeLogs = diff.getChangeLogs();
        StringBuilder builder = new StringBuilder();
        for (Diff.ChangeLog changeLog : changeLogs) {
            indent(builder, changeLog.getDepth());
            if(Objects.isNull(aClass)){
                builder.append(changeLog.getStatus()).append(":").append(changeLog.getFieldName());
            }
            else if (changeLog.getFieldName().equals(aClass.getSimpleName()) && changeLog.isParent()) {
                builder.append(changeLog.getStatus()).append(":").append(aClass.getSimpleName());
                builder.append(System.getProperty("line.separator"));
            } else {
                try {
                    if(changeLog.getType().equals(aClass.getSimpleName()) && changeLog.isParent()){
                        builder.append(changeLog.getStatus())
                                .append(":")
                                .append((changeLog.getFieldName()));
                        builder.append(System.getProperty("line.separator"));

                    }
                    else if(changeLog.getStatus().equals(Status.UPDATE)){
                        builder.append(changeLog.getStatus())
                                .append(":")
                                .append((changeLog.getFieldName()))
                                .append(" from ")
                                .append(changeLog.getOldValue())
                                .append(" to ")
                                .append(changeLog.getValue());
                        builder.append(System.getProperty("line.separator"));
                    }
                else if (changeLog.getStatus().equals(Status.DELETE) ){
                        builder.append(changeLog.getStatus())
                                .append(":")
                                .append((changeLog.getFieldName()));
                        builder.append(System.getProperty("line.separator"));
                }else {
                        builder.append(changeLog.getStatus())
                                .append(":")
                                .append((changeLog.getFieldName()))
                                .append(" as ")
                                .append(changeLog.getValue());
                        builder.append(System.getProperty("line.separator"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        return builder.toString();
    }

    private static void indent(StringBuilder builder, int depth) {
        for (int j = 0; j < depth; j++) {
            builder.append(" ");
        }
    }


}
