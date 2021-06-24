package feiq;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FlynnQQ {
    private LoginForm loginForm = null;
    private ChatForm chatForm = null;
    private MainForm mainForm = null;
    private FileForm fileForm = null;
    private String root = null;
    private String ip = null;
    private String nick = null;
    private Connection connection = null;
    private ResultSet result = null;
    private Socket socket = null;
    private PreparedStatement ps = null;
    private int headCount;
    private int head_num;
    private HeadIcon userIcon = null;
    private FriendItem[] friendList = null;
    private ChatFormPool chatPool = null;
    
    private FlynnQQ() {
        init();
    }

    private void init() {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            root = "./images/";
            headCount = 45;
            head_num = 2;
            loginForm = new LoginForm();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    private void sendFile(String ip) {
        fileForm.startSend(ip);
    }

    private class ChatFormPool {
        private ChatForm[] chatPool;
        private String[] ip;
        private int pCount = 50;

        ChatFormPool() {
            chatPool = new ChatForm[pCount];
            ip = new String[pCount];
            for (int i = 0; i < pCount; i++) {
                chatPool[i] = null;
                ip[i] = "";
            }
        }

        private ChatForm getChatForm(String ip) {
            for (int i = 0; i < pCount; i++) {
                if (this.ip[i].trim().equals(ip))
                    return chatPool[i];
            }
            return null;
        }

        private ChatForm createChatForm(FriendItem con) {
            if (getChatForm(con.getIp()) == null)
                for (int i = 0; i < pCount; i++) {
                    if (this.ip[i].trim().equals("")) {
                        chatPool[i] = new ChatForm(con);
                        ip[i] = con.getIp();
                        return chatPool[i];
                    }
                }
            return getChatForm(con.getIp());
        }

        private void returnChatForm(ChatForm cf) {
            for (int i = 0; i < pCount; i++)
                if (chatPool[i] == cf) {
                    chatPool[i] = null;
                    this.ip[i] = "";
                }
        }

    }


    //接收消息监听
    private void msgRec() {
        while (true) {
            try {
                byte[] byteRec = new byte[1024];
                int len;
                StringBuilder strB = new StringBuilder();
                String strEnd = "";
                while ((len = socket.getInputStream().read(byteRec)) != -1) {
                    strEnd = new String(byteRec, 0, len);
                    if (strEnd.contains("@END")) break;
                    strB.append(strEnd);
                }
                if (strEnd.indexOf("@END") != 0)
                    strB.append(strEnd, 0, strEnd.indexOf("@END"));
                //System.out.println(strB.toString());
                String strRec = strB.toString();
                int pos = strRec.indexOf("@");
                String body = "";
                //System.out.println(pos);
                String head = strRec.substring(0, pos);
                if (strRec.length() > pos)
                    body = strRec.substring(pos + 1);
                switch (head) {
                    case "OnConnect":
                    case "DisConnect":
                        mainForm.notice(head, body);
                        break;
                    case "Refresh":
                        mainForm.stateRefresh(body);
                        break;
                    case "FileRecReady":
                        sendFile(body);
                        break;
                    default:
                        if (chatPool.getChatForm(head) == null) {
                            for (FriendItem con : friendList) {
                                if (con.getIp().trim().equals(head)) {
                                    chatPool.createChatForm(con);
                                    break;
                                }
                            }
                        }
                        if (chatPool.getChatForm(head) != null) {
                            Objects.requireNonNull(chatPool.getChatForm(head)).receive(strRec);
                            Objects.requireNonNull(chatPool.getChatForm(head)).setVisible(true);
                        }
                        break;
                }
            } catch (IOException e) {
                //System.out.println("服务器正在维护中！连接已断开...");
                break;
            }
        }
    }

    private void msgSend(String ip, String str) {
        try {
            String strSend = ip + "@" + str;
            OutputStream output = socket.getOutputStream();
            output.write(strSend.getBytes());
            output.write("@END".getBytes());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //登录窗口
    private class LoginForm extends JFrame {
        private JLabel loadPic;
        private JLabel IPText;
        private JTextField nickText;
        private JLabel tip;
        private String head_path;
        private double xPos = 0;
        private double yPos = 0;
        private Thread load = null;
        private JLabel headFirst;
        private JLabel headNow;
        private JLabel headNext;
        private JLabel headLoad;
        private HeadIcon icon;
        private boolean isFind;

        private void isLast() {
            icon = new HeadIcon(head_num + ".jpg");
            icon.setImage(icon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
            headNext.setIcon(icon);
            head_num -= 1;
            if (head_num == 0)
                head_num = headCount;
            icon = new HeadIcon(head_num + ".jpg");
            icon.setImage(icon.getImage().getScaledInstance(80, 80, Image.SCALE_DEFAULT));
            headNow.setIcon(icon);
            int index = head_num - 1;
            if (head_num == 1)
                index = headCount;
            icon = new HeadIcon(index + ".jpg");
            icon.setImage(icon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
            headFirst.setIcon(icon);
        }

        private void isNext() {
            icon = new HeadIcon(head_num + ".jpg");
            icon.setImage(icon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
            headFirst.setIcon(icon);
            head_num += 1;
            if (head_num > headCount)
                head_num = 1;
            icon = new HeadIcon(head_num + ".jpg");
            icon.setImage(icon.getImage().getScaledInstance(80, 80, Image.SCALE_DEFAULT));
            headNow.setIcon(icon);
            int index = head_num + 1;
            if (head_num == headCount)
                index = 1;
            icon = new HeadIcon(index + ".jpg");
            icon.setImage(icon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
            headNext.setIcon(icon);
        }

        private LoginForm() {
            init();
            verity();
        }


        private void init() {
            //窗口去边框
            this.setUndecorated(true);

            JPanel Panel = new JPanel();
            Panel.setLayout(null);
            Panel.setBounds(0, 0, 400, 300);

            //连接界面
            ImageIcon loadPicture = new ImageIcon(root + "connect.png");
            loadPic = new JLabel();
            loadPic.setBounds(0, 0, 400, 300);
            loadPic.setIcon(loadPicture);
            loadPic.setOpaque(false);
            loadPic.setVisible(false);
            Panel.add(loadPic);
            headLoad = new JLabel();
            headLoad.setBounds(160, 110, 80, 80);
            headLoad.setVisible(false);
            Panel.add(headLoad);

            //最小化和关闭
            JLabel min;
            min = new JLabel();
            min.setBounds(340, 0, 30, 32);
            min.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setExtendedState(JFrame.ICONIFIED);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    min.setIcon(new ImageIcon(root + "min_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    min.setIcon(null);
                }
            });
            Panel.add(min);
            JLabel close;
            close = new JLabel();
            close.setBounds(370, 0, 30, 32);
            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.exit(0);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(new ImageIcon(root + "close_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(null);
                }
            });
            Panel.add(close);

            IPText = new JLabel();
            IPText.setBounds(120, 155, 150, 20);
            IPText.setFont(new Font("微软雅黑", Font.PLAIN, 15));
            IPText.setHorizontalAlignment(SwingConstants.CENTER);
            Panel.add(IPText);

            nickText = new JTextField();
            nickText.setBounds(120, 190, 150, 20);
            nickText.setFont(new Font("微软雅黑", Font.PLAIN, 15));
            nickText.setBorder(BorderFactory.createEmptyBorder());
            nickText.setHorizontalAlignment(JTextField.CENTER);
            nickText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        head_path = head_num + ".jpg";
                        connect();
                    }
                }
            });
            Panel.add(nickText);

            tip = new JLabel("连接服务器失败,请检查网络!");
            tip.setBounds(120, 220, 160, 20);
            tip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            tip.setForeground(Color.RED);
            tip.setVisible(false);
            Panel.add(tip);

            JLabel loginButton = new JLabel();
            loginButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    head_path = head_num + ".jpg";
                    connect();
                }
            });
            loginButton.setBounds(93, 247, 218, 30);
            Panel.add(loginButton);

            ImageIcon backPic = new ImageIcon(root + "FirstPage.png");
            JLabel loginPic = new JLabel();
            loginPic.setOpaque(false);
            loginPic.setBounds(0, 0, 400, 300);
            loginPic.setIcon(backPic);
            Panel.add(loginPic);

            //设置头像
            headFirst = new JLabel();
            headFirst.setBounds(75, 74, 50, 50);
            headFirst.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isLast();
                }
            });
            headFirst.setVisible(true);
            headNow = new JLabel();
            headNow.setBounds(160, 60, 80, 80);
            headNow.addMouseWheelListener(e -> {
                int scroll = e.getWheelRotation();
                if (scroll > 0)
                    isNext();
                else if (scroll < 0)
                    isLast();
            });
            headNow.setVisible(true);
            headNext = new JLabel();
            headNext.setBounds(275, 74, 50, 50);
            headNext.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isNext();
                }
            });
            headNext.setVisible(true);
            Panel.add(headFirst);
            Panel.add(headNow);
            Panel.add(headNext);

            this.setLayout(null);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setBounds((1920 - 400) / 2, (1080 - 300) / 2, 400, 300);
            this.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (xPos == 0) return;
                    setLocation(getX() + (int) (e.getXOnScreen() - xPos), getY() + (int) (e.getYOnScreen() - yPos));
                    xPos = e.getXOnScreen();
                    yPos = e.getYOnScreen();
                }
            });
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (getMousePosition().getY() > 40)
                        return;
                    if (xPos == 0) {
                        xPos = e.getXOnScreen();
                        yPos = e.getYOnScreen();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (xPos == 0) return;
                    loginForm.setLocation(loginForm.getX() + (int) (e.getXOnScreen() - xPos), loginForm.getY() + (int) (e.getYOnScreen() - yPos));
                    xPos = 0;
                    yPos = 0;
                }
            });
            this.add(Panel);
            this.setVisible(false);
        }

        //连接数据库
        private void verity() {
            try {

                IPText.setText(ip);
                Class.forName("com.mysql.jdbc.Driver");
                //connection = DriverManager.getConnection("jdbc:mysql://"+Server.ip+"/feiq",Server.username,Server.password);
                if (ip.equals(Server.sqlIp)) {
                    connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/feiq", "root", "root");
                } else {
//                    connection = DriverManager.getConnection("jdbc:mysql://192.168.212.151:3306/feiq", "root", "root");
                    connection = DriverManager.getConnection("jdbc:mysql://" + Server.sqlIp + ":3306/feiq", Server.username, Server.password);
                }
                String sql = "Select * from users where ip = ?";
                ps = connection.prepareStatement(sql);
                ps.setString(1, ip);
                result = ps.executeQuery();
                isFind = false;
                if (result.next()) {
                    isFind = true;
                    nick = result.getString("nickname");
                    head_path = result.getString("headpic");
                    connect();
                } else {
                    isNext();
                    this.setVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                load = null;
                isNext();
                IPText.setVisible(true);
                nickText.setVisible(true);
                loadPic.setVisible(false);
                headLoad.setVisible(false);
                tip.setVisible(true);
                this.setVisible(true);
            }
        }

        //连接按钮单击事件
        private void connect() {
            load = new Thread(this::loginVerity);
            loadPic.setVisible(true);
            icon = new HeadIcon(head_path);
            icon.setImage(icon.getImage().getScaledInstance(80, 80, Image.SCALE_DEFAULT));
            headLoad.setIcon(icon);
            headLoad.setVisible(true);
            IPText.setVisible(false);
            nickText.setVisible(false);
            loadPic.setVisible(true);
            tip.setVisible(false);
            this.setVisible(true);
            load.start();
        }

        //登录中界面
        private void loginVerity() {
            try {
                if (!isFind) {
                    signUp();
                    userIcon = new HeadIcon(head_num + ".jpg");
                } else
                    userIcon = new HeadIcon(head_path);
                mainForm = new MainForm(nick);
                Thread.sleep(1000);
                boolean result = true;
                try {
                    socket = new Socket(Server.ip, 2020);
                } catch (Exception e) {
                    result = false;
                }
                if (result) {
                    new Thread(FlynnQQ.this::msgRec).start();
                    msgSend("connect", "");
                    loginForm.setVisible(false);
                    loginForm = null;
                    mainForm.setVisible(true);
                } else {
                    if (isFind) {
                        nickText.setText(nick);
                        headNow.setIcon(headLoad.getIcon());
                        headFirst.setIcon(new HeadIcon("last.png"));
                        headNext.setIcon(new HeadIcon("next.png"));
                    }
                    load = null;
                    IPText.setVisible(true);
                    nickText.setVisible(true);
                    loadPic.setVisible(false);
                    headLoad.setVisible(false);
                    tip.setVisible(true);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void signUp() {
            try {
                nick = nickText.getText();
                head_path = head_num + ".jpg";
                ps = connection.prepareStatement("insert into users values (null,?,?,?)");
                ps.setString(1, ip);
                ps.setString(2, nick);
                ps.setString(3, head_path);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    //主窗口
    private class MainForm extends JFrame {
        private JLabel[] headList;
        private JLabel[] nameList;
        private JLabel[] stateIcon;
        private String username;
        private JLabel min;
        private JLabel close;
        private NoticeForm noticeForm;

        private int count;
        private double xPos = 0;
        private double yPos = 0;
        private int selectNow;
        private int normal_width = 280;
        private int normal_height = 585;
        private SystemTray systemTray = null;
        private TrayIcon trayIcon = null;

        private int list_normal_height = 40;
        private int list_onfocus_height = 60;
        private int list_left_padding = 20;
        private ImageIcon list_normal_bg;
        private ImageIcon list_onfocus_bg;
        private ImageIcon list_checked_bg;

        private int head_normal_height = 30;
        private int head_normal_width = 30;
        private int head_onfocus_height = 48;
        private int head_onfocus_width = 48;
        private int head_normal_top_padding = 5;
        private int head_onfocus_top_padding = 6;

        private int name_normal_width = 180;
        private int name_normal_height = 40;
        private int name_normal_padding = 70;
        private int name_onfocus_width = 162;
        private int name_onfocus_height = 60;
        private int name_onfocus_padding = 88;

        private MainForm(String username) {
            this.setVisible(false);
            chatPool = new ChatFormPool();
            this.username = username;
            init();
            listRefresh(new FriendItem());
            fileForm = new FileForm();
        }

        private void stateRefresh(String str) {
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    if (str.contains(friendList[i].getIp()))
                        stateIcon[i].setIcon(new ImageIcon(root + "onconnect.png"));
                    else
                        stateIcon[i].setIcon(new ImageIcon(root + "disconnect.png"));
                }
            }
        }

        private void notice(String type, String ip) {
            for (int i = 0; i < count; i++) {
                if (ip.equals(friendList[i].getIp())) {
                    stateIcon[i].setIcon(new ImageIcon(root + type + ".png"));
                    noticeForm = new NoticeForm(friendList[i], type);
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        noticeForm.setVisible(false);
                        noticeForm = null;
                    }).start();
                }
            }
        }

        private void init() {
            this.setUndecorated(true);
            this.setType(Type.UTILITY);
            Image icon = new ImageIcon(root + "icon5.png").getImage();
            trayIcon = new TrayIcon(icon, "fq");
            trayIcon.addActionListener(e -> setVisible(true));
            try {
                if (systemTray == null) {
                    systemTray = SystemTray.getSystemTray();
                    if (trayIcon != null) {
                        systemTray.remove(trayIcon);
                    }
                }
                systemTray.add(trayIcon);
            } catch (AWTException e1) {
                e1.printStackTrace();
            }
            list_normal_bg = new ImageIcon(root + "list_white.png");
            list_onfocus_bg = new ImageIcon(root + "list_blue.png");
            list_checked_bg = new ImageIcon(root + "list_checked_blue.png");

            selectNow = -1;
            min = new JLabel();
            close = new JLabel();
            JPanel panel = new JPanel();
            panel.setLayout(null);
            panel.setBackground(Color.white);
            panel.setVisible(true);

            JPanel mainPanel = new JPanel();
            mainPanel.setBounds(0, 0, normal_width, normal_height);
            mainPanel.setLayout(null);

            JLabel userName = new JLabel(username);
            userName.setFont(new Font("微软雅黑", Font.BOLD, 15));
            userName.setForeground(Color.WHITE);
            userName.setBounds(105, 50, 125, 25);
            mainPanel.add(userName);

            JTextField search = new JTextField();
            search.setOpaque(false);
            search.setBounds(35, 110, 240, 20);
            search.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            search.setBorder(BorderFactory.createEmptyBorder());
            mainPanel.add(search);

            //滚动条
            JScrollPane scrollPane = new JScrollPane(panel);
            JLabel scroll = new JLabel(new ImageIcon(root + "Scrollbarlostfocus.png"));
            scroll.setVisible(false);
            scroll.setOpaque(false);
            scroll.setBounds(273, 210, 7, 31);
            scroll.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    scroll.setIcon(new ImageIcon(root + "Scrollbaronfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    scroll.setIcon(new ImageIcon(root + "Scrollbarlostfocus.png"));
                }
            });
            mainPanel.add(scroll);

            scrollPane.setBounds(0, 180, 300, 380);
            scrollPane.setBackground(Color.white);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    scroll.setIcon(new ImageIcon(root + "Scrollonfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    scroll.setIcon(null);
                }
            });
            JScrollBar vScrollBar = scrollPane.getVerticalScrollBar();
            vScrollBar.setUnitIncrement(8);


            count = 0;
            try {
//                String sql = "Select * from users where ip <> ?";
                String sql = "Select * from users";
                ps = connection.prepareStatement(sql);
//                ps.setString(1, ip);
                result = ps.executeQuery();
                if (result != null) {
                    result.last();
                    count = result.getRow();
                }
                friendList = new FriendItem[count];
                headList = new JLabel[count];
                nameList = new JLabel[count];
                stateIcon = new JLabel[count];
                if (count > 0) {
                    for (int i = 0; i < friendList.length; i++) {

                        assert result != null;
                        result.absolute(i + 1);
                        String ip = result.getString("ip");
                        String nickname = result.getString("nickname");
                        String headPic = result.getString("headpic");
                        HeadIcon normalIcon = new HeadIcon(headPic);
                        normalIcon.setImage(normalIcon.getImage().getScaledInstance(head_normal_width, head_normal_height, Image.SCALE_DEFAULT));
                        HeadIcon checkedIcon = new HeadIcon(headPic);
                        checkedIcon.setImage(checkedIcon.getImage().getScaledInstance(head_onfocus_width, head_onfocus_height, Image.SCALE_DEFAULT));

                        friendList[i] = new FriendItem(ip, nickname, headPic, normalIcon, checkedIcon);
                        friendList[i].setOpaque(false);
                        friendList[i].addMouseListener(new myMouseListener(chatForm));
                        friendList[i].setBounds(0, i * list_normal_height, normal_width, list_normal_height);

                        nameList[i] = new JLabel(friendList[i].getNickname() + " (" + friendList[i].getIp() + ")");
                        nameList[i].setFont(new Font("微软雅黑", Font.PLAIN, 13));
                        nameList[i].setOpaque(false);
                        nameList[i].setBounds(name_normal_padding, i * name_normal_height, name_normal_width, name_normal_height);

                        stateIcon[i] = new JLabel();
                        stateIcon[i].setBounds(255, i * list_normal_height + list_normal_height / 2 - 5, 10, 10);

                        headList[i] = new JLabel(friendList[i].getNormalIcon());
                        headList[i].setBounds(list_left_padding, head_normal_top_padding + i * list_normal_height, normal_width, head_normal_height);

                        panel.setPreferredSize(new Dimension(280, (i + 2) * list_normal_height));
                        panel.add(stateIcon[i]);
                        panel.add(nameList[i]);
                        panel.add(friendList[i]);
                        panel.add(headList[i]);
                    }
                    mainPanel.add(scrollPane);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            min = new JLabel();
            min.setBounds(220, 0, 30, 32);
            min.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    min.setIcon(new ImageIcon(root + "min_normal.png"));
                    mainForm.dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    min.setIcon(new ImageIcon(root + "min_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    min.setIcon(new ImageIcon(root + "min_normal.png"));
                }
            });
            mainPanel.add(min);

            close = new JLabel();
            close.setBounds(250, 0, 30, 32);
            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    mainForm = null;
                    if (systemTray == null) {
                        systemTray = SystemTray.getSystemTray();
                        if (trayIcon != null) {
                            systemTray.remove(trayIcon);
                        }
                    }
                    System.exit(0);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(new ImageIcon(root + "close_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(new ImageIcon(root + "close_normal.png"));
                }
            });
            mainPanel.add(close);

            JLabel backPic = new JLabel(new ImageIcon(root + "mainform.png"));
            backPic.setBounds(0, 0, 280, 585);
            backPic.setOpaque(false);
            mainPanel.add(backPic);

            userIcon.setImage(userIcon.getImage().getScaledInstance(58, 58, Image.SCALE_DEFAULT));
            JLabel headPic = new JLabel();
            headPic.setOpaque(false);
            headPic.setBounds(32, 32, 58, 58);
            headPic.setIcon(userIcon);
            mainPanel.add(headPic);

            this.setLayout(null);
            this.setBounds(1920 - (normal_width) * 4 / 3, 280 / 3, normal_width, normal_height);
            this.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (xPos == 0) return;
                    setLocation(getX() + (int) (e.getXOnScreen() - xPos), getY() + (int) (e.getYOnScreen() - yPos));
                    xPos = e.getXOnScreen();
                    yPos = e.getYOnScreen();
                }
            });
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (getMousePosition().getY() > 32)
                        return;
                    if (xPos == 0) {
                        xPos = e.getXOnScreen();
                        yPos = e.getYOnScreen();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (xPos == 0) return;
                    mainForm.setLocation(mainForm.getX() + (int) (e.getXOnScreen() - xPos), mainForm.getY() + (int) (e.getYOnScreen() - yPos));
                    xPos = 0;
                    yPos = 0;
                }
            });

            this.add(mainPanel);
            //this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }


        private class myMouseListener extends MouseAdapter {
            private ChatForm chatForm;

            private myMouseListener(ChatForm form) {
                this.chatForm = form;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                FriendItem get = (FriendItem) e.getSource();
                if (e.getClickCount() == 2) {

                    chatForm = chatPool.createChatForm(get);
                    chatForm.setVisible(true);
                    ////System.out.println(chatForm.hashCode());
                    chatForm.inputText.requestFocus();
                } else if (e.getClickCount() == 1) {
                    for (int i = 0; i < friendList.length; i++)
                        if (friendList[i] == get) {
                            selectNow = i;
                            break;
                        }
                    listRefresh(get);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                FriendItem label = (FriendItem) e.getSource();
                listRefresh(label);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                listRefresh(new FriendItem());
            }
        }

        //刷新好友列表
        private void listRefresh(FriendItem label) {
            for (int i = 0; i < friendList.length; i++) {
                if (i < selectNow || selectNow < 0) {
                    nameList[i].setBounds(name_normal_padding, i * name_normal_height, name_normal_width, name_normal_height);
                    friendList[i].setBounds(0, i * list_normal_height, normal_width, list_normal_height);
                    friendList[i].setIcon(list_normal_bg);
                    headList[i].setBounds(list_left_padding, i * list_normal_height + head_normal_top_padding, head_normal_width, head_normal_height);
                    headList[i].setIcon(friendList[i].getNormalIcon());
                    stateIcon[i].setBounds(255, i * list_normal_height + list_normal_height / 2 - 5, 10, 10);
                } else if (i == selectNow) {
                    nameList[i].setBounds(name_onfocus_padding, i * name_normal_height, name_onfocus_width, name_onfocus_height);
                    friendList[i].setBounds(0, i * list_normal_height, normal_width, list_onfocus_height);
                    friendList[i].setIcon(list_checked_bg);
                    headList[i].setBounds(list_left_padding, i * list_normal_height + head_onfocus_top_padding, head_onfocus_width, head_onfocus_height);
                    headList[i].setIcon(friendList[i].getCheckedIcon());
                    stateIcon[i].setBounds(255, i * list_normal_height + list_onfocus_height / 2 - 5, 10, 10);
                } else if (i > selectNow) {
                    nameList[i].setBounds(name_normal_padding, i * name_normal_height + (name_onfocus_height - name_normal_height), name_normal_width, name_normal_height);
                    friendList[i].setBounds(0, i * list_normal_height + (list_onfocus_height - list_normal_height), normal_width, list_normal_height);
                    friendList[i].setIcon(list_normal_bg);
                    headList[i].setBounds(list_left_padding, i * list_normal_height + head_normal_top_padding + (list_onfocus_height - list_normal_height), head_normal_width, head_normal_height);
                    headList[i].setIcon(friendList[i].getNormalIcon());
                    stateIcon[i].setBounds(255, i * list_normal_height + (list_onfocus_height - list_normal_height) + list_normal_height / 2 - 5, 10, 10);
                }
                if (friendList[i] == label && i != selectNow) {
                    friendList[i].setIcon(list_onfocus_bg);
                }
            }
        }
    }


    //拖拽文件
    private class DropTargetListenerImpl implements DropTargetListener {
        private JTextArea textArea;

        private DropTargetListenerImpl(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            //System.out.println("dragEnter: 拖拽目标进入组件区域");
            FileForm fileForm = new FileForm();
            fileForm.fileCount = 1;

        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            //System.out.println("dragOver: 拖拽目标在组件区域内移动");
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            //System.out.println("dragExit: 拖拽目标离开组件区域");
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            //System.out.println("dropActionChanged: 当前 drop 操作被修改");
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            boolean complete = false;
            try {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    complete = true;
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    @SuppressWarnings("unchecked")
                    List<File> fileList = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (fileList.size() > 0) {
                        textArea.append(fileList.get(0).getAbsolutePath() + "\n");
                    }
                }
            } catch (IOException | UnsupportedFlavorException e) {
                e.printStackTrace();
            }
            if (complete)
                dtde.dropComplete(true);
        }
    }
    
    //聊天窗口
    private class ChatForm extends JFrame {
        private StyledDocument typedStr;
        private JTextArea inputText;
        private JLabel openFile;
        private SimpleAttributeSet styleIP;
        private SimpleAttributeSet styleRec;
        private SimpleAttributeSet styleSend;
        private SimpleAttributeSet styleDef;
        private Date date = null;
        private FriendItem contact;
        private int xPos = 0;
        private int yPos = 0;

        private ChatForm(FriendItem contact) {
            this.contact = contact;
            init();
        }

        private void init() {
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(null);
            mainPanel.setBounds(0, 0, 438, 495);

            JLabel nickname = new JLabel(contact.getNickname(), JLabel.CENTER);
            nickname.setBounds(119, 10, 200, 20);
            nickname.setFont(new Font("微软雅黑", Font.PLAIN, 17));
            nickname.setForeground(Color.WHITE);
            nickname.setOpaque(false);
            mainPanel.add(nickname);

            JLabel minIcon = new JLabel();
            minIcon.setBounds(378, 1, 30, 32);
            minIcon.setIcon(new ImageIcon(root + "min_normal.png"));
            minIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setExtendedState(JFrame.ICONIFIED);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    minIcon.setIcon(new ImageIcon(root + "min_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    minIcon.setIcon(new ImageIcon(root + "min_normal.png"));
                }
            });
            mainPanel.add(minIcon);

            JLabel closeIcon = new JLabel();
            closeIcon.setBounds(408, 0, 30, 32);
            closeIcon.setIcon(new ImageIcon(root + "close_normal.png"));
            closeIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    closeIcon.setIcon(new ImageIcon(root + "close_normal.png"));
                    onClose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    closeIcon.setIcon(new ImageIcon(root + "close_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeIcon.setIcon(new ImageIcon(root + "close_normal.png"));
                }
            });
            mainPanel.add(closeIcon);
            JTextPane outputText = new JTextPane();
            outputText.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(outputText);
            scrollPane.setBounds(15, 50, 408, 294);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            mainPanel.add(scrollPane);

            typedStr = outputText.getStyledDocument();
            styleIP = new SimpleAttributeSet();
            styleRec = new SimpleAttributeSet();
            styleDef = new SimpleAttributeSet();
            styleSend = new SimpleAttributeSet();

            StyleConstants.setFontFamily(styleIP, "微软雅黑");
            StyleConstants.setForeground(styleIP, new Color(40, 205, 251));
            StyleConstants.setUnderline(styleIP, true);
            StyleConstants.setFontSize(styleIP, 12);

            StyleConstants.setFontFamily(styleRec, "微软雅黑");
            StyleConstants.setForeground(styleRec, new Color(0, 54, 255));
            StyleConstants.setFontSize(styleRec, 12);
            StyleConstants.setFontFamily(styleSend, "微软雅黑");
            StyleConstants.setForeground(styleSend, new Color(23, 128, 99));
            StyleConstants.setFontSize(styleSend, 12);
            StyleConstants.setFontFamily(styleDef, "微软雅黑");
            StyleConstants.setForeground(styleDef, Color.BLACK);
            StyleConstants.setFontSize(styleDef, 13);
            StyleConstants.setSpaceAbove(styleDef, 1.5f);

            openFile = new JLabel();
            openFile.setBounds(38, 350, 28, 24);
            openFile.setIcon(null);
            openFile.setOpaque(false);
            openFile.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openFile.setIcon(null);
                    fileForm.setVisible(true);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    openFile.setIcon(new ImageIcon(root + "openfile.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    openFile.setIcon(null);
                }
            });
            mainPanel.add(openFile);
            
            inputText = new JTextArea();

            inputText.setBounds(5, 378, 428, 72);
            inputText.setLineWrap(true);
            inputText.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            inputText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        send();
                    else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                        onClose();
                }
            });
            inputText.setBorder(BorderFactory.createEmptyBorder());
            mainPanel.add(inputText);

            // 创建拖拽目标监听器
            DropTargetListener listener = new DropTargetListenerImpl(inputText);

            // 在 textArea 上注册拖拽目标监听器
            new DropTarget(inputText, DnDConstants.ACTION_COPY_OR_MOVE, listener, true);

            JLabel sendButton = new JLabel();
            sendButton.setBounds(270, 450, 64, 28);
            sendButton.setIcon(new ImageIcon(root + "button_send_normal.png"));
            sendButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    send();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    sendButton.setIcon(new ImageIcon(root + "button_send_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sendButton.setIcon(new ImageIcon(root + "button_send_normal.png"));
                }
            });
            mainPanel.add(sendButton);

            JLabel closeButton = new JLabel();
            closeButton.setBounds(350, 450, 64, 28);
            closeButton.setIcon(new ImageIcon(root + "button_close_normal.png"));
            closeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    closeButton.setIcon(new ImageIcon(root + "button_close_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeButton.setIcon(new ImageIcon(root + "button_close_normal.png"));
                }
            });
            mainPanel.add(closeButton);

            JLabel background = new JLabel();
            background.setBounds(0, 0, 438, 495);
            background.setIcon(new ImageIcon(root + "chat_background.png"));
            mainPanel.add(background);

            this.setUndecorated(true);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setBounds((screenSize.width - 438) / 2, (screenSize.height - 495) / 2, 438, 495);
            this.setResizable(false);
            HeadIcon icon = new HeadIcon(contact.getHeadPic());
            icon.setImage(icon.getImage().getScaledInstance(48, 48, Image.SCALE_DEFAULT));
            this.setIconImage(icon.getImage());
            this.setLayout(null);
            this.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (xPos == 0) return;
                    setLocation(getX() + (e.getXOnScreen() - xPos), getY() + (e.getYOnScreen() - yPos));
                    xPos = e.getXOnScreen();
                    yPos = e.getYOnScreen();
                }
            });
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (getMousePosition().getY() > 40)
                        return;
                    if (xPos == 0) {
                        xPos = e.getXOnScreen();
                        yPos = e.getYOnScreen();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (xPos == 0) return;
                    reLocate(e.getXOnScreen(), e.getYOnScreen());
                    xPos = 0;
                    yPos = 0;
                }
            });
            this.add(mainPanel);
            this.setVisible(true);
        }

        private void reLocate(int x, int y) {
            this.setLocation(this.getX() + (x - xPos), this.getY() + (y - yPos));
        }

        private void onClose() {
            chatPool.returnChatForm(chatPool.getChatForm(contact.getIp()));
            this.dispose();
            //this.setVisible(false);
        }

        private void send() {
            String filename = inputText.getText().trim();
            File file = new File(filename);
            if (!file.exists())
                try {
                    if (inputText.getText().trim().equals(""))
                        return;
                    date = new Date();
                    String dateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
                    typedStr.insertString(typedStr.getLength(), nick + "(", styleSend);
                    typedStr.insertString(typedStr.getLength(), ip, styleIP);
                    typedStr.insertString(typedStr.getLength(), ") " + dateTime, styleSend);
                    typedStr.insertString(typedStr.getLength(), "\n    " + inputText.getText() + "\n", styleDef);
                    msgSend(contact.getIp(), inputText.getText());
                    inputText.setText("");
                    inputText.requestFocus();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            else {
                fileForm.addFile("upload", contact, filename);
                fileForm.setVisible(true);
            }

        }

        private void receive(String strRec) {
            try {
                date = new Date();
                String dateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
                typedStr.insertString(typedStr.getLength(), contact.getNickname() + " (", styleRec);
                typedStr.insertString(typedStr.getLength(), contact.getIp(), styleIP);
                typedStr.insertString(typedStr.getLength(), ") " + dateTime, styleRec);
                typedStr.insertString(typedStr.getLength(), "\n    " + strRec + "\n", styleDef);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private class NoticeForm extends JFrame {
        private FriendItem user;
        private String state;

        private NoticeForm(FriendItem user, String state) {
            this.user = user;
            if (state.equals("OnConnect"))
                this.state = "上线通知：";
            else
                this.state = "下线通知：";
            init();
        }

        private void init() {

            JPanel panel = new JPanel();
            panel.setBounds(1760, 938, 160, 100);
            panel.setLayout(null);

            HeadIcon icon = new HeadIcon(user.getHeadPic());
            icon.setImage(icon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
            JLabel head = new JLabel();
            head.setBounds(10, 30, 50, 50);
            head.setIcon(icon);

            JLabel notice = new JLabel(state);
            notice.setBounds(10, 5, 80, 25);
            notice.setFont(new Font("微软雅黑", Font.BOLD, 12));
            panel.add(notice);

            JLabel name = new JLabel(user.getNickname());
            name.setBounds(70, 35, 80, 20);
            name.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            panel.add(name);

            JLabel ip = new JLabel("IP:" + user.getIp());
            ip.setBounds(70, 60, 80, 20);
            ip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            panel.add(ip);

            JLabel back = new JLabel();
            back.setBounds(0, 0, 160, 100);
            back.setOpaque(false);
            back.setIcon(new ImageIcon(root + "notice.png"));
            panel.add(back);
            panel.add(head);

            this.setUndecorated(true);
            this.setType(Window.Type.UTILITY);
            this.setBounds(1760, 938, 160, 100);
            this.add(panel);
            this.setVisible(true);
        }
    }

    //文件窗口
    private class FileForm extends JFrame {
        private JLabel[] upload;
        private JLabel[] username;
        private JLabel[] filename;
        private JLabel[] fileList;
        private JLabel[] transOf;
        private JLabel[] fileDir;
        private JPanel innerPanel;
        private int fileCount;
        private int xPos = 0;
        private int yPos = 0;
        private Socket fileSocket;
        private String fileSend;
        private String downloadPath;
        private FileItem[] sendList;
        private FileItem[] recList;
        private int sendCount;
        private Thread send;
        private Thread receive;
        private boolean waitForSend;
        private boolean alreadySend;
        private boolean alreadyRec;
        private int sendIndex;
        private final int maxFileCount = 50;

        private FileForm() {
            fileCount = 0;
            alreadySend = false;
            alreadyRec = true;
            sendCount = 0;
            downloadPath = "C:\\feiq\\download";
            if (!(new File(downloadPath).exists())) {
                new File(downloadPath).mkdirs();
            }
            downloadPath += "\\";
            this.setVisible(false);
            init();

            //连接文件传送服务器
            try {
                fileSocket = new Socket(Server.ip, 2048);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //接收线程
            receive = new Thread(() -> {
                while (true) fileRec();
            });
            receive.start();
            //发送线程
            send = new Thread(() -> {
                while (true) sendFile();
            });
            send.start();

            sendList = new FileItem[maxFileCount];
            recList = new FileItem[maxFileCount];
            for (int i = 0; i < maxFileCount; i++) {
                recList[i] = new FileItem();
                sendList[i] = new FileItem();
            }
        }

        private void startSend(String ip) {
            for (int i = 0; i < maxFileCount; i++) {
                if (sendList[i].ip.equals(ip)) {
                    fileSend = sendList[i].filename;
                    sendIndex = sendList[i].index;
                    sendList[i].ip = "";
                    sendList[i].filename = "";
                    sendList[i].index = 0;
                    sendCount--;
                    alreadySend = true;
                    break;
                }
            }
        }

        private void sendFile() {
            if (alreadySend) {
                try {
                    alreadySend = false;
                    File file = new File(fileSend);
                    FileInputStream fileInput = new FileInputStream(file);
                    OutputStream fileOutput = fileSocket.getOutputStream();
                    fileOutput.write((ip + "@" + file.getName() + "/" + file.length()).getBytes());
                    fileOutput.flush();
                    long size = file.length();
                    byte[] bytes = new byte[1024];
                    int len;
                    long sendSize = 0;
                    while ((len = fileInput.read(bytes)) != -1) {
                        fileOutput.write(bytes, 0, len);
                        fileOutput.flush();
                        sendSize += len;
                        transOf[sendIndex].setText((sendSize / size * 100) + "%");
                        Thread.sleep(500);
                    }
                    fileOutput.write("@END".getBytes());
                    fileOutput.flush();
                    fileInput.close();
                    transOf[sendIndex].setText("完成");
                    alreadySend = false;
                    waitForSend = false;
                    msgSend("SendOver", ip);
                    Thread.sleep(1000);
                    checkFileSend();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void fileRec() {
            if (alreadyRec) {
                try {
                    byte[] bytes = new byte[1024];
                    int len;
                    InputStream fileInput = fileSocket.getInputStream();
                    len = fileInput.read(bytes);
                    String str = new String(bytes, 0, len);
                    int pos = str.indexOf("@");
                    int pos2 = str.indexOf("/");
                    String ip = str.substring(0, pos);
                    String name = str.substring(pos + 1, pos2);
                    int size = Integer.parseInt(str.substring(pos2 + 1));
                    long recSize = 0;
                    int index = 0;
                    for (FriendItem list : friendList) {
                        if (list.getIp().equals(ip)) {
                            index = addFile("download", list, downloadPath + name);
                            this.setVisible(true);
                            break;
                        }
                    }
                    File file = new File(downloadPath + name);
                    if (!file.exists())
                        file.createNewFile();
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    while ((len = fileInput.read(bytes)) != -1) {
                        String txt = new String(bytes);
                        if (txt.contains("@END")) {
                            bytes = txt.substring(0, txt.indexOf("@END")).getBytes();
                            len = bytes.length;
                        }
                        fileOutput.write(bytes, 0, len);
                        recSize += len;
                        transOf[index].setText((recSize / size * 100) + "%");
                        if (txt.contains("@END")) break;
                    }
                    fileOutput.flush();
                    fileOutput.close();
                    transOf[index].setText("完成");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void checkFileSend() {
            if (sendCount > 0) {
                if (!waitForSend) {
                    for (int i = 0; i < maxFileCount; i++) {
                        if (!sendList[i].ip.equals("")) {
                            msgSend("FileSend", sendList[i].ip);
                            waitForSend = true;
                            return;
                        }
                    }
                }
            }
        }

        private void appendSendList(String ip, String filename, int index) {
            for (int i = 0; i < maxFileCount; i++) {
                if (sendList[i].filename.equals("")) {
                    sendList[i].ip = ip;
                    sendList[i].filename = filename;
                    sendList[i].index = index;
                    sendCount++;
                    break;
                }
            }

            checkFileSend();
        }

        private int addFile(String type, FriendItem user, String name) {
            if (fileCount == 0) {
                fileCount++;
                upload = new JLabel[fileCount];
                filename = new JLabel[fileCount];
                username = new JLabel[fileCount];
                transOf = new JLabel[fileCount];
                fileList = new JLabel[fileCount];
                fileDir = new JLabel[fileCount];
            } else {
                fileCount++;
                upload = Arrays.copyOf(upload, fileCount);
                username = Arrays.copyOf(username, fileCount);
                filename = Arrays.copyOf(filename, fileCount);
                transOf = Arrays.copyOf(filename, fileCount);
                fileList = Arrays.copyOf(fileList, fileCount);
                fileDir = Arrays.copyOf(fileDir, fileCount);

            }
            int top = (fileCount - 1) * 30;
            upload[fileCount - 1] = new JLabel();
            upload[fileCount - 1].setIcon(new ImageIcon(root + type + ".png"));
            upload[fileCount - 1].setBounds(10, top, 30, 30);

            filename[fileCount - 1] = new JLabel();
            filename[fileCount - 1].setText(new File(name).getName());
            filename[fileCount - 1].setFont(new Font("微软雅黑", Font.PLAIN, 14));
            filename[fileCount - 1].setBounds(50, top, 120, 30);

            username[fileCount - 1] = new JLabel();
            username[fileCount - 1].setText(user.getIp());
            username[fileCount - 1].setFont(new Font("微软雅黑", Font.PLAIN, 14));
            username[fileCount - 1].setBounds(180, top, 120, 30);

            transOf[fileCount - 1] = new JLabel();
            transOf[fileCount - 1].setFont(new Font("微软雅黑", Font.PLAIN, 14));
            transOf[fileCount - 1].setBounds(310, top, 80, 30);
            if (type.equals("upload"))
                transOf[fileCount - 1].setText("等待发送");
            else
                transOf[fileCount - 1].setText("等待接受");

            fileList[fileCount - 1] = new JLabel();
            fileList[fileCount - 1].setBounds(0, top, 430, 30);
            fileList[fileCount - 1].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    JLabel label = (JLabel) e.getSource();
                    label.setIcon(new ImageIcon(root + "filelist.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    JLabel label = (JLabel) e.getSource();
                    label.setIcon(null);
                }
            });

            fileDir[fileCount - 1] = new JLabel();
            fileDir[fileCount - 1].setIcon(new ImageIcon(root + "filedir.png"));
            fileDir[fileCount - 1].setBounds(390, top, 30, 30);
            fileDir[fileCount - 1].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String[] cmdDir = {"explorer.exe", new File(name).getParent()};
                    try {
                        Runtime.getRuntime().exec(cmdDir);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });

            innerPanel.add(upload[fileCount - 1]);
            innerPanel.add(filename[fileCount - 1]);
            innerPanel.add(username[fileCount - 1]);
            innerPanel.add(transOf[fileCount - 1]);
            innerPanel.add(fileDir[fileCount - 1]);
            innerPanel.add(fileList[fileCount - 1]);
            innerPanel.setPreferredSize(new Dimension(430, fileCount * 30 + 30));

            if (type.equals("upload")) appendSendList(user.getIp(), name, fileCount - 1);
            return fileCount - 1;
        }

        private void init() {
            JPanel panel = new JPanel();
            panel.setBounds(0, 0, 430, 260);
            panel.setLayout(null);

            innerPanel = new JPanel();
            innerPanel.setBackground(Color.WHITE);
            innerPanel.setLayout(null);

            JScrollPane scroll = new JScrollPane(innerPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setBounds(0, 38, 450, 210);

            JScrollBar vScrollBar = scroll.getVerticalScrollBar();
            vScrollBar.setUnitIncrement(5);

            scroll.setBackground(Color.white);
            panel.add(scroll);

            JLabel closeButton = new JLabel();
            closeButton.setBounds(400, 0, 30, 32);
            closeButton.setIcon(new ImageIcon(root + "close_normal.png"));
            closeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClose();
                    closeButton.setIcon(new ImageIcon(root + "close_normal.png"));
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    closeButton.setIcon(new ImageIcon(root + "close_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeButton.setIcon(new ImageIcon(root + "close_normal.png"));
                }
            });
            panel.add(closeButton);

            JLabel minButton = new JLabel();
            minButton.setBounds(370, 0, 30, 32);
            minButton.setIcon(new ImageIcon(root + "min_normal.png"));
            minButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    minButton.setIcon(new ImageIcon(root + "min_normal.png"));
                    onMin();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    minButton.setIcon(new ImageIcon(root + "min_onfocus.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    minButton.setIcon(new ImageIcon(root + "min_normal.png"));
                }
            });
            panel.add(minButton);

            JLabel back = new JLabel();
            back.setBounds(0, 0, 430, 260);
            back.setIcon(new ImageIcon(root + "fileform.png"));
            panel.add(back);
            this.add(panel);
            this.setUndecorated(true);
            this.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (xPos == 0) return;
                    setLocation(getX() + (e.getXOnScreen() - xPos), getY() + (e.getYOnScreen() - yPos));
                    xPos = e.getXOnScreen();
                    yPos = e.getYOnScreen();
                }
            });
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (getMousePosition().getY() > 38)
                        return;
                    if (xPos == 0) {
                        xPos = e.getXOnScreen();
                        yPos = e.getYOnScreen();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (xPos == 0) return;
                    setLocation(getX() + (e.getXOnScreen() - xPos), getY() + (e.getYOnScreen() - yPos));
                    xPos = 0;
                    yPos = 0;
                }
            });
            this.setBounds((1920 - 430) / 2, (1080 - 260) / 2, 430, 260);
        }

        private void onClose() {
            this.dispose();
        }

        private void onMin() {
            this.setExtendedState(JFrame.ICONIFIED);
        }
    }

    public static void main(String[] args) {
        new FlynnQQ();
    }
}