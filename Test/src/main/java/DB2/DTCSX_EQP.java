package DB2;

import java.sql.Timestamp;

public class DTCSX_EQP {
    private String eqpid;         // 设备ID
    private String eqpname;       // 设备名称
    private String mode;          // 模式
    private String state;         // 状态
    private String userid;        // 用户ID
    private Integer lockseq;      // 锁定序列
    private Integer nextseq;      // 下一序列
    private String lastfunction;  // 最后执行的功能
    private Timestamp lastupdate; // 最后更新时间
    private String ctrlmode;      // 控制模式
    private String linkmode;      // 链接模式
    private String linkstate;     // 链接状态
    private String opimode;       // OPI模式

    // 构造函数
    public DTCSX_EQP() {}

    // Getters and Setters
    public String getEqpid() {
        return eqpid;
    }

    public void setEqpid(String eqpid) {
        this.eqpid = eqpid;
    }

    public String getEqpname() {
        return eqpname;
    }

    public void setEqpname(String eqpname) {
        this.eqpname = eqpname;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Integer getLockseq() {
        return lockseq;
    }

    public void setLockseq(Integer lockseq) {
        this.lockseq = lockseq;
    }

    public Integer getNextseq() {
        return nextseq;
    }

    public void setNextseq(Integer nextseq) {
        this.nextseq = nextseq;
    }

    public String getLastfunction() {
        return lastfunction;
    }

    public void setLastfunction(String lastfunction) {
        this.lastfunction = lastfunction;
    }

    public Timestamp getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(Timestamp lastupdate) {
        this.lastupdate = lastupdate;
    }

    public String getCtrlmode() {
        return ctrlmode;
    }

    public void setCtrlmode(String ctrlmode) {
        this.ctrlmode = ctrlmode;
    }

    public String getLinkmode() {
        return linkmode;
    }

    public void setLinkmode(String linkmode) {
        this.linkmode = linkmode;
    }

    public String getLinkstate() {
        return linkstate;
    }

    public void setLinkstate(String linkstate) {
        this.linkstate = linkstate;
    }

    public String getOpimode() {
        return opimode;
    }

    public void setOpimode(String opimode) {
        this.opimode = opimode;
    }

    @Override
    public String toString() {
        return "DtcsxEqp{" +
                "eqpid='" + eqpid + '\'' +
                ", eqpname='" + eqpname + '\'' +
                ", mode='" + mode + '\'' +
                ", state='" + state + '\'' +
                ", userid='" + userid + '\'' +
                ", lockseq=" + lockseq +
                ", nextseq=" + nextseq +
                ", lastfunction='" + lastfunction + '\'' +
                ", lastupdate=" + lastupdate +
                ", ctrlmode='" + ctrlmode + '\'' +
                ", linkmode='" + linkmode + '\'' +
                ", linkstate='" + linkstate + '\'' +
                ", opimode='" + opimode + '\'' +
                '}';
    }
}