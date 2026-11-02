package com.esports.view.common;

import com.esports.config.ThemeConfig;
import com.esports.dao.StatsDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Utility for rendering JFreeChart visualizations styled with FlatLaf Cyber Dark theme.
 */
public class ChartUtil {

    private static final Color DARK_BG = ThemeConfig.COLOR_CARD_BG;
    private static final Color TEXT_COLOR = ThemeConfig.COLOR_TEXT_PRIMARY;
    private static final Color GRID_COLOR = new Color(45, 53, 72);

    private static final Color[] PALETTE = {
            ThemeConfig.COLOR_ACCENT_CYAN,
            ThemeConfig.COLOR_ACCENT_PURPLE,
            ThemeConfig.COLOR_SUCCESS,
            ThemeConfig.COLOR_WARNING,
            ThemeConfig.COLOR_DANGER,
            new Color(255, 111, 0),
            new Color(0, 184, 212),
            new Color(156, 39, 176)
    };

    /**
     * Maps tournament statuses to dedicated semantic cyber dark theme colors.
     */
    private static Color getStatusColor(String status, int fallbackIdx) {
        if (status == null) return PALETTE[fallbackIdx % PALETTE.length];
        return switch (status.toUpperCase()) {
            case "REGISTRATION_OPEN" -> ThemeConfig.COLOR_SUCCESS;       // Emerald Green (#10B981)
            case "ONGOING"           -> ThemeConfig.COLOR_ACCENT_CYAN;   // Cyan (#06B6D4)
            case "UPCOMING"          -> ThemeConfig.COLOR_WARNING;       // Amber/Gold (#F59E0B)
            case "REGISTRATION_CLOSED" -> ThemeConfig.COLOR_ACCENT_BLUE; // Cobalt Blue (#3B82F6)
            case "COMPLETED"         -> ThemeConfig.COLOR_ACCENT_PURPLE; // Electric Purple (#8B5CF6)
            case "CANCELLED"         -> ThemeConfig.COLOR_DANGER;        // Crimson Red (#EF4444)
            case "DRAFT"             -> new Color(100, 116, 139);        // Slate Gray
            default                  -> PALETTE[fallbackIdx % PALETTE.length];
        };
    }

