/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.jdbc.xa;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class BitronixJdbcXaDataSourceProvider implements XADataSource
{

    private final XADataSource xaDataSource;
    public static XADataSource xaDatasourceHolder;

    public BitronixJdbcXaDataSourceProvider()
    {
        this.xaDataSource = xaDatasourceHolder;
        xaDatasourceHolder = null;

    }

    @Override
    public XAConnection getXAConnection() throws SQLException
    {
        return xaDataSource.getXAConnection();
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException
    {
        return xaDataSource.getXAConnection(user, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        return xaDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        xaDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {
        xaDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        return xaDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return xaDataSource.getParentLogger();
    }
}