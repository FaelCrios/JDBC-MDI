import javax.swing.*;


import java.awt.*;
import java.awt.event.*;
import java.sql.*;
  
public class Aplicacao extends JFrame implements ActionListener {  
  Connection connection;
  Statement statement; 
  JDesktopPane desktop;
        
  
  JMenuItem Insert;
  JMenuItem Consult;
  JMenuItem End;
  JMenuItem Verify;
  
  JanelaConsulta janelaConsulta;
  JanelaVerifica janelaVerifica;
  
  public Aplicacao() {  
      super("Wallet System Interface");  
        
      setBounds(300,300,800,800);  
      setExtendedState(JFrame.MAXIMIZED_BOTH);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        
      desktop = new JDesktopPane();  
      add(desktop);  
        
      setJMenuBar(Menu());
      
      BDstart();
      
      janelaConsulta = new JanelaConsulta(desktop, connection);  
      desktop.add(janelaConsulta);  
      janelaConsulta.setVisible(false);
      
      janelaVerifica = new JanelaVerifica(desktop, connection);  
      desktop.add(janelaVerifica);  
      janelaVerifica.setVisible(false);
    
      setVisible(true);  
  }  
  
  JMenuBar Menu() {
    JMenuBar menuBar = new JMenuBar();
      JMenu menuBD = new JMenu("Banco de Bados");
      menuBar.add(menuBD);
    
        Insert = new JMenuItem("Insere Dados");
        menuBD.add(Insert);
        Consult = new JMenuItem("Consulta Tabela");
        menuBD.add(Consult);
        Verify = new JMenuItem("Verifica os valores de compra");
        menuBD.add(Verify);
        End = new JMenuItem("Termina");
        menuBar.add(End);
      
      
        Insert.addActionListener(this);
        Consult.addActionListener(this);
        Verify.addActionListener(this);
        End.addActionListener(this);
        return menuBar;
  }
  
  public void actionPerformed(ActionEvent e) {
    
      try {
        statement.executeUpdate("CREATE TABLE CARTEIRA (NOME VARCHAR(30), TELEFONE VARCHAR(15), CPF VARCHAR(15), RG VARCHAR(15))");
        statement.executeUpdate("CREATE TABLE ACOES (NOME VARCHAR(30), VALOR VARCHAR(6))");
        JOptionPane.showMessageDialog(null, "Tabela criada com sucesso.", "Sistema", JOptionPane.INFORMATION_MESSAGE);
      } catch (SQLException error) {
        //JOptionPane.showMessageDialog(desktop, "Erro na criação da tabela"+error, "Sistema", JOptionPane.ERROR_MESSAGE);
      } catch (NullPointerException error) {
        JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+error, "Erro", JOptionPane.ERROR_MESSAGE);
      }
    if (e.getSource() == Insert) {
      new JanelaInsere(desktop, connection);
    } else if (e.getSource() == Consult) {
      janelaConsulta.setVisible(true);
    } else if (e.getSource() == End) {
      System.exit(0);
    } else if (e.getSource() == Verify) {
      
      janelaVerifica.setVisible(true);
  }
  }
  
  void BDstart() {
    try {
      Class.forName("org.hsql.jdbcDriver");
      connection = DriverManager.getConnection("jdbc:HypersonicSQL:hsql://localhost:8080", "sa", "");
      statement = connection.createStatement();
    } catch (ClassNotFoundException error) {
      JOptionPane.showMessageDialog(null, "Infelizmente o Bando de dados não pode ser encontrado"+error, "Error", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    } catch (SQLException error) {
      JOptionPane.showMessageDialog(null, "Erro ao inicializar o bando de dados"+error, "Erro", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }
  
  public void finalize() {
    try {
      statement.close();
      connection.close();
    } catch (SQLException error) {
      JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+error, "Erro", JOptionPane.ERROR_MESSAGE);
    }
  }
    
  public static void main(String[] args ) {  
      new Aplicacao();  
  }  
}  

class JanelaInsere extends JInternalFrame {
  PreparedStatement pStmt;
  JDesktopPane desktop;
  JButton botao1;
  JTextField tf1, tf2,tf3,tf4;

  public JanelaInsere(JDesktopPane deskTop, Connection connection) {
    super("Wallet System Interface - Inserção", true, true, true, true);
    desktop = deskTop;
    try {
      pStmt = connection.prepareStatement("INSERT INTO CARTEIRA VALUES (?, ?, ?, ?)");

      setLayout(new GridLayout(10,2));
      add(new JLabel("Nome: "));add(tf1 = new JTextField(30));
          
      add(new JLabel("Telefone: "));add(tf2 = new JTextField(12));
      
      add(new JLabel("CPF: "));add(tf3 = new JTextField(12));
      
      add(new JLabel("RG: ")); add(tf4 = new JTextField(10));
     
      add(botao1 = new JButton("Insere"), BorderLayout.SOUTH);
      pack();
      setVisible(true);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
      desktop.add(this);

      botao1.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            pStmt.setString(1, tf1.getText());
            pStmt.setString(2, tf2.getText());
            pStmt.setString(3, tf3.getText());
            pStmt.setString(4, tf4.getText());
            
            tf1.setText("");
            tf2.setText("");
            tf3.setText("");
            tf4.setText("");
            pStmt.executeUpdate();
          } catch (Exception error) {
            JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+error, "Erro", JOptionPane.ERROR_MESSAGE);
          }
        }
      });

    } catch (Exception error) {
      JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+error, "Erro", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  public void finalize() {
    try {
      pStmt.close();
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Problema interno.\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
    }
  }
}

