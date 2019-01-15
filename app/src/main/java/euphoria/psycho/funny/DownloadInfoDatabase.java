package euphoria.psycho.funny;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.funny.model.DownloadInfo;


public class DownloadInfoDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static volatile DownloadInfoDatabase INSTANCE;

    public DownloadInfoDatabase(Context context) {
        super(context, new File(Environment.getExternalStorageDirectory(), "downloadinfos.db").getAbsolutePath(),
                null,
                DATABASE_VERSION);

    }

    public void clearAllTables() {
        final SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("DELETE FROM `downloadInfos`");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).close();
            if (!db.inTransaction()) {
                db.execSQL("VACUUM");
            }
        }
    }

    public List<DownloadInfo> getAllTasks() {
        SQLiteDatabase db = getReadableDatabase();
        final String _sql = "select * from downloadInfos";
        final Cursor _cursor = db.rawQuery(_sql, null);
        try {
            final int _cursorIndexOfCurrentBytes = _cursor.getColumnIndexOrThrow("currentBytes");
            final int _cursorIndexOfFileName = _cursor.getColumnIndexOrThrow("fileName");
            final int _cursorIndexOfFinished = _cursor.getColumnIndexOrThrow("finished");
            final int _cursorIndexOfId = _cursor.getColumnIndexOrThrow("_id");
            final int _cursorIndexOfMessage = _cursor.getColumnIndexOrThrow("message");
            final int _cursorIndexOfStatus = _cursor.getColumnIndexOrThrow("status");
            final int _cursorIndexOfTotalBytes = _cursor.getColumnIndexOrThrow("totalBytes");
            final int _cursorIndexOfUrl = _cursor.getColumnIndexOrThrow("url");
            final List<DownloadInfo> result = new ArrayList<DownloadInfo>(_cursor.getCount());
            while (_cursor.moveToNext()) {
                final DownloadInfo _item;
                _item = new DownloadInfo();
                _item.currentBytes = _cursor.getLong(_cursorIndexOfCurrentBytes);
                _item.fileName = _cursor.getString(_cursorIndexOfFileName);
                final int _tmp;
                _tmp = _cursor.getInt(_cursorIndexOfFinished);
                _item.finished = _tmp != 0;
                _item.id = _cursor.getLong(_cursorIndexOfId);
                _item.message = _cursor.getString(_cursorIndexOfMessage);
                _item.status = _cursor.getInt(_cursorIndexOfStatus);
                _item.totalBytes = _cursor.getLong(_cursorIndexOfTotalBytes);
                _item.url = _cursor.getString(_cursorIndexOfUrl);
                result.add(_item);
            }

            return result;
        } finally {
            _cursor.close();
        }
    }

    public DownloadInfo getDownloadInfo(long id) {
        final String _sql = "select * from downloadInfos where _id=? limit 1";
        SQLiteDatabase db = getReadableDatabase();
        final Cursor _cursor = db.rawQuery(_sql, new String[]{Long.toString(id)});
        try {
            final int _cursorIndexOfCurrentBytes = _cursor.getColumnIndexOrThrow("currentBytes");
            final int _cursorIndexOfFileName = _cursor.getColumnIndexOrThrow("fileName");
            final int _cursorIndexOfFinished = _cursor.getColumnIndexOrThrow("finished");
            final int _cursorIndexOfId = _cursor.getColumnIndexOrThrow("_id");
            final int _cursorIndexOfMessage = _cursor.getColumnIndexOrThrow("message");
            final int _cursorIndexOfStatus = _cursor.getColumnIndexOrThrow("status");
            final int _cursorIndexOfTotalBytes = _cursor.getColumnIndexOrThrow("totalBytes");
            final int _cursorIndexOfUrl = _cursor.getColumnIndexOrThrow("url");
            final DownloadInfo _result;
            if (_cursor.moveToFirst()) {
                _result = new DownloadInfo();
                _result.currentBytes = _cursor.getLong(_cursorIndexOfCurrentBytes);
                _result.fileName = _cursor.getString(_cursorIndexOfFileName);
                final int _tmp;
                _tmp = _cursor.getInt(_cursorIndexOfFinished);
                _result.finished = _tmp != 0;
                _result.id = _cursor.getLong(_cursorIndexOfId);
                _result.message = _cursor.getString(_cursorIndexOfMessage);
                _result.status = _cursor.getInt(_cursorIndexOfStatus);
                _result.totalBytes = _cursor.getLong(_cursorIndexOfTotalBytes);
                _result.url = _cursor.getString(_cursorIndexOfUrl);
            } else {
                _result = null;
            }

            return _result;
        } finally {
            _cursor.close();
        }
    }

    public List<DownloadInfo> getPendingTasks() {
        SQLiteDatabase db = getReadableDatabase();
        final String _sql = "select * from downloadInfos where finished=0 or finished is null";
        final Cursor _cursor = db.rawQuery(_sql, null);
        try {
            final int _cursorIndexOfCurrentBytes = _cursor.getColumnIndexOrThrow("currentBytes");
            final int _cursorIndexOfFileName = _cursor.getColumnIndexOrThrow("fileName");
            final int _cursorIndexOfFinished = _cursor.getColumnIndexOrThrow("finished");
            final int _cursorIndexOfId = _cursor.getColumnIndexOrThrow("_id");
            final int _cursorIndexOfMessage = _cursor.getColumnIndexOrThrow("message");
            final int _cursorIndexOfStatus = _cursor.getColumnIndexOrThrow("status");
            final int _cursorIndexOfTotalBytes = _cursor.getColumnIndexOrThrow("totalBytes");
            final int _cursorIndexOfUrl = _cursor.getColumnIndexOrThrow("url");
            final List<DownloadInfo> result = new ArrayList<DownloadInfo>(_cursor.getCount());
            while (_cursor.moveToNext()) {
                final DownloadInfo _item;
                _item = new DownloadInfo();
                _item.currentBytes = _cursor.getLong(_cursorIndexOfCurrentBytes);
                _item.fileName = _cursor.getString(_cursorIndexOfFileName);
                final int _tmp;
                _tmp = _cursor.getInt(_cursorIndexOfFinished);
                _item.finished = _tmp != 0;
                _item.id = _cursor.getLong(_cursorIndexOfId);
                _item.message = _cursor.getString(_cursorIndexOfMessage);
                _item.status = _cursor.getInt(_cursorIndexOfStatus);
                _item.totalBytes = _cursor.getLong(_cursorIndexOfTotalBytes);
                _item.url = _cursor.getString(_cursorIndexOfUrl);
                result.add(_item);
            }

            return result;
        } finally {
            _cursor.close();
        }
    }

    public void deleteDownloadInfo(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            String query = "delete from downloadInfos where _id=?";
            db.execSQL(query, new String[]{Long.toString(id)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();

        }
    }

    public long insertTask(DownloadInfo downloadInfo) {
        final SQLiteDatabase db = getWritableDatabase();
        // String sql = "INSERT OR IGNORE INTO `downloadInfos`(`currentBytes`,`fileName`,`finished`,`_id`,`message`,`status`,`totalBytes`,`url`) VALUES (?,?,?,nullif(?, 0),?,?,?,?)";

        try {
            ContentValues values = new ContentValues();
            values.put("currentBytes", downloadInfo.currentBytes);
            values.put("fileName", downloadInfo.fileName);
            values.put("finished", downloadInfo.finished ? 1 : 0);
            values.put("message", downloadInfo.message);
            values.put("status", downloadInfo.status);
            values.put("totalBytes", downloadInfo.totalBytes);
            values.put("url", downloadInfo.url);

            long _result = db.insert("downloadInfos", null, values);
            db.setTransactionSuccessful();
            return _result;
        } finally {
            db.endTransaction();
        }
    }

    public void updateTask(DownloadInfo downloadInfo) {
        final SQLiteDatabase db = getWritableDatabase();
        String sql = "UPDATE OR IGNORE `downloadInfos` SET `currentBytes` = ?,`fileName` = ?,`finished` = ?,`_id` = ?,`message` = ?,`status` = ?,`totalBytes` = ?,`url` = ? WHERE `_id` = ?";

        db.beginTransaction();
        try {
            db.execSQL(sql, new String[]{
                    Long.toString(downloadInfo.currentBytes),
                    downloadInfo.fileName,
                    downloadInfo.finished ? Integer.toString(1) : Integer.toString(0),
                    Long.toString(downloadInfo.id),
                    Integer.toString(downloadInfo.status),
                    Long.toString(downloadInfo.totalBytes),
                    downloadInfo.url,
                    Long.toString(downloadInfo.id)
            });
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static DownloadInfoDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DownloadInfoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DownloadInfoDatabase(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `downloadInfos` (`currentBytes` INTEGER NOT NULL, `fileName` TEXT, `finished` INTEGER NOT NULL, `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `message` TEXT, `status` INTEGER NOT NULL, `totalBytes` INTEGER NOT NULL, `url` TEXT)");
        db.execSQL("CREATE UNIQUE INDEX `index_downloadInfos_fileName` ON `downloadInfos` (`fileName`)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
