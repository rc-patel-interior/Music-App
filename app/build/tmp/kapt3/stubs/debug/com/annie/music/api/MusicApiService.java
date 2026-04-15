package com.annie.music.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\bf\u0018\u0000 \u00102\u00020\u0001:\u0001\u0010J\u0018\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0007\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t2\b\b\u0001\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\n0\t2\b\b\u0001\u0010\u000f\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/annie/music/api/MusicApiService;", "", "getDownloadUrl", "Lcom/annie/music/api/StreamInfo;", "videoId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getStream", "getSuggested", "", "Lcom/annie/music/api/Track;", "getTrending", "Lcom/annie/music/api/TrendingResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "query", "Companion", "app_debug"})
public abstract interface MusicApiService {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BASE_URL = "https://annie.qzz.io/";
    @org.jetbrains.annotations.NotNull()
    public static final com.annie.music.api.MusicApiService.Companion Companion = null;
    
    @retrofit2.http.GET(value = "api/search")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object search(@retrofit2.http.Query(value = "q")
    @org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.annie.music.api.Track>> $completion);
    
    @retrofit2.http.GET(value = "api/trending")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTrending(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.annie.music.api.TrendingResponse> $completion);
    
    @retrofit2.http.GET(value = "api/suggested")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getSuggested(@retrofit2.http.Query(value = "v")
    @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.annie.music.api.Track>> $completion);
    
    @retrofit2.http.GET(value = "api/stream")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStream(@retrofit2.http.Query(value = "v")
    @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.annie.music.api.StreamInfo> $completion);
    
    @retrofit2.http.GET(value = "api/download")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDownloadUrl(@retrofit2.http.Query(value = "v")
    @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.annie.music.api.StreamInfo> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/annie/music/api/MusicApiService$Companion;", "", "()V", "BASE_URL", "", "app_debug"})
    public static final class Companion {
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String BASE_URL = "https://annie.qzz.io/";
        
        private Companion() {
            super();
        }
    }
}