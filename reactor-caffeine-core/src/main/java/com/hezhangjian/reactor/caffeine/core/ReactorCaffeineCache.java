package com.hezhangjian.reactor.caffeine.core;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ReactorCaffeineCache<K, V> {
    private final AsyncCache<K, V> cache;

    private ReactorCaffeineCache(Builder<? super K, ? super V> builder) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        caffeine.maximumSize(builder.maximumSize);

        if (builder.expireAfterWrite != null) {
            caffeine.expireAfterWrite(builder.expireAfterWrite);
        }
        if (builder.expireAfterAccess != null) {
            caffeine.expireAfterAccess(builder.expireAfterAccess);
        }
        if (builder.refreshAfterWrite != null) {
            caffeine.refreshAfterWrite(builder.refreshAfterWrite);
        }

        this.cache = caffeine.buildAsync();
    }

    /**
     * @see AsyncCache#getIfPresent(Object)
     */
    public Mono<V> getIfPresent(K key) {
        CompletableFuture<@NonNull V> future = cache.getIfPresent(key);
        if (future == null) {
            return Mono.empty();
        }
        return Mono.fromFuture(future);
    }

    public Mono<Void> put(K key, Mono<V> value) {
        return value
                .doOnNext(v -> cache.put(key, CompletableFuture.completedFuture(v)))
                .then();
    }

    public Mono<Void> put(K key, V value) {
        return Mono.fromRunnable(() -> cache.put(key, CompletableFuture.completedFuture(value)));
    }

    /**
     * @see com.github.benmanes.caffeine.cache.Cache#invalidate(Object)
     */
    public Mono<Void> invalidate(K key) {
        return Mono.fromRunnable(() -> cache.synchronous().invalidate(key));
    }

    /**
     * @see com.github.benmanes.caffeine.cache.Cache#invalidateAll()
     */
    public Mono<Void> invalidateAll() {
        return Mono.fromRunnable(() -> cache.synchronous().invalidateAll());
    }

    public static Builder<Object, Object> builder() {
        return new Builder<Object, Object> ();
    }

    public static class Builder<K, V> {
        /**
         * @see Caffeine#maximumSize(long)
         */
        private long maximumSize;

        /**
         * @see Caffeine#expireAfterAccess(Duration)
         */
        private Duration expireAfterWrite;

        /**
         * @see Caffeine#expireAfterWrite(Duration)
         */
        private Duration expireAfterAccess;

        /**
         * @see Caffeine#refreshAfterWrite(Duration)
         */
        private Duration refreshAfterWrite;

        private Builder() {
        }

        public Builder<K, V> maximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public Builder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        public Builder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        public Builder<K, V> refreshAfterWrite(Duration duration) {
            this.refreshAfterWrite = duration;
            return this;
        }

        public <K1 extends K, V1 extends V>  ReactorCaffeineCache<K1, V1> build() {
            return new ReactorCaffeineCache<K1, V1>(this);
        }
    }
}
