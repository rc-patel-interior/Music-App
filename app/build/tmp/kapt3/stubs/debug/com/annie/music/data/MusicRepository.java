package com.annie.music.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J$\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\n\u0010\u000bJ$\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\r\u0010\u000bJ*\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\u00062\u0006\u0010\b\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0011\u0010\u000bJ\u001c\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u0006H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0014\u0010\u0015J*\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\u00062\u0006\u0010\u0017\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0018\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0019"}, d2 = {"Lcom/annie/music/data/MusicRepository;", "", "apiService", "Lcom/annie/music/api/MusicApiService;", "(Lcom/annie/music/api/MusicApiService;)V", "getDownloadUrl", "Lkotlin/Result;", "Lcom/annie/music/api/StreamInfo;", "videoId", "", "getDownloadUrl-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getStream", "getStream-gIAlu-s", "getSuggested", "", "Lcom/annie/music/api/Track;", "getSuggested-gIAlu-s", "getTrending", "Lcom/annie/music/api/TrendingResponse;", "getTrending-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "query", "search-gIAlu-s", "app_debug"})
public final class MusicRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.annie.music.api.MusicApiService apiService = null;
    
    @javax.inject.Inject()
    public MusicRepository(@org.jetbrains.annotations.NotNull()
    com.annie.music.api.MusicApiService apiService) {
        super();
    }
}