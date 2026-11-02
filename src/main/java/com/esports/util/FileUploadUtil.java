package com.esports.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility for selecting, validating, and saving image uploads (Team Logos, Player Avatars).
 */
public class FileUploadUtil {

    private static final String UPLOADS_DIR = "uploads";

    static {
        // Ensure upload directories exist
        try {
            Files.createDirectories(Paths.get(UPLOADS_DIR, "logos"));
            Files.createDirectories(Paths.get(UPLOADS_DIR, "avatars"));
            Files.createDirectories(Paths.get(UPLOADS_DIR, "banners"));
        } catch (IOException e) {
            System.err.println("Could not create uploads directory: " + e.getMessage());
        }
    }

    /**
     * Opens a JFileChooser dialog to select an image file and saves it to the target subfolder.
     * @param parent The parent Swing component
     * @param subfolder "logos", "avatars", or "banners"
     * @return The saved relative file path, or null if cancelled or failed
     */
    public static String chooseAndSaveImage(Component parent, String subfolder) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image File (PNG, JPG, JPEG)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files (*.png, *.jpg, *.jpeg, *.webp)", "png", "jpg", "jpeg", "webp"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String ext = getFileExtension(selectedFile.getName());
                String uniqueName = System.currentTimeMillis() + "_" + (System.nanoTime() % 10000) + "." + ext;
                Path destination = Paths.get(UPLOADS_DIR, subfolder, uniqueName);
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                return destination.toString().replace("\\", "/");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Error uploading file: " + ex.getMessage(), "Upload Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    private static String getFileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot > 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1).toLowerCase();
        }
        return "png";
    }
}
