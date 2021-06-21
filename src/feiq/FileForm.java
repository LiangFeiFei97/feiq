//package feiq;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseMotionAdapter;
//import java.io.*;
//import java.net.Socket;
//import java.util.Arrays;
//
////TODO file
//public class FileForm extends JFrame {
//
//    private JLabel[] upload;
//    private JLabel[] username;
//    private JLabel[] filename;
//    private JLabel[] fileList;
//    private JLabel[] transOf;
//    private JLabel[] fileDir;
//    private JPanel innerPanel;
//    private int fileCount;
//    private int xPos = 0;
//    private int yPos = 0;
//    private Socket fileSocket;
//    private String fileRec;
//    private String fileSend;
//    private String downloadPath;
//    private FileItem[] sendList;
//    private FileItem[] recList;
//    private int sendCount;
//    private int recCount;
//    private Thread send;
//    private Thread receive;
//    private boolean isSend;
//    private boolean isRec;
//    private int sendIndex;
//    private final int maxFileCount = 50;
//
//    private FileForm() {
//        fileCount = 0;
//        isSend = false;
//        isRec = true;
//        sendCount = 0;
//        recCount = 0;
//        downloadPath = "C:\\feiq\\download";
//        if (!(new File(downloadPath).exists()))
//            new File(downloadPath).mkdirs();
//        downloadPath += "\\";
//        this.setVisible(false);
//        init();
//        try {
//            fileSocket = new Socket(Server.ip, 2048);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        receive = new Thread(() -> {
//            if (isSend)
//                fileRec();
//        });
//        receive.start();
//        send = new Thread(() -> {
//            if (isRec)
//                fileSend();
//        });
//        sendList = new FileItem[maxFileCount];
//        recList = new FileItem[maxFileCount];
//        for (int i = 0; i < maxFileCount; i++) {
//            recList[i] = new FileItem();
//            sendList[i] = new FileItem();
//        }
//    }
//
//    private void isSend(String ip) {
//        for (int i = 0; i < maxFileCount; i++) {
//            if (sendList[i].ip.equals(ip)) {
//                fileSend = sendList[i].filename;
//                sendIndex = sendList[i].index;
//                sendList[i].ip = "";
//                sendList[i].filename = "";
//                sendList[i].index = 0;
//                sendCount--;
//                isSend = true;
//                break;
//            }
//        }
//    }
//
////        class FileItem {
////            private String ip;
////            private String filename;
////            private int index;
////
////            FileItem() {
////                this.ip = "";
////                this.filename = "";
////                this.index = 0;
////            }
////        }
//
//    private void fileSend() {
//        try {
//            File file = new File(fileSend);
//            FileInputStream fileInput = new FileInputStream(file);
//            OutputStream fileOutput = fileSocket.getOutputStream();
//            fileOutput.write((ip + "@" + file.getName() + "/" + file.length()).getBytes());
//            long size = file.length();
//            byte[] bytes = new byte[1024];
//            int len;
//            long sendSize = 0;
//            while ((len = fileInput.read(bytes)) != -1) {
//                fileOutput.write(bytes, 0, len);
//                sendSize += len;
//                transOf[sendIndex].setText((sendSize / size * 100) + "%");
//            }
//            transOf[sendIndex].setText("发送成功");
//            isSend = false;
//            sendOver();
//            msgSend("SendOver", ip);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void fileRec() {
//        try {
//            byte[] bytes = new byte[1024];
//            int len;
//            InputStream fileInput = fileSocket.getInputStream();
//            len = fileInput.read(bytes);
//            String str = new String(bytes, 0, len);
//            int pos = str.indexOf("@");
//            int pos2 = str.indexOf("/");
//            String ip = str.substring(0, pos);
//            String name = str.substring(pos + 1, pos2);
//            int size = Integer.parseInt(str.substring(pos2 + 1));
//            long recSize = 0;
//            int index = 0;
//            for (FriendItem list : friendList) {
//                if (list.getIp().equals(ip)) {
//                    index = addFile("download", list, name);
//                    break;
//                }
//            }
//            File file = new File(downloadPath + name);
//            if (!file.exists())
//                file.createNewFile();
//            FileOutputStream fileOutput = new FileOutputStream(file);
//            while ((len = fileInput.read(bytes)) != -1) {
//                fileOutput.write(bytes, 0, len);
//                recSize += len;
//                transOf[index].setText((recSize / size * 100) + "%");
//            }
//            transOf[index].setText("接收成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void sendOver() {
//        if (sendCount > 0) {
//            if (!isSend) {
//                for (int i = 0; i < maxFileCount; i++) {
//                    if (!sendList[i].ip.equals("")) {
//                        msgSend("FileSend", sendList[i].ip);
//                    }
//                }
//
//            }
//        }
//    }
//
//    private void appendSendList(String ip, String filename, int index) {
//        for (int i = 0; i < maxFileCount; i++) {
//            if (sendList[i].filename.equals("")) {
//                sendList[i].ip = ip;
//                sendList[i].filename = filename;
//                sendList[i].index = index;
//                sendCount++;
//                break;
//            }
//        }
//        sendOver();
//    }
//
//
//    private int addFile(String type, FriendItem user, String name) {
//        if (fileCount == 0) {
//            fileCount++;
//            upload = new JLabel[fileCount];
//            filename = new JLabel[fileCount];
//            username = new JLabel[fileCount];
//            transOf = new JLabel[fileCount];
//            fileList = new JLabel[fileCount];
//            fileDir = new JLabel[fileCount];
//        } else {
//            fileCount++;
//            upload = Arrays.copyOf(upload, fileCount);
//            username = Arrays.copyOf(username, fileCount);
//            filename = Arrays.copyOf(filename, fileCount);
//            transOf = Arrays.copyOf(filename, fileCount);
//            fileList = Arrays.copyOf(fileList, fileCount);
//            fileDir = Arrays.copyOf(fileDir, fileCount);
//
//        }
//        int top = (fileCount - 1) * 30;
//        upload[fileCount - 1] = new JLabel();
//        upload[fileCount - 1].setIcon(new ImageIcon(root + type + ".png"));
//        upload[fileCount - 1].setBounds(10, top, 30, 30);
//
//        filename[fileCount - 1] = new JLabel();
//        filename[fileCount - 1].setText(new File(name).getName());
//        filename[fileCount - 1].setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        filename[fileCount - 1].setBounds(50, top, 120, 30);
//
//        username[fileCount - 1] = new JLabel();
//        username[fileCount - 1].setText(user.getNickname());
//        username[fileCount - 1].setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        username[fileCount - 1].setBounds(180, top, 150, 30);
//
//        transOf[fileCount - 1] = new JLabel("0%");
//        transOf[fileCount - 1].setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        transOf[fileCount - 1].setBounds(350, top, 40, 30);
//
//        fileList[fileCount - 1] = new JLabel();
//        fileList[fileCount - 1].setBounds(0, top, 430, 30);
//        fileList[fileCount - 1].addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                JLabel label = (JLabel) e.getSource();
//                label.setIcon(new ImageIcon(root + "filelist.png"));
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                JLabel label = (JLabel) e.getSource();
//                label.setIcon(null);
//            }
//        });
//
//        fileDir[fileCount - 1] = new JLabel();
//        fileDir[fileCount - 1].setIcon(new ImageIcon(root + "filedir.png"));
//        fileDir[fileCount - 1].setBounds(390, top, 30, 30);
//
//        innerPanel.add(upload[fileCount - 1]);
//        innerPanel.add(filename[fileCount - 1]);
//        innerPanel.add(username[fileCount - 1]);
//        innerPanel.add(transOf[fileCount - 1]);
//        innerPanel.add(fileDir[fileCount - 1]);
//        innerPanel.add(fileList[fileCount - 1]);
//        innerPanel.setPreferredSize(new Dimension(430, fileCount * 30 + 30));
//
//        if (type.equals("upload"))
//            appendSendList(user.getIp(), name, fileCount - 1);
//        return fileCount - 1;
//    }
//
//    private void init() {
//        JPanel panel = new JPanel();
//        panel.setBounds(0, 0, 430, 260);
//        panel.setLayout(null);
//
//        innerPanel = new JPanel();
//        innerPanel.setBackground(Color.WHITE);
//        innerPanel.setLayout(null);
//
//        JScrollPane scroll = new JScrollPane(innerPanel);
//        scroll.setBorder(BorderFactory.createEmptyBorder());
//        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        scroll.setBounds(0, 38, 450, 210);
//
//        JScrollBar vScrollBar = scroll.getVerticalScrollBar();
//        vScrollBar.setUnitIncrement(5);
//
//        scroll.setBackground(Color.white);
//        panel.add(scroll);
//
//        JLabel closeButton = new JLabel();
//        closeButton.setBounds(400, 0, 30, 32);
//        closeButton.setIcon(new ImageIcon(root + "close_normal.png"));
//        closeButton.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                onClose();
//                closeButton.setIcon(new ImageIcon(root + "close_normal.png"));
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                closeButton.setIcon(new ImageIcon(root + "close_onfocus.png"));
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                closeButton.setIcon(new ImageIcon(root + "close_normal.png"));
//            }
//        });
//        panel.add(closeButton);
//
//        JLabel minButton = new JLabel();
//        minButton.setBounds(370, 0, 30, 32);
//        minButton.setIcon(new ImageIcon(root + "min_normal.png"));
//        minButton.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                minButton.setIcon(new ImageIcon(root + "min_normal.png"));
//                onMin();
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                minButton.setIcon(new ImageIcon(root + "min_onfocus.png"));
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                minButton.setIcon(new ImageIcon(root + "min_normal.png"));
//            }
//        });
//        panel.add(minButton);
//
//        JLabel back = new JLabel();
//        back.setBounds(0, 0, 430, 260);
//        back.setIcon(new ImageIcon(root + "fileform.png"));
//        panel.add(back);
//        this.add(panel);
//        this.setUndecorated(true);
//        this.addMouseMotionListener(new MouseMotionAdapter() {
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                if (xPos == 0) return;
//                setLocation(getX() + (e.getXOnScreen() - xPos), getY() + (e.getYOnScreen() - yPos));
//                xPos = e.getXOnScreen();
//                yPos = e.getYOnScreen();
//            }
//        });
//        this.addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                if (getMousePosition().getY() > 38)
//                    return;
//                if (xPos == 0) {
//                    xPos = e.getXOnScreen();
//                    yPos = e.getYOnScreen();
//                }
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                if (xPos == 0) return;
//                setLocation(getX() + (e.getXOnScreen() - xPos), getY() + (e.getYOnScreen() - yPos));
//                xPos = 0;
//                yPos = 0;
//            }
//        });
//        this.setBounds((1920 - 430) / 2, (1080 - 260) / 2, 430, 260);
//    }
//
//    private void onClose() {
//        this.dispose();
//    }
//
//    private void onMin() {
//        this.setExtendedState(JFrame.ICONIFIED);
//    }
//}
