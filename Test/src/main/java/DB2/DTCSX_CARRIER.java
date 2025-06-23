package DB2;

import java.sql.Timestamp;

public class DTCSX_CARRIER {
    private String eqpid;         // 设备ID
    private String carid;         // 载体ID
    private String portid;        // 端口ID
    private String sublotid;      // 子批次ID
    private String cartype;       // 载体类型
    private String carcate;       // 载体类别
    private String carusage;      // 载体用途
    private String resvportid;    // 预留端口ID
    private String state;         // 状态
    private Integer loadseq;      // 装载序列
    private String resvflag;      // 预留标志
    private String lastflag;      // 最后标志
    private String holdflag;      // 保留标志
    private String lastfunction;  // 最后执行的功能
    private Timestamp lastupdate; // 最后更新时间

    // 构造函数
    public DTCSX_CARRIER() {}

    // Getters and Setters
    public String getEqpid() {
        return eqpid;
    }

    public void setEqpid(String eqpid) {
        this.eqpid = eqpid;
    }

    public String getCarid() {
        return carid;
    }

    public void setCarid(String carid) {
        this.carid = carid;
    }

    public String getPortid() {
        return portid;
    }

    public void setPortid(String portid) {
        this.portid = portid;
    }

    public String getSublotid() {
        return sublotid;
    }

    public void setSublotid(String sublotid) {
        this.sublotid = sublotid;
    }

    public String getCartype() {
        return cartype;
    }

    public void setCartype(String cartype) {
        this.cartype = cartype;
    }

    public String getCarcate() {
        return carcate;
    }

    public void setCarcate(String carcate) {
        this.carcate = carcate;
    }

    public String getCarusage() {
        return carusage;
    }

    public void setCarusage(String carusage) {
        this.carusage = carusage;
    }

    public String getResvportid() {
        return resvportid;
    }

    public void setResvportid(String resvportid) {
        this.resvportid = resvportid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getLoadseq() {
        return loadseq;
    }

    public void setLoadseq(Integer loadseq) {
        this.loadseq = loadseq;
    }

    public String getResvflag() {
        return resvflag;
    }

    public void setResvflag(String resvflag) {
        this.resvflag = resvflag;
    }

    public String getLastflag() {
        return lastflag;
    }

    public void setLastflag(String lastflag) {
        this.lastflag = lastflag;
    }

    public String getHoldflag() {
        return holdflag;
    }

    public void setHoldflag(String holdflag) {
        this.holdflag = holdflag;
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

    @Override
    public String toString() {
        return "DTCSX_CARRIER{" +
                "eqpid='" + eqpid + '\'' +
                ", carid='" + carid + '\'' +
                ", portid='" + portid + '\'' +
                ", sublotid='" + sublotid + '\'' +
                ", cartype='" + cartype + '\'' +
                ", carcate='" + carcate + '\'' +
                ", carusage='" + carusage + '\'' +
                ", resvportid='" + resvportid + '\'' +
                ", state='" + state + '\'' +
                ", loadseq=" + loadseq +
                ", resvflag='" + resvflag + '\'' +
                ", lastflag='" + lastflag + '\'' +
                ", holdflag='" + holdflag + '\'' +
                ", lastfunction='" + lastfunction + '\'' +
                ", lastupdate=" + lastupdate +
                '}';
    }
}