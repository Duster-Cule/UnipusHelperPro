package org.unipus.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class StringProcesser {
    /**
     * U校园中不允许输入特殊字符，本方法将特殊字符转为空格返回。
     * @param string 需处理的字符串
     * @return 处理后的字符串
     */
    public static String processIlligalCharacter(String string) {

        String y = "a-zA-ZёЁÀ-ÿ0-9一-龥Ѐ–ӿ\uFF00-\uFFEF\uFFF0-\uFFFF　 ⼀-\u2FDF㐀-䶵豈-\uFAFF\uE863가-\uD7AFᄀ-ᇿ\u3130-\u318Fꥠ-\uA97Fힰ-\uD7FF\u3040-ゟ゠-ヿㇰ-ㇿĀ-ſ\u0080-ÿḀ-ỿ„€「」『』々ヽヾ\\[],\\.\\{}\\(\\)<>?/\\|`~\"'@#\\$%&*《》（）+=_￥¥。，、;:：；【】…？！!”“’‘•·\n\t\\\\-\\s";

        String regex = "[^" + y + "]" ;

        return string.replaceAll(regex, " ");
    }


    /**
     * 解密函数：将带 "unipus." 前缀的 hex 密文，和后端给定的 k 值，
     * 转换为明文 JSON 字符串。
     *
     * @param dataWithPrefix 带前缀的密文，例如 "unipus.b9b79632…"
     * @param k              后端返回的密钥片段，例如 "20250627"
     * @return               解密后的 JSON 字符串
     */
    public static String decrypt(String dataWithPrefix, String k) {
        if (dataWithPrefix == null || !dataWithPrefix.startsWith("unipus.")) {
            return dataWithPrefix;
        }
        String hexCipher = dataWithPrefix.substring("unipus.".length());

        String keyString = "1a2b3c4d" + k;
        byte[] keyBytes  = keyString.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] cipherBytes = StringProcesser.hexStringToByteArray(hexCipher);

        String base64Cipher = Base64.getEncoder().encodeToString(cipherBytes);

        Cipher cipher;
        byte[] decryptedPadded = new byte[0];
        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            decryptedPadded = cipher.doFinal(Base64.getDecoder().decode(base64Cipher));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int i = decryptedPadded.length;
        while (i > 0 && decryptedPadded[i - 1] == 0) {
            i--;
        }
        byte[] decrypted = new byte[i];
        System.arraycopy(decryptedPadded, 0, decrypted, 0, i);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static String toPlainText(String html) {
        if (html == null) return "";
        Document doc = Jsoup.parse(html);

        // 把 <br> 转换成换行，把段落之间加空行
        for (Element br : doc.select("br")) br.after("\n");
        for (Element p  : doc.select("p"))  { p.prepend("\n"); p.append("\n"); }

        // 也可按需处理列表项（取消注释）
        // for (Element li : doc.select("li")) li.prepend("- ");

        String text = doc.text(); // 自动解码 &nbsp; &amp; 等实体
        text = text.replace('\u00A0',' ')              // &nbsp;
                .replaceAll("[ \t]+\n", "\n")       // 行尾多余空格
                .replaceAll("\n{3,}", "\n\n")       // 折叠多余空行
                .trim();
        return text;
    }

    public static boolean isValidJson(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int idx = 0; idx < len; idx += 2) {
            data[idx / 2] = (byte) ((Character.digit(s.charAt(idx), 16) << 4)
                    + Character.digit(s.charAt(idx+1), 16));
        }
        return data;
    }

}
