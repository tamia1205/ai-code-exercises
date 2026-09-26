import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FileManager class
 */
class FileManagerTest {
    private static final String TEST_DIR = "test_files";
    private FileManager fileManager;
    private File testDirectory;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary test directory
        testDirectory = new File(TEST_DIR);
        if (!testDirectory.exists()) {
            testDirectory.mkdir();
        }

        // Create a FileManager instance pointing to the test directory
        fileManager = new FileManager(testDirectory.getAbsolutePath());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test directory and files
        deleteDirectory(testDirectory);
    }

    @Test
    void testSaveFile() {
        String fileName = "test_file.txt";
        String content = "This is a test file content.";

        assertTrue(fileManager.saveFile(fileName, content));
        assertEquals(1, fileManager.getFilesProcessed());
        assertEquals(0, fileManager.getErrors().size());

        // Verify file was created with correct content
        File savedFile = new File(testDirectory, fileName);
        assertTrue(savedFile.exists());
    }

    @Test
    void testSaveFileWithSubdirectories() {
        String fileName = "subdir/nested/test_file.txt";
        String content = "File in nested directory.";

        assertTrue(fileManager.saveFile(fileName, content));

        // Verify file and parent directories were created
        File savedFile = new File(testDirectory, fileName);
        assertTrue(savedFile.exists());
    }

    @Test
    void testReadFile() throws IOException {
        // Create a test file first
        String fileName = "read_test.txt";
        String content = "Content to be read.\nSecond line.";
        
        File file = new File(testDirectory, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }

        // Read the file using FileManager
        String readContent = fileManager.readFile(fileName);
        assertNotNull(readContent);
        assertEquals(content + "\n", readContent); // Note: readFile appends newline after each line
    }

    @Test
    void testReadNonExistentFile() {
        String fileName = "nonexistent.txt";
        
        assertNull(fileManager.readFile(fileName));
        assertEquals(1, fileManager.getErrors().size());
        assertTrue(fileManager.getErrors().get(0).contains("File does not exist"));
    }

    @Test
    void testDeleteFile() throws IOException {
        // Create a test file
        String fileName = "to_delete.txt";
        File file = new File(testDirectory, fileName);
        file.createNewFile();
        
        assertTrue(fileManager.deleteFile(fileName));
        assertFalse(file.exists());
    }

    @Test
    void testDeleteNonExistentFile() {
        String fileName = "nonexistent_to_delete.txt";
        
        assertFalse(fileManager.deleteFile(fileName));
        assertEquals(1, fileManager.getErrors().size());
    }

    @Test
    void testCopyFile() throws IOException {
        // Create source file
        String sourceFileName = "source.txt";
        String destFileName = "destination.txt";
        String content = "Content to be copied.";
        
        File sourceFile = new File(testDirectory, sourceFileName);
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(content);
        }
        
        // Copy the file
        assertTrue(fileManager.copyFile(sourceFileName, destFileName));
        
        // Verify destination file exists with same content
        File destFile = new File(testDirectory, destFileName);
        assertTrue(destFile.exists());
        
        String copiedContent = Files.readString(destFile.toPath());
        assertEquals(content, copiedContent);
    }

    @Test
    void testCopyNonExistentFile() {
        String sourceFileName = "nonexistent_source.txt";
        String destFileName = "nonexistent_dest.txt";
        
        assertFalse(fileManager.copyFile(sourceFileName, destFileName));
        assertEquals(1, fileManager.getErrors().size());
    }

    @Test
    void testCreateZipArchive() throws IOException {
        // Create some test files
        String file1Name = "file1.txt";
        String file2Name = "file2.txt";
        String zipFileName = "archive.zip";
        
        File file1 = new File(testDirectory, file1Name);
        File file2 = new File(testDirectory, file2Name);
        
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write("Content of file 1");
            writer2.write("Content of file 2");
        }
        
        List<String> filesToInclude = Arrays.asList(file1Name, file2Name);
        
        // Create zip archive
        assertTrue(fileManager.createZipArchive(zipFileName, filesToInclude));
        
        // Verify zip file was created
        File zipFile = new File(testDirectory, zipFileName);
        assertTrue(zipFile.exists());
    }

    @Test
    void testListFiles() throws IOException {
        // Create some test files
        String file1Name = "list_test1.txt";
        String file2Name = "list_test2.txt";
        
        File file1 = new File(testDirectory, file1Name);
        File file2 = new File(testDirectory, file2Name);
        file1.createNewFile();
        file2.createNewFile();
        
        List<String> files = fileManager.listFiles(".");
        
        assertTrue(files.contains(file1Name));
        assertTrue(files.contains(file2Name));
        assertTrue(files.size() >= 2); // Could contain other test files
    }

    @Test
    void testListNonExistentDirectory() {
        List<String> files = fileManager.listFiles("nonexistent_dir");
        
        assertTrue(files.isEmpty());
        assertEquals(1, fileManager.getErrors().size());
    }

    @Test
    void testSetBufferSize() throws IOException {
        int newBufferSize = 8192;
        fileManager.setBufferSize(newBufferSize);
        
        // Create source file with content larger than default buffer
        String sourceFileName = "large_source.txt";
        String destFileName = "large_dest.txt";
        
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            contentBuilder.append("Line ").append(i).append(" with some content to make it longer.\n");
        }
        String content = contentBuilder.toString();
        
        File sourceFile = new File(testDirectory, sourceFileName);
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(content);
        }
        
        // Copy the file with new buffer size
        assertTrue(fileManager.copyFile(sourceFileName, destFileName));
        
        // Verify copy was successful
        File destFile = new File(testDirectory, destFileName);
        assertTrue(destFile.exists());
    }

    /**
     * Utility method to recursively delete a directory
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}