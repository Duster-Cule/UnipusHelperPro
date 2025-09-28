package org.unipus.web;

/* (っ*´Д`)っ 小代码要被看光啦 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Production-ready PersistentCookieJar for OkHttp.
 * <p>
 * Features:
 * - Thread-safe in-memory storage (ConcurrentHashMap + CopyOnWriteArrayList).
 * - Domain/path/secure matching for loadForRequest.
 * - Periodic cleanup of expired cookies (daemon thread).
 * - Optional encrypted persistence to disk (AES-GCM). If secretKey == null, persistence disabled.
 * - Public API for inspect/add/remove/clear/save/load/shutdown.
 */
public final class PersistentCookieJar implements CookieJar, Closeable {

    private final ConcurrentMap<String, CopyOnWriteArrayList<Cookie>> store = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner;
    private final File persistenceFile;    // may be null if persistence disabled
    private final SecretKey encryptionKey; // may be null if persistence disabled
    private final Gson gson = new Gson();
    private final long saveDebounceMillis = 1000; // combine frequent updates
    private volatile long lastSaveRequestedAt = 0L;
    private final Object persistLock = new Object();

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int GCM_IV_LEN = 12;
    private static final int SALT_LEN = 16;
    private static final int GCM_TAG_LEN_BITS = 128;