    /**
     * Creates a styled Donut / Pie Chart for Tournament Status distribution.
     * Shows accurate counts, percentages, and labels dynamically.
     */
    public static ChartPanel createPieChart(String title, Map<String, Integer> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (data != null) {
            data.forEach((status, count) -> {
                if (count != null && count > 0) {
                    dataset.setValue(status, count);
                }
            });
        }

        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        styleChartCommon(chart);

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(DARK_BG);
        plot.setOutlinePaint(null);
        plot.setLabelBackgroundPaint(new Color(26, 32, 44, 220));
        plot.setLabelPaint(TEXT_COLOR);
        plot.setLabelOutlinePaint(ThemeConfig.COLOR_BORDER);
        plot.setLabelFont(ThemeConfig.FONT_SMALL);

        // Accurate labels with Status Name, Count, and Percentage: "{0}: {1} ({2})"
        StandardPieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})",
                new DecimalFormat("#,##0"),
                new DecimalFormat("0.0%")
        );
        plot.setLabelGenerator(labelGenerator);
        plot.setLegendLabelToolTipGenerator(new StandardPieSectionLabelGenerator("{0}: {1} Tournaments ({2})"));

        // Meaningful empty state
        plot.setNoDataMessage("No tournament status records found in database.");
        plot.setNoDataMessageFont(ThemeConfig.FONT_BOLD);
        plot.setNoDataMessagePaint(ThemeConfig.COLOR_TEXT_MUTED);

        // Apply dedicated status colors
        int idx = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, getStatusColor(key.toString(), idx));
            idx++;
        }

        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(DARK_BG);
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates a styled Multi-Series Bar Chart for Game Popularity.
     * Displays Tournaments, Registered Teams, and Players per Game Title, sorted by tournament count desc.
     */
    public static ChartPanel createGamePopularityChart(String title, List<StatsDAO.GamePopularityStat> stats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (stats != null) {
            for (StatsDAO.GamePopularityStat stat : stats) {
                dataset.addValue(stat.tournamentCount(), "Tournaments", stat.gameName());
                dataset.addValue(stat.teamCount(), "Teams", stat.gameName());
                dataset.addValue(stat.playerCount(), "Players", stat.gameName());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Game Title",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        styleChartCommon(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(DARK_BG);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setOutlinePaint(null);

        // Dark axis styling
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        domainAxis.setLabelPaint(TEXT_COLOR);
        domainAxis.setTickLabelFont(ThemeConfig.FONT_SMALL);
        domainAxis.setCategoryMargin(0.15);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        rangeAxis.setLabelPaint(TEXT_COLOR);
        rangeAxis.setTickLabelFont(ThemeConfig.FONT_SMALL);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Empty state message
        plot.setNoDataMessage("No game activity recorded in database.");
        plot.setNoDataMessageFont(ThemeConfig.FONT_BOLD);
        plot.setNoDataMessagePaint(ThemeConfig.COLOR_TEXT_MUTED);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.04);

        // Series colors: Tournaments (Cyan), Teams (Purple), Players (Green)
        renderer.setSeriesPaint(0, ThemeConfig.COLOR_ACCENT_CYAN);
        renderer.setSeriesPaint(1, ThemeConfig.COLOR_ACCENT_PURPLE);
        renderer.setSeriesPaint(2, ThemeConfig.COLOR_SUCCESS);

        // Numeric labels on top of bars
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##0")));
        renderer.setDefaultItemLabelPaint(TEXT_COLOR);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 10));

        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(DARK_BG);
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates a styled Bar Chart for Top Teams Leaderboard.
     * Displays Total Points, Wins, and Matches Played sorted by total points descending.
     */
    public static ChartPanel createTopTeamsChart(String title, List<StatsDAO.TeamPointsStat> stats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (stats != null) {
            for (StatsDAO.TeamPointsStat stat : stats) {
                dataset.addValue(stat.totalPoints(), "Total Points", stat.teamName());
                dataset.addValue(stat.matchesWon(), "Wins", stat.teamName());
                dataset.addValue(stat.matchesPlayed(), "Matches Played", stat.teamName());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "eSports Team",
                "Performance Metrics",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        styleChartCommon(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(DARK_BG);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setOutlinePaint(null);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        domainAxis.setLabelPaint(TEXT_COLOR);
        domainAxis.setTickLabelFont(ThemeConfig.FONT_SMALL);
        domainAxis.setCategoryMargin(0.15);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        rangeAxis.setLabelPaint(TEXT_COLOR);
        rangeAxis.setTickLabelFont(ThemeConfig.FONT_SMALL);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        plot.setNoDataMessage("No team performance records found in database.");
        plot.setNoDataMessageFont(ThemeConfig.FONT_BOLD);
        plot.setNoDataMessagePaint(ThemeConfig.COLOR_TEXT_MUTED);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.04);

        // Series colors: Total Points (Purple), Wins (Green), Matches Played (Gold)
        renderer.setSeriesPaint(0, ThemeConfig.COLOR_ACCENT_PURPLE);
        renderer.setSeriesPaint(1, ThemeConfig.COLOR_SUCCESS);
        renderer.setSeriesPaint(2, ThemeConfig.COLOR_WARNING);

        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##0")));
        renderer.setDefaultItemLabelPaint(TEXT_COLOR);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 10));

        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(DARK_BG);
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates a styled Bar Chart for Top Players (Kills).
     * Displays Player Name, Team Name, and Total Kills sorted descending.
     * Shows a meaningful empty state when kill statistics are unrecorded.
     */
    public static ChartPanel createTopPlayersChart(String title, List<StatsDAO.PlayerKillsStat> stats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (stats != null) {
            for (StatsDAO.PlayerKillsStat stat : stats) {
                if (stat.kills() > 0) {
                    dataset.addValue(stat.kills(), "Total Kills", stat.gamerTag() + " (" + stat.teamName() + ")");
                }
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Player (Team)",
                "Total Kills",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        styleChartCommon(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(DARK_BG);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setOutlinePaint(null);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        domainAxis.setLabelPaint(TEXT_COLOR);
        domainAxis.setTickLabelFont(ThemeConfig.FONT_SMALL);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        rangeAxis.setLabelPaint(TEXT_COLOR);
        rangeAxis.setTickLabelFont(ThemeConfig.FONT_SMALL);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Meaningful empty state when kill statistics are not recorded yet
        plot.setNoDataMessage("No player kill statistics recorded yet. Kills are logged upon match completion.");
        plot.setNoDataMessageFont(ThemeConfig.FONT_BOLD);
        plot.setNoDataMessagePaint(ThemeConfig.COLOR_TEXT_MUTED);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, ThemeConfig.COLOR_DANGER);

        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##0")));
        renderer.setDefaultItemLabelPaint(TEXT_COLOR);
        renderer.setDefaultItemLabelFont(ThemeConfig.FONT_SMALL);

        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(DARK_BG);
        panel.setOpaque(false);
        return panel;
    }

    /**
     * General styled Bar Chart for backwards compatibility with existing views.
     */
    public static ChartPanel createBarChart(String title, String categoryAxisLabel, String valueAxisLabel,
                                            Map<String, Integer> data, Color barColor) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (data != null) {
            data.forEach((cat, val) -> {
                if (val != null && val > 0) {
                    dataset.addValue(val, "Metrics", cat);
                }
            });
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        styleChartCommon(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(DARK_BG);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setOutlinePaint(null);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        domainAxis.setLabelPaint(TEXT_COLOR);
        domainAxis.setTickLabelFont(ThemeConfig.FONT_SMALL);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        rangeAxis.setLabelPaint(TEXT_COLOR);
        rangeAxis.setTickLabelFont(ThemeConfig.FONT_SMALL);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        plot.setNoDataMessage("No records found in database.");
        plot.setNoDataMessageFont(ThemeConfig.FONT_BOLD);
        plot.setNoDataMessagePaint(ThemeConfig.COLOR_TEXT_MUTED);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, barColor != null ? barColor : ThemeConfig.COLOR_ACCENT_CYAN);
        renderer.setShadowVisible(false);

        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##0")));
        renderer.setDefaultItemLabelPaint(TEXT_COLOR);
        renderer.setDefaultItemLabelFont(ThemeConfig.FONT_SMALL);

        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(DARK_BG);
        panel.setOpaque(false);
        return panel;
    }

    private static void styleChartCommon(JFreeChart chart) {
        chart.setBackgroundPaint(DARK_BG);
        chart.setAntiAlias(true);

        TextTitle chartTitle = chart.getTitle();
        if (chartTitle != null) {
            chartTitle.setPaint(TEXT_COLOR);
            chartTitle.setFont(ThemeConfig.FONT_BOLD);
        }

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(DARK_BG);
            chart.getLegend().setItemPaint(ThemeConfig.COLOR_TEXT_MUTED);
            chart.getLegend().setItemFont(ThemeConfig.FONT_SMALL);
        }
    }
}
