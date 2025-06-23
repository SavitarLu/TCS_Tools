package DB2;

import java.sql.Timestamp;

public class DTCSX_PORT {
    private String eqpid;          // 设备ID
    private String portid;         // 端口ID
    private String porttype;       // 端口类型
    private String stgsubtype;     // 存储子类型
    private String state;          // 状态
    private String sublotid;       // 子批次ID
    private String carid;          // 载体ID
    private Timestamp stateupdate; // 状态更新时间
    private Integer lockseq;       // 锁定序列
    private Integer nextseq;       // 下一序列
    private String mcsportid;      // MCS端口ID
    private String lastfunction;   // 最后执行的功能
    private Timestamp lastupdate;  // 最后更新时间
    private String desttype;       // 目标类型
    private String cplcartype;     // CPL载体类型
    private String inquiryflg;     // 查询标志
    private String cplcmdflg;      // CPL命令标志

    // 无参构造函数
    public DTCSX_PORT() {}

    // 全参构造函数
    public DTCSX_PORT(String eqpid, String portid, String porttype, String stgsubtype,
                      String state, String sublotid, String carid, Timestamp stateupdate,
                      Integer lockseq, Integer nextseq, String mcsportid, String lastfunction,
                      Timestamp lastupdate, String desttype, String cplcartype,
                      String inquiryflg, String cplcmdflg) {
        this.eqpid = eqpid;
        this.portid = portid;
        this.porttype = porttype;
        this.stgsubtype = stgsubtype;
        this.state = state;
        this.sublotid = sublotid;
        this.carid = carid;
        this.stateupdate = stateupdate;
        this.lockseq = lockseq;
        this.nextseq = nextseq;
        this.mcsportid = mcsportid;
        this.lastfunction = lastfunction;
        this.lastupdate = lastupdate;
        this.desttype = desttype;
        this.cplcartype = cplcartype;
        this.inquiryflg = inquiryflg;
        this.cplcmdflg = cplcmdflg;
    }

    // Getter 和 Setter 方法
    public String getEqpid() {
        return eqpid;
    }

    public void setEqpid(String eqpid) {
        this.eqpid = eqpid;
    }

    public String getPortid() {
        return portid;
    }

    public void setPortid(String portid) {
        this.portid = portid;
    }

    public String getPorttype() {
        return porttype;
    }

    public void setPorttype(String porttype) {
        this.porttype = porttype;
    }

    public String getStgsubtype() {
        return stgsubtype;
    }

    public void setStgsubtype(String stgsubtype) {
        this.stgsubtype = stgsubtype;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSublotid() {
        return sublotid;
    }

    public void setSublotid(String sublotid) {
        this.sublotid = sublotid;
    }

    public String getCarid() {
        return carid;
    }

    public void setCarid(String carid) {
        this.carid = carid;
    }

    public Timestamp getStateupdate() {
        return stateupdate;
    }

    public void setStateupdate(Timestamp stateupdate) {
        this.stateupdate = stateupdate;
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

    public String getMcsportid() {
        return mcsportid;
    }

    public void setMcsportid(String mcsportid) {
        this.mcsportid = mcsportid;
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

    public String getDesttype() {
        return desttype;
    }

    public void setDesttype(String desttype) {
        this.desttype = desttype;
    }

    public String getCplcartype() {
        return cplcartype;
    }

    public void setCplcartype(String cplcartype) {
        this.cplcartype = cplcartype;
    }

    public String getInquiryflg() {
        return inquiryflg;
    }

    public void setInquiryflg(String inquiryflg) {
        this.inquiryflg = inquiryflg;
    }

    public String getCplcmdflg() {
        return cplcmdflg;
    }

    public void setCplcmdflg(String cplcmdflg) {
        this.cplcmdflg = cplcmdflg;
    }

    @Override
    public String toString() {
        return "DTCSX_PORT{" +
                "eqpid='" + eqpid + '\'' +
                ", portid='" + portid + '\'' +
                ", porttype='" + porttype + '\'' +
                ", stgsubtype='" + stgsubtype + '\'' +
                ", state='" + state + '\'' +
                ", sublotid='" + sublotid + '\'' +
                ", carid='" + carid + '\'' +
                ", stateupdate=" + stateupdate +
                ", lockseq=" + lockseq +
                ", nextseq=" + nextseq +
                ", mcsportid='" + mcsportid + '\'' +
                ", lastfunction='" + lastfunction + '\'' +
                ", lastupdate=" + lastupdate +
                ", desttype='" + desttype + '\'' +
                ", cplcartype='" + cplcartype + '\'' +
                ", inquiryflg='" + inquiryflg + '\'' +
                ", cplcmdflg='" + cplcmdflg + '\'' +
                '}';
    }
}