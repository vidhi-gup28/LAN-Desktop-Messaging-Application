package com.lanmessenger.client.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Enterprise desktop UI utilities, color palette, and audio chimes.
 * Strict professional enterprise light theme design.
 */
public final class UiUtils {

    // Professional enterprise light theme palette
    public static final Color COLOR_NAVY_PRIMARY    = new Color(30, 58, 138);   // Muted Navy #1E3A8A
    public static final Color COLOR_NAVY_HOVER      = new Color(29, 78, 216);   // #1D4ED8
    public static final Color COLOR_BG_WHITE        = new Color(255, 255, 255); // #FFFFFF
    public static final Color COLOR_BG_LIGHT_GREY   = new Color(248, 249, 250); // #F8F9FA
    public static final Color COLOR_PANEL_GREY      = new Color(240, 242, 245); // #F0F2F5
    public static final Color COLOR_BORDER_GREY     = new Color(209, 213, 219); // #D1D5DB
    public static final Color COLOR_BORDER_SUBTLE   = new Color(229, 231, 235); // #E5E7EB
    public static final Color COLOR_TEXT_PRIMARY    = new Color(17, 24, 39);    // #111827
    public static final Color COLOR_TEXT_SECONDARY  = new Color(107, 114, 128); // #6B7280
    public static final Color COLOR_TEXT_MUTED      = new Color(156, 163, 175); // #9CA3AF
    public static final Color COLOR_ONLINE_GREEN    = new Color(22, 163, 74);   // #16A34A
    public static final Color COLOR_OFFLINE_GREY    = new Color(156, 163, 175); // #9CA3AF
    public static final Color COLOR_BADGE_BG        = new Color(220, 38, 38);   // #DC2626
    public static final Color COLOR_OUTGOING_BG     = new Color(238, 244, 254); // Subtle Light Blue tint
    public static final Color COLOR_OUTGOING_BORDER = new Color(191, 219, 254); // Soft blue border
    public static final Color COLOR_INCOMING_BG     = new Color(255, 255, 255); // Crisp White
    public static final Color COLOR_INCOMING_BORDER = new Color(229, 231, 235); // Soft grey border

    // Standard desktop enterprise typography
    public static final Font FONT_HEADER      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_BODY        = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_CODE        = new Font("Consolas", Font.PLAIN, 12);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, HH:mm").withZone(ZoneId.systemDefault());

    private UiUtils() {
    }

    public static String formatTimestamp(long timestampMillis) {
        Instant instant = Instant.ofEpochMilli(timestampMillis);
        LocalDate msgDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        if (msgDate.equals(today)) {
            return TIME_FORMAT.format(instant);
        } else {
            return DATE_TIME_FORMAT.format(instant);
        }
    }

    /**
     * Small enterprise avatar icon for desktop directory.
     */
    public static Icon createAvatarIcon(String name, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Subtle desktop background fill
        g2.setColor(new Color(226, 232, 240));
        g2.fillRect(0, 0, size, size);

        // Thin 1px border
        g2.setColor(new Color(203, 213, 225));
        g2.drawRect(0, 0, size - 1, size - 1);

        // Navy blue text initials
        g2.setColor(COLOR_NAVY_PRIMARY);
        String initial = getInitials(name);
        int fontSize = Math.max(9, size / 2);
        g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));

        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(initial)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(initial, x, y);

        g2.dispose();
        return new ImageIcon(image);
    }

    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String clean = name.trim();
        if (clean.contains("Broadcast")) {
            return "ALL";
        }
        String[] parts = clean.split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return ("" + clean.charAt(0)).toUpperCase();
    }

    /**
     * Synthesizes a clean, discreet enterprise audio beep.
     */
    public static void playNotificationSound() {
        new Thread(() -> {
            try {
                int sampleRate = 44100;
                int durationMs = 120;
                int numSamples = (sampleRate * durationMs) / 1000;
                byte[] audio = new byte[numSamples * 2];

                double freq = 784.00; // G5 note - gentle desktop beep
                for (int i = 0; i < numSamples; i++) {
                    double time = i / (double) sampleRate;
                    double envelope = Math.exp(-8.0 * (time / (durationMs / 1000.0)));
                    double sample = 0.4 * Math.sin(2 * Math.PI * freq * time) * envelope;
                    short val = (short) (sample * 32767);
                    audio[2 * i] = (byte) (val & 0xFF);
                    audio[2 * i + 1] = (byte) ((val >> 8) & 0xFF);
                }

                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();
                    line.write(audio, 0, audio.length);
                    line.drain();
                }
            } catch (Exception ignored) {
            }
        }, "Desktop-Chime-Thread").start();
    }
}
