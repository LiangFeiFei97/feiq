package feiq;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
import java.awt.Component;
import javax.swing.border.LineBorder;
import java.awt.AlphaComposite;
import java.awt.GradientPaint;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.JFrame;

import java.awt.EventQueue;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JLabel;

import com.sun.awt.AWTUtilities;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.JPasswordField;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
/**
 * 用户登录窗体（按照设计图片效果制作）
 * @author admin
 *
 */
public class Login1{

    private JFrame frame;//窗体
    private JTextField userNameField;//用户名输入框
    private JPasswordField passwordField;//密码输入框
    private JTextField verifyCodeField;//验证码输入框
    private String verifyCode;//验证码图片中的验证码值

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Login1 window = new Login1();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * Create the application.
     */
    private Login1() {
        initialize();
    }
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        //将存储验证码文件夹下的所有验证码图片删除
        FileUtils.deleteAllFiles("./verifyCodeImg");
        //自定义圆角输入框边界线
        MyLineBorder myLineBorder = new MyLineBorder(new Color(192, 192, 192), 1 , true);
        //只显示输入框的下边框
        MatteBorder bottomBorder = new MatteBorder(0, 0, 1, 0, new Color(192, 192, 192));
        //设置JFrame禁用本地外观，使用下面自定义设置的外观；
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame();
        frame.setBounds(0, 0, 300, 490);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        /**
         * 对窗体进行基本设置
         */
        //设置窗体在计算机窗口的中心部位显示
        frame.setLocationRelativeTo(frame.getOwner());
        // 去掉窗口的装饰
        frame.setUndecorated(true);
        //采用指定的窗口装饰风格
        frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        //设置窗体圆角，最后两个参数分别为圆角的宽度、高度数值，一般这两个数值都是一样的
        AWTUtilities.setWindowShape(frame,
                new RoundRectangle2D.Double(0.0D, 0.0D, frame.getWidth(), frame.getHeight(), 20.0D, 20.0D));
        //设置背景颜色，记住一定要修改frame.getContentPane()的颜色，因为我们看到的都是这个的颜色而并不是frame的颜色
        frame.getContentPane().setBackground(Color.white);
        /**
         * 插入顶部非凡汽车背景图片
/*         *//*
        //创建具有分层的JLayeredPane
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, -5, 300, 200);
        frame.getContentPane().add(layeredPane);
        // 创建图片对象
        ImageIcon img = new ImageIcon(Login1.class.getResource("/images/dingbu.jpg"));
        //设置图片在窗体中显示的宽度、高度
        img.setImage(img.getImage().getScaledInstance(300, 200,Image.SCALE_DEFAULT));

        JPanel panel = new JPanel();
        panel.setBounds(0, -5, 300, 200);
        layeredPane.add(panel, JLayeredPane.DEFAULT_LAYER);

        JLabel lblNewLabel = new JLabel("");
        panel.add(lblNewLabel);
        lblNewLabel.setIcon(img);
        *//**
         * 插入窗体关闭的背景图片及功能
         *//*
        // 创建图片对象
        ImageIcon closeImg = new ImageIcon(Login1.class.getResource("/images/close.png"));
        //设置图片在窗体中显示的宽度、高度
        closeImg.setImage(closeImg.getImage().getScaledInstance(31, 31,Image.SCALE_DEFAULT));

        JPanel closePanel = new JPanel();
        closePanel.setBounds(269, -5, 31, 31);
        layeredPane.add(closePanel,JLayeredPane.MODAL_LAYER);

        JLabel closeLabel = new JLabel("");
        closePanel.add(closeLabel);
        closeLabel.setIcon(closeImg);
        closeLabel.addMouseListener(new MouseAdapter() {
            //鼠标点击关闭图片，实现关闭窗体的功能
            @Override
            public void mouseClicked(MouseEvent e) {
                //dispose();
                System.exit(0);//使用dispose();也可以关闭只是不是真正的关闭
            }
            //鼠标进入，换关闭的背景图片
            @Override
            public void mouseEntered(MouseEvent e) {
                // 创建图片对象
                ImageIcon closeImg1 = new ImageIcon(Login1.class.getResource("/images/mouse_close.png"));
                //设置图片在窗体中显示的宽度、高度
                closeImg1.setImage(closeImg1.getImage().getScaledInstance(31, 31,Image.SCALE_DEFAULT));
                closeLabel.setIcon(closeImg1);
            }
            //鼠标离开，换关闭的背景图片
            @Override
            public void mouseExited(MouseEvent e) {
                // 创建图片对象
                ImageIcon closeImg = new ImageIcon(Login1.class.getResource("/images/close.png"));
                //设置图片在窗体中显示的宽度、高度
                closeImg.setImage(closeImg.getImage().getScaledInstance(31, 31,Image.SCALE_DEFAULT));
                closeLabel.setIcon(closeImg);
            }
        });
        *//**
         * 插入窗体最小化的背景图片及功能
         *//*
        // 创建图片对象
        ImageIcon minImg = new ImageIcon(Login1.class.getResource("/images/min.png"));
        //设置图片在窗体中显示的宽度、高度
        minImg.setImage(minImg.getImage().getScaledInstance(31, 31,Image.SCALE_DEFAULT));

        JPanel minPanel = new JPanel();
        minPanel.setBounds(237, -5, 31, 31);
        layeredPane.add(minPanel,JLayeredPane.MODAL_LAYER);

        JLabel minLabel = new JLabel("");
        minLabel.addMouseListener(new MouseAdapter() {
            //鼠标点击最小化图片，实现最小化窗体的功能
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.setExtendedState(JFrame.ICONIFIED);//最小化窗体
            }
            //鼠标进入，换最小化的背景图片
            @Override
            public void mouseEntered(MouseEvent e) {
                // 创建图片对象
                ImageIcon minImg1 = new ImageIcon(Login1.class.getResource("/images/mouse_min.png"));
                //设置图片在窗体中显示的宽度、高度
                minImg1.setImage(minImg1.getImage().getScaledInstance(31, 31,Image.SCALE_DEFAULT));
                minLabel.setIcon(minImg1);
            }
            //鼠标离开，换最小化的背景图片
            @Override
            public void mouseExited(MouseEvent e) {
                // 创建图片对象
                ImageIcon minImg = new ImageIcon(Login1.class.getResource("/images/min.png"));
                //设置图片在窗体中显示的宽度、高度
                minImg.setImage(minImg.getImage().getScaledInstance(31, 31,Image.SCALE_DEFAULT));
                minLabel.setIcon(minImg);
            }
        });
        minPanel.add(minLabel);
        minLabel.setIcon(minImg);
        *//**
         * 插入用户名输入框前面的图片
         *//*
        // 创建图片对象
        ImageIcon userNameImg = new ImageIcon(Login1.class.getResource("/images/user_name.png"));
        //设置图片在窗体中显示的宽度、高度
        userNameImg.setImage(userNameImg.getImage().getScaledInstance(40, 40,Image.SCALE_DEFAULT));

        JLabel userNameLabel = new JLabel("");
        userNameLabel.setBounds(0, 220, 40, 40);
        userNameLabel.setIcon(userNameImg);
        //默认获取光标
        userNameLabel.setFocusable(true);
        frame.getContentPane().add(userNameLabel);*/
        /**
         * 添加圆角的用户名输入框
         */
        userNameField = new JTextField();
        userNameField.setBounds(50, 220, 235, 50);
        userNameField.setBorder(bottomBorder);
        userNameField.setText("  用户名");
        userNameField.setFont(new Font("微软雅黑", 0, 14));
        userNameField.setForeground(Color.GRAY);//默认设置输入框中的文字颜色为灰色
        userNameField.addFocusListener(new FocusAdapter() {
            //获取光标事件
            @Override
            public void focusGained(FocusEvent e) {
                //获取焦点时，输入框中内容是“用户名”，那么去掉输入框中显示的内容；
                if("用户名".equals((userNameField.getText().trim()))){
                    userNameField.setText("");
                    userNameField.setForeground(Color.black);//设置颜色为黑色
                }
            }
            //失去光标事件
            @Override
            public void focusLost(FocusEvent e) {
                //失去焦点时，如果输入框中去掉空格后的字符串为空串则显示用户名
                if("".equals((userNameField.getText().trim()))){
                    userNameField.setText("  用户名");
                    userNameField.setFont(new Font("微软雅黑", 0, 14));
                    userNameField.setForeground(Color.GRAY);//默认设置输入框中的文字颜色为灰色
                }
            }
        });
        frame.getContentPane().add(userNameField);
        userNameField.setColumns(10);
        /**
         * 插入密码输入框前面的图片
         */
/*        // 创建图片对象
        ImageIcon passwordImg = new ImageIcon(Login1.class.getResource("/images/password.png"));
        //设置图片在窗体中显示的宽度、高度
        passwordImg.setImage(passwordImg.getImage().getScaledInstance(40, 40,Image.SCALE_DEFAULT));

        JLabel passwordLabel = new JLabel("");
        passwordLabel.setBounds(0, 280, 40, 40);
        passwordLabel.setIcon(passwordImg);
        frame.getContentPane().add(passwordLabel);*/
        /**
         * 添加圆角的密码输入框
         */
        passwordField = new JPasswordField();
        passwordField.setBounds(50, 280, 235, 50);
        passwordField.setBorder(bottomBorder);
        passwordField.setText("  密码");
        passwordField.setFont(new Font("微软雅黑", 0, 14));
        passwordField.setForeground(Color.GRAY);//默认设置输入框中的文字颜色为灰色
        passwordField.setEchoChar((char)0);//显示密码输入框中内容
        passwordField.addFocusListener(new FocusAdapter() {
            //获取光标事件
            @Override
            public void focusGained(FocusEvent e) {
                //获取焦点时，输入框中内容是“用户名”，那么去掉输入框中显示的内容；
                if("密码".equals((passwordField.getText().trim()))){
                    passwordField.setText("");
                    passwordField.setEchoChar('*');//显示密码输入框中内容
                    passwordField.setForeground(Color.black);//设置颜色为黑色
                }
            }
            //失去光标事件
            @Override
            public void focusLost(FocusEvent e) {
                //失去焦点时，如果输入框中去掉空格后的字符串为空串则显示用户名
                if("".equals((passwordField.getText().trim()))){
                    passwordField.setText("  密码");
                    passwordField.setFont(new Font("微软雅黑", 0, 14));
                    passwordField.setForeground(Color.GRAY);//默认设置输入框中的文字颜色为灰色
                    passwordField.setEchoChar((char)0);//显示密码输入框中内容
                }
            }
        });

