package com.exam.tests.support;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceEncodingConsistencyTest {
    private static final List<String> KNOWN_MOJIBAKE_FRAGMENTS = List.of(
            "瀵姳绗�",
            "濞村鐦�",
            "閺堢喍鑵�",
            "闁哄牏鍠�",
            "婵炴潙顑�",
            "鐠囨洖宓�",
            "閼板啳鐦�",
            "鐎涳妇鏁�",
            "閺佹瑥绗€",
            "閻劍鍩�",
            "閸旂姾娴囬幋鎰",
            "瑜版挸澧�",
            "娑撳秴鐡ㄩ崷",
            "閸欐垵绔�",
            "閹绘劒姘�",
            "缁夋垹娲�",
            "鐎靛棛鐖�",
            "閻ц缍�",
            "妫版娲�",
            "閸欏﹥鐗�",
            "閹存劕濮�",
            "婢惰精瑙�",
            "鐢ㄦ埛",
            "璇曞嵎",
            "鑰冭瘯",
            "棰樼洰",
            "鍙婃牸",
            "鍙傛暟",
            "绯荤粺",
            "褰撳墠",
            "鍙戝竷"
    );

    @Test
    void javaSourcesShouldBeUtf8AndFreeOfKnownMojibake() throws IOException {
        List<String> issues = new ArrayList<>();

        for (Path root : List.of(Paths.get("src", "main", "java"), Paths.get("src", "test", "java"))) {
            if (!Files.exists(root)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root)) {
                stream.filter(path -> path.toString().endsWith(".java"))
                        .forEach(path -> inspectJavaFile(path, issues));
            }
        }

        assertTrue(issues.isEmpty(), String.join(System.lineSeparator(), issues));
    }

    private void inspectJavaFile(Path path, List<String> issues) {
        if ("SourceEncodingConsistencyTest.java".equals(path.getFileName().toString())) {
            return;
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            issues.add(path + " 无法读取: " + e.getMessage());
            return;
        }

        String text;
        try {
            text = decodeStrictUtf8(bytes);
        } catch (CharacterCodingException e) {
            issues.add(path + " 不是有效的 UTF-8 文件");
            return;
        }

        for (String fragment : KNOWN_MOJIBAKE_FRAGMENTS) {
            if (text.contains(fragment)) {
                issues.add(path + " 包含疑似乱码片段: " + fragment);
            }
        }
    }

    private String decodeStrictUtf8(byte[] bytes) throws CharacterCodingException {
        CharBuffer charBuffer = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes));
        return charBuffer.toString();
    }
}
