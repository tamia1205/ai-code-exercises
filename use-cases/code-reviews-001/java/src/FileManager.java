import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * File management utility for handling common file operations.
 */
public class FileManager {
    private String basePath;
    private boolean createDirectories;
    private int bufferSize = 4096;

    // Track operations for logging
    private int filesProcessed = 0;
    private List<String> errors = new ArrayList<>();

    /**
     * Constructor with base path
     */
    public FileManager(String basePath) {
        this.basePath = basePath;
        this.createDirectories = true;
    }

    /**
     * Constructor with options
     */
    public FileManager(String basePath, boolean createDirectories) {
        this.basePath = basePath;
        this.createDirectories = createDirectories;
    }

    /**
     * Save content to a file
     */
    public boolean saveFile(String fileName, String content) {
        File file = new File(basePath + File.separator + fileName);

        // Create parent directories if they don't exist
        if (this.createDirectories) {
            file.getParentFile().mkdirs();
        }

        // Write to file
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(content);
            filesProcessed++;
            return true;
        } catch (IOException e) {
            errors.add("Error saving file " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read content from a file
     */
    public String readFile(String fileName) {
        File file = new File(basePath + File.separator + fileName);
        StringBuilder content = new StringBuilder();

        if (!file.exists()) {
            errors.add("File does not exist: " + fileName);
            return null;
        }

        // Read from file
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(file);
            bufferedReader = new BufferedReader(reader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }

            filesProcessed++;
            return content.toString();
        } catch (IOException e) {
            errors.add("Error reading file " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete a file
     */
    public boolean deleteFile(String fileName) {
        File file = new File(basePath + File.separator + fileName);

        if (!file.exists()) {
            errors.add("Cannot delete - file does not exist: " + fileName);
            return false;
        }

        boolean result = file.delete();
        if (result) {
            filesProcessed++;
        } else {
            errors.add("Failed to delete file: " + fileName);
        }

        return result;
    }

    /**
     * Copy a file
     */
    public boolean copyFile(String sourceFileName, String destFileName) {
        File sourceFile = new File(basePath + File.separator + sourceFileName);
        File destFile = new File(basePath + File.separator + destFileName);

        if (!sourceFile.exists()) {
            errors.add("Source file does not exist: " + sourceFileName);
            return false;
        }

        // Create parent directories for destination if they don't exist
        if (this.createDirectories) {
            destFile.getParentFile().mkdirs();
        }

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(destFile);

            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            filesProcessed++;
            return true;
        } catch (IOException e) {
            errors.add("Error copying file from " + sourceFileName + " to " + destFileName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a ZIP archive of files
     */
    public boolean createZipArchive(String zipFileName, List<String> filesToInclude) {
        try {
            FileOutputStream fos = new FileOutputStream(basePath + File.separator + zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            byte[] buffer = new byte[bufferSize];

            for (String fileName : filesToInclude) {
                File file = new File(basePath + File.separator + fileName);

                if (!file.exists()) {
                    errors.add("File does not exist, skipping: " + fileName);
                    continue;
                }

                FileInputStream fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(fileName));

                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();
                fis.close();
            }

            zos.close();
            filesProcessed++;
            return true;
        } catch (IOException e) {
            errors.add("Error creating ZIP archive " + zipFileName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * List files in directory
     */
    public List<String> listFiles(String directoryPath) {
        File directory = new File(basePath + File.separator + directoryPath);
        List<String> fileList = new ArrayList<>();

        if (!directory.exists() || !directory.isDirectory()) {
            errors.add("Directory does not exist: " + directoryPath);
            return fileList;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.add(file.getName());
            }
        }

        return fileList;
    }

    /**
     * Get the number of files processed
     */
    public int getFilesProcessed() {
        return filesProcessed;
    }

    /**
     * Get list of errors
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Set buffer size for file operations
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}