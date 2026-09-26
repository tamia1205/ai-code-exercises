package com.example.images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ImageProcessor {

    public static void main(String[] args) {
        // First we generate 100 images from source_images folder
        try {
            multiplyImages("source_images", "sample_images", 100);
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
        }

        // Simulate processing a batch of images
        try {
            processImageFolder("sample_images", "processed_images");
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void multiplyImages(String inputFolder, String outputFolder, int multiplicationFactor) throws IOException {
        File folder = new File(inputFolder);
        File[] imageFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".png"));

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No images found in the folder");
            return;
        }

        // Create output directory if it doesn't exist
        File outputDir = new File(outputFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Multiply each image file
        for (File imageFile : imageFiles) {
            String fileName = imageFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));

            // Create multiple copies
            for (int i = 1; i <= multiplicationFactor; i++) {
                String newFileName = String.format("%s_%d%s", baseName, i, extension);
                File outputFile = new File(outputDir, newFileName);

                try {
                    // Copy the file using Java NIO for better performance
                    Files.copy(imageFile.toPath(), outputFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Error copying file: " + fileName + " - " + e.getMessage());
                }
            }
        }
    }

    public static void processImageFolder(String inputFolder, String outputFolder) throws IOException {
        // Get the Runtime instance to monitor memory
        Runtime runtime = Runtime.getRuntime();

        // Helper method to print memory stats
        BiConsumer<String, Runtime> printMemoryStats = (stage, rt) -> {
            long totalMemory = rt.totalMemory() / (1024 * 1024);
            long freeMemory = rt.freeMemory() / (1024 * 1024);
            long usedMemory = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
            long maxMemory = rt.maxMemory() / (1024 * 1024);

            System.out.println("\n=== Memory Stats at " + stage + " ===");
            System.out.println("Used Memory: " + usedMemory + " MB");
            System.out.println("Free Memory: " + freeMemory + " MB");
            System.out.println("Total Memory: " + totalMemory + " MB");
            System.out.println("Maximum Memory: " + maxMemory + " MB");
            System.out.println("==============================\n");
        };

        // Print initial memory state
        printMemoryStats.accept("START", runtime);

        File folder = new File(inputFolder);
        File[] imageFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".png"));

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No images found in the folder");
            return;
        }

        // Create output directory if it doesn't exist
        File outputDir = new File(outputFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Store all images in memory first
        List<BufferedImage> images = new ArrayList<>();
        System.out.println("Loading all images into memory...");

        for (File imageFile : imageFiles) {
            BufferedImage image = ImageIO.read(imageFile);
            images.add(image);
            System.out.println("Loaded: " + imageFile.getName());
        }

        // Print memory usage after loading
        printMemoryStats.accept("AFTER LOADING", runtime);

        // Process all images
        System.out.println("Processing all images...");
        List<BufferedImage> processedImages = new ArrayList<>();

        for (BufferedImage image : images) {
            BufferedImage processed = applyEffects(image);
            processedImages.add(processed);
        }

        // Print memory usage after processing
        printMemoryStats.accept("AFTER PROCESSING", runtime);

        // Save all processed images
        System.out.println("Saving all processed images...");
        for (int i = 0; i < imageFiles.length; i++) {
            String outputName = outputFolder + File.separator + "processed_" + imageFiles[i].getName();
            ImageIO.write(processedImages.get(i), getImageFormat(imageFiles[i].getName()), new File(outputName));
            System.out.println("Saved: " + outputName);
        }

        // Print final memory usage
        printMemoryStats.accept("END", runtime);

        System.out.println("All images processed successfully");
    }

    private static BufferedImage applyEffects(BufferedImage original) {
        // Simulate memory-intensive image processing
        int width = original.getWidth();
        int height = original.getHeight();

        // Create a new image with the same dimensions
        BufferedImage processed = new BufferedImage(width, height, original.getType());

        // Process each pixel - simple grayscale conversion for this example
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);

                int alpha = (rgb >> 24) & 0xff;
                int red = (rgb >> 16) & 0xff;
                int green = (rgb >> 8) & 0xff;
                int blue = rgb & 0xff;

                // Convert to grayscale
                int gray = (red + green + blue) / 3;

                // Set new RGB
                int newRGB = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                processed.setRGB(x, y, newRGB);
            }
        }

        return processed;
    }

    private static String getImageFormat(String filename) {
        if (filename.toLowerCase().endsWith(".png")) {
            return "png";
        } else {
            return "jpeg";
        }
    }
}