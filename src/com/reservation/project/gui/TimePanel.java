package com.reservation.project.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;

/**
 * 时间选择面板类，用于选择开始和结束时间
 * 包含年、月、日、时、分的选择下拉框
 */
public class TimePanel extends JPanel {

    // 开始时间的下拉框组件
    private JComboBox<String> startYear;      // 开始年份下拉框
    private JComboBox<String> startMonth;     // 开始月份下拉框
    private JComboBox<String> startDay;       // 开始日期下拉框
    private JComboBox<String> startHour;      // 开始小时下拉框
    private JComboBox<String> startMinute;    // 开始分钟下拉框

    // 结束时间的下拉框组件
    private JComboBox<String> endYear;        // 结束年份下拉框
    private JComboBox<String> endMonth;       // 结束月份下拉框
    private JComboBox<String> endDay;         // 结束日期下拉框
    private JComboBox<String> endHour;        // 结束小时下拉框
    private JComboBox<String> endMinute;      // 结束分钟下拉框

    /**
     * 构造函数，初始化时间选择面板
     */
    public TimePanel() {
        // 设置网格布局，2行1列，水平间距6，垂直间距6
        setLayout(new GridLayout(2, 1, 6, 6));

        // 创建开始和结束时间的面板
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        // 初始化开始时间的下拉框
        startYear = new JComboBox<String>(buildYearRange());    // 构建年份范围下拉框
        startMonth = new JComboBox<String>(buildRange(1, 12)); // 构建月份范围下拉框(1-12)
        startDay = new JComboBox<String>(buildRange(1, 31));     // 构建日期范围下拉框(1-31)
        startHour = new JComboBox<String>(buildRange(0, 23));   // 构建小时范围下拉框(0-23)
        // 分钟下拉框，每5分钟为一个间隔
        startMinute = new JComboBox<String>(new String[]{"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"});

        // 初始化结束时间的下拉框
        endYear = new JComboBox<String>(buildYearRange());      // 构建年份范围下拉框
        endMonth = new JComboBox<String>(buildRange(1, 12));    // 构建月份范围下拉框(1-12)
        endDay = new JComboBox<String>(buildRange(1, 31));      // 构建日期范围下拉框(1-31)
        endHour = new JComboBox<String>(buildRange(0, 23));    // 构建小时范围下拉框(0-23)
        // 分钟下拉框，每5分钟为一个间隔
        endMinute = new JComboBox<String>(new String[]{"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"});

        // 获取当前时间
        Calendar now = Calendar.getInstance();
        String y = String.valueOf(now.get(Calendar.YEAR));          // 当前年份
        String m = two(now.get(Calendar.MONTH) + 1);               // 当前月份(补零)
        String d = two(now.get(Calendar.DAY_OF_MONTH));             // 当前日期(补零)



        // 设置开始时间的默认值
        startYear.setSelectedItem(y);      // 设置当前年份
        startMonth.setSelectedItem(m);     // 设置当前月份
        startDay.setSelectedItem(d);       // 设置当前日期
        startHour.setSelectedItem("09");   // 设置开始时间为9点
        startMinute.setSelectedItem("00"); // 设置开始分钟为0分



        // 设置结束时间的默认值
        endYear.setSelectedItem(y);        // 设置当前年份
        endMonth.setSelectedItem(m);      // 设置当前月份
        endDay.setSelectedItem(d);        // 设置当前日期
        endHour.setSelectedItem("10");    // 设置结束时间为10点
        endMinute.setSelectedItem("00");  // 设置结束分钟为0分

        // 将开始时间组件添加到面板
        startPanel.add(new JLabel("开始"));  // 添加"开始"标签
        addDateTimeCombo(startPanel, startYear, startMonth, startDay, startHour, startMinute);

        // 将结束时间组件添加到面板
        endPanel.add(new JLabel("结束"));    // 添加"结束"标签
        addDateTimeCombo(endPanel, endYear, endMonth, endDay, endHour, endMinute);

        // 将开始和结束面板添加到主面板
        add(startPanel);
        add(endPanel);
    }

