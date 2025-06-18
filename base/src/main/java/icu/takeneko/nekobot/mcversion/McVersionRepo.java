/*
 * Copyright (c) 2021, 2022 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package icu.takeneko.nekobot.mcversion;

import com.google.gson.stream.JsonReader;
import icu.takeneko.nekobot.util.HttpResponse;
import icu.takeneko.nekobot.util.HttpUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class McVersionRepo {
    public final String DEFAULT_VERSION = "latestStable";
    private final int updatePeriodSec = 120;
    private static final String metaHost = "meta.fabricmc.net";

    private final Logger LOGGER = LoggerFactory.getLogger(McVersionRepo.class);

    private final List<McVersionUpdateHandler> updateHandlers = new CopyOnWriteArrayList<>();
    private final Map<String, Boolean> validVersions = new ConcurrentHashMap<>();
    private volatile String latest;
    private volatile String latestStable;

    public @Nullable String getLatest() {
        return latest;
    }

    public @Nullable String getLatestStable() {
        return latestStable;
    }

    public boolean isValidVersion(String version) {
        if (version == null) return false;
        if (version.equalsIgnoreCase("latest") || version.equalsIgnoreCase("latestStable")) return true;
        if (version.contains("/")) return false;

        return validVersions.computeIfAbsent(version, McVersionRepo::checkVersion);
    }

    private static boolean checkVersion(String version) {
        try {
            HttpResponse response = HttpUtil.makeRequest(HttpUtil.toUri(metaHost, "/v2/versions/game"));

            if (response.statusCode() != 200) {
                throw new IOException("request failed with code " + response.statusCode());
            }

            try (JsonReader reader = new JsonReader(response.reader())) {
                reader.beginArray();

                while (reader.hasNext()) {
                    reader.beginObject();

                    while (reader.hasNext()) {
                        if (reader.nextName().equals("version")) {
                            if (reader.nextString().equals(version)) return true;
                        } else {
                            reader.skipValue();
                        }
                    }

                    reader.endObject();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * Resolve latest/latestStable to the actual versions, null to the latest stable version, otherwise pass through.
     */
    public @Nullable String resolve(String name) {
        if (name == null) name = DEFAULT_VERSION;

        if (name.equalsIgnoreCase("latestStable")) {
            return latestStable;
        } else if (name.equalsIgnoreCase("latest")) {
            return latest;
        } else if (isValidVersion(name)) {
            return name;
        } else {
            return null;
        }
    }

    public void registerUpdateHandler(McVersionUpdateHandler handler) {
        updateHandlers.add(handler);

        String latest = this.latest;
        String latestStable = this.latestStable;

        if (latest != null && latestStable != null) {
            handler.onMcVersionUpdate(latest, latestStable);
        }
    }

    public boolean update() {
        try {
            HttpResponse response = HttpUtil.makeRequest(HttpUtil.toUri(metaHost, "/v2/versions/game"));

            if (response.statusCode() != 200) {
                throw new IOException("request failed with code " + response.statusCode());
            }

            String latest = null;
            String latestStable = null;

            try (JsonReader reader = new JsonReader(response.reader())) {
                boolean detectedLatest = false;

                reader.beginArray();

                while (reader.hasNext()) {
                    Boolean stable = null;
                    String version = null;

                    reader.beginObject();

                    while (reader.hasNext()) {
                        switch (reader.nextName()) {
                            case "version" -> version = reader.nextString();
                            case "stable" -> stable = reader.nextBoolean();
                            default -> reader.skipValue();
                        }
                    }

                    reader.endObject();

                    if (stable == null || version == null) {
                        throw new IOException("malformed version entry, missing version or stable attribute");
                    }

                    if (!detectedLatest) {
                        latest = version;
                        detectedLatest = true;
                    }

                    if (stable) {
                        latestStable = version;
                        break;
                    }
                }
            }

            if (latest == null || latestStable == null) {
                throw new IOException("no mc version entries");
            } else if (latest.equals(this.latest) && latestStable.equals(this.latestStable)) {
                return true;
            }

            this.latest = latest;
            this.latestStable = latestStable;

            validVersions.clear();
            if (latest != null) validVersions.put(latest, true);
            if (latestStable != null) validVersions.put(latestStable, true);

            for (McVersionUpdateHandler handler : updateHandlers) {
                handler.onMcVersionUpdate(latest, latestStable);
            }
            return true;
        } catch (Throwable t) {
            HttpUtil.logError("mc version retrieval failed", t, LOGGER);
            t.printStackTrace();
            return false;
        }
    }

    @FunctionalInterface
    public interface McVersionUpdateHandler {
        void onMcVersionUpdate(String latest, String latestStable);
    }
}
