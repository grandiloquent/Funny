package euphoria.psycho.funny.server;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CONTENT = "content";
    private static final String CREATED_AT = "created_at";
    private static final int DATABASE_VERSION = 1;
    private static volatile DatabaseHelper INSTANCE;
    private static final String TABLE_NAME = "note";
    private static final String TAG = "Funny/DatabaseHelper";
    private static final String TITLE = "title";
    private static final String UPDATED_AT = "updated_at";
    public DatabaseHelper(Context context) {
        super(context, new File(Environment.getExternalStorageDirectory(), "database.db").getAbsolutePath(), null, DATABASE_VERSION);

    }

    public void deleteNote(Note note) {
        getWritableDatabase().delete(TABLE_NAME, "_id=?", new String[]{Long.toString(note.ID)});

    }

    public Note fetchNote(long id) {
        Note note = new Note();
        Cursor cursor = getReadableDatabase().rawQuery("select title,content from note where _id=?", new String[]{Long.toString(id)});

        if (cursor.moveToNext()) {

            note.ID = id;
            note.Title = cursor.getString(0);
            note.Content = cursor.getString(1);
        }
        cursor.close();

        return note;


    }

    public List<String> fetchTabList() {

        List<String> tabList = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("select tag from tags order by tag collate localized", null);

        while (cursor.moveToNext()) {

            tabList.add(cursor.getString(0));
        }
        cursor.close();

        return tabList;
    }

    public List<Note> fetchTitles() {
        List<Note> notes = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery("select _id,title from note order by updated_at desc", null);

        while (cursor.moveToNext()) {
            Note note = new Note();
            note.ID = cursor.getLong(0);
            note.Title = cursor.getString(1);

            notes.add(note);
        }
        cursor.close();
        return notes;

    }

    public void insert(Note note) {
        SQLiteDatabase database = getWritableDatabase();

        database.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(CONTENT, note.Content);
            values.put(TITLE, note.Title);
            values.put(CREATED_AT, new Date().getTime());
            values.put(UPDATED_AT, new Date().getTime());

            long rowId = database.insert(TABLE_NAME, null, values);
            note.ID = rowId;
            database.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            database.endTransaction();

            database.close();
        }

    }


    public List<Note> searchTitle(String word) {

        List<Note> notes = new ArrayList<>(); // Title collate localized

        Cursor cursor = getReadableDatabase().rawQuery("select _id,title from note where title like \"%" + word + "%\" order by updated_at desc", null);

        while (cursor.moveToNext()) {
            Note note = new Note();
            note.ID = cursor.getLong(0);
            note.Title = cursor.getString(1);

            notes.add(note);
        }
        cursor.close();
        return notes;

    }

    public List<Note> searchTitles(String word) {

        List<Note> notes = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery("select _id,title from note where content like \"%" + word + "%\" order by updated_at desc", null);

        while (cursor.moveToNext()) {
            Note note = new Note();
            note.ID = cursor.getLong(0);
            note.Title = cursor.getString(1);

            notes.add(note);
        }
        cursor.close();
        return notes;

    }

    public void update(Note note) {
        SQLiteDatabase database = getWritableDatabase();

        database.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(CONTENT, note.Content);
            values.put(TITLE, note.Title);
            //values.put(CREATED_AT, new Date().getTime());
            values.put(UPDATED_AT, new Date().getTime());
            database.update(TABLE_NAME, values, "_id=?", new String[]{Long.toString(note.ID)});
            database.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            database.endTransaction();

            database.close();
        }

    }

    public static DatabaseHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS note (\n" +
                "    _id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    title VARCHAR(50) NOT NULL,\n" +
                "    content TEXT NOT NULL,\n" +

                "    created_at BIGINT NOT NULL,\n" +
                "    updated_at BIGINT NOT NULL,\n" +
                "    UNIQUE (created_at)\n" +
                ");");

        db.setLocale(Locale.CHINA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
