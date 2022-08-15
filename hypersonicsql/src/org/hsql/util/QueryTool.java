/*
 * QueryTool.java
 */

package org.hsql.util;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class QueryTool extends Applet implements WindowListener,ActionListener {
  static Properties pProperties=new Properties();
  boolean bApplication;
/**
 * You can start QueryTool without a browser and applet
 * using using this method. Type 'java QueryTool' to start it.
 * This is necessary if you want to use the standalone version
 * because appletviewer and internet browers do not allow the
 * applet to write to disk.
 */
  static Frame fMain;
  public static void main(String arg[]) {
    fMain=new Frame("Query Tool");
    QueryTool q=new QueryTool();
    q.bApplication=true;
    for(int i=0;i<arg.length;i++) {
      String p=arg[i];
      if(p.equals("-?")) {
        printHelp();
      }
      if(p.charAt(0)=='-') {
        pProperties.put(p.substring(1),arg[i+1]);
        i++;
      }
    }
    q.init();
    q.start();
    fMain.add("Center",q);
    MenuBar menu=new MenuBar();
    Menu file=new Menu("File");
    file.add("Exit");
    file.addActionListener(q);
    menu.add(file);
    fMain.setMenuBar(menu);
    fMain.setSize(500,400);
    fMain.show();
    fMain.addWindowListener(q);
  }
/**
 * Initializes the window and the database and inserts some test data.
 */
  public void init() {
    initGUI();
    Properties p=pProperties;
    if(!bApplication) {
      // default for applets is in-memory (.)
      p.put("database",".");
      try {
        // but it may be also a HTTP connection (http://)
        // try to use url as provided on the html page as parameter
        pProperties.put("database",getParameter("database"));
      } catch(Exception e) {
      }
    }
    String driver=p.getProperty("driver","org.hsql.jdbcDriver");
    String url=p.getProperty("url","jdbc:HypersonicSQL:");
    String database=p.getProperty("database","test");
    String user=p.getProperty("user","sa");
    String password=p.getProperty("password","");
    boolean test=p.getProperty("test","true").equalsIgnoreCase("true");
    boolean log=p.getProperty("log","true").equalsIgnoreCase("true");
    try {
      if(log) {
        trace("driver  ="+driver);
        trace("url     ="+url);
        trace("database="+database);
        trace("user    ="+user);
        trace("password="+password);
        trace("test    ="+test);
        trace("log     ="+log);
        DriverManager.setLogStream(System.out);
      }
      // As described in the JDBC FAQ:
      // http://java.sun.com/products/jdbc/jdbc-frequent.html;
      // Why doesn't calling class.forName() load my JDBC driver?
      // There is a bug in the JDK 1.1.x that can cause Class.forName() to fail.
      new org.hsql.jdbcDriver();
      Class.forName(driver).newInstance();
      cConn=DriverManager.getConnection(url+database,user,password);
    } catch (Exception e) {
      System.out.println("QueryTool.init: "+e.getMessage());
      e.printStackTrace();
    }
    sRecent=new String[iMaxRecent];
    iRecent=0;
    try {
      sStatement=cConn.createStatement();
    } catch(SQLException e) {
      System.out.println("Exception: "+e);
    }
    if(test) {
      insertTestData();
    }
    txtCommand.requestFocus();
  }
  void trace(String s) {
    System.out.println(s);
  }
  Connection cConn;
  Statement sStatement;
/**
 * This is function handles the events when a button is clicked or
 * when the used double-clicked on the listbox of recent commands.
 */
  public boolean action(Event evt, Object arg) {
    String s=arg.toString();
    if(s.equals("Execute")) {
      String sCmd=txtCommand.getText();
      String g[]=new String[1];
      try {
        sStatement.execute(sCmd);
        int r=sStatement.getUpdateCount();
        if(r==-1) {
          formatResultSet(sStatement.getResultSet());
        } else {
          g[0]="update count";
          gResult.setHead(g);
          g[0]=""+r;
          gResult.addRow(g);
        }
        setRecent(txtCommand.getText());
      } catch(SQLException e) {
        g[0]="SQL Error";
        gResult.setHead(g);
        g[0]=e.getMessage();
        gResult.addRow(g);
      }
      gResult.repaint();
      txtCommand.selectAll();
      txtCommand.requestFocus();
    } else if(s.equals("Script")) {
      String sScript=getScript();
      txtCommand.setText(sScript);
      txtCommand.selectAll();
      txtCommand.requestFocus();
    } else if(s.equals("Exit")) {
      System.exit(0);
    } else { // recent
      txtCommand.setText(s);
    }
    return true;
  }
  void formatResultSet(ResultSet r) {
    try {
      ResultSetMetaData m=r.getMetaData();
      int col=m.getColumnCount();
      String h[]=new String[col];
      for(int i=1;i<=col;i++) {
        h[i-1]=m.getColumnLabel(i);
      }
      gResult.setHead(h);
      while(r.next()) {
        for(int i=1;i<=col;i++) {
          h[i-1]=r.getString(i);
          if(r.wasNull()) {
            h[i-1]="(null)";
          }
        }
        gResult.addRow(h);
      }
    } catch(SQLException e) {
    }
  }
  String getScript() {
    try {
      ResultSet rResult=sStatement.executeQuery("SCRIPT");
      StringBuffer a=new StringBuffer();
      while(rResult.next()) {
        a.append(rResult.getString(1));
        a.append('\n');
      }
      a.append('\n');
      return a.toString();
    } catch(SQLException e) {
      return "";
    }
  }
/**
 * Adds a String to the Listbox of recent commands.
 */
  private void setRecent(String s) {
    for(int i=0;i<iMaxRecent;i++) {
      if(s.equals(sRecent[i])) {
        return;
      }
    }
    if(sRecent[iRecent]!=null) {
      choRecent.remove(sRecent[iRecent]);
    }
    sRecent[iRecent]=s;
    iRecent=(iRecent+1)%iMaxRecent;
    choRecent.addItem(s);
  }
  String sRecent[];
  static int iMaxRecent=24;
  int iRecent;
  TextArea txtCommand;
  Button butExecute,butScript;
  Choice choRecent;
  Grid gResult;
/**
 * Create the graphical user interface. This is AWT code.
 */
  private void initGUI() {
    // all panels
    Panel pQuery=new Panel();
    Panel pCommand=new Panel();
    Panel pButton=new Panel();
    Panel pRecent=new Panel();
    Panel pResult=new Panel();
    Panel pBorderWest=new Panel();
    Panel pBorderEast=new Panel();
    Panel pBorderSouth=new Panel();
    pQuery.setLayout(new BorderLayout());
    pCommand.setLayout(new BorderLayout());
    pButton.setLayout(new BorderLayout());
    pRecent.setLayout(new BorderLayout());
    pResult.setLayout(new BorderLayout());
    pBorderWest.setBackground(SystemColor.control);
    pBorderSouth.setBackground(SystemColor.control);
    pBorderEast.setBackground(SystemColor.control);
    // labels
    Label lblCommand=new Label(" Command",Label.LEFT);
    Label lblRecent=new Label(" Recent",Label.LEFT);
    Label lblResult=new Label(" Result",Label.LEFT);
    lblCommand.setBackground(SystemColor.control);
    lblRecent.setBackground(SystemColor.control);
    lblResult.setBackground(SystemColor.control);
    // buttons
    butExecute=new Button("Execute");
    butScript=new Button("Script");
    pButton.add("South",butScript);
    pButton.add("Center",butExecute);
    // command - textarea
    Font fFont=new Font("Dialog",Font.PLAIN,12);
    txtCommand=new TextArea(5,40);
    txtCommand.setFont(fFont);
    // recent - choice
    choRecent=new Choice();
    // result - grid
    gResult=new Grid();
    // combine it
    setLayout(new BorderLayout());
    pRecent.add("Center",choRecent);
    pRecent.add("North",lblRecent);
    pCommand.add("North",lblCommand);
    pCommand.add("East",pButton);
    pCommand.add("Center",txtCommand);
    pCommand.add("South",pRecent);
    pResult.add("North",lblResult);
    pResult.add("Center",gResult);
    pQuery.add("North",pCommand);
    pQuery.add("Center",pResult);
    add("Center",pQuery);
    add("West",pBorderWest);
    add("East",pBorderEast);
    add("South",pBorderSouth);
    layout();
  }
  static String sTestData[]= {
  "create table Place (Code integer,Name varchar(255))",
  "create index iCode on Place (Code)",
  "delete from place",
  "insert into Place values (4900,'Langenthal')",
  "insert into Place values (8000,'Zurich')",
  "insert into Place values (3000,'Berne')",
  "insert into Place values (1200,'Geneva')",
  "insert into Place values (6900,'Lugano')",
  "create table Customer (Nr integer,Name varchar(255),Place integer)",
  "create index iNr on Customer (Nr)",
  "delete from Customer",
  "insert into Customer values (1,'Meier',3000)",
  "insert into Customer values (2,'Mueller',8000)",
  "insert into Customer values (3,'Devaux',1200)",
  "insert into Customer values (4,'Rossi',6900)",
  "insert into Customer values (5,'Rickli',3000)",
  "insert into Customer values (6,'Graf',3000)",
  "insert into Customer values (7,'Mueller',4900)",
  "insert into Customer values (8,'May',1200)",
  "insert into Customer values (9,'Berger',8000)",
  "insert into Customer values (10,'D''Ascoli',6900)",
  "insert into Customer values (11,'Padruz',1200)",
  "insert into Customer values (12,'Hug',4900)"
  };
  void insertTestData() {
    for(int i=0;i<sTestData.length;i++) {
      try {
        sStatement.executeQuery(sTestData[i]);
      } catch(SQLException e) {
        System.out.println("Exception: "+e);
      }
    }
    setRecent("select * from place");
    setRecent("select * from Customer");
    setRecent("select * from Customer where place<>3000");
    setRecent("select * from place where code>3000 or code=1200");
    setRecent("select * from Customer where nr<=8\nand name<>'Mueller'");
    setRecent("update Customer set name='Russi'\nwhere name='Rossi'");
    setRecent("delete from Customer where place=8000");
    setRecent("insert into place values(3600,'Thun')");
    setRecent("drop index Customer.iNr");
    setRecent("select * from Customer where name like '%e%'");
    setRecent("select count(*),min(code),max(code),sum(code) from place");
    String s="select * from Customer,place\n"+
        "where Customer.place=place.code\n"+
        "and place.name='Berne'";
    setRecent(s);
    txtCommand.setText(s);
    txtCommand.selectAll();
  }
  static void printHelp() {
    System.out.println(
    "Usage: java QueryTool [-options]\n"+
    "where options include:\n"+
    "    -driver <classname>  name of the driver class\n"+
    "    -url <name>          first part of the jdbc url\n"+
    "    -database <name>     second part of the jdbc url\n"+
    "    -user <name>         username used for connection\n"+
    "    -password <name>     password for this user\n"+
    "    -test <true/false>   insert test data\n"+
    "    -log <true/false>    write log to system out");
    System.exit(0);
  }
  public void windowActivated(WindowEvent e) {
  }
  public void windowDeactivated(WindowEvent e) {
  }
  public void windowClosed(WindowEvent e) {
  }
  public void windowClosing(WindowEvent ev) {
    try {
      cConn.close();
    } catch(Exception e) {
    }
    if(fMain!=null) {
      fMain.dispose();
    }
    System.exit(0);
  }
  public void windowDeiconified(WindowEvent e) {
  }
  public void windowIconified(WindowEvent e) {
  }
  public void windowOpened(WindowEvent e) {
  }
  public void actionPerformed(ActionEvent ev) {
    String s=ev.getActionCommand();
    if(s!=null && s.equals("Exit")) {
      windowClosing(null);
    }
  }
}