    /**
     * 向面板添加日期时间选择组件
     * @param p 要添加组件的面板
     * @param y 年份下拉框
     * @param mo 月份下拉框
     * @param d 日期下拉框
     * @param h 小时下拉框
     * @param mi 分钟下拉框
     */
    private void addDateTimeCombo(JPanel p, JComboBox<String> y, JComboBox<String> mo, JComboBox<String> d,
                                  JComboBox<String> h, JComboBox<String> mi) {
        p.add(y);
        p.add(new JLabel("年"));   // 添加年份下拉框和"年"标签
        p.add(mo);
        p.add(new JLabel("月"));   // 添加月份下拉框和"月"标签
        p.add(d);
        p.add(new JLabel("日"));   // 添加日期下拉框和"日"标签
        p.add(h);
        p.add(new JLabel("时"));   // 添加小时下拉框和"时"标签
        p.add(mi);
        p.add(new JLabel("分"));   // 添加分钟下拉框和"分"标签
    }

    /**
     * 构建年份范围数组
     * @return 包含当前年份-1到当前年份+4的字符串数组
     */
    private String[] buildYearRange() {
        Calendar c = Calendar.getInstance();    // 获取当前日历实例
        int cur = c.get(Calendar.YEAR);        // 获取当前年份
        String[] arr = new String[6]; // 当前年-1 到 当前年+4
        int idx = 0;
        for (int y = cur - 1; y <= cur + 4; y++) {
            arr[idx++] = String.valueOf(y);
        }
        return arr;
    }

    /**
    * 构建一个从from到to的字符串数组，每个元素是对应数字的两位数表示
    * @param from 起始数字（包含）
    * @param to 结束数字（包含）
    * @return 包含from到to所有数字的两位数表示的字符串数组
    */
    private String[] buildRange(int from, int to) {
        // 创建一个大小为to-from+1的字符串数组
        String[] arr = new String[to - from + 1];
        int idx = 0;
        // 遍历从from到to的所有数字
        for (int i = from; i <= to; i++) {
            // 将当前数字转换为两位数表示并存入数组
            arr[idx++] = two(i);
        }
        return arr;
    }

    /**
    * 将整数转换为两位数的字符串格式
    * 如果整数小于10，前面补0；否则直接转换为字符串
    * @param n 需要转换的整数
    * @return 两位数的字符串表示
    */
    private String two(int n) {
    // 使用三元运算符判断数字是否小于10
    // 如果小于10，在前面补0；否则直接将数字转换为字符串
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    /**
    * 获取开始时间的文本表示
    * 该方法组合了年、月、日、时、分的下拉框选择值，并格式化为"yyyy-MM-dd HH:mm:00"的字符串格式
    * @return 返回格式化后的开始时间字符串，格式为"年-月-日 时:分:00"
    */
    public String getStartText() {
    // 从下拉框中获取选中的年、月、日、时、分，并拼接成时间字符串
        return startYear.getSelectedItem() + "-" + startMonth.getSelectedItem() + "-" + startDay.getSelectedItem()
                + " " + startHour.getSelectedItem() + ":" + startMinute.getSelectedItem() + ":00";
    }

    /**
    * 获取结束时间的文本表示
    * 该方法组合了年份、月份、日期、小时和分钟，格式化为"YYYY-MM-DD HH:MM:00"的字符串
    * @return 返回格式化后的结束时间字符串，例如"2023-12-31 23:59:00"
    */
    public String getEndText() {
    // 从下拉列表中获取选中的年份、月份、日期、小时和分钟
    // 并将它们组合成一个格式化的时间字符串
        return endYear.getSelectedItem() + "-" + endMonth.getSelectedItem() + "-" + endDay.getSelectedItem()
                + " " + endHour.getSelectedItem() + ":" + endMinute.getSelectedItem() + ":00";
    }
}