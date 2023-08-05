package kr.kj.baram.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class MysqlModule {
    // required params;
    private final String userID;
    private final String userPassword;
    private final String databaseUrl;
    private final String dbName;

    // optional params;
    private final String dbPort;
    private final String charEncoding;

    private final String baseSplitChar;

    // member
    private Connection conn; // DB 커넥션 연결 객체
    private Statement state;

    public Connection getConn() {
        return this.conn;
    }
    public String getBaseSplitChar(){ return this.baseSplitChar; }

    public MysqlModule(MysqlBuilder builder) {
        this.userID = builder.userID;
        this.userPassword = builder.userPassword;
        this.databaseUrl = builder.databaseUrl;
        this.dbName = builder.dbName;

        this.charEncoding = builder.charEncoding;
        this.dbPort = builder.dbPort;

        this.baseSplitChar = builder.baseSplitChar;
    }

    public PreparedStatement getInsertPreparedStats(String insertQuery, List<Object> params) {
        if (conn == null || state == null) return null;
        int parameterIndex = 1;
        PreparedStatement ps;

        try {
            ps = conn.prepareStatement(insertQuery);

            for (Object o : params) {
                if (o instanceof String)
                    ps.setString(parameterIndex, (String) o);
                else if (o instanceof Integer)
                    ps.setInt(parameterIndex, (Integer) o);
                else if (o instanceof Double)
                    ps.setDouble(parameterIndex, (Double) o);
                else if (o instanceof Boolean)
                    ps.setBoolean(parameterIndex, (Boolean) o);
                else
                    return null;

                parameterIndex++;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }

        return ps;
    }

    public String getBaseInsertQuery(String tableName, boolean bReplaceInto) {
        ArrayList<String> keySet = new ArrayList<>();
        StringBuilder resultQuery = new StringBuilder();

        if(conn != null && state != null) {
            ArrayList<String> res = this.runQuery("select * from " + tableName + " limit 1", keySet);

            if(res != null) {
                String fieldNames = String.join(", ", keySet);
                String valueParts = keySet.stream().map(k -> "?").collect(Collectors.joining(", "));

                resultQuery
                        .append((bReplaceInto)? "replace into ": "insert ignore into ")
                        .append(tableName)
                        .append(" (").append(fieldNames).append(") ")
                        .append("values (").append(valueParts).append(")")
                ;
            }
        }

        return resultQuery.toString();
    }

    public String getBaseInsertQuery(String tableName) {
        return getBaseInsertQuery(tableName, false);
    }

    public int executeUpdate(String query) {
        int retVal = 0;

        if(conn != null && state != null && query != null && !query.isEmpty()) {
            try {
                retVal = state.executeUpdate(query);
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }

        return retVal;
    }

    public int executeUpdate(PreparedStatement ps){
        int retVal = 0;

        if(conn != null && state != null && ps != null) {
            try {
                retVal = ps.executeUpdate();
                ps.close();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }

        return retVal;
    }


    public int executeUpdates(List<PreparedStatement> psList) {
        int retVal = 0;

        if(conn != null && state != null && psList != null) {
            retVal = psList.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(ps -> {
                        int rVal = 0;
                        try {
                            rVal = ps.executeUpdate();
                            ps.close();
                        } catch (SQLException sqlException){
                            sqlException.printStackTrace();
                        }
                        return rVal;
                    })
                    .sum();
        }

        return retVal;
    }

    /**
     * Execute query and get result split by given splitChar
     * Data fields are saved in given KeySet
     *
     * @param query        query
     * @param keySet       field values
     * @param splitChar    delimiter
     * @param enclosedChar enclosed character
     * @return result array list of string, or null if some error occurs
     */
    public ArrayList<String> runQuery(String query, ArrayList<String> keySet, String splitChar, String enclosedChar) {
        if (conn == null || state == null) return null;
        ArrayList<String> resultList = null;

        try {
            resultList = new ArrayList<String>();

            ResultSet rs = state.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            if (keySet == null)
                keySet = new ArrayList<>();

            for (int i = 1; i <= columnCount; i++)
                keySet.add(rsmd.getColumnName(i));

            while (rs.next()) {
                ArrayList<String> items = new ArrayList<String>();

                for (int i = 0; i < columnCount; i++) {
                    items.add(
                            new StringBuilder(enclosedChar)
                                    .append(rs.getString(keySet.get(i)))
                                    .append(enclosedChar)
                                    .toString()
                    );
                }

                resultList.add(String.join(splitChar, items));
            }

            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return resultList;
    }

    public ArrayList<String> runQuery(String query) {
        return runQuery(query, null, baseSplitChar, "");
    }

    public ArrayList<String> runQuery(String query, ArrayList<String> keySet) {
        return runQuery(query, keySet, baseSplitChar, "");
    }

    public void connect() {
        Properties props = new Properties();

        props.put("user", userID);
        props.put("password", userPassword);

        if (charEncoding != null && !charEncoding.isEmpty())
            props.put("characterEncoding", charEncoding);

        String dbUrl = String.format("jdbc:mysql://%s:%s/%s", databaseUrl, dbPort, dbName);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(dbUrl, props);
            state = conn.createStatement();
        } catch (ClassNotFoundException cfe) {
            cfe.printStackTrace();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void close() {
        try {
            if (conn != null)
                conn.close();
            if (state != null)
                state.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }


    public static class MysqlBuilder {
        // required params;
        private String userID;
        private String userPassword;
        private String databaseUrl;
        private String dbName;

        // optional params;
        private String dbPort = "3306";
        private String charEncoding;
        private String baseSplitChar = ",";

        public MysqlBuilder(String userID, String userPassword,
                            String databaseUrl, String dbName) {
            this.userID = userID;
            this.userPassword = userPassword;
            this.databaseUrl = databaseUrl;
            this.dbName = dbName;
        }

        public MysqlBuilder setDatabasePort(String dbPort) {
            this.dbPort = dbPort;
            return this;
        }

        public MysqlBuilder setCharEncoding(String charEncoding) {
            this.charEncoding = charEncoding;
            return this;
        }

        public MysqlBuilder setBaseSplitChar(String baseSplitChar) {
            this.baseSplitChar = baseSplitChar;
            return this;
        }

        public MysqlModule build() {
            return new MysqlModule(this);
        }
    }
}
