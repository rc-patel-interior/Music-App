package com.annie.music.ui.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000*\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0000\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a$\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a2\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00060\r2\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\bH\u0007\u00a8\u0006\u000e"}, d2 = {"HomeScreen", "", "viewModel", "Lcom/annie/music/ui/MusicViewModel;", "TrackItem", "track", "Lcom/annie/music/api/Track;", "onTrackClick", "Lkotlin/Function1;", "TrendingSection", "title", "", "tracks", "", "app_debug"})
public final class HomeScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull()
    com.annie.music.ui.MusicViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TrendingSection(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.util.List<com.annie.music.api.Track> tracks, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.annie.music.api.Track, kotlin.Unit> onTrackClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TrackItem(@org.jetbrains.annotations.NotNull()
    com.annie.music.api.Track track, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.annie.music.api.Track, kotlin.Unit> onTrackClick) {
    }
}