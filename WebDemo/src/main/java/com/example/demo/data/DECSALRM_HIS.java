package com.example.demo.data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 设备报警历史信息实体类，对应数据库表 LS2WPRD.DECSALRM_HIS
 */
public class DECSALRM_HIS implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 设备ID（主键） - 对应数据库字段 EQP_ID */
    private String eqp_id;

    /** 报警时间（主键） - 对应数据库字段 ALM_DTTM */
    private Timestamp alm_dttm;

    /** 报警ID（主键） - 对应数据库字段 ALM_ID */
    private String alm_id;

    /** 报警代码 - 对应数据库字段 ALM_CODE */
    private String alm_code;

    /** 报警状态 - 对应数据库字段 ALM_STAT */
    private String alm_stat;

    /** 报警文本描述 - 对应数据库字段 ALM_TEXT */
    private String alm_text;
    private String formattedAlmDttm; // 添加格式化后的日期字段
    // 无参构造函数
    public DECSALRM_HIS() {}

    // 全参构造函数
    public DECSALRM_HIS(String eqp_id, Timestamp alm_dttm, String alm_id,
                       String alm_code, String alm_stat, String alm_text) {
        this.eqp_id = eqp_id;
        this.alm_dttm = alm_dttm;
        this.alm_id = alm_id;
        this.alm_code = alm_code;
        this.alm_stat = alm_stat;
        this.alm_text = alm_text;
    }

    // ------------------- 标准 Getter 和 Setter 方法 -------------------
// 添加对应的 getter 和 setter 方法
    public String getFormattedAlmDttm() {
        return formattedAlmDttm;
    }

    public void setFormattedAlmDttm(String formattedAlmDttm) {
        this.formattedAlmDttm = formattedAlmDttm;
    }
    public String getEqp_id() {
        return eqp_id;
    }

    public void setEqp_id(String eqp_id) {
        this.eqp_id = eqp_id;
    }

    public Timestamp getAlm_dttm() {
        return alm_dttm;
    }

    public void setAlm_dttm(Timestamp alm_dttm) {
        this.alm_dttm = alm_dttm;
    }

    public String getAlm_id() {
        return alm_id;
    }

    public void setAlm_id(String alm_id) {
        this.alm_id = alm_id;
    }

    public String getAlm_code() {
        return alm_code;
    }

    public void setAlm_code(String alm_code) {
        this.alm_code = alm_code;
    }

    public String getAlm_stat() {
        return alm_stat;
    }

    public void setAlm_stat(String alm_stat) {
        this.alm_stat = alm_stat;
    }

    public String getAlm_text() {
        return alm_text;
    }

    public void setAlm_text(String alm_text) {
        this.alm_text = alm_text;
    }

    @Override
    public String toString() {
        return "DECSALRM_HIS{" +
                "eqp_id='" + eqp_id + '\'' +
                ", alm_dttm=" + alm_dttm +
                ", alm_id='" + alm_id + '\'' +
                ", alm_code='" + alm_code + '\'' +
                ", alm_stat='" + alm_stat + '\'' +
                ", alm_text='" + alm_text + '\'' +
                '}';
    }
}