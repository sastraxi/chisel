package com.lyricfind.dpgscript;

import com.lyricfind.dpgscript.annotations.Required;
import com.lyricfind.dpgscript.annotations.Unique;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple JDBC process a la BigScript.
 *
 * Commands are added from files or one-by-one, statements always separated by a semicolon then a newline
 */
public abstract class JDBCProcess extends BaseProcess {

    public String jdbcURI = null;
    public String SQL_SEPARATOR = ";\n";
    protected void setup(@Unique String jdbcURI, @Required String SQL_SEPARATOR)
            throws Exception
    {
        this.jdbcURI = jdbcURI;
        this.SQL_SEPARATOR = SQL_SEPARATOR;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds a file using the
     */
    protected final void addResource(String path) throws IOException
    {
        addFile(this.getClass().getClassLoader().getResourceAsStream(path));
    }

    /**
     * Adds SQL statements from the given InputStream, separated by SQL_SEPARATOR.
     */
    protected final void addFile(InputStream s) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(s));
        StringBuilder sql = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
        {
            int pos = line.indexOf(SQL_SEPARATOR);
            if (pos == -1) {
                sql.append(line);
            } else {
                sql.append(line.substring(0, pos));
                addStatement(sql.toString().trim());
                sql.setLength(0);
                sql.append(line.substring(pos + SQL_SEPARATOR.length()));
            }
        }

        String finalSQL = sql.toString().trim();
        if (finalSQL.length() > 0) {
            addStatement(finalSQL);
        }
    }

    private List<String> statements = new ArrayList<String>();
    protected final void addStatement(String statement)
    {
        statements.add(statement);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int currentStatement = 0;

    @Override
    public void doStart(Object restartPoint) throws Exception
    {
        if (restartPoint != null) {
            currentStatement = (Integer) restartPoint;
        }

        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        conn.setAutoCommit(true);
        for (; currentStatement < statements.size(); ++currentStatement)
        {
            if (Thread.interrupted()) throw new InterruptedException();
            while (isPaused()) {
                Thread.sleep(200);
            }
            statement.execute(statements.get(currentStatement));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Only one connection is created currently.
     * Use this function to
     */
    private Connection connection;
    protected Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(jdbcURI);
        }
        return connection;
    }

    @Override
    public void doCleanup()
    {
        if (connection != null) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
                connection.close();
            } catch (SQLException e) {
                // TODO logging
            }
        }
        connection = null;
    }

    @Override
    public boolean isPauseable() {
        return true;
    }

    @Override
    public Object doGetMessage()
    {
        if (isPaused()) {
            return String.format("Paused: %s/%s [%.1f%%]\n\n%s",
                    currentStatement, statements.size(), (float) currentStatement / statements.size(),
                    statements.get(currentStatement));
        }
        else if (isActive())
        {
            return String.format("Running: %s/%s [%.1f%%]\n\n%s",
                    currentStatement, statements.size(), (float) currentStatement / statements.size(),
                    statements.get(currentStatement));
        }
        else if (didFinish())
        {
            return String.format("Finished successfully.");
        }
        return null;
    }

    @Override
    public Object getRestartPoint() {
        return currentStatement;
    }

}
