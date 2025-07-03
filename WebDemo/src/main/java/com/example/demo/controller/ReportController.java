package com.example.demo.controller;

import com.example.demo.data.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.example.demo.util.DB2Util.*;

@Controller
@RequestMapping("/report")
public class ReportController {

    /**
     * 报表首页 - 展示导航栏
     */
    @GetMapping
    public String index() {
        return "layout"; // 直接返回布局模板
    }

    /**
     * DECSALRM_HIS报表页面
     */
    @GetMapping("/decsalrm-his")
    public String decsalrmHisReport(Model model,
                                    @RequestParam(required = false) String eqpId,
                                    @RequestParam(required = false) String almId,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size) {
        model.addAttribute("activePage", "decsalrm-his"); // 添加活动页面标识

        // 构建查询条件
        StringBuilder sql = new StringBuilder("SELECT * FROM LS2WPRD.DECSALRM_HIS WHERE 1=1");

        if (eqpId != null && !eqpId.isEmpty()) {
            sql.append(" AND EQP_ID LIKE '%").append(eqpId).append("%'");
        }
        if (almId != null && !almId.isEmpty()) {
            sql.append(" AND ALM_ID LIKE '%").append(almId).append("%'");
        }
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND ALM_DTTM >= TIMESTAMP('").append(startDate).append(" 00:00:00')");
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND ALM_DTTM <= TIMESTAMP('").append(endDate).append(" 23:59:59')");
        }

        // 计算总记录数
        String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") AS TEMP";
        Integer totalElements = executeCountQuery(countSql);

        // 添加分页
        sql.append(" ORDER BY ALM_DTTM DESC");
        sql.append(" OFFSET ").append(page * size).append(" ROWS FETCH NEXT ").append(size).append(" ROWS ONLY");

        // 执行查询
        List<DECSALRM_HIS> eventList = executeQuery(sql.toString(), DECSALRM_HIS.class);
        // 对日期进行格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (DECSALRM_HIS event : eventList) {
            if (event.getAlm_dttm() != null) {
                event.setFormattedAlmDttm(sdf.format(event.getAlm_dttm()));
            }
        }
        // 计算总页数
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // 将分页结果添加到模型
        model.addAttribute("eventList", eventList);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("startRow", page * size + 1);
        model.addAttribute("endRow", Math.min((page + 1) * size, totalElements));

        // 保留查询条件
        model.addAttribute("eqpId", eqpId);
        model.addAttribute("almId", almId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "report/decsalrm-his";
    }
}