class JanelaConsulta extends JInternalFrame implements ActionListener {
  PreparedStatement pStmt;
  JDesktopPane desktop;
  JButton button1,button2;
  JTextField tf1;
  JTextArea textArea1;

  public JanelaConsulta(JDesktopPane d, Connection con) {
    super("Wallet System Interface - Consulta", true, true, true, true);
    desktop = d;
    try {
      pStmt = con.prepareStatement("SELECT * FROM CARTEIRA WHERE NOME LIKE ?");

      JPanel label1 = new JPanel();
      label1.add(new JLabel("Nome: "));label1.add(tf1 = new JTextField(30));
      
      label1.add(button1 = new JButton("Pesquisa"));label1.add(button2 = new JButton("Mostra Todos"));
      add(label1, BorderLayout.NORTH);
      label1 = new JPanel();
      JScrollPane scrollPane = new JScrollPane(textArea1 = new JTextArea(30, 70));
      label1.add(scrollPane);
      add(label1, BorderLayout.CENTER);

      button1.addActionListener(this);
      button2.addActionListener(this);
      pack();
      setDefaultCloseOperation(HIDE_ON_CLOSE);

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == button1){
      try {
        textArea1.setText("");
      pStmt.setString(1, tf1.getText());
      ResultSet resultSet = pStmt.executeQuery();
      while (resultSet.next()) {
        String nome = resultSet.getString(1);
        String numero = resultSet.getString(2);
        String cpf = resultSet.getString(3);
        String rg = resultSet.getString(4);
        textArea1.append("Nome: "+nome + "\n" +"Telefone: "+ numero + "\n"+"CPF: " + cpf +"\nRG: "+rg+"\n\n\n");
      }
      tf1.selectAll();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    } else if(e.getSource() == button2){
      try {
        textArea1.setText("");
      ResultSet resultSet = pStmt.executeQuery("SELECT * FROM CARTEIRA");
      while(resultSet.next()){
          // (NOME VARCHAR(30), TELEFONE VARCHAR(15), CPF VARCHAR(15), RG VARCHAR(15))
          String nome = resultSet.getString("NOME");
          String telefone = resultSet.getString("TELEFONE");
          String cpf = resultSet.getString("CPF");
          String rg = resultSet.getString("RG");
          textArea1.append("Nome : "+nome + "\nTelefone: "+telefone+"\nCPF: "+cpf+"\nRg: "+rg+"\n\n\n");
      }
    }catch (Exception ex) {
      JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
    }
  }
  }
  
   public void finalize() {
     try {
       pStmt.close();
     } catch (SQLException e) {
       JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
     }
   }
}

class JanelaVerifica extends JInternalFrame implements ActionListener {
  PreparedStatement pStmt;
  JDesktopPane desktop;
  JButton button1;
  JTextField tf1;
  JTextArea textArea1;

  public JanelaVerifica(JDesktopPane d, Connection con) {
    super("Wallet System Interface - Sistema de verificação de preço", true, true, true, true);


    
    desktop = d;
    try {
      pStmt = con.prepareStatement("SELECT * FROM ACOES");
      setLayout(new BorderLayout());
      add(textArea1 = new JTextArea(30,70));
      add(button1 = new JButton("Atualizar"), BorderLayout.SOUTH);
      button1.addActionListener(this);
      pack();
      setDefaultCloseOperation(HIDE_ON_CLOSE);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  public void actionPerformed(ActionEvent e) {
    try {
        textArea1.setText("");
      ResultSet resultSet = pStmt.executeQuery("SELECT * FROM ACOES");
      while(resultSet.next()){
          String nome = resultSet.getString("NOME");
          String valor = resultSet.getString("VALOR");
          textArea1.append("Nome da ação: "+nome + "\nValor: "+valor+"\n\n\n");
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
    }
  }
  
   public void finalize() {
     try {
       pStmt.close();
     } catch (SQLException e) {
       JOptionPane.showMessageDialog(desktop, "Problema interno.\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
     }
   }
}
