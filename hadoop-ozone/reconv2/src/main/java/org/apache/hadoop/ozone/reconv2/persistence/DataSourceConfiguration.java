package org.apache.hadoop.ozone.reconv2.persistence;

/**
 * Common configuration needed to instantiate {@link javax.sql.DataSource}.
 */
public interface DataSourceConfiguration {
  /**
   * Get database driver class name available on the classpath.
   */
  String getDriverClass();

  /**
   * Get Jdbc Url for the database server.
   */
  String getJdbcUrl();

  /**
   * Get username for the db.
   */
  String getUserName();

  /**
   * Get password for the db.
   */
  String getPassword();

  /**
   * Should autocommit be turned on for the datasource.
   */
  boolean setAutoCommit();

  /**
   * Sets the maximum time (in milliseconds) to wait before a call to
   * getConnection is timed out.
   */
  long getConnectionTimeout();

  /**
   * Get a string representation of {@link org.jooq.SQLDialect}.
   */
  String getSqlDialect();

  /**
   * In a production database this should be set to something like 10.
   * SQLite does not allow multiple connections, hence this defaults to 1.
   */
  Integer getMaxActiveConnections();

  /**
   * Sets the maximum connection age (in seconds).
   */
  long getMaxConnectionAge();

  /**
   * Sets the maximum idle connection age (in seconds).
   */
  long getMaxIdleConnectionAge();

  /**
   * Statement specific to database, usually SELECT 1.
   */
  String getConnectionTestStatement();

  /**
   * How often to test idle connections for being active (in seconds).
   */
  long getIdleConnectionTestPeriod();
}
