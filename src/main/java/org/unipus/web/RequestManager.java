package org.unipus.web;

import com.google.gson.Gson;
import okhttp3.*;
import okio.Buffer;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 请求管理器：集中记录由 UnipusRequest 发起的所有请求，便于调试。
 * - 默认仅保存在内存（环形缓冲，容量可配置）；
 * - 提供保存到文件的方法（JSON Lines，一行一条记录）。
 * - 为每个请求保存 TaskId（由调用方传入；例如 login 使用 "task-用户名"，其他通常为当前线程名）。
 * - 支持 Chrome DevTools Network 面板的所有参数。
 */
public final class RequestManager {

    private static final RequestManager INSTANCE = new RequestManager();

    // 响应体 peek 上限（字节），避免大响应造成内存压力；可通过 setter 配置
    private static volatile long PEEK_LIMIT_BYTES = 10L * 1024 * 1024; // 10MB

    /** 设置用于记录的响应 peek 上限（字节） */
    public static void setResponsePeekLimitBytes(long bytes) {
        if (bytes <= 0) throw new IllegalArgumentException("peek limit must be > 0");
        PEEK_LIMIT_BYTES = bytes;
    }
    /** 获取当前响应 peek 上限（字节） */
    public static long getResponsePeekLimitBytes() { return PEEK_LIMIT_BYTES; }

    public static RequestManager getInstance() {
        return INSTANCE;
    }

    private final Object lock = new Object();
    private final List<RequestRecord> records = new ArrayList<>();
    private final CopyOnWriteArrayList<RequestListener> listeners = new CopyOnWriteArrayList<>();
    private int capacity = 2048; // 默认容量
    private final Gson gson = new Gson();

    private RequestManager() {}

    // ===================== 监听器接口 =====================

    public interface RequestListener {
        /**
         * 当新的请求记录被添加时调用
         * @param record 新添加的请求记录
         */
        void onRequestAdded(RequestRecord record);

        /**
         * 当记录被清空时调用
         */
        void onRecordsCleared();
    }

    public void addListener(RequestListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RequestListener listener) {
        listeners.remove(listener);
    }

    private void notifyRequestAdded(RequestRecord record) {
        for (RequestListener listener : listeners) {
            try {
                listener.onRequestAdded(record);
            } catch (Exception e) {
                // 忽略监听器异常，避免影响主流程
                System.err.println("Error in request listener: " + e.getMessage());
            }
        }
    }

    private void notifyRecordsCleared() {
        for (RequestListener listener : listeners) {
            try {
                listener.onRecordsCleared();
            } catch (Exception e) {
                System.err.println("Error in request listener: " + e.getMessage());
            }
        }
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        synchronized (lock) {
            this.capacity = capacity;
            // 截断至新容量
            int overflow = records.size() - capacity;
            if (overflow > 0) {
                records.subList(0, overflow).clear();
            }
        }
    }

    public int getCapacity() {
        synchronized (lock) {
            return capacity;
        }
    }

    public void clear() {
        synchronized (lock) {
            records.clear();
        }
        notifyRecordsCleared();
    }

    public List<RequestRecord> getRecords() {
        synchronized (lock) {
            return new ArrayList<>(records);
        }
    }

    public void record(RequestRecord record) {
        synchronized (lock) {
            if (records.size() >= capacity) {
                // 移除最旧
                records.removeFirst();
            }
            records.add(record);
        }
        notifyRequestAdded(record);
    }