        frame.getContentPane().add(passwordField);
        /**
         * 插入验证码输入框前面的图片
         */
        // 创建图片对象
/*        ImageIcon verifyCodeImg = new ImageIcon(Login1.class.getResource("/images/verify_code.png"));
        //设置图片在窗体中显示的宽度、高度
        verifyCodeImg.setImage(verifyCodeImg.getImage().getScaledInstance(40, 40,Image.SCALE_DEFAULT));

        JLabel verifyCodeLabel = new JLabel("");
        verifyCodeLabel.setBounds(0, 340, 40, 40);
        verifyCodeLabel.setIcon(verifyCodeImg);
        frame.getContentPane().add(verifyCodeLabel);*/
        /**
         * 添加圆角的验证码输入框
         */
        verifyCodeField = new JTextField();
        verifyCodeField.setBounds(50, 340, 135, 50);
        verifyCodeField.setBorder(bottomBorder);
        verifyCodeField.setText("  验证码");
        verifyCodeField.setFont(new Font("微软雅黑", 0, 14));
        verifyCodeField.setForeground(Color.GRAY);//默认设置输入框中的文字颜色为灰色
        verifyCodeField.addFocusListener(new FocusAdapter() {
            //获取光标事件
            @Override
            public void focusGained(FocusEvent e) {
                //获取焦点时，输入框中内容是“用户名”，那么去掉输入框中显示的内容；
                if("验证码".equals((verifyCodeField.getText().trim()))){
                    verifyCodeField.setText("");
                    verifyCodeField.setForeground(Color.black);//设置颜色为黑色
                }
            }
            //失去光标事件
            @Override
            public void focusLost(FocusEvent e) {
                //失去焦点时，如果输入框中去掉空格后的字符串为空串则显示用户名
                if("".equals((verifyCodeField.getText().trim()))){
                    verifyCodeField.setText("  验证码");
                    verifyCodeField.setFont(new Font("微软雅黑", 0, 14));
                    verifyCodeField.setForeground(Color.GRAY);//默认设置输入框中的文字颜色为灰色
                }
            }
        });

