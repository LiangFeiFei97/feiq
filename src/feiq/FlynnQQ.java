package feiq;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.bind.annotation.XmlType;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FlynnQQ {
    private LoginForm loginForm = null;
    private CharForm charForm = null;
    private MainForm mainForm = null;
    private MySQL mysql = null;
    private String root = null;
    private String ip = null;
    private Socket socket = null;
    private PreparedStatement ps = null;
    private HeadIcon[] headIcon = null;
    private int headCount;
    private int head_num;
    private HeadIcon userIcon = null;

    private FlynnQQ() {
        init();
    }

    private void init() {
        root = "./images/";
        headCount = 45;
        head_num = 2;
        headIcon = new HeadIcon[headCount + 1];
        for (int i = 1; i <= headCount; i++) {
            headIcon[i] = new HeadIcon(i + ".jpg");
            headIcon[i].setIndex(i);
        }
        loginForm = new LoginForm();
    }

    private static class HeadIcon extends ImageIcon {
        private int index;

        HeadIcon(String filename) {
            super("./headicons/" + filename);
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    //登录窗口
    private class LoginForm extends JFrame {
        private JLabel loadPic;
        private JLabel loginPic;
        private JLabel IPText;
        private JTextField nickText;
        private JLabel tip;
        private String nick;
        private String head_path;
        private double xPos = 0;
        private double yPos = 0;
        private int xStart = 0;
        private int yStart = 0;
        private boolean startMove = false;
        private Thread load = null;
        private Connection connection;
        private JLabel headFirst;
        private JLabel headNow;
        private JLabel headNext;
        private JLabel headLoad;
        private HeadIcon icon;
        private boolean isFind;

        //TODO LoginForm
        private LoginForm() {
            init();
            load = null;
            isNext();
            IPText.setVisible(true);
            nickText.setVisible(true);
            loadPic.setVisible(false);
            headLoad.setVisible(false);
            tip.setVisible(true);
            this.setVisible(true);
//            verity();
        }

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

        private void init() {
            //窗口去边框
            this.setUndecorated(true);

            //设置窗口拖动和关闭
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
                    loginForm.dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    min.setIcon(new ImageIcon(root + "minicon.png"));
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
                    close.setIcon(new ImageIcon(root + "closeicon.png"));
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
//                    connect();
                    nick = nickText.getText();
                    userIcon = new HeadIcon(head_path);
                    mainForm = new MainForm(nick);
                }
            });
            loginButton.setBounds(93, 247, 218, 30);
            Panel.add(loginButton);

            ImageIcon backPic = new ImageIcon(root + "FirstPage.png");
            loginPic = new JLabel();
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
            headNow.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int scroll = e.getWheelRotation();
                    if (scroll > 0)
                        isNext();
                    else if (scroll < 0)
                        isLast();

                }
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
            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (getMousePosition().getY() > 40)
                        return;
                    if (!startMove) {
                        xStart = loginForm.getX();
                        yStart = loginForm.getY();
                        xPos = e.getXOnScreen();
                        yPos = e.getYOnScreen();
                        startMove = true;
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!startMove) return;
                    loginForm.setLocation(xStart + (int) (e.getXOnScreen() - xPos), yStart + (int) (e.getYOnScreen() - yPos));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!startMove) return;
