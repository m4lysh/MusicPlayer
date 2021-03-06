package com.project.artur.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class SongsFinderService extends Service {
    private static boolean isRunning = false;
    private final String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private final String[] projection = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION};

    private final IBinder songsBinder = new LocalSongBinder();

    private List<Song> songsFound;

    public class LocalSongBinder extends Binder {
        SongsFinderService getService() {

            //zwracamy instancje serwisu, przez nią odwołamy się następnie do metod.
            return SongsFinderService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);

    }

    public SongsFinderService() {
    }

    public void setSongsFound(List<Song> songsFound) {
        this.songsFound = songsFound;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return songsBinder;
    }

    public void findSongsList(final MusicGroupFragment.OnMusicGroupActionListener onMusicGroupActionListener) {
        if ((AllSongsList.getInstance().getAllSongs() == null || AllSongsList.getInstance().getAllSongs().size() == 0) && isRunning == false) {
            Thread searching = new Thread(new Runnable() {
                @Override
                public void run() {
                    isRunning = true;
                    songsFound = new ArrayList<>();
                    Cursor cursor = getContentResolver().query(uri,
                            projection, selection, null, null);

                    if (cursor.getCount() == 0) {
                        System.out.println("Nie znaleziono żadnej piosenki");

                    } else {
                        List<Song> songsFound = new ArrayList<>();
                        cursor.moveToFirst();
                        do {

                            Uri playableUri
                                    = Uri.withAppendedPath(uri,
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));

                            songsFound.add(new Song(
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                                    playableUri,
                                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))

                            ));
                        } while (cursor.moveToNext());
                        cursor.close();
                        AllSongsList.getInstance().setAllSongs(songsFound);
                        onMusicGroupActionListener.refreshMusicList();
                        isRunning = false;
                    }
                }
            });
            searching.start();
        }
    }
}