    /**
     * Constructor.
     *
     * @param persistenceFile If null -> persistence disabled. If non-null and encryptionKey != null, persistence enabled.
     * @param encryptionKey   SecretKey used for AES-GCM encryption (must be 128/192/256-bit AES key); if null persistence disabled.
     *                        In production, provide SecretKey from secure keystore.
     * @param cleanupIntervalSeconds how often to run background cleanup of expired cookies; 0 disables periodic cleanup.
     * @throws IOException If loading persisted cookies fails.
     */
    public PersistentCookieJar(File persistenceFile, SecretKey encryptionKey, long cleanupIntervalSeconds) throws IOException {
        this.persistenceFile = persistenceFile;
        this.encryptionKey = encryptionKey;

        // Start cleaner as daemon so it doesn't prevent JVM exit
        if (cleanupIntervalSeconds > 0) {
            this.cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "cookie-jar-cleaner");
                t.setDaemon(true);
                return t;
            });
            this.cleaner.scheduleAtFixedRate(this::cleanupExpired, cleanupIntervalSeconds, cleanupIntervalSeconds, TimeUnit.SECONDS);
        } else {
            this.cleaner = null;
        }

        // Load persisted cookies if available and persistence enabled
        if (this.persistenceFile != null && this.encryptionKey != null && this.persistenceFile.exists()) {
            try {
                List<CookieSerializable> list = readFromFile(this.persistenceFile, this.encryptionKey);
                for (CookieSerializable cs : list) {
                    Cookie c = cs.toCookie();
                    if (c.expiresAt() > System.currentTimeMillis()) {
                        addToStore(c);
                    }
                }
            } catch (Exception e) {
                throw new IOException("Failed to load persisted cookies", e);
            }
        }
    }

    /* ---------------- CookieJar interface ---------------- */

    @Override
    public void saveFromResponse(@NotNull HttpUrl url, List<Cookie> cookies) {
        boolean changed = false;
        long now = System.currentTimeMillis();
        for (Cookie c : cookies) {
            if (c.expiresAt() <= now) {
                // expired - remove if existing
                changed |= removeFromStore(c);
            } else {
                changed |= addToStore(c);
            }
        }
        if (changed) scheduleSave();
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> result = new ArrayList<>();
        long now = System.currentTimeMillis();

        String host = url.host();
        String path = url.encodedPath();
        boolean isHttps = url.isHttps();

        // iterate over domains in store (efficient approach could index by host suffix; here we keep simple but correct)
        for (Map.Entry<String, CopyOnWriteArrayList<Cookie>> entry : store.entrySet()) {
            CopyOnWriteArrayList<Cookie> list = entry.getValue();
            for (Cookie c : list) {
                // skip expired
                if (c.expiresAt() <= now) {
                    // lazily remove expired
                    removeFromStore(c);
                    continue;
                }
                if (!domainMatches(host, c.domain())) continue;
                if (!pathMatches(path, c.path())) continue;
                if (c.secure() && !isHttps) continue;
                result.add(c);
            }
        }

        // Sort by path-length descending per cookie selection rules (optional but consistent)
        result.sort((a, b) -> Integer.compare(b.path().length(), a.path().length()));
        return result;
    }

    /* ---------------- Public management API ---------------- */

    /** Return copy of all persisted in-memory cookies. */
    public List<Cookie> getAllCookies() {
        return store.values().stream()
                .flatMap(Collection::stream)
                .filter(c -> c.expiresAt() > System.currentTimeMillis())
                .collect(Collectors.toList());
    }

    /** Return cookies matching a particular URL (same logic as loadForRequest). */
    public List<Cookie> getCookiesForUrl(HttpUrl url) {
        return loadForRequest(url);
    }

    /** Add a cookie manually (e.g., from login flow). Returns true if store changed. */
    public boolean addCookie(Cookie cookie) {
        boolean changed = addToStore(cookie);
        if (changed) scheduleSave();
        return changed;
    }

    /** Remove a cookie (match by name/domain/path). */
    public boolean removeCookie(Cookie cookie) {
        boolean removed = removeFromStore(cookie);
        if (removed) scheduleSave();
        return removed;
    }

    /** Remove all cookies. */
    public void clear() {
        boolean had = !store.isEmpty();
        store.clear();
        if (had) scheduleSave();
    }

    /** Force save to disk immediately (if persistence enabled). */
    public void saveNow() throws IOException {
        if (persistenceFile == null || encryptionKey == null) return;
        synchronized (persistLock) {
            List<CookieSerializable> list = collectSerializableList();
            writeToFile(persistenceFile, encryptionKey, list);
            lastSaveRequestedAt = System.currentTimeMillis();
        }
    }

    /** Graceful shutdown: stop cleaner, save, keep executor terminated. */
    @Override
    public void close() {
        if (cleaner != null) {
            cleaner.shutdown();
            try { cleaner.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
        try { saveNow(); } catch (IOException ignored) {}
    }

    /* ---------------- Internal helpers ---------------- */

    private boolean addToStore(Cookie c) {
        String domainKey = normalizeDomainKey(c.domain());
        CopyOnWriteArrayList<Cookie> list = store.computeIfAbsent(domainKey, d -> new CopyOnWriteArrayList<>());
        String key = cookieIdentityKey(c);
        for (int i = 0; i < list.size(); i++) {
            Cookie existing = list.get(i);
            if (cookieIdentityKey(existing).equals(key)) {
                // replace if different (value/expiry changed)
                if (!existing.equals(c)) {
                    list.set(i, c);
                    return true;
                } else {
                    return false; // no change
                }
            }
        }
        list.add(c);
        return true;
    }

    private boolean removeFromStore(Cookie c) {
        String domainKey = normalizeDomainKey(c.domain());
        CopyOnWriteArrayList<Cookie> list = store.get(domainKey);
        if (list == null) return false;
        boolean removed = list.removeIf(existing -> cookieIdentityKey(existing).equals(cookieIdentityKey(c)));
        if (list.isEmpty()) store.remove(domainKey, list);
        return removed;
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (Map.Entry<String, CopyOnWriteArrayList<Cookie>> entry : store.entrySet()) {
            CopyOnWriteArrayList<Cookie> list = entry.getValue();
            boolean localChanged = list.removeIf(c -> c.expiresAt() <= now);
            if (localChanged) changed = true;
            if (list.isEmpty()) store.remove(entry.getKey(), list);
        }
        if (changed) {
            try { saveNow(); } catch (IOException ignored) {}
        }
    }

    private void scheduleSave() {
        if (persistenceFile == null || encryptionKey == null) return;
        lastSaveRequestedAt = System.currentTimeMillis();
        // Debounce saves: schedule a single delayed save in background
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cookie-save-trigger");
            t.setDaemon(true);
            return t;
        }).schedule(() -> {
            long delta = System.currentTimeMillis() - lastSaveRequestedAt;
            if (delta >= saveDebounceMillis) {
                try { saveNow(); } catch (IOException ignored) {}
            }
        }, saveDebounceMillis, TimeUnit.MILLISECONDS);
    }

    private List<CookieSerializable> collectSerializableList() {
        long now = System.currentTimeMillis();
        List<CookieSerializable> out = new ArrayList<>();
        for (Cookie c : getAllCookies()) {
            if (c.expiresAt() > now) out.add(new CookieSerializable(c));
        }
        return out;
    }

    /* ---------------- Persistence (AES-GCM encrypted JSON) ---------------- */

    private static void writeToFile(File f, SecretKey key, List<CookieSerializable> list) throws IOException {
        byte[] plaintext = new Gson().toJson(list).getBytes(StandardCharsets.UTF_8);
        byte[] salt = new byte[SALT_LEN];
        RANDOM.nextBytes(salt);
        byte[] iv = new byte[GCM_IV_LEN];
        RANDOM.nextBytes(iv);
        try {
            byte[] ciphertext = aesGcmEncrypt(key, iv, plaintext);
            // File format: [salt(16)][iv(12)][ciphertext]
            // Note: we don't derive key from salt here because caller supplies SecretKey.
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(salt);
                fos.write(iv);
                fos.write(ciphertext);
            }
            // Set permissive file permission limitations as best-effort (POSIX aware systems)
            try { f.setReadable(false, false); f.setReadable(true, true); f.setWritable(false, false); f.setWritable(true, true); } catch (Exception ignored) {}
        } catch (Exception e) {
            throw new IOException("Failed to encrypt and write cookie file", e);
        }
    }

    private static List<CookieSerializable> readFromFile(File f, SecretKey key) throws IOException {
        byte[] all = Files.readAllBytes(f.toPath());
        if (all.length < (SALT_LEN + GCM_IV_LEN + 1)) return Collections.emptyList();
        byte[] salt = Arrays.copyOfRange(all, 0, SALT_LEN);
        byte[] iv = Arrays.copyOfRange(all, SALT_LEN, SALT_LEN + GCM_IV_LEN);
        byte[] ciphertext = Arrays.copyOfRange(all, SALT_LEN + GCM_IV_LEN, all.length);
        try {
            byte[] plaintext = aesGcmDecrypt(key, iv, ciphertext);
            String json = new String(plaintext, StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<CookieSerializable>>() {}.getType();
            List<CookieSerializable> list = new Gson().fromJson(json, listType);
            return list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            throw new IOException("Failed to decrypt/read cookie file", e);
        }
    }

    private static byte[] aesGcmEncrypt(SecretKey key, byte[] iv, byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LEN_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        return cipher.doFinal(plaintext);
    }

    private static byte[] aesGcmDecrypt(SecretKey key, byte[] iv, byte[] ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LEN_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(ciphertext);
    }

    /* ---------------- Utilities ---------------- */

    private static String normalizeDomainKey(String domain) {
        // store normalized domain without leading dot
        if (domain == null) return "";
        return domain.startsWith(".") ? domain.substring(1) : domain;
    }

    private static boolean domainMatches(String host, String cookieDomain) {
        if (cookieDomain == null) return false;
        String cd = normalizeDomainKey(cookieDomain).toLowerCase(Locale.ROOT);
        String h = host.toLowerCase(Locale.ROOT);
        if (h.equals(cd)) return true;
        // Allow cookie domain match if host is a subdomain of cookie domain
        return h.endsWith("." + cd);
    }

    private static boolean pathMatches(String requestPath, String cookiePath) {
        if (cookiePath == null || cookiePath.isEmpty()) return true;
        if (requestPath == null) requestPath = "/";
        if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
        if (!cookiePath.startsWith("/")) cookiePath = "/" + cookiePath;
        return requestPath.startsWith(cookiePath);
    }

    private static String cookieIdentityKey(Cookie c) {
        return c.name() + "|" + normalizeDomainKey(c.domain()) + "|" + c.path();
    }

    /* ---------------- Serializable POJO for persistence ---------------- */

    private static final class CookieSerializable {
        String name;
        String value;
        String domain;
        String path;
        long expiresAt;
        boolean secure;
        boolean httpOnly;

        CookieSerializable() {}

        CookieSerializable(Cookie c) {
            this.name = c.name();
            this.value = c.value();
            this.domain = c.domain();
            this.path = c.path();
            this.expiresAt = c.expiresAt();
            this.secure = c.secure();
            this.httpOnly = c.httpOnly();
        }

        Cookie toCookie() {
            Cookie.Builder b = new Cookie.Builder()
                    .name(name)
                    .value(value)
                    .path(path == null ? "/" : path)
                    .expiresAt(expiresAt);
            // We always set domain using domain(), which is acceptable for typical matching.
            b.domain(domain);
            if (secure) b.secure();
            if (httpOnly) b.httpOnly();
            return b.build();
        }
    }

    /* ---------------- Helper: derive SecretKey from password (demonstration only) ---------------- */

    /**
     * Demonstration helper: derive AES key from password+salt using PBKDF2-HMAC-SHA256.
     * Production: prefer storing SecretKey in OS keystore instead.
     */
    public static SecretKey deriveKeyFromPassword(char[] password, byte[] salt, int iterations, int keyBits) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = f.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /* ---------------- End of class ---------------- */
}
