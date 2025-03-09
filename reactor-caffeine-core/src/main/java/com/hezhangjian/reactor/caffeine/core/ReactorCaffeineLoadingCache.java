package com.hezhangjian.reactor.caffeine.core;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ReactorCaffeineLoadingCache<K, V> {
    private final AsyncLoadingCache<K, V> cache;

    private ReactorCaffeineLoadingCache(Builder<K, V> builder) {
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

        if (builder.removalListener != null) {
            caffeine.removalListener(builder.removalListener);
        }

        this.cache = caffeine.buildAsync(builder.loader);
    }

    /**
     * @see AsyncLoadingCache#get(Object)
     */
    public Mono<V> get(K key) {
        return Mono.fromFuture(cache.get(key));
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

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
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

        private RemovalListener<K, V> removalListener;

        private AsyncCacheLoader<K, V> loader;

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

        public Builder<K, V> removalListener(RemovalListener<K, V> removalListener) {
            this.removalListener = removalListener;
            return this;
        }

        public Builder<K, V> loader(AsyncCacheLoader<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public ReactorCaffeineLoadingCache<K, V> build() {
            return new ReactorCaffeineLoadingCache<>(this);
        }
    }
}
