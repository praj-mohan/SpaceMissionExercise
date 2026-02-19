package com.example;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Dashboard {
    static final Color BG     = new Color(245,247,251), WHITE = Color.WHITE;
    static final Color BORDER = new Color(218,224,236), TEXT  = new Color(22,32,54);
    static final Color MUTED  = new Color(110,122,150);
    static final Color BLUE   = new Color(37,99,235),   GREEN = new Color(22,163,74);
    static final Color RED    = new Color(220,38,38),   AMBER = new Color(202,138,4);
    static final Color PURPLE = new Color(109,40,217);
    static final Color[] PAL  = {BLUE, GREEN, AMBER, RED, PURPLE,
                                  new Color(16,185,129), new Color(236,72,153)};
    static final Font FB = new Font("SansSerif", Font.BOLD,  12);
    static final Font FP = new Font("SansSerif", Font.PLAIN, 12);
    static final Font FS = new Font("SansSerif", Font.PLAIN, 11);
    static final Font FV = new Font("SansSerif", Font.BOLD,  24);
    static List<String[]> view;
    static final List<Runnable> onChange = new ArrayList<>();
    public static void show() {
        view = new ArrayList<>(Main.spaceMission);
        SwingUtilities.invokeLater(Dashboard::build);
    }
    static void build() {
        JFrame f = new JFrame("Dashboard");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setExtendedState(Frame.MAXIMIZED_BOTH);
        f.getContentPane().setBackground(BG);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));
        DefaultTableModel tm = new DefaultTableModel(rows(view), Main.columnHeader) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        hdr.add(lbl("Dashboard", new Font("SansSerif",Font.BOLD,20), TEXT), BorderLayout.WEST);
        hdr.add(lbl(Main.spaceMission.size() + " missions  •  space_missions.csv", FS, MUTED), BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        long   succ = Main.spaceMission.stream().filter(m -> "Success".equals(m[8])).count();
        int    tot  = Main.spaceMission.size();
        int    minY = 9999, maxY = 0;
        Set<String> cos = new HashSet<>();
        for (String[] m : Main.spaceMission) {
            cos.add(m[0]);
            try { int y = LocalDate.parse(m[2]).getYear(); if(y<minY)minY=y; if(y>maxY)maxY=y; }
            catch (Exception ignored) {}
        }
        stats.add(statCard("Total Missions", ""+tot,
                            minY+"–"+maxY,    BLUE));
        stats.add(statCard("Success Rate",   Math.round((float)succ/tot*1000f)/10f+"%",
                            succ+" successful", GREEN));
        stats.add(statCard("Companies",      ""+cos.size(),
                            "Top: "+shorten(Main.getMostUsedRocket(),18), PURPLE));
        stats.add(statCard("Avg / Year",     ""+Main.getAverageMissionsPerYear(minY, maxY),
                            "launches/year",  AMBER));

        JPanel fbar = bordered(new FlowLayout(FlowLayout.LEFT, 8, 8));
        fbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        Set<String> cSet=new TreeSet<>(), sSet=new TreeSet<>();
        int mn=9999, mx=0;
        for (String[] m : Main.spaceMission) {
            cSet.add(m[0]); sSet.add(m[8]);
            try { int y=LocalDate.parse(m[2]).getYear(); if(y<mn)mn=y; if(y>mx)mx=y; }
            catch (Exception ignored) {}
        }
        List<String> yrs = new ArrayList<>(); yrs.add("All Years");
        for (int y=mn; y<=mx; y++) yrs.add(""+y);
        String[] yearArr = yrs.toArray(new String[0]);

        JComboBox<String> coCB = combo(prepend("All Companies", cSet));
        JComboBox<String> stCB = combo(prepend("All Statuses",  sSet));
        JComboBox<String> fyB  = combo(yearArr);
        JComboBox<String> tyB  = combo(yearArr);
        tyB.setSelectedIndex(yearArr.length - 1);
        JTextField srch = searchBox();
        JButton    rst  = resetBtn();

        fbar.add(lbl("Filters:", FB, TEXT));
        fbar.add(fgroup("Company",   coCB));
        fbar.add(fgroup("Status",    stCB));
        fbar.add(fgroup("From Year", fyB));
        fbar.add(fgroup("To Year",   tyB));
        fbar.add(fgroup("Search",    srch));
        fbar.add(rst);

        JPanel charts = new JPanel(new GridLayout(1, 3, 10, 0));
        charts.setOpaque(false);
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        charts.add(chartButton("Missions by Company", "View bar chart of top companies by mission count", BLUE,   () -> openChartFrame("Missions by Company", new BarPanel(),  BLUE)));
        charts.add(chartButton("Status Breakdown",    "View donut chart of mission status breakdown",     PURPLE, () -> openChartFrame("Status Breakdown",    new DonutPanel(), PURPLE)));
        charts.add(chartButton("Launches per Year",   "View line chart of launches over time",            AMBER,  () -> openChartFrame("Launches per Year",   new LinePanel(),  AMBER)));
        onChange.add(() -> {});
        JPanel tpanel = bordered(new BorderLayout());
        tpanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        JLabel rowLbl = lbl("", FS, MUTED);
        JPanel tbar = new JPanel(new BorderLayout());
        tbar.setBackground(new Color(243,245,250));
        tbar.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER), new EmptyBorder(8,14,8,14)));
        tbar.add(lbl("Mission Data Table", FB, TEXT), BorderLayout.WEST);
        tbar.add(rowLbl, BorderLayout.EAST);
        JTable tbl = new JTable(tm);
        tbl.setFont(FP); tbl.setRowHeight(28);
        tbl.setBackground(WHITE); tbl.setForeground(TEXT);
        tbl.setGridColor(BORDER); tbl.setShowHorizontalLines(true); tbl.setShowVerticalLines(false);
        tbl.setSelectionBackground(new Color(219,234,254)); tbl.setFillsViewportHeight(true);
        tbl.setAutoCreateRowSorter(true);
        tbl.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,11));
        tbl.getTableHeader().setBackground(new Color(243,245,250));
        tbl.getTableHeader().setForeground(MUTED);
        tbl.getTableHeader().setBorder(new MatteBorder(0,0,1,0,BORDER));
        tbl.setIntercellSpacing(new Dimension(0,0));
        applyRenderers(tbl);
        JScrollPane sp = new JScrollPane(tbl); sp.setBorder(null);
        tpanel.add(tbar, BorderLayout.NORTH);
        tpanel.add(sp,   BorderLayout.CENTER);
        Runnable applyFilter = () -> {
            String co = coCB.getSelectedIndex()==0 ? null : (String)coCB.getSelectedItem();
            String st = stCB.getSelectedIndex()==0 ? null : (String)stCB.getSelectedItem();
            int    fy = fyB.getSelectedIndex()==0  ? -1   : Integer.parseInt((String)fyB.getSelectedItem());
            int    ty = tyB.getSelectedIndex()==0  ? -1   : Integer.parseInt((String)tyB.getSelectedItem());
            String raw = srch.getText().trim();
            String q  = "Search…".equals(raw)||raw.isEmpty() ? null : raw;
            view = Main.spaceMission.stream().filter(m -> {
                if (co != null && !co.equals(m[0])) return false;
                if (st != null && !st.equals(m[8])) return false;
                try {
                    int y = LocalDate.parse(m[2]).getYear();
                    if (fy>0 && y<fy) return false;
                    if (ty>0 && y>ty) return false;
                } catch (Exception ignored) {}
                if (q != null) {
                    for (String cell : m)
                        if (cell!=null && cell.toLowerCase().contains(q.toLowerCase())) return true;
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
            tm.setDataVector(rows(view), Main.columnHeader);
            applyRenderers(tbl);
            rowLbl.setText("Showing "+view.size()+" of "+Main.spaceMission.size()+"   ");
            onChange.forEach(Runnable::run);
        };
        for (JComboBox<?> cb : new JComboBox[]{coCB, stCB, fyB, tyB})
            cb.addActionListener(e -> applyFilter.run());
        srch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { applyFilter.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter.run(); }
        });
        rst.addActionListener(e -> {
            coCB.setSelectedIndex(0); stCB.setSelectedIndex(0);
            fyB.setSelectedIndex(0);  tyB.setSelectedIndex(yearArr.length-1);
            srch.setText("Search…");  srch.setForeground(MUTED);
            view = new ArrayList<>(Main.spaceMission);
            tm.setDataVector(rows(view), Main.columnHeader);
            applyRenderers(tbl);
            rowLbl.setText("Showing "+view.size()+" of "+Main.spaceMission.size()+"   ");
            onChange.forEach(Runnable::run);
        });

        rowLbl.setText("Showing "+view.size()+" of "+Main.spaceMission.size()+"   ");

        root.add(hdr);    root.add(Box.createVerticalStrut(12));
        root.add(stats);  root.add(Box.createVerticalStrut(12));
        root.add(fbar);   root.add(Box.createVerticalStrut(12));
        root.add(charts); root.add(Box.createVerticalStrut(12));
        root.add(tpanel); root.add(Box.createVerticalStrut(20));

        JScrollPane outer = new JScrollPane(root,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outer.setBorder(null);
        outer.getViewport().setBackground(BG);
        outer.getVerticalScrollBar().setUnitIncrement(16);
        f.add(outer);
        f.setVisible(true);
    }
    static class BarPanel extends JPanel {
        BarPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Map<String,Integer> map = new LinkedHashMap<>();
            for (String[] r : view) map.merge(r[0], 1, Integer::sum);
            List<Map.Entry<String,Integer>> top = map.entrySet().stream()
                .sorted((a,b) -> b.getValue()-a.getValue()).limit(7).collect(Collectors.toList());
            if (top.isEmpty()) { drawEmpty(g, getWidth(), getHeight()); return; }
            Graphics2D g2 = aa(g);
            int W=getWidth(), H=getHeight(), PL=8, PR=8, PT=16, PB=38;
            int cH=H-PT-PB, n=top.size(), maxV=top.get(0).getValue();
            int slW=(W-PL-PR)/n, bW=Math.max(8, slW-12);
            g2.setFont(FS); g2.setColor(BORDER);
            for (int i=1; i<=4; i++) {
                int gy=PT+cH-cH*i/4; g2.drawLine(PL,gy,W-PR,gy);
                g2.setColor(MUTED); g2.drawString(""+(maxV*i/4),PL,gy-2); g2.setColor(BORDER);
            }
            for (int i=0; i<n; i++) {
                int bH=(int)((double)top.get(i).getValue()/maxV*cH);
                int x=PL+i*slW+(slW-bW)/2, y=PT+cH-bH;
                g2.setColor(PAL[i%PAL.length]); g2.fillRoundRect(x,y,bW,bH,6,6);
                g2.setFont(new Font("SansSerif",Font.BOLD,10)); g2.setColor(TEXT);
                String v=""+top.get(i).getValue();
                g2.drawString(v, x+(bW-g2.getFontMetrics().stringWidth(v))/2, y-2);
                g2.setFont(FS); g2.setColor(MUTED);
                String lb=shorten(top.get(i).getKey(),10);
                g2.drawString(lb, x+(bW-g2.getFontMetrics().stringWidth(lb))/2, H-PB+13);
            }
            g2.dispose();
        }
    }
    static class DonutPanel extends JPanel {
        DonutPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Map<String,Integer> map = new LinkedHashMap<>();
            for (String[] r : view) map.merge(r[8], 1, Integer::sum);
            if (map.isEmpty()) { drawEmpty(g, getWidth(), getHeight()); return; }
            Graphics2D g2 = aa(g);
            int W=getWidth(), H=getHeight(), legH=52;
            int sz=Math.min(W-16, H-legH)-8;
            if (sz<20) { g2.dispose(); return; }
            int cx=W/2, cy=(H-legH)/2, or=sz/2, ir=(int)(or*.56);
            int tot=map.values().stream().mapToInt(i->i).sum();
            Color[] cols={GREEN,RED,AMBER,MUTED,PURPLE};
            List<String> keys=new ArrayList<>(map.keySet());
            double ang=90;
            for (int i=0; i<keys.size(); i++) {
                double sw=(double)map.get(keys.get(i))/tot*360;
                g2.setColor(cols[i%cols.length]);
                g2.fill(new Arc2D.Double(cx-or,cy-or,or*2,or*2,ang,sw,Arc2D.PIE));
                ang+=sw;
            }
            g2.setColor(WHITE); g2.fillOval(cx-ir,cy-ir,ir*2,ir*2);
            g2.setFont(FV); g2.setColor(TEXT);
            String ts=""+tot; int tw=g2.getFontMetrics().stringWidth(ts);
            g2.drawString(ts, cx-tw/2, cy+8);
            g2.setFont(FS); g2.setColor(MUTED); g2.drawString("total",cx-14,cy+20);
            int cw=W/Math.max(1,keys.size());
            for (int i=0; i<keys.size(); i++) {
                Color c=cols[i%cols.length]; int lx=i*cw+4, ly=H-legH+6;
                g2.setColor(c); g2.fillRoundRect(lx,ly,10,10,3,3);
                g2.setFont(FS); g2.setColor(MUTED); g2.drawString(shorten(keys.get(i),9),lx+13,ly+9);
                g2.setFont(new Font("SansSerif",Font.BOLD,10)); g2.setColor(c);
                g2.drawString(""+map.get(keys.get(i)),lx+13,ly+20);
            }
            g2.dispose();
        }
    }
    static class LinePanel extends JPanel {
        LinePanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Map<Integer,Integer> map=new TreeMap<>();
            for (String[] r : view)
                try { map.merge(LocalDate.parse(r[2]).getYear(),1,Integer::sum); }
                catch (Exception ignored) {}
            if (map.size()<2) { drawEmpty(g, getWidth(), getHeight()); return; }
            Graphics2D g2 = aa(g);
            int W=getWidth(), H=getHeight(), PL=28, PR=10, PT=14, PB=34;
            int cW=W-PL-PR, cH=H-PT-PB;
            List<Integer> ys=new ArrayList<>(map.keySet()), cs=new ArrayList<>(map.values());
            int n=ys.size(), maxV=cs.stream().max(Integer::compareTo).orElse(1);
            g2.setFont(FS); g2.setColor(BORDER);
            for (int i=0; i<=4; i++) {
                int gy=PT+cH-cH*i/4; g2.drawLine(PL,gy,PL+cW,gy);
                g2.setColor(MUTED); g2.drawString(""+(maxV*i/4),0,gy+4); g2.setColor(BORDER);
            }
            int[] px=new int[n], py=new int[n];
            for (int i=0; i<n; i++) {
                px[i]=PL+i*cW/(n-1);
                py[i]=PT+cH-(int)((double)cs.get(i)/maxV*cH);
            }
            int[] fx=new int[n+2], fy=new int[n+2];
            System.arraycopy(px,0,fx,0,n); System.arraycopy(py,0,fy,0,n);
            fx[n]=px[n-1]; fy[n]=PT+cH; fx[n+1]=px[0]; fy[n+1]=PT+cH;
            g2.setColor(new Color(202,138,4,40)); g2.fillPolygon(fx,fy,n+2);
            g2.setColor(AMBER); g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            for (int i=1; i<n; i++) g2.drawLine(px[i-1],py[i-1],px[i],py[i]);
            g2.setStroke(new BasicStroke(1f));
            for (int i=0; i<n; i++) {
                g2.setColor(WHITE); g2.fillOval(px[i]-3,py[i]-3,7,7);
                g2.setColor(AMBER); g2.drawOval(px[i]-3,py[i]-3,7,7);
                if (n<=14 || i%3==0) {
                    g2.setFont(FS); g2.setColor(MUTED);
                    String yr=""+ys.get(i); int tw=g2.getFontMetrics().stringWidth(yr);
                    g2.drawString(yr, px[i]-tw/2, H-PB+13);
                }
            }
            g2.dispose();
        }
    }
    static class AltRenderer extends DefaultTableCellRenderer {
        private final int skip;
        AltRenderer(int s) { skip=s; }
        @Override public Component getTableCellRendererComponent(
                JTable t,Object v,boolean sel,boolean foc,int r,int c) {
            if (c==skip) return t.getColumnModel().getColumn(c).getCellRenderer()
                                 .getTableCellRendererComponent(t,v,sel,foc,r,c);
            JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            l.setFont(FP); l.setBorder(new EmptyBorder(0,10,0,4));
            l.setBackground(sel?new Color(219,234,254):(r%2==0?WHITE:new Color(250,251,254)));
            l.setForeground(TEXT); l.setOpaque(true); return l;
        }
    }

    static class BadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t,Object v,boolean sel,boolean foc,int r,int c) {
            JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            String s=v==null?"":v.toString();
            l.setHorizontalAlignment(CENTER);
            l.setFont(new Font("SansSerif",Font.BOLD,11));
            l.setBorder(new EmptyBorder(0,6,0,6)); l.setOpaque(true);
            l.setBackground(sel?new Color(219,234,254):(r%2==0?WHITE:new Color(250,251,254)));
            if      (s.equalsIgnoreCase("Success"))       l.setForeground(GREEN);
            else if (s.equalsIgnoreCase("Failure"))       l.setForeground(RED);
            else if (s.toLowerCase().contains("partial")) l.setForeground(AMBER);
            else                                          l.setForeground(MUTED);
            return l;
        }
    }


    static void applyRenderers(JTable tbl) {
        int sc = statusCol();
        tbl.setDefaultRenderer(Object.class, new AltRenderer(sc));
        if (sc>=0) tbl.getColumnModel().getColumn(sc).setCellRenderer(new BadgeRenderer());
    }

    static int statusCol() {
        for (int i=0; i<Main.columnHeader.length; i++)
            if (Main.columnHeader[i].toLowerCase().contains("status")) return i;
        return -1;
    }

    static JPanel statCard(String title, String value, String sub, Color accent) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(12,16,12,16)));
        JPanel stripe = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(accent); g.fillRoundRect(0,0,4,getHeight(),4,4); }
        };
        stripe.setPreferredSize(new Dimension(5,1)); stripe.setOpaque(false);
        JPanel body = new JPanel(); body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS)); body.setOpaque(false);
        body.setBorder(new EmptyBorder(0,10,0,0));
        body.add(lbl(title, FS, MUTED));
        body.add(Box.createVerticalStrut(4));
        body.add(lbl(value, FV, accent));
        body.add(lbl(sub,   FS, MUTED));
        p.add(stripe, BorderLayout.WEST); p.add(body, BorderLayout.CENTER);
        return p;
    }

    static JPanel chartButton(String title, String subtitle, Color accent, Runnable onClick) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(18, 20, 18, 20)));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel th = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        th.setOpaque(false);
        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(accent); g.fillOval(0, 2, 9, 9);
            }
        };
        dot.setPreferredSize(new Dimension(10, 14)); dot.setOpaque(false);
        th.add(dot); th.add(lbl(title, FB, TEXT));

        JLabel subLbl = lbl(subtitle, FS, MUTED);

        JButton btn = new JButton("Open Chart");
        btn.setFont(FS); btn.setForeground(BLUE); btn.setBackground(accent);
        btn.setBorder(new CompoundBorder(new LineBorder(accent, 1, true), new EmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(accent.darker()); }
            public void mouseExited (MouseEvent e) { btn.setBackground(accent); }
        });

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(btn);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(th);
        body.add(Box.createVerticalStrut(4));
        body.add(subLbl);
        body.add(Box.createVerticalStrut(10));
        body.add(btnWrap);

        p.add(body, BorderLayout.CENTER);

        btn.addActionListener(e -> onClick.run());
        p.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onClick.run(); }
            public void mouseEntered(MouseEvent e) { p.setBackground(new Color(245, 247, 255)); }
            public void mouseExited (MouseEvent e) { p.setBackground(WHITE); }
        });
        return p;
    }

    static void openChartFrame(String title, JPanel chart, Color accent) {
        JFrame cf = new JFrame(title);
        cf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        cf.setSize(700, 480);
        cf.setLocationRelativeTo(null);
        cf.getContentPane().setBackground(BG);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER), new EmptyBorder(0,0,10,0)));
        hdr.add(lbl(title, new Font("SansSerif", Font.BOLD, 16), TEXT), BorderLayout.WEST);

        JPanel chartWrap = chartCard(title, chart, accent);

        onChange.add(() -> chart.repaint());

        root.add(hdr, BorderLayout.NORTH);
        root.add(chartWrap, BorderLayout.CENTER);

        cf.add(root);
        cf.setVisible(true);
    }

    static JPanel chartCard(String title, JPanel chart, Color accent) {
        JPanel p = bordered(new BorderLayout(0,8));
        p.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(12,14,12,14)));
        JPanel th = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); th.setOpaque(false);
        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(accent); g.fillOval(0,2,9,9); }
        };
        dot.setPreferredSize(new Dimension(10,14)); dot.setOpaque(false);
        th.add(dot); th.add(lbl(title, FB, TEXT));
        p.add(th,    BorderLayout.NORTH);
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    static JPanel bordered(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(WHITE);
        p.setBorder(new LineBorder(BORDER,1,true)); return p;
    }

    static JLabel lbl(String t, Font f, Color c) {
        JLabel l=new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }

    static JComboBox<String> combo(String[] items) {
        JComboBox<String> cb=new JComboBox<>(items);
        cb.setFont(FP); cb.setBackground(WHITE); cb.setFocusable(false);
        cb.setPreferredSize(new Dimension(138,28)); return cb;
    }

    static JPanel fgroup(String label, JComponent c) {
        JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setOpaque(false);
        JLabel l=lbl(label,FS,MUTED); l.setBorder(new EmptyBorder(0,2,2,0));
        p.add(l); p.add(c); return p;
    }

    static JTextField searchBox() {
        JTextField tf=new JTextField(14); tf.setFont(FP); tf.setForeground(MUTED); tf.setText("Search…");
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(3,8,3,8)));
        tf.setPreferredSize(new Dimension(155,28));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if("Search…".equals(tf.getText())){tf.setText("");tf.setForeground(TEXT);} }
            public void focusLost (FocusEvent e) { if(tf.getText().isEmpty()){tf.setText("Search…");tf.setForeground(MUTED);} }
        }); return tf;
    }

    static JButton resetBtn() {
        JButton b=new JButton("↺  Reset"); b.setFont(FS); b.setForeground(MUTED); b.setBackground(WHITE);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(5,10,5,10)));
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e){b.setForeground(BLUE); b.setBorder(new CompoundBorder(new LineBorder(BLUE,1,true),  new EmptyBorder(5,10,5,10)));}
            public void mouseExited (MouseEvent e){b.setForeground(MUTED);b.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true),new EmptyBorder(5,10,5,10)));}
        }); return b;
    }

    static Object[][] rows(List<String[]> data) { return data.toArray(new Object[0][]); }

    static String[] prepend(String first, Set<String> rest) {
        List<String> l=new ArrayList<>(); l.add(first); l.addAll(rest); return l.toArray(new String[0]);
    }

    static String shorten(String s, int max) {
        return s==null?"":s.length()<=max?s:s.substring(0,max-1)+"…";
    }

    static Graphics2D aa(Graphics g) {
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    static void drawEmpty(Graphics g, int W, int H) {
        Graphics2D g2=aa(g); g2.setFont(new Font("SansSerif",Font.ITALIC,11)); g2.setColor(MUTED);
        String m="No data"; g2.drawString(m, W/2-g2.getFontMetrics().stringWidth(m)/2, H/2+4);
        g2.dispose();
    }
}
