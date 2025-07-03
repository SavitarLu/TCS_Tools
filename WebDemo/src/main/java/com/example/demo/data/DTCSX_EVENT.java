package com.example.demo.data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 设备事件信息实体类，对应数据库表 TCSX.DTCSX_EVENT
 */
public class DTCSX_EVENT implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 设备ID（主键） - 对应数据库字段 EQPID */
    private String eqpid;

    /** 序列号（主键） - 对应数据库字段 SEQNO */
    private Integer seqno;

    /** 事件ID - 对应数据库字段 EVTID */
    private String evtid;

    /** 端口ID - 对应数据库字段 PORTID */
    private String portid;

    /** 载具ID - 对应数据库字段 CARID */
    private String carid;

    /** 子批次ID - 对应数据库字段 SUBLOTID */
    private String sublotid;

    /** 文本描述 - 对应数据库字段 TEXT */
    private String text;

    /** 最后执行函数 - 对应数据库字段 LASTFUNCTION */
    private String lastfunction;

    /** 最后更新时间 - 对应数据库字段 LASTUPDATE */
    private Timestamp lastupdate;

    // 无参构造函数
    public DTCSX_EVENT() {}

    // 全参构造函数
    public DTCSX_EVENT(String eqpid, Integer seqno, String evtid, String portid,
                       String carid, String sublotid, String text, String lastfunction,
                       Timestamp lastupdate) {
        this.eqpid = eqpid;
        this.seqno = seqno;
        this.evtid = evtid;
        this.portid = portid;
        this.carid = carid;
        this.sublotid = sublotid;
        this.text = text;
        this.lastfunction = lastfunction;
        this.lastupdate = lastupdate;
    }

    // ------------------- 标准 Getter 和 Setter 方法 -------------------

    public String getEqpid() {
        return eqpid;
    }

    public void setEqpid(String eqpid) {
        this.eqpid = eqpid;
    }

    public Integer getSeqno() {
        return seqno;
    }

    public void setSeqno(Integer seqno) {
        this.seqno = seqno;
    }

    public String getEvtid() {
        return evtid;
    }

    public void setEvtid(String evtid) {
        this.evtid = evtid;
    }

    public String getPortid() {
        return portid;
    }

    public void setPortid(String portid) {
        this.portid = portid;
    }

    public String getCarid() {
        return carid;
    }

    public void setCarid(String carid) {
        this.carid = carid;
    }

    public String getSublotid() {
        return sublotid;
    }

    public void setSublotid(String sublotid) {
        this.sublotid = sublotid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        return "DTCSX_EVENT{" +
                "eqpid='" + eqpid + '\'' +
                ", seqno=" + seqno +
                ", evtid='" + evtid + '\'' +
                ", portid='" + portid + '\'' +
                ", carid='" + carid + '\'' +
                ", sublotid='" + sublotid + '\'' +
                ", text='" + text + '\'' +
                ", lastfunction='" + lastfunction + '\'' +
                ", lastupdate=" + lastupdate +
                '}';
    }
}