        frame.getContentPane().add(verifyCodeField);
        verifyCodeField.setColumns(10);
        /**
         * 添加验证码图片
         */
        JLabel verifyCodeImgLabel = new JLabel("");
        verifyCodeImgLabel.setBounds(190, 340, 95, 50);
        verifyCodeImgLabel.setBorder(myLineBorder);
        frame.getContentPane().add(verifyCodeImgLabel);
        //生成一张验证码图片
        verifyCode = VerifyCodeUtils.createOneCodeImage();
        //将刚生成的验证码图片显示在窗体中去
        ImageIcon verifyCodeSourceImg = new ImageIcon("./verifyCodeImg/"+verifyCode+".jpg");// 创建图片对象
        //设置图片在窗体中显示的宽度、高度
        verifyCodeSourceImg.setImage(verifyCodeSourceImg.getImage().getScaledInstance(95, 50,Image.SCALE_DEFAULT));
        verifyCodeImgLabel.setIcon(verifyCodeSourceImg);
        //点击验证码图片，换一个新的验证码图片
        verifyCodeImgLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //删除上一次的验证码图片
                File file = new File("./src/verifyCodeImg/"+verifyCode+".jpg");
                if(file.exists()){
                    file.delete();
                }
                //生成一张新的验证码图片
                verifyCode = VerifyCodeUtils.createOneCodeImage();
                ImageIcon verifyCodeSourceImg1 = new ImageIcon("./verifyCodeImg/"+verifyCode+".jpg");
                verifyCodeSourceImg1.setImage(verifyCodeSourceImg1.getImage().getScaledInstance(95, 50,Image.SCALE_DEFAULT));
                verifyCodeImgLabel.setIcon(verifyCodeSourceImg1);
            }
        });
        /**
         * 添加提示性信息的JLabel
         */
        JLabel reminderMessage = new JLabel("",JLabel.CENTER);
        reminderMessage.setBounds(15, 395, 270, 20);
        reminderMessage.setForeground(Color.red);
        reminderMessage.setFont(new Font("微软雅黑", 0, 12));
        frame.getContentPane().add(reminderMessage);
        /**
         * 添加圆角的提交按钮
         */
        MyButton myButton = new MyButton("安全登录", 0);
        myButton.setBounds(15, 420, 270, 50);
        frame.getContentPane().add(myButton);


        //设置窗体可见
        frame.setVisible(true);
    }

}


