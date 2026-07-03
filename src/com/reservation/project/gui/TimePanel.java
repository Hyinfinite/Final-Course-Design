package com.reservation.project.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;

public class TimePanel extends JPanel {

    private JComboBox<String> startYear;
    private JComboBox<String> startMonth;
    private JComboBox<String> startDay;
    private JComboBox<String> startHour;
    private JComboBox<String> startMinute;

    private JComboBox<String> endYear;
    private JComboBox<String> endMonth;
    private JComboBox<String> endDay;
    private JComboBox<String> endHour;
    private JComboBox<String> endMinute;

    public TimePanel() {
        setLayout(new GridLayout(2, 1, 6, 6));

        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        startYear = new JComboBox<String>(buildYearRange());
        startMonth = new JComboBox<String>(buildRange(1, 12));
        startDay = new JComboBox<String>(buildRange(1, 31));
        startHour = new JComboBox<String>(buildRange(0, 23));
        startMinute = new JComboBox<String>(new String[]{"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"});

        endYear = new JComboBox<String>(buildYearRange());
        endMonth = new JComboBox<String>(buildRange(1, 12));
        endDay = new JComboBox<String>(buildRange(1, 31));
        endHour = new JComboBox<String>(buildRange(0, 23));
        endMinute = new JComboBox<String>(new String[]{"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"});

        Calendar now = Calendar.getInstance();
        String y = String.valueOf(now.get(Calendar.YEAR));
        String m = two(now.get(Calendar.MONTH) + 1);
        String d = two(now.get(Calendar.DAY_OF_MONTH));

        startYear.setSelectedItem(y);
        startMonth.setSelectedItem(m);
        startDay.setSelectedItem(d);
        startHour.setSelectedItem("09");
        startMinute.setSelectedItem("00");

        endYear.setSelectedItem(y);
        endMonth.setSelectedItem(m);
        endDay.setSelectedItem(d);
        endHour.setSelectedItem("10");
        endMinute.setSelectedItem("00");

        startPanel.add(new JLabel("开始"));
        addDateTimeCombo(startPanel, startYear, startMonth, startDay, startHour, startMinute);

        endPanel.add(new JLabel("结束"));
        addDateTimeCombo(endPanel, endYear, endMonth, endDay, endHour, endMinute);

        add(startPanel);
        add(endPanel);
    }

    private void addDateTimeCombo(JPanel p, JComboBox<String> y, JComboBox<String> mo, JComboBox<String> d,
                                  JComboBox<String> h, JComboBox<String> mi) {
        p.add(y);  p.add(new JLabel("年"));
        p.add(mo); p.add(new JLabel("月"));
        p.add(d);  p.add(new JLabel("日"));
        p.add(h);  p.add(new JLabel("时"));
        p.add(mi); p.add(new JLabel("分"));
    }

    private String[] buildYearRange() {
        Calendar c = Calendar.getInstance();
        int cur = c.get(Calendar.YEAR);
        String[] arr = new String[6]; // 当前年-1 到 当前年+4
        int idx = 0;
        for (int y = cur - 1; y <= cur + 4; y++) {
            arr[idx++] = String.valueOf(y);
        }
        return arr;
    }

    private String[] buildRange(int from, int to) {
        String[] arr = new String[to - from + 1];
        int idx = 0;
        for (int i = from; i <= to; i++) {
            arr[idx++] = two(i);
        }
        return arr;
    }

    private String two(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    public String getStartText() {
        return startYear.getSelectedItem() + "-" + startMonth.getSelectedItem() + "-" + startDay.getSelectedItem()
                + " " + startHour.getSelectedItem() + ":" + startMinute.getSelectedItem() + ":00";
    }

    public String getEndText() {
        return endYear.getSelectedItem() + "-" + endMonth.getSelectedItem() + "-" + endDay.getSelectedItem()
                + " " + endHour.getSelectedItem() + ":" + endMinute.getSelectedItem() + ":00";
    }
}