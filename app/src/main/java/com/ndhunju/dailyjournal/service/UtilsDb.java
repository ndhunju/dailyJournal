package com.ndhunju.dailyjournal.service;

/**
 * Created by dhunju on 9/26/2015.
 */
public class UtilsDb {

    public static String formatInsertSt(String newTable, String newCols, String oldTable, String oldCols){
        String sqlTrans = String.format("INSERT INTO %s (%s) SELECT %s FROM %S",
                newTable, newCols, oldCols, oldTable);
        return  sqlTrans;
    }

    public static String getColumns(String[] columns){
        StringBuilder builder = new StringBuilder();
        String delimiter = ",";
        for(int i = 0; i < columns.length -1 ; i++)
            builder.append(columns[i]+delimiter);
        builder.append(columns[columns.length-1]);
        return builder.toString();
    }

    public static String getRenameTableSt(String tableName, String prefix){
        return "ALTER TABLE " + tableName + " RENAME  TO " + prefix + tableName;
    }

    public static String getDropTableSt(String tableName, String prefix){
        return "DROP TABLE " + prefix + tableName;
    }
}