/**
 * 对文件操作的工具类
 * @author admin
 *
 */
class FileUtils {

    /**
     * 删除指定文件夹下的所有文件，此文件夹内只有文件，没有任何文件夹
     */
    static Boolean deleteAllFiles(String filePath){
        Boolean result = false;
        File file = new File(filePath);
        File temp = null;
        if(file.exists()){
            String [] tempList = file.list();
            for (String string : tempList) {
                temp = new File(filePath+"/"+string);
                if(temp.isFile()){
                    temp.delete();
                }
            }
            tempList = file.list();
            if(tempList.length==0){
                result = true;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Boolean result = FileUtils.deleteAllFiles("./verifyCodeImg");
        System.out.println(result);
    }

}

/**
 * 自定义带有圆角的按钮工具类
 * @author admin
 *
 */
class MyButton extends JButton {
    /* 决定圆角的弧度 */
    public static int radius = 4;
    public static Color COLOR1, COLOR2;
    public static int pink = 3, ashen = 2, sky = 1, stone = 0;
    /* 粉红 */
    public static Color pink1 = new Color(39, 121, 181);
    public static Color pink2 = new Color(39, 121, 181);
    /* 灰白 */
    public static Color ashen1 = new Color(39, 121, 181);
    public static Color ashen2 = new Color(39, 121, 181);
    /* 深宝石蓝 */
    public static Color stone1 = new Color(39, 121, 181);
    public static Color stone2 = new Color(39, 121, 181);
    /* 淡天蓝色 */
    public static Color sky1 = new Color(39, 121, 181);
    public static Color sky2 = new Color(39, 121, 181);
    /* 光标进入按钮判断 */
    private boolean hover;

    public MyButton() {
        this("", stone);
    }

    public MyButton(String name) {
        this(name, stone);
    }

    public MyButton(String name, int style) {
        super.setText(name);
        if (style == pink) {
            COLOR1 = pink1;
            COLOR2 = pink2;
        }
        if (style == ashen) {
            COLOR1 = ashen1;
            COLOR2 = ashen2;
        }
        if (style == stone) {
            COLOR1 = stone1;
            COLOR2 = stone2;
        }
        if (style == sky) {
            COLOR1 = sky1;
            COLOR2 = sky2;
        }
        paintcolor(COLOR1, COLOR2);
    }

    private void paintcolor(Color COLOR1, Color COLOR2) {
        setMargin(new Insets(0, 0, 0, 0));
        setFont(new Font("微软雅黑", 0, 14));
        setBorderPainted(false);
        setForeground(Color.white);
        setFocusPainted(false);
        setContentAreaFilled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        int height = getHeight();
        int with = getWidth();
        float tran = 0.9F;
        /*if (!hover) { 鼠标离开/进入时的透明度改变量
            tran = 0.6F;
        }*/
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        /* GradientPaint是颜色渐变类 */
        GradientPaint p1;
        GradientPaint p2;
        if (getModel().isPressed()) {
            p1 = new GradientPaint(0, 0, new Color(0, 0, 0), 0, height, new Color(100, 100, 100), true);
            p2 = new GradientPaint(0, 1, new Color(0, 0, 0, 50), 0, height, new Color(255, 255, 255, 100), true);
        } else {
            p1 = new GradientPaint(0, 0, new Color(100, 100, 100), 0, height, new Color(0, 0, 0), true);
            p2 = new GradientPaint(0, 1, new Color(255, 255, 255, 100), 0, height, new Color(0, 0, 0, 50), true);
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tran));
        RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0, with - 1, height - 1, radius, radius);
        // 最后两个参数数值越大，按钮越圆，以下同理
        Shape clip = g2d.getClip();
        g2d.clip(r2d);
        GradientPaint gp = new GradientPaint(0.0F, 0.0F, COLOR1, 0.0F, height / 2, COLOR2, true);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, with, height);
        g2d.setClip(clip);
        g2d.setPaint(p1);
        g2d.drawRoundRect(0, 0, with - 3, height - 3, radius, radius);
        g2d.setPaint(p2);
        g2d.drawRoundRect(1, 1, with - 3, height - 3, radius, radius);
        g2d.dispose();
        super.paintComponent(g);
    }

    public static void main(String args[]) {
        JFrame frm = new JFrame();
        MyButton but = new MyButton("圆角JButton", 0);
        frm.setLayout(null);
        frm.setBounds(800, 400, 500, 200);
        but.setBounds(30, 30, 200, 50);
        frm.add(but);
        frm.setDefaultCloseOperation(3);
        frm.setVisible(true);
    }
}


/**
 * 为创建圆角输入框自定义边框线条工具类
 * @author admin
 *
 */
class MyLineBorder extends LineBorder{


    private static final long serialVersionUID = 1L;

    public MyLineBorder(Color color, int thickness, boolean roundedCorners) {
        super(color, thickness, roundedCorners);
    }



    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Color oldColor = g.getColor();
        Graphics2D g2 = (Graphics2D)g;
        int i;
        g2.setRenderingHints(rh);
        g2.setColor(lineColor);
        for(i = 0; i < thickness; i++)  {
            if(!roundedCorners)
                g2.drawRect(x+i, y+i, width-i-i-1, height-i-i-1);
            else
                g2.drawRoundRect(x+i, y+i, width-i-i-1, height-i-i-1, 10, 10); //就是这一句 ，后两个参数控制圆角的大小
        }
        g2.setColor(oldColor);
    }


}


/**
 * 生成验证码图片的工具类
 * @author admin
 *
 */
class VerifyCodeUtils {

    // 使用到Algerian字体，系统里没有的话需要安装字体，字体只显示大写，去掉了1,0,i,o几个容易混淆的字符
    public static final String VERIFY_CODES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static Random random = new Random();

    /**
     * 使用系统默认字符源生成验证码
     *
     * @param verifySize
     *            验证码长度
     * @return
     */
    public static String generateVerifyCode(int verifySize) {
        return generateVerifyCode(verifySize, VERIFY_CODES);
    }

    /**
     * 使用指定源生成验证码
     *
     * @param verifySize
     *            验证码长度
     * @param sources
     *            验证码字符源
     * @return
     */
    public static String generateVerifyCode(int verifySize, String sources) {
        if (sources == null || sources.length() == 0) {
            sources = VERIFY_CODES;
        }
        int codesLen = sources.length();
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder verifyCode = new StringBuilder(verifySize);
        for (int i = 0; i < verifySize; i++) {
            verifyCode.append(sources.charAt(rand.nextInt(codesLen - 1)));
        }
        return verifyCode.toString();
    }

    /**
     * 生成随机验证码文件,并返回验证码值
     *
     * @param w
     * @param h
     * @param outputFile
     * @param verifySize
     * @return
     * @throws IOException
     */
    public static String outputVerifyImage(int w, int h, File outputFile, int verifySize) throws IOException {
        String verifyCode = generateVerifyCode(verifySize);
        outputImage(w, h, outputFile, verifyCode);
        return verifyCode;
    }

    /**
     * 输出随机验证码图片流,并返回验证码值
     *
     * @param w
     * @param h
     * @param os
     * @param verifySize
     * @return
     * @throws IOException
     */
    public static String outputVerifyImage(int w, int h, OutputStream os, int verifySize) throws IOException {
        String verifyCode = generateVerifyCode(verifySize);
        outputImage(w, h, os, verifyCode);
        return verifyCode;
    }

    /**
     * 生成指定验证码图像文件
     *
     * @param w
     * @param h
     * @param outputFile
     * @param code
     * @throws IOException
     */
    public static void outputImage(int w, int h, File outputFile, String code) throws IOException {
        if (outputFile == null) {
            return;
        }
        File dir = outputFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            outputFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(outputFile);
            outputImage(w, h, fos, code);
            fos.close();
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 输出指定验证码图片流
     *
     * @param w
     * @param h
     * @param os
     * @param code
     * @throws IOException
     */
    public static void outputImage(int w, int h, OutputStream os, String code) throws IOException {
        int verifySize = code.length();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Random rand = new Random();
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color[] colors = new Color[5];
        Color[] colorSpaces = new Color[] { Color.WHITE, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA,
                Color.ORANGE, Color.PINK, Color.YELLOW };
        float[] fractions = new float[colors.length];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = colorSpaces[rand.nextInt(colorSpaces.length)];
            fractions[i] = rand.nextFloat();
        }
        Arrays.sort(fractions);

        g2.setColor(Color.GRAY);// 设置边框色
        g2.fillRect(0, 0, w, h);

        Color c = getRandColor(200, 250);
        g2.setColor(c);// 设置背景色
        g2.fillRect(0, 2, w, h - 4);

        // 绘制干扰线
        Random random = new Random();
        g2.setColor(getRandColor(160, 200));// 设置线条的颜色
        for (int i = 0; i < 20; i++) {
            int x = random.nextInt(w - 1);
            int y = random.nextInt(h - 1);
            int xl = random.nextInt(6) + 1;
            int yl = random.nextInt(12) + 1;
            g2.drawLine(x, y, x + xl + 40, y + yl + 20);
        }

        // 添加噪点
        float yawpRate = 0.05f;// 噪声率
        int area = (int) (yawpRate * w * h);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int rgb = getRandomIntColor();
            image.setRGB(x, y, rgb);
        }

        shear(g2, w, h, c);// 使图片扭曲

        g2.setColor(getRandColor(100, 160));
        int fontSize = h - 4;
        Font font = new Font("Algerian", Font.ITALIC, fontSize);
        g2.setFont(font);
        char[] chars = code.toCharArray();
        for (int i = 0; i < verifySize; i++) {
            AffineTransform affine = new AffineTransform();
            affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1),
                    (w / verifySize) * i + fontSize / 2, h / 2);
            g2.setTransform(affine);
            g2.drawChars(chars, i, 1, ((w - 10) / verifySize) * i + 5, h / 2 + fontSize / 2 - 10);
        }

        g2.dispose();
        ImageIO.write(image, "jpg", os);
    }

    private static Color getRandColor(int fc, int bc) {
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static int getRandomIntColor() {
        int[] rgb = getRandomRgb();
        int color = 0;
        for (int c : rgb) {
            color = color << 8;
            color = color | c;
        }
        return color;
    }

    private static int[] getRandomRgb() {
        int[] rgb = new int[3];
        for (int i = 0; i < 3; i++) {
            rgb[i] = random.nextInt(255);
        }
        return rgb;
    }

    private static void shear(Graphics g, int w1, int h1, Color color) {
        shearX(g, w1, h1, color);
        shearY(g, w1, h1, color);
    }

    private static void shearX(Graphics g, int w1, int h1, Color color) {

        int period = random.nextInt(2);

        boolean borderGap = true;
        int frames = 1;
        int phase = random.nextInt(2);

        for (int i = 0; i < h1; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(0, i, w1, 1, (int) d, 0);
            if (borderGap) {
                g.setColor(color);
                g.drawLine((int) d, i, 0, i);
                g.drawLine((int) d + w1, i, w1, i);
            }
        }

    }

    private static void shearY(Graphics g, int w1, int h1, Color color) {

        int period = random.nextInt(40) + 10; // 50;

        boolean borderGap = true;
        int frames = 20;
        int phase = 7;
        for (int i = 0; i < w1; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            if (borderGap) {
                g.setColor(color);
                g.drawLine(i, (int) d, i, 0);
                g.drawLine(i, (int) d + h1, i, h1);
            }

        }

    }

    //生成一张验证码图片，并保存到项目的verifyCodeImg文件夹下
    public static String createOneCodeImage(){
        String imgName = "";
        try {
            File dir = new File("./verifyCodeImg");
            int w = 95, h = 50;
            String verifyCode = generateVerifyCode(4);
            File file = new File(dir, verifyCode + ".jpg");
            outputImage(w, h, file, verifyCode);
            imgName = verifyCode;
        } catch (IOException e) {
            imgName = "";
            e.printStackTrace();
        }finally{
            return imgName;
        }
    }

    public static void main(String[] args) throws IOException {

        String codeImage = VerifyCodeUtils.createOneCodeImage();
        System.out.println(codeImage);

    }
}