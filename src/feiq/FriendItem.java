package feiq;
import javax.swing.*;

public class FriendItem extends JLabel {
    private String ip;
    private String nickname;
    private String headPic;
    private ImageIcon normalIcon;
    private ImageIcon checkedIcon;

    FriendItem() {
        super();
    }

    FriendItem(String ip, String nickname, String headPic, ImageIcon normalIcon, ImageIcon checkedIcon) {
        this.ip = ip;
        this.nickname = nickname;
        this.headPic = headPic;
        this.normalIcon = normalIcon;
        this.checkedIcon = checkedIcon;
    }

    public ImageIcon getNormalIcon() {
        return normalIcon;
    }

    public ImageIcon getCheckedIcon() {
        return checkedIcon;
    }


    public String getIp() {
        return ip;
    }

    public String getNickname() {
        return nickname;
    }


    public String getHeadPic() {
        return headPic;
    }
}
