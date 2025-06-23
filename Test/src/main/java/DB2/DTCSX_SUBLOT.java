package DB2;

import java.sql.Timestamp;

public class DTCSX_SUBLOT {
    private String eqpid;         // 设备ID
    private String sublotid;      // 子批次ID
    private String state;         // 状态
    private String carid;         // 载体ID
    private Integer sublotcnt;    // 子批次数量
    private Integer lotpcs;       // 批次数量
    private Integer wafpcs;       // 晶圆数量
    private String lotid;         // 批次ID
    private String routeid;       // 路由ID
    private String routever;      // 路由版本
    private String ope_no;        // 工序号
    private String recipeid;      // 配方ID
    private String measid;        // 测量ID
    private String procid;        // 工艺ID
    private String lotcate;       // 批次类别
    private String lastflag;      // 最后标志
    private String holdflag;      // 保留标志
    private String prodid;        // 产品ID
    private String opeid;         // 操作ID
    private String lastfunction;  // 最后执行的功能
    private Timestamp lastupdate; // 最后更新时间
    private String kemlot_no;     // KEM批次号

    // 无参构造函数
    public DTCSX_SUBLOT() {}

    // 全参构造函数
    public DTCSX_SUBLOT(String eqpid, String sublotid, String state, String carid,
                        Integer sublotcnt, Integer lotpcs, Integer wafpcs, String lotid,
                        String routeid, String routever, String ope_no, String recipeid,
                        String measid, String procid, String lotcate, String lastflag,
                        String holdflag, String prodid, String opeid, String lastfunction,
                        Timestamp lastupdate, String kemlot_no) {
        this.eqpid = eqpid;
        this.sublotid = sublotid;
        this.state = state;
        this.carid = carid;
        this.sublotcnt = sublotcnt;
        this.lotpcs = lotpcs;
        this.wafpcs = wafpcs;
        this.lotid = lotid;
        this.routeid = routeid;
        this.routever = routever;
        this.ope_no = ope_no;
        this.recipeid = recipeid;
        this.measid = measid;
        this.procid = procid;
        this.lotcate = lotcate;
        this.lastflag = lastflag;
        this.holdflag = holdflag;
        this.prodid = prodid;
        this.opeid = opeid;
        this.lastfunction = lastfunction;
        this.lastupdate = lastupdate;
        this.kemlot_no = kemlot_no;
    }

    // Getter 和 Setter 方法
    public String getEqpid() {
        return eqpid;
    }

    public void setEqpid(String eqpid) {
        this.eqpid = eqpid;
    }

    public String getSublotid() {
        return sublotid;
    }

    public void setSublotid(String sublotid) {
        this.sublotid = sublotid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCarid() {
        return carid;
    }

    public void setCarid(String carid) {
        this.carid = carid;
    }

    public Integer getSublotcnt() {
        return sublotcnt;
    }

    public void setSublotcnt(Integer sublotcnt) {
        this.sublotcnt = sublotcnt;
    }

    public Integer getLotpcs() {
        return lotpcs;
    }

    public void setLotpcs(Integer lotpcs) {
        this.lotpcs = lotpcs;
    }

    public Integer getWafpcs() {
        return wafpcs;
    }

    public void setWafpcs(Integer wafpcs) {
        this.wafpcs = wafpcs;
    }

    public String getLotid() {
        return lotid;
    }

    public void setLotid(String lotid) {
        this.lotid = lotid;
    }

    public String getRouteid() {
        return routeid;
    }

    public void setRouteid(String routeid) {
        this.routeid = routeid;
    }

    public String getRoutever() {
        return routever;
    }

    public void setRoutever(String routever) {
        this.routever = routever;
    }

    public String getOpe_no() {
        return ope_no;
    }

    public void setOpe_no(String ope_no) {
        this.ope_no = ope_no;
    }

    public String getRecipeid() {
        return recipeid;
    }

    public void setRecipeid(String recipeid) {
        this.recipeid = recipeid;
    }

    public String getMeasid() {
        return measid;
    }

    public void setMeasid(String measid) {
        this.measid = measid;
    }

    public String getProcid() {
        return procid;
    }

    public void setProcid(String procid) {
        this.procid = procid;
    }

    public String getLotcate() {
        return lotcate;
    }

    public void setLotcate(String lotcate) {
        this.lotcate = lotcate;
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

    public String getProdid() {
        return prodid;
    }

    public void setProdid(String prodid) {
        this.prodid = prodid;
    }

    public String getOpeid() {
        return opeid;
    }

    public void setOpeid(String opeid) {
        this.opeid = opeid;
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

    public String getKemlot_no() {
        return kemlot_no;
    }

    public void setKemlot_no(String kemlot_no) {
        this.kemlot_no = kemlot_no;
    }

    @Override
    public String toString() {
        return "DTCSX_SUBLOT{" +
                "eqpid='" + eqpid + '\'' +
                ", sublotid='" + sublotid + '\'' +
                ", state='" + state + '\'' +
                ", carid='" + carid + '\'' +
                ", sublotcnt=" + sublotcnt +
                ", lotpcs=" + lotpcs +
                ", wafpcs=" + wafpcs +
                ", lotid='" + lotid + '\'' +
                ", routeid='" + routeid + '\'' +
                ", routever='" + routever + '\'' +
                ", ope_no='" + ope_no + '\'' +
                ", recipeid='" + recipeid + '\'' +
                ", measid='" + measid + '\'' +
                ", procid='" + procid + '\'' +
                ", lotcate='" + lotcate + '\'' +
                ", lastflag='" + lastflag + '\'' +
                ", holdflag='" + holdflag + '\'' +
                ", prodid='" + prodid + '\'' +
                ", opeid='" + opeid + '\'' +
                ", lastfunction='" + lastfunction + '\'' +
                ", lastupdate=" + lastupdate +
                ", kemlot_no='" + kemlot_no + '\'' +
                '}';
    }
}