//                    System.out.println(loginForm.getX() + " " + loginForm.getY());
//                    System.out.println(e.getXOnScreen() + " " + e.getYOnScreen());
                    startMove = false;
                    xPos = 0;
                    yPos = 0;
                }
            };
            this.addMouseListener(adapter);
            this.addMouseMotionListener(adapter);
            this.add(Panel);
            this.setVisible(false);
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

        private void verity() {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
                IPText.setText(ip);
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://10.8.24.98/feiq", "root", "123456");
                ps = connection.prepareStatement("Select * from users where ip = ?");
                ps.setString(1, ip);
                ResultSet rs = ps.executeQuery();
                isFind = false;
                if (rs.next()) {
                    isFind = true;
                    nick = rs.getString("nickname");
                    head_path = rs.getString("headpic");
                    connect();
                } else {
                    isNext();
                    this.setVisible(true);
                }
            } catch (Exception e) {
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

        private void connect() {
            load = new Thread(new Runnable() {
                @Override
                public void run() {
                    loginVerity();
                }
            });
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

        private void loginVerity() {
            try {
                Thread.sleep(1000);
                boolean result = true;
                try {
                    socket = new Socket("10.8.24.98", 2020);
                } catch (Exception e) {
                    result = false;
                }
                if (result) {
                    if (!isFind) {
                        signUp();
                        userIcon = new HeadIcon(head_num + ".jpg");
                    } else
                        userIcon = new HeadIcon(head_path);

                    mainForm = new MainForm(nick);
                    loginForm.setVisible(false);
                    loginForm = null;
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
    }


    //主窗口
    private class MainForm extends JFrame {
        private JLabel[] friendList;
        private JLabel[] headList;
        private JLabel[] nameList;
        private JPanel mainPanel;
        private JTextField search;
        private JScrollPane scrollPane;
        private String username;
        private JLabel min;
        private JLabel close;
        private double xPos = 0;
        private double yPos = 0;
        private int selectNow;
        private int normal_width = 280;
        private int normal_height = 585;

        private int list_normal_height = 40;
        private int list_onfocus_height = 60;
        private int list_left_padding = 20;
        private ImageIcon list_normal_bg;
        private ImageIcon list_onfocus_bg;
        private ImageIcon list_checked_bg;

        private int head_normal_height = 30;
        private int head_onfocus_height = 48;
        private int head_normal_top_padding = 5;
        private int head_onfocus_top_padding = 6;
        private ImageIcon head_normal_bg;
        private ImageIcon head_checked_bg;

        private int name_normal_width = 180;
        private int name_normal_height = 40;
        private int name_normal_padding = 70;
        private int name_onfocus_width = 162;
        private int name_onfocus_height = 60;
        private int name_onfocus_padding = 88;

        private MainForm(String username) {
            this.username = username;
            init();
            listRefresh(new JLabel());
        }

        private void init() {
            this.setUndecorated(true);
            list_normal_bg = new ImageIcon(root + "list_white.png");
            list_onfocus_bg = new ImageIcon(root + "list_blue.png");
            list_checked_bg = new ImageIcon(root + "list_checked_blue.png");
            head_normal_bg = new ImageIcon(root + "head_normal.png");
            head_checked_bg = new ImageIcon(root + "head_checked.jpg");
            selectNow = -1;
            min = new JLabel();
            close = new JLabel();
            JPanel panel = new JPanel();
            panel.setLayout(null);
            panel.setBackground(Color.white);
            panel.setVisible(true);

            mainPanel = new JPanel();
            mainPanel.setBounds(0, 0, normal_width, normal_height);
            mainPanel.setLayout(null);


            JLabel userName = new JLabel(username);
            userName.setFont(new Font("微软雅黑", Font.BOLD, 14));
            userName.setForeground(Color.WHITE);
            userName.setBounds(105, 50, 125, 25);
            mainPanel.add(userName);

            search = new JTextField();
            search.setOpaque(false);
            search.setBounds(35, 110, 240, 20);
            search.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            search.setBorder(BorderFactory.createEmptyBorder());
            mainPanel.add(search);

            scrollPane = new JScrollPane(panel);
            JLabel scroll = new JLabel(new ImageIcon(root + "Scrollbarlostfocus.png"));
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

            friendList = new JLabel[10];
            headList = new JLabel[10];
            nameList = new JLabel[10];
            for (int i = 0; i < friendList.length; i++) {

                nameList[i] = new JLabel("Nickname(10.8.24." + (i + 97) + ")");
                nameList[i].setFont(new Font("微软雅黑", Font.PLAIN, 13));
                nameList[i].setOpaque(false);
                //nameList[i].setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
                nameList[i].setBounds(name_normal_padding, i * name_normal_height, name_normal_width, name_normal_height);

                headList[i] = new JLabel(head_normal_bg);
                headList[i].setOpaque(false);
                headList[i].setBounds(list_left_padding, head_normal_top_padding + i * list_normal_height, normal_width, head_normal_height);

                friendList[i] = new JLabel(list_normal_bg);
                friendList[i].setOpaque(false);
                friendList[i].addMouseListener(new myMouseListener());
                friendList[i].setBounds(0, i * list_normal_height, normal_width, list_normal_height);

                panel.setPreferredSize(new Dimension(280, (i + 2) * list_normal_height));
                panel.add(nameList[i]);
                panel.add(friendList[i]);
                panel.add(headList[i]);
            }
            mainPanel.add(scrollPane);

            min = new JLabel();
            min.setBounds(220, 0, 30, 32);
            min.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    mainForm.dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    min.setIcon(new ImageIcon(root + "minicon.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    min.setIcon(null);
                }
            });
            mainPanel.add(min);

            close = new JLabel();
            close.setBounds(250, 0, 30, 32);
            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    mainForm = null;
                    System.exit(0);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(new ImageIcon(root + "closeicon.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(null);
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
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        private class myMouseListener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                if (e.getClickCount() == 2) {
                    if (charForm == null)
                        charForm = new CharForm();
                    else
                        charForm.setVisible(true);
                    charForm.inputText.requestFocus();
                } else if (e.getClickCount() == 1) {
                    for (int i = 0; i < friendList.length; i++)
                        if (friendList[i] == label) {
                            selectNow = i;
                            break;
                        }
                    listRefresh(label);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                listRefresh(label);
            }
        }

        private void listRefresh(JLabel label) {
            for (int i = 0; i < friendList.length; i++) {
                if (i < selectNow || selectNow < 0) {
                    nameList[i].setBounds(name_normal_padding, i * name_normal_height, name_normal_width, name_normal_height);
                    friendList[i].setBounds(0, i * list_normal_height, normal_width, list_normal_height);
                    friendList[i].setIcon(list_normal_bg);
                    headList[i].setBounds(list_left_padding, i * list_normal_height + head_normal_top_padding, head_normal_height, head_normal_height);
                    headList[i].setIcon(head_normal_bg);
                } else if (i == selectNow) {
                    nameList[i].setBounds(name_onfocus_padding, i * name_normal_height, name_onfocus_width, name_onfocus_height);
                    friendList[i].setBounds(0, i * list_normal_height, normal_width, list_onfocus_height);
                    friendList[i].setIcon(list_checked_bg);
                    headList[i].setBounds(list_left_padding, i * list_normal_height + head_onfocus_top_padding, head_onfocus_height, head_onfocus_height);
                    headList[i].setIcon(head_checked_bg);
                } else if (i > selectNow) {
                    nameList[i].setBounds(name_normal_padding, i * name_normal_height + (name_onfocus_height - name_normal_height), name_normal_width, name_normal_height);
                    friendList[i].setBounds(0, i * list_normal_height + (list_onfocus_height - list_normal_height), normal_width, list_normal_height);
                    friendList[i].setIcon(list_normal_bg);
                    headList[i].setBounds(list_left_padding, i * list_normal_height + head_normal_top_padding + (list_onfocus_height - list_normal_height), head_normal_height, head_normal_height);
                    headList[i].setIcon(head_normal_bg);

                }
                if (friendList[i] == label && i != selectNow) {
                    friendList[i].setIcon(list_onfocus_bg);
                }
            }
        }
    }


    //聊天窗口
    private class CharForm extends JFrame {
        private StyledDocument typedStr;
        private JTextArea inputText;
        private SimpleAttributeSet styleRec;
        private SimpleAttributeSet styleSend;
        private SimpleAttributeSet styleDef;
        private DatagramSocket dsRec;
        private DatagramSocket dsSend;
        private DatagramPacket dpRec;
        private DatagramPacket dpSend;
        private InetSocketAddress address;
        private Date date;

        private CharForm() {
            init();
            connect();
        }

        private void connect() {
            Thread rec = new Thread(new Receive());
            rec.start();
        }

        class Receive implements Runnable {
            @Override
            public void run() {
                try {
                    dpRec = new DatagramPacket(new byte[2048], 2048);
                    while (true) {
                        dsRec.receive(dpRec);
                        byte[] data = dpRec.getData();
                        date = new Date();
                        String dateTime = new SimpleDateFormat("HH:mm:ss").format(date);
                        typedStr.insertString(typedStr.getLength(), "袁启(" + dpRec.getAddress() + ")" + dateTime, styleRec);
                        typedStr.insertString(typedStr.getLength(), "\n" + new String(data, 0, dpRec.getLength()) + "\n\n", styleDef);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        private void init() {
            date = new Date();

            try { // 使用Windows的界面风格
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                dsRec = new DatagramSocket(2009);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setBounds((screenSize.width - 500) / 2, (screenSize.height - 600) / 2, 510, 600);
            this.setResizable(false);
            this.setLayout(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(null);
            mainPanel.setBounds(0, 0, 500, 600);

            JTextPane outputText = new JTextPane();
            outputText.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(outputText);
            scrollPane.setBounds(10, 10, 480, 400);
            mainPanel.add(scrollPane);

            typedStr = outputText.getStyledDocument();
            styleRec = new SimpleAttributeSet();
            styleDef = new SimpleAttributeSet();
            styleSend = new SimpleAttributeSet();

            StyleConstants.setFontFamily(styleRec, "微软雅黑");
            StyleConstants.setForeground(styleRec, Color.BLUE);
            StyleConstants.setFontSize(styleRec, 12);
            StyleConstants.setFontFamily(styleSend, "微软雅黑");
            StyleConstants.setForeground(styleSend, Color.RED);
            StyleConstants.setFontSize(styleSend, 12);
            StyleConstants.setFontFamily(styleDef, "微软雅黑");
            StyleConstants.setForeground(styleDef, Color.BLACK);
            StyleConstants.setFontSize(styleDef, 16);

            inputText = new JTextArea();
            inputText.setBounds(10, 420, 480, 100);
            inputText.setLineWrap(true);
            inputText.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            inputText.setBorder(BorderFactory.createLineBorder(Color.black, 1));
            mainPanel.add(inputText);

            JButton sendButton = new JButton("发送");
            sendButton.setBounds(340, 530, 60, 30);
            sendButton.addActionListener(new MyListener());
            mainPanel.add(sendButton);

            JButton closeButton = new JButton("关闭");
            closeButton.setBounds(430, 530, 60, 30);
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    charForm.setVisible(false);
                }
            });
            mainPanel.add(closeButton);

            this.add(mainPanel);
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        class MyListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dsSend = new DatagramSocket();
                    //for(String port:portList)
                    address = new InetSocketAddress("255.255.255.255", 2019);
                    byte[] data;
                    data = inputText.getText().getBytes();
                    dpSend = new DatagramPacket(data, data.length, address);
                    date = new Date();
                    String dateTime = new SimpleDateFormat("HH:mm:ss").format(date);
                    typedStr.insertString(typedStr.getLength(), "Flynn(127.0.0.1)" + dateTime, styleSend);
                    typedStr.insertString(typedStr.getLength(), "\n" + inputText.getText() + "\n\n", styleDef);

                    dsSend.send(dpSend);
                    inputText.setText("");
                    inputText.requestFocus();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        new FlynnQQ();
    }
}