import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class FixFileFormat {
    public static void main(String[] args) throws IOException {
        String filePath = "src/main/resources/data/操作系统/操作系统.txt";
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        List<String> fixedLines = new ArrayList<>();
        
        for (String line : lines) {
            // 删除行号前缀：匹配行首的空格+数字+箭头
            String fixed = line.replaceFirst("^\\s*\\d+→", "");
            fixedLines.add(fixed);
        }
        
        Files.write(Paths.get(filePath), fixedLines, StandardCharsets.UTF_8);
        System.out.println("文件修复完成！处理了 " + fixedLines.size() + " 行");
    }
}
