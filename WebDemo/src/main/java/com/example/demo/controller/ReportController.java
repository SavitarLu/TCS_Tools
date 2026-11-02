package com.example.demo.controller;

import com.example.demo.data.DECSALRM_HIS;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.util.DB2Util.executeCountQuery;
import static com.example.demo.util.DB2Util.executeQuery;

@Controller
@RequestMapping("/report")
public class ReportController {

    private static final int MAX_PAGE_SIZE = 500;

    /**
     * 报表首页 - 展示导航栏
     */
    @GetMapping
    public String index() {
        return "layout";
    }

    /**
     * DECSALRM_HIS 报表页面。
     *
     * <p>查询条件全部通过 {@code ?} 占位符传入，日期只接受 yyyy-MM-dd 形式，
     * 避免拼接字符串带来的 SQL 注入。</p>
     */
    @GetMapping("/decsalrm-his")
    public String decsalrmHisReport(Model model,
                                    @RequestParam(required = false) String eqpId,
                                    @RequestParam(required = false) String almId,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size) {
        model.addAttribute("activePage", "decsalrm-his");

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        StringBuilder sql = new StringBuilder("SELECT * FROM LS2WPRD.DECSALRM_HIS WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (eqpId != null && !eqpId.isEmpty()) {
            sql.append(" AND EQP_ID LIKE ?");
            params.add("%" + eqpId + "%");
        }
        if (almId != null && !almId.isEmpty()) {
            sql.append(" AND ALM_ID LIKE ?");
            params.add("%" + almId + "%");
        }
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND ALM_DTTM >= TIMESTAMP(?)");
            params.add(startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND ALM_DTTM <= TIMESTAMP(?)");
            params.add(endDate + " 23:59:59");
        }

        int totalElements = executeCountQuery(
                "SELECT COUNT(*) FROM (" + sql + ") AS TEMP", params.toArray());

        sql.append(" ORDER BY ALM_DTTM DESC");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(safePage * safeSize);
        params.add(safeSize);

        List<DECSALRM_HIS> eventList = executeQuery(sql.toString(), DECSALRM_HIS.class, params.toArray());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (DECSALRM_HIS event : eventList) {
            if (event.getAlm_dttm() != null) {
                event.setFormattedAlmDttm(sdf.format(event.getAlm_dttm()));
            }
        }

        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        model.addAttribute("eventList", eventList);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("pageSize", safeSize);
        // 没有记录时不做 +1，避免显示 "1 - 0"
        model.addAttribute("startRow", totalElements == 0 ? 0 : safePage * safeSize + 1);
        model.addAttribute("endRow", Math.min((safePage + 1) * safeSize, totalElements));

        // 保留查询条件
        model.addAttribute("eqpId", eqpId);
        model.addAttribute("almId", almId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "report/decsalrm-his";
    }
}