    /**
     * 保存记录为 JSON Lines（每行一条 JSON）。
     */
    public void saveToFile(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8))) {
            for (RequestRecord r : getRecords()) {
                writer.write(gson.toJson(r));
                writer.write(System.lineSeparator());
            }
        }
    }

    public void saveToFile(File file) throws IOException {
        saveToFile(file.toPath());
    }

    // ===================== 数据结构 =====================

    public static final class RequestRecord {
        // 基本信息
        public final long timestamp;                    // 发起时间（epoch millis）
        public final String isoTime;                    // ISO-8601 字符串
        public final String taskId;                     // 任务标识（由调用方传入）

        // Request URL & Method (Chrome DevTools)
        public final String requestUrl;                 // Request URL
        public final String requestMethod;              // Request Method (GET/POST/...)

        // Status & Remote Address
        public final @Nullable Integer statusCode;      // Status Code
        public final @Nullable String statusText;       // Status Text (OK, Not Found, etc.)
        public final @Nullable String remoteAddress;    // Remote Address (IP:Port)
        public final @Nullable String localAddress;     // Local Address (IP:Port)

        // Policy & Security
        public final @Nullable String referrerPolicy;   // Referrer Policy
        public final @Nullable String protocol;         // Protocol (HTTP/1.1, HTTP/2, etc.)

        // Headers
        public final Map<String, List<String>> requestHeaders;  // Request Headers
        public final Map<String, List<String>> responseHeaders; // Response Headers

        // Payload & Response
        public final @Nullable String requestPayload;   // Request Payload (complete body)
        public final @Nullable String responseBody;     // Response Body (complete)
        public final long requestSize;                  // Request size in bytes
        public final long responseSize;                 // Response size in bytes

        // Cookies
        public final List<CookieInfo> requestCookies;   // Request Cookies
        public final List<CookieInfo> allCookies;       // All cookies in the jar at the time of request
        public final List<CookieInfo> responseCookies;  // Response Cookies (Set-Cookie)

        // Timing
        public final int durationMs;                    // 请求耗时
        public final @Nullable String error;            // 异常消息（成功为 null）

        private RequestRecord(long timestamp,
                              String taskId,
                              String requestUrl,
                              String requestMethod,
                              @Nullable Integer statusCode,
                              @Nullable String statusText,
                              @Nullable String remoteAddress,
                              @Nullable String localAddress,
                              @Nullable String referrerPolicy,
                              @Nullable String protocol,
                              Map<String, List<String>> requestHeaders,
                              Map<String, List<String>> responseHeaders,
                              @Nullable String requestPayload,
                              @Nullable String responseBody,
                              long requestSize,
                              long responseSize,
                              List<CookieInfo> requestCookies,
                              List<CookieInfo> allCookies,
                              List<CookieInfo> responseCookies,
                              int durationMs,
                              @Nullable String error) {
            this.timestamp = timestamp;
            this.isoTime = Instant.ofEpochMilli(timestamp).toString();
            this.taskId = taskId;
            this.requestUrl = requestUrl;
            this.requestMethod = requestMethod;
            this.statusCode = statusCode;
            this.statusText = statusText;
            this.remoteAddress = remoteAddress;
            this.localAddress = localAddress;
            this.referrerPolicy = referrerPolicy;
            this.protocol = protocol;
            this.requestHeaders = requestHeaders;
            this.responseHeaders = responseHeaders;
            this.requestPayload = requestPayload;
            this.responseBody = responseBody;
            this.requestSize = requestSize;
            this.responseSize = responseSize;
            this.requestCookies = requestCookies;
            this.allCookies = allCookies;
            this.responseCookies = responseCookies;
            this.durationMs = durationMs;
            this.error = error;
        }

        public static RequestRecord from(Request request,
                                         @Nullable Response response,
                                         long startMillis,
                                         long endMillis,
                                         String taskId,
                                         @Nullable List<Cookie> allCookiesFromJar,
                                         @Nullable Throwable error) {
            int duration = (int) Math.max(0, endMillis - startMillis);

            // Basic request info
            String requestUrl = request.url().toString();
            String requestMethod = request.method();

            // Status info
            Integer statusCode = response != null ? response.code() : null;
            String statusText = response != null ? response.message() : null;

            // Remote & Local addresses
            String remoteAddress = extractRemoteAddress(response);
            String localAddress = null; // OkHttp doesn't easily expose local address

            // Protocol info
            String protocol = response != null ? response.protocol().toString() : null;
            String referrerPolicy = extractReferrerPolicy(request, response);

            // Headers
            Map<String, List<String>> requestHeaders = toHeaderMap(request.headers());
            Map<String, List<String>> responseHeaders = response != null ?
                toHeaderMap(response.headers()) : new LinkedHashMap<>();

            // Payload & Response body
            String requestPayload = tryExtractRequestBody(request);
            String responseBody = tryExtractResponseBody(response);

            // Sizes
            long requestSize = calculateRequestSize(request);
            long responseSize = calculateResponseSize(response);

            // Cookies
            List<CookieInfo> requestCookies = extractRequestCookies(request);
            List<CookieInfo> allCookies = allCookiesFromJar != null ?
                allCookiesFromJar.stream().map(CookieInfo::from).collect(Collectors.toList()) :
                new ArrayList<>();
            List<CookieInfo> responseCookies = extractResponseCookies(response);

            String err = error != null ? String.valueOf(error) : null;

            return new RequestRecord(startMillis, taskId, requestUrl, requestMethod,
                statusCode, statusText, remoteAddress, localAddress, referrerPolicy, protocol,
                requestHeaders, responseHeaders, requestPayload, responseBody,
                requestSize, responseSize, requestCookies, allCookies, responseCookies, duration, err);
        }

        /**
         * 创建模拟的请求记录用于测试
         * @param startMillis 开始时间戳
         * @param endMillis 结束时间戳
         * @param taskId 任务ID
         * @param requestUrl 请求URL
         * @param requestMethod 请求方法
         * @param statusCode 状态码
         * @param statusText 状态文本
         * @param remoteAddress 远程地址
         * @param localAddress 本地地址
         * @param referrerPolicy 引用策略
         * @param protocol 协议
         * @param requestHeaders 请求头
         * @param responseHeaders 响应头
         * @param requestPayload 请求体
         * @param responseBody 响应体
         * @param requestSize 请求大小
         * @param responseSize 响应大小
         * @param requestCookies 请求Cookie
         * @param responseCookies 响应Cookie
         * @param error 错误信息
         * @return 模拟的请求记录
         */
        public static RequestRecord createMock(long startMillis,
                                               long endMillis,
                                               String taskId,
                                               String requestUrl,
                                               String requestMethod,
                                               @Nullable Integer statusCode,
                                               @Nullable String statusText,
                                               @Nullable String remoteAddress,
                                               @Nullable String localAddress,
                                               @Nullable String referrerPolicy,
                                               @Nullable String protocol,
                                               Map<String, List<String>> requestHeaders,
                                               Map<String, List<String>> responseHeaders,
                                               @Nullable String requestPayload,
                                               @Nullable String responseBody,
                                               long requestSize,
                                               long responseSize,
                                               List<CookieInfo> requestCookies,
                                               List<CookieInfo> allCookies,
                                               List<CookieInfo> responseCookies,
                                               @Nullable Throwable error) {
            int duration = (int) Math.max(0, endMillis - startMillis);
            String errorStr = error != null ? error.getClass().getSimpleName() + ": " + error.getMessage() : null;

            return new RequestRecord(
                startMillis,
                taskId,
                requestUrl,
                requestMethod,
                statusCode,
                statusText,
                remoteAddress,
                localAddress,
                referrerPolicy,
                protocol,
                requestHeaders != null ? requestHeaders : new LinkedHashMap<>(),
                responseHeaders != null ? responseHeaders : new LinkedHashMap<>(),
                requestPayload,
                responseBody,
                requestSize,
                responseSize,
                requestCookies != null ? requestCookies : new ArrayList<>(),
                allCookies != null ? allCookies : new ArrayList<>(),
                responseCookies != null ? responseCookies : new ArrayList<>(),
                duration,
                errorStr
            );
        }

        private static Map<String, List<String>> toHeaderMap(Headers headers) {
            Map<String, List<String>> map = new LinkedHashMap<>();
            for (String name : headers.names()) {
                map.put(name, headers.values(name));
            }
            return map;
        }

        private static @Nullable String extractRemoteAddress(Response response) {
            if (response == null) return null;
            try {
                // Try to extract from response or connection info
                // OkHttp doesn't directly expose remote address, but we can try from headers
                String host = response.request().url().host();
                int port = response.request().url().port();

                // Attempt DNS resolution for display purposes
                try {
                    InetAddress addr = InetAddress.getByName(host);
                    return addr.getHostAddress() + ":" + port;
                } catch (Exception e) {
                    return host + ":" + port;
                }
            } catch (Exception e) {
                return null;
            }
        }

        private static @Nullable String extractReferrerPolicy(Request request, Response response) {
            // Check for Referrer-Policy header in response
            if (response != null) {
                String policy = response.header("Referrer-Policy");
                if (policy != null) return policy;
            }

            // Check for referrer in request headers
            String referer = request.header("Referer");
            if (referer != null) {
                return "default"; // Has referrer but no explicit policy
            }

            return "no-referrer-when-downgrade"; // Default policy
        }

        private static @Nullable String tryExtractRequestBody(Request request) {
            RequestBody body = request.body();
            if (body == null) return null;

            try {
                Buffer buffer = new Buffer();
                // 将请求体写入我们自己的缓冲区，不会影响原始请求体的发送
                body.writeTo(buffer);

                MediaType contentType = body.contentType();
                Charset charset = StandardCharsets.UTF_8;
                if (contentType != null) {
                    Charset detectedCharset = contentType.charset(StandardCharsets.UTF_8);
                    if (detectedCharset != null) charset = detectedCharset;
                }

                return buffer.readString(charset);
            } catch (Exception e) {
                return "<unavailable: " + e.getMessage() + ">";
            }
        }

        private static @Nullable String tryExtractResponseBody(Response response) {
            if (response == null) return null;

            try {
                // 非破坏性读取响应体，避免消费原始流
                ResponseBody peek = response.peekBody(RequestManager.getResponsePeekLimitBytes());
                if (peek == null) return null;

                MediaType contentType = peek.contentType();
                Charset charset = StandardCharsets.UTF_8;
                if (contentType != null) {
                    Charset detectedCharset = contentType.charset(StandardCharsets.UTF_8);
                    if (detectedCharset != null) charset = detectedCharset;
                }

                return peek.string();
            } catch (Exception e) {
                return "<unavailable: " + e.getMessage() + ">";
            }
        }

        private static long calculateResponseSize(Response response) {
            if (response == null) return 0;

            long size = 0;

            // 状态行
            String statusLine = response.protocol() + " " + response.code() + " " + response.message() + "\r\n";
            size += statusLine.getBytes(StandardCharsets.UTF_8).length;

            // 头
            for (String name : response.headers().names()) {
                for (String value : response.headers().values(name)) {
                    String header = name + ": " + value + "\r\n";
                    size += header.getBytes(StandardCharsets.UTF_8).length;
                }
            }

            // 空行
            size += 2; // \r\n
            // Body 大小：优先 Content-Length；否则用 peekBody 估算，不消费原始流
            ResponseBody body = response.body();
            if (body != null) {
                try {
                    long cl = body.contentLength();
                    if (cl >= 0) {
                        size += cl;
                    } else {
                        try {
                            ResponseBody peek = response.peekBody(RequestManager.getResponsePeekLimitBytes());
                            if (peek != null) {
                                byte[] bs = peek.bytes();
                                size += bs != null ? bs.length : 0;
                            }
                        } catch (Exception ignored) {
                            // ignore
                        }
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }

            return size;
        }

        private static long calculateRequestSize(Request request) {
            long size = 0;

            // Request line
            String requestLine = request.method() + " " + request.url().encodedPath() +
                (request.url().encodedQuery() != null ? "?" + request.url().encodedQuery() : "") +
                " HTTP/1.1\r\n";
            size += requestLine.getBytes(StandardCharsets.UTF_8).length;

            // Headers
            for (String name : request.headers().names()) {
                for (String value : request.headers().values(name)) {
                    String header = name + ": " + value + "\r\n";
                    size += header.getBytes(StandardCharsets.UTF_8).length;
                }
            }

            // Empty line between headers and body
            size += 2; // \r\n
            // Body
            RequestBody body = request.body();
            if (body != null) {
                try {
                    long cl = body.contentLength();
                    if (cl >= 0) {
                        size += cl;
                    } else {
                        // Fallback to buffering
                        Buffer buffer = new Buffer();
                        body.writeTo(buffer);
                        size += buffer.size();
                    }
                } catch (Exception e) {
                    try {
                        Buffer buffer = new Buffer();
                        body.writeTo(buffer);
                        size += buffer.size();
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            }

            return size;
        }

        private static List<CookieInfo> extractRequestCookies(Request request) {
            List<CookieInfo> cookies = new ArrayList<>();

            // Extract from Cookie header
            String cookieHeader = request.header("Cookie");
            if (cookieHeader != null) {
                // Parse Cookie header: "name1=value1; name2=value2"
                String[] pairs = cookieHeader.split(";\\s*");
                for (String pair : pairs) {
                    String[] parts = pair.split("=", 2);
                    if (parts.length == 2) {
                        cookies.add(new CookieInfo(parts[0].trim(), parts[1].trim(), null, null, null, false, false));
                    }
                }
            }

            return cookies;
        }

        private static List<CookieInfo> extractResponseCookies(Response response) {
            List<CookieInfo> cookies = new ArrayList<>();

            if (response == null) return cookies;

            // Extract from Set-Cookie headers
            List<String> setCookieHeaders = response.headers("Set-Cookie");
            for (String setCookieHeader : setCookieHeaders) {
                CookieInfo cookieInfo = parseCookieFromSetCookieHeader(setCookieHeader);
                if (cookieInfo != null) {
                    cookies.add(cookieInfo);
                }
            }

            return cookies;
        }

        private static @Nullable CookieInfo parseCookieFromSetCookieHeader(String setCookieHeader) {
            try {
                // Parse Set-Cookie header: "name=value; Domain=.example.com; Path=/; Secure; HttpOnly"
                String[] parts = setCookieHeader.split(";\\s*");
                if (parts.length == 0) return null;

                // First part is name=value
                String[] nameValue = parts[0].split("=", 2);
                if (nameValue.length != 2) return null;

                String name = nameValue[0].trim();
                String value = nameValue[1].trim();
                String domain = null;
                String path = null;
                String expires = null;
                boolean secure = false;
                boolean httpOnly = false;

                // Parse attributes
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (part.equalsIgnoreCase("Secure")) {
                        secure = true;
                    } else if (part.equalsIgnoreCase("HttpOnly")) {
                        httpOnly = true;
                    } else if (part.toLowerCase().startsWith("domain=")) {
                        domain = part.substring(7);
                    } else if (part.toLowerCase().startsWith("path=")) {
                        path = part.substring(5);
                    } else if (part.toLowerCase().startsWith("expires=")) {
                        expires = part.substring(8);
                    }
                }

                return new CookieInfo(name, value, domain, path, expires, secure, httpOnly);
            } catch (Exception e) {
                return null;
            }
        }
    }

    // Cookie information class
    public static final class CookieInfo {
        public final String name;
        public final String value;
        public final @Nullable String domain;
        public final @Nullable String path;
        public final @Nullable String expires;
        public final boolean secure;
        public final boolean httpOnly;

        public CookieInfo(String name, String value, @Nullable String domain,
                         @Nullable String path, @Nullable String expires,
                         boolean secure, boolean httpOnly) {
            this.name = name;
            this.value = value;
            this.domain = domain;
            this.path = path;
            this.expires = expires;
            this.secure = secure;
            this.httpOnly = httpOnly;
        }

        public static CookieInfo from(Cookie cookie) {
            return new CookieInfo(
                cookie.name(),
                cookie.value(),
                cookie.domain(),
                cookie.path(),
                String.valueOf(cookie.expiresAt()),
                cookie.secure(),
                cookie.httpOnly()
            );
        }
    }
}
