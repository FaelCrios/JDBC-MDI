/*
 * CodeSwitcher.java
 */

package org.hsql.util;
import java.io.*;
import java.util.*;

public class CodeSwitcher {
  private Vector vList;
  private Vector vSwitchOn;
  private Vector vSwitchOff;
  private Vector vSwitches;
  private boolean bAdd,bRemove;
  private final static int MAX_LINELENGTH=82;
  public static void main(String a[]) {
    CodeSwitcher s=new CodeSwitcher();
    if(a.length==0) {
      showUsage();
      return;
    }
    boolean path=false;
    for(int i=0;i<a.length;i++) {
      String p=a[i];
      if(p.startsWith("+")) {
        if(p.length()==1) {
          s.bAdd=true;
        } else {
          s.vSwitchOn.addElement(p.substring(1));
        }
      } else if(p.startsWith("-")) {
        if(p.length()==1) {
          s.bRemove=true;
        } else {
          s.vSwitchOff.addElement(p.substring(1));
        }
      } else {
        s.addDir(p);
        path=true;
      }
    }
    if(!path) {
      printError("no path specified");
      showUsage();
    }
    s.process();
    if(s.vSwitchOff.size()==0 && s.vSwitchOn.size()==0) {
      s.printSwitches();
    }
  }
  static void showUsage() {
    System.out.println("Usage: java CodeSwitcher [paths] [labels] [+][-]");
    System.out.println("If no labels are specified then all used");
    System.out.println("labels in the source code are shown.");
    System.out.println("Use +MODE to switch on the things labeld MODE");
    System.out.println("Use -MODE to switch off the things labeld MODE");
    System.out.println("Path: Any number of path or files may be");
    System.out.println("specified. Use . for the current directory");
    System.out.println("(including sub-directories).");
    System.out.println("Example: java CodeSwitcher +JAVA2 .");
    System.out.println("This example switches on code labeled JAVA2");
    System.out.println("in all *.java files in the current directory");
    System.out.println("and all subdirectories.");
    System.out.println("java CodeSwitcher + .");
    System.out.println("Adds test code to the code.");
    System.out.println("java CodeSwitcher - .");
    System.out.println("Removed test code from the code.");
  }
  CodeSwitcher() {
    vList=new Vector();
    vSwitchOn=new Vector();
    vSwitchOff=new Vector();
    vSwitches=new Vector();
  }
  void process() {
    int len=vList.size();
    for(int i=0;i<len;i++) {
      System.out.print(".");
      String file=(String)vList.elementAt(i);
      if(bAdd || bRemove) {
        int maxlen=testFile(file);
        if(bAdd && !bRemove) {
          addTest(file,maxlen);
        } else {
          removeTest(file);
        }
      } else {
        if(!processFile(file)) {
          System.out.println("in file "+file+" !");
        }
      }
    }
    System.out.println("");
  }
  void printSwitches() {
    System.out.println("Used labels:");
    for(int i=0;i<vSwitches.size();i++) {
      System.out.println((String)(vSwitches.elementAt(i)));
    }
  }
  void addDir(String path) {
    File f=new File(path);
    if(f.isFile() && path.endsWith(".java")) {
      vList.addElement(path);
    } else if(f.isDirectory()) {
      String list[]=f.list();
      for(int i=0;i<list.length;i++) {
        addDir(path+File.separatorChar+list[i]);
      }
    }
  }
  void removeTest(String name) {
    File f=new File(name);
    File fnew=new File(name+".new");
    try {
      LineNumberReader read=new LineNumberReader(new FileReader(f));
      FileWriter write=new FileWriter(fnew);
      while(true) {
        String line=read.readLine();
        if(line==null) {
          break;
        }
        if(line.startsWith("Profile.visit(")) {
          int s=line.indexOf(';');
          line=line.substring(s+1);
        }
        write.write(line+"\r\n");
      }
      read.close();
      write.flush();
      write.close();
      File fbak=new File(name+".bak");
      fbak.delete();
      f.renameTo(fbak);
      File fcopy=new File(name);
      fnew.renameTo(fcopy);
      fbak.delete();
    } catch(Exception e) {
      printError(e.getMessage());
    }
  }
  void addTest(String name,int maxline) {
    File f=new File(name);
    File fnew=new File(name+".new");
    String key=name;
    key=key.replace('\\','.');
    try {
      LineNumberReader read=new LineNumberReader(new FileReader(f));
      FileWriter write=new FileWriter(fnew);
      int l=0;
      boolean longline=false;
      while(true) {
        String line=read.readLine();
        if(line==null) {
          break;
        }
        if(line.startsWith(" ")) {
          int spaces=0;
          for(;spaces<line.length();spaces++) {
            if(line.charAt(spaces)!=' ') {
              break;
            }
          }
          if(spaces>3 && testLine(line) && !longline) {
            line="Profile.visit(\""+key+"\","+l+","+maxline+");"+line;
            l++;
          } else if(isLongline(line)) {
            longline=true;
          } else {
            longline=false;
          }
        }
        write.write(line+"\r\n");
      }
      read.close();
      write.flush();
      write.close();
      File fbak=new File(name+".bak");
      fbak.delete();
      f.renameTo(fbak);
      File fcopy=new File(name);
      fnew.renameTo(fcopy);
      fbak.delete();
    } catch(Exception e) {
      printError(e.getMessage());
    }
  }
  int testFile(String name) {
    File f=new File(name);
    try {
      LineNumberReader read=new LineNumberReader(new FileReader(f));
      int l=1,maxline=0;
      boolean longline=false;
      while(true) {
        String line=read.readLine();
        if(line==null) {
          break;
        }
        if(line.length()>MAX_LINELENGTH && !line.startsWith("Profile.")) {
          System.out.println("long line in "+name+" at line "+l);
        }
        if(line.startsWith(" ")) {
          int spaces=0;
          for(;spaces<line.length();spaces++) {
            if(line.charAt(spaces)!=' ') {
              break;
            }
          }
          if(spaces>3 && testLine(line) && !longline) {
            maxline++;
          } else if(isLongline(line)) {
            longline=true;
          } else {
            longline=false;
          }
          String s=line.substring(spaces);
          if(s.startsWith("if(")) {
            if(!s.endsWith(" {")) {
              System.out.println("if( without { in "+name+" at line "+l);
            }
          } else if(s.startsWith("} else if(")) {
            if(!s.endsWith(" {")) {
              System.out.println("} else if without { in "+name+" at line "+l);
            }
          } else if(s.startsWith("while(")) {
            if(!s.endsWith(" {")) {
              System.out.println("while( without { in "+name+" at line "+l);
            }
          } else if(s.startsWith("switch(")) {
            if(!s.endsWith(" {")) {
              System.out.println("switch( without { in "+name+" at line "+l);
            }
          } else if(s.startsWith("do ")) {
            if(!s.endsWith(" {")) {
              System.out.println("do without { in "+name+" at line "+l);
            }
          }
        }
        l++;
      }
      read.close();
      return maxline;
    } catch(Exception e) {
      printError(e.getMessage());
    }
    return -1;
  }
  boolean testLine(String line) {
    if(!line.endsWith(";")) {
      return false;
    }
    if(line.trim().startsWith("super(")) {
      return false;
    }
    return true;
  }
  boolean isLongline(String s) {
    char c=s.charAt(s.length()-1);
    if(",(+-&|".indexOf(c)>=0) {
      return true;
    }
    return false;
  }
  boolean processFile(String name) {
    File f=new File(name);
    File fnew=new File(name+".new");
    int state=0; // 0=normal 1=inside_if 2=inside_else
    boolean switchoff=false;
    boolean working=false;
    try {
      LineNumberReader read=new LineNumberReader(new FileReader(f));
      FileWriter write=new FileWriter(fnew);
      while(true) {
        String line=read.readLine();
        if(line==null) {
          break;
        }
        if(working) {
          if(line.equals("/*") || line.equals("*/")) {
            continue;
          }
        }
        if(!line.startsWith("//#")) {
          write.write(line+"\r\n");
        } else {
          if(line.startsWith("//#ifdef ")) {
            if(state!=0) {
              printError("'#ifdef' not allowed inside '#ifdef'");
              return false;
            }
            write.write(line+"\r\n");
            state=1;
            String s=line.substring(9);
            if(vSwitchOn.indexOf(s)!=-1) {
              working=true;
              switchoff=false;
            } else if(vSwitchOff.indexOf(s)!=-1) {
              working=true;
              write.write("/*\r\n");
              switchoff=true;
            }
            if(vSwitches.indexOf(s)==-1) {
              vSwitches.addElement(s);
            }
          } else if(line.startsWith("//#else")) {
            if(state!=1) {
              printError("'#else' without '#ifdef'");
              return false;
            }
            state=2;
            if(!working) {
              write.write(line+"\r\n");
            } else if(switchoff) {
              write.write("*/\r\n");
              write.write(line+"\r\n");
              switchoff=false;
            } else {
              write.write(line+"\r\n");
              write.write("/*\r\n");
              switchoff=true;
            }
          } else if(line.startsWith("//#endif")) {
            if(state==0) {
              printError("'#endif' without '#ifdef'");
              return false;
            }
            state=0;
            if(working && switchoff) {
              write.write("*/\r\n");
            }
            write.write(line+"\r\n");
            working=false;
          } else {
            write.write(line+"\r\n");
          }
        }
      }
      if(state!=0) {
        printError("'#endif' missing");
        return false;
      }
      read.close();
      write.flush();
      write.close();
      File fbak=new File(name+".bak");
      fbak.delete();
      f.renameTo(fbak);
      File fcopy=new File(name);
      fnew.renameTo(fcopy);
      fbak.delete();
      return true;
    } catch(Exception e) {
      printError(e.getMessage());
      return false;
    }
  }
  static void printError(String error) {
    System.out.println("");
    System.out.println("ERROR: "+error);
  }
}
