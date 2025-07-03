package com.example.demo.data;

import java.io.Serializable;

/**
 * 用户信息实体类，对应数据库表 LS2WPRD.DUSER
 */
public class DUSER implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID（主键） - 对应数据库字段 USER_ID */
    private String user_id;

    /** 密码 - 对应数据库字段 PASSWD */
    private String passwd;

    /** 用户姓名 - 对应数据库字段 USER_NAME */
    private String user_name;

    /** 用户描述 - 对应数据库字段 USER_DESC */
    private String user_desc;

    /** 邮箱地址 - 对应数据库字段 MAIL_ADRESS */
    private String mail_adress;

    /** 用户组ID 1-10 - 对应数据库字段 USERG_ID1 到 USERG_ID10 */
    private String userg_id1;
    private String userg_id2;
    private String userg_id3;
    private String userg_id4;
    private String userg_id5;
    private String userg_id6;
    private String userg_id7;
    private String userg_id8;
    private String userg_id9;
    private String userg_id10;

    // 无参构造函数
    public DUSER() {}

    // 全参构造函数
    public DUSER(String userId, String passwd, String userName, String userDesc,
                 String mailAdress, String usergId1, String usergId2, String usergId3,
                 String usergId4, String usergId5, String usergId6, String usergId7,
                 String usergId8, String usergId9, String usergId10) {
        this.user_id = userId;
        this.passwd = passwd;
        this.user_name = userName;
        this.user_desc = userDesc;
        this.mail_adress = mailAdress;
        this.userg_id1 = usergId1;
        this.userg_id2 = usergId2;
        this.userg_id3 = usergId3;
        this.userg_id4 = usergId4;
        this.userg_id5 = usergId5;
        this.userg_id6 = usergId6;
        this.userg_id7 = usergId7;
        this.userg_id8 = usergId8;
        this.userg_id9 = usergId9;
        this.userg_id10 = usergId10;
    }

    // ------------------- 标准 Getter 和 Setter 方法 -------------------

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserName(String userName) {
        this.user_name = userName;
    }

    public String getUserDesc() {
        return user_desc;
    }

    public void setUserDesc(String userDesc) {
        this.user_desc = userDesc;
    }

    public String getMailAdress() {
        return mail_adress;
    }

    public void setMailAdress(String mailAdress) {
        this.mail_adress = mailAdress;
    }

    public String getUsergId1() {
        return userg_id1;
    }

    public void setUsergId1(String usergId1) {
        this.userg_id1 = usergId1;
    }

    // 省略 usergId2 到 usergId10 的 Getter 和 Setter（格式相同）

    @Override
    public String toString() {
        return "DUSER{" +
                "user_id='" + user_id + '\'' +
                ", passwd='" + passwd + '\'' +
                ", user_Name='" + user_name + '\'' +
                ", user_Desc='" + user_desc + '\'' +
                ", mail_adress='" + mail_adress + '\'' +
                ", userg_id1='" + userg_id1 + '\'' +
                ", userg_id2='" + userg_id2 + '\'' +
                ", userg_id3='" + userg_id3 + '\'' +
                ", userg_id4='" + userg_id4 + '\'' +
                ", userg_id5='" + userg_id5 + '\'' +
                ", userg_id6='" + userg_id6 + '\'' +
                ", userg_id7='" + userg_id7 + '\'' +
                ", userg_id8='" + userg_id8 + '\'' +
                ", userg_id9='" + userg_id9 + '\'' +
                ", userg_id10='" + userg_id10 + '\'' +
                '}';
    }
}