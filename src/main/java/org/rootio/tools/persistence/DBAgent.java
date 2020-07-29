package org.rootio.tools.persistence;


import org.rootio.configuration.Configuration;

import java.sql.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBAgent {

    private static String databaseUrl = Configuration.getProperty("database_url");
    private static Semaphore semaphore = new Semaphore(1);


    /**
     * Gets a database connection to the specified database
     *
     * @return Database connection to the specified database
     */
    private static synchronized Connection getDBConnection(String databaseUrl) throws SQLException {
        try {
            semaphore.acquire();
            return DriverManager.getConnection(databaseUrl);
        } catch (SQLException ex) {
            throw (ex);
        } catch (InterruptedException e) {
            return null;
        } finally {
            semaphore.release();
        }
    }

    /**
     * Retrieves data from a table in the DB according to specified criteria
     * @param tableName the name of the table from which records are being retrieved
     * @param columns a List of the columns to be returned in the result set
     * @param filterColumns the columns whose values are to be used to filter the result set
     * @param selectionArgs arguments for the filter columns in the SQL query
     * @param groupBy the group by clause of the query in the format "field1, field2"
     * @param having the having clause of the query in the format "field1, field2"
     * @param orderBy the order by clause of the query in the format "field1, field2"
     * @param limit the limit clause of the SQl query in the format "skip, count"
     * @return List of Object Lists, each nested list representing a row
     * @throws SQLException
     */
    public static List<List<Object>> getData(String tableName, List<String> columns, List<String> filterColumns, List<String> selectionArgs, List<String> groupBy, String having, List<String> orderBy, String sortDirection, String limit) throws SQLException {
        return getData(generateSelectQuery(tableName, columns, filterColumns, groupBy, having, orderBy, sortDirection, limit), selectionArgs);
    }

    /**
     * Generates an SQL query to be used to select records from a table in the DB
     * @param columns List of columns to be returned in the resultset
     * @param filterColumns Columns that will be used to filter the results
     * @param groupBy the group by clause of the query in the format "field1, field2"
     * @param having the having clause of the query in the format "field1, field2"
     * @param orderBy the order by clause of the query in the format "field1, field2"
     * @param limit the limit clause of the SQl query in the format "skip, count"
     * @return SQl query to be used to fetch records from the DB
     */
    private static String generateSelectQuery(String tableName, List<String> columns, List<String> filterColumns, List<String> groupBy, String having, List<String> orderBy, String sortDirection, String limit) {
        StringBuffer query = new StringBuffer();
        query.append("Select ");
        query.append(String.join(",", columns));
        query.append(" from "+tableName);
        if (filterColumns.size() > 0) {
            query.append(" where ");
            query.append(String.join(" = ? and ", filterColumns));
            query.append(" = ? ");
        }
        if (orderBy != null) {
            query.append(" order by");
            query.append(String.join(", ", orderBy));
            query.append(sortDirection);
        }
        if (groupBy != null) {
            query.append(" group by ");
            query.append(String.join(", ", groupBy));
        }
        if (having != null) {
            query.append(" having ");
            query.append(having);
        }
        if (limit != null) {
            query.append(" limit ");
            query.append(limit);
        }
        return query.toString();
    }

    /**
     * Generates an SQL query to be used to insert records into a table in the DB
     * @param tableName the name of the table into which records are being inserted
     * @param values a hashmap whose keys represent the columns and values represent the values for respective columns
     * @return SQL query to be used to insert records into the table
     */
    private static String generateInsertQuery(String tableName, HashMap<String, Object> values) {
        StringBuffer query = new StringBuffer();
        query.append(" Insert into " + tableName);
        query.append(" (" + String.join(",", values.keySet()) + ") ");
        query.append(" values (" + String.join(",", values.values().stream().map(v -> "?").collect(Collectors.toList())) + ")");
        return query.toString();
    }

    /**
     * generates an SQL query that is used to delete records from a table
     * @param tableName the table from which to delete records
     * @param whereClause the where clause of the delete query in the form "field1 = ? and field2 = ?"
     * @return Sql query to be used to delete from the table
     */
    private static String generateDeleteQuery(String tableName, String whereClause) {
        StringBuffer query = new StringBuffer();
        query.append(" delete from " + tableName);
        query.append(" where " + whereClause);
        return query.toString();
    }

    /**
     * generates an SQL query used to update records in a specified table
     * @param tableName the name of the table in which to update records
     * @param updateClause the update clause in the form "field1 = ? and field2 = ?"
     * @param whereClause the where clause to identify the records marked for update in the form "field1= ? and field2 = ?"
     * @return Sql query that is used to update records in the table
     */
    private static String generateUpdateQuery(String tableName, String updateClause, String whereClause) {
        StringBuffer query = new StringBuffer();
        query.append(" update " + tableName + " set " + updateClause + " where " + whereClause);
        return query.toString();
    }


    /**
     * Retrieves records from  the DB according to specified criteria
     * @param rawQuery the SQL query specifying the records to fetch
     * @param selectionArgs arguments to be passed to the SQL query parameters
     * @return List of Lists of objects, each nested list representing a row
     * @throws SQLException
     */
    public static List<List<Object>> getData(String rawQuery, List<String> selectionArgs) throws SQLException {
        try (Connection con = getDBConnection(databaseUrl)) {
            PreparedStatement query = con.prepareStatement(rawQuery);
            for (int i = 0; i < selectionArgs.size(); i++) {
                query.setObject(i + 1, selectionArgs.get(i));
            }

            ResultSet res = query.executeQuery();
            List<List<Object>> data = new ArrayList();
            while (res.next()) {
                List<Object> row = new ArrayList();
                int i = 0;
                while (true) {
                    try {
                        row.add(res.getObject(i));
                        i++;
                    } catch (SQLException ex) {
                        break;
                    }
                }
                data.add(row);
            }
            return data;
        } catch (SQLException ex) {
            throw ex;
        } finally {
            semaphore.release();
        }
    }

    /**
     * Saves records to a table in the DB
     * @param tableName the name of the table to which a record is being persisted
     * @param data a hashmap whose keys are column names and values are column values
     * @return number of rows affected by the Sql transaction
     * @throws SQLException
     */
    public static synchronized long saveData(String tableName, HashMap<String, Object> data) throws SQLException {
        try (Connection con = getDBConnection(databaseUrl)) {
            PreparedStatement query = con.prepareStatement(generateInsertQuery(tableName, data));
            final AtomicInteger i = new AtomicInteger(0);
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                query.setObject(i.incrementAndGet(), v);
            }
            return query.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger("org.rootio", ex.getMessage() == null ? "Null pointer exception(DBAgent.saveData)" : ex.getMessage());
            throw ex;
        } finally {
            semaphore.release();
        }
    }

    /**
     * deletes a record from a table in the DB
     * @param tableName the name of the table from which a record is to be deleted
     * @param whereClause where clause specifying the rows to be considered for deletion, in form "field1=? and field2=?"
     * @param whereArgs List of arguments to fed into the SQL query matching its parameters
     * @return number of records affected by the delete operation
     * @throws SQLException
     */
    public static synchronized long deleteRecords(String tableName, String whereClause, List<String> whereArgs) throws SQLException {
        try (Connection con = getDBConnection(databaseUrl)) {
            PreparedStatement query = con.prepareStatement(generateDeleteQuery(tableName, whereClause));
            final AtomicInteger i = new AtomicInteger(0);
            for (String arg : whereArgs) {
                query.setObject(i.incrementAndGet(), arg);
            }
            return query.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger("org.rootio", ex.getMessage() == null ? "Null pointer exception(DBAgent.saveData)" : ex.getMessage());
            throw ex;
        } finally {
            semaphore.release();
        }
    }


    /**
     * Updates records in the specified table in the DB
     * @param tableName the name of the table in which records are to be updated
     * @param updateClause clause specifying columns whose data is to be updated in form "field1=? and field2=?"
     * @param updateArgs List of string arguments to be fed into parameters in the SQL query
     * @param whereClause where clause of the SQL query in the form "field1=? and field2=?"
     * @param whereArgs args to be fed into the parameters of the SQl query for the where clause
     * @return number of records affected by the update operation
     * @throws SQLException
     */
    public static synchronized long updateRecords(String tableName, String updateClause, List<String> updateArgs, String whereClause, List<String> whereArgs) throws SQLException {
        try (Connection con = getDBConnection(databaseUrl)) {
            PreparedStatement query = con.prepareStatement(generateUpdateQuery(tableName, updateClause, whereClause));
            final AtomicInteger i = new AtomicInteger(0);
            for (String arg : Stream.concat(updateArgs.stream(), whereArgs.stream()).collect(Collectors.toList())) {
                query.setObject(i.incrementAndGet(), arg);
            }
            return query.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger("org.rootio", ex.getMessage() == null ? "Null pointer exception(DBAgent.saveData)" : ex.getMessage());
            throw ex;
        } finally {
            semaphore.release();
        }
    }


}
