import java.sql.*;

public class Acoes {
  public static void main(String[] args) {
    try {
      Class.forName("org.hsql.jdbcDriver");
      Connection connection = DriverManager.getConnection("jdbc:HypersonicSQL:hsql://localhost:8080", "sa", "");
      Statement statement = connection.createStatement();
      statement.executeUpdate("CREATE TABLE ACOES (NOME VARCHAR(30), VALOR VARCHAR(6))");
      statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('AMER3', '15.07' )");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('VIIA3', '21.87')");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('CASH3', '12.11' )");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('CVCB3', '16.21')");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('ALPARGATAS ON', '8.29')");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('CAREFOUR', '28.21')");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('BRADESPAR', '16,12')");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('BRAO', '32,12')");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('ALTAR', '16,12')");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('PUC', '61,12')");
        statement.executeUpdate("INSERT INTO ACOES " + "VALUES ('ITAU', '87,12')");

      statement.close();
      connection.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
