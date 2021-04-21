package com.example.contenteprovider.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.UnsupportedSchemeException
import android.net.Uri
import android.provider.BaseColumns._ID
import com.example.contenteprovider.database.NotesDatabaseHelper.Companion.TABLE_NOTES

class NotesProvider : ContentProvider() {
    //Objeto responsável por validar a URL de requisiição do content provider
    private lateinit var mUriMatcher: UriMatcher
    private lateinit var dbHelper: NotesDatabaseHelper

    override fun onCreate(): Boolean {
        mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        mUriMatcher.addURI(AUTHORITY, "notes", NOTES)
        mUriMatcher.addURI(AUTHORITY, "notes/#", NOTES_BY_ID)
        if(context != null) {
            dbHelper = NotesDatabaseHelper(context as Context)
        }

        return true
    }


    //SErve para deletar dados do provider
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        //match verifica se a uri é válida
        if(mUriMatcher.match(uri) == NOTES_BY_ID){
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val linesAffect = db.delete(TABLE_NOTES,"$_ID =?", arrayOf(uri.lastPathSegment))
            db.close()

            //notificando para o content provider tudo que foi alterado no banco de dados
            context?.contentResolver?.notifyChange(uri, null)
            return linesAffect
        }else {
            throw UnsupportedSchemeException("Uri inválida para exclusão!")
        }
    }

    //Valida uma URL para requisição de arquivos, por exemplo imagens.
    override fun getType(uri: Uri): String? = throw UnsupportedSchemeException("Uri não implementado")

    //Serve para inserir dados na aplicação
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (mUriMatcher.match(uri) == NOTES) {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val id = db.insert(TABLE_NOTES, null, values)

            //Declarando o endereco de content provider, e foi inserido o valor no SQLITE
            val insertUri = Uri.withAppendedPath(BASE_URI, id.toString())
            db.close()
            context?.contentResolver?.notifyChange(uri, null)
            return insertUri
        } else {
            throw UnsupportedSchemeException("Uri inválida para inserção!")
        }
    }

    //Onde inicializa tudo do Content Provider

    //SElect do Contente Provider, selecionar arquivos, banco de dados, dado de retorno do banco de dados.
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when {
            mUriMatcher.match(uri) == NOTES -> {
                val db:SQLiteDatabase = dbHelper.writableDatabase
                val cursor =
                    db.query(TABLE_NOTES, projection, selection, selectionArgs, null, null, sortOrder)
                cursor.setNotificationUri((context as Context).contentResolver, uri)
                cursor
            }
            mUriMatcher.match(uri) == NOTES_BY_ID -> {
                val db:SQLiteDatabase = dbHelper.writableDatabase
                val cursor = db.query(TABLE_NOTES, projection, "$_ID = ?", arrayOf(uri.lastPathSegment), null, null, sortOrder)
                cursor.setNotificationUri((context as Context).contentResolver, uri)
                cursor
            }
            else -> {
                throw UnsupportedSchemeException("Uri não implementada")
            }
        }
    }

    //Atualização do ID do ContentProvider
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (mUriMatcher.match(uri) == NOTES_BY_ID) {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val linesAffect = db.update(TABLE_NOTES, values, "$_ID = ?", arrayOf(uri.lastPathSegment))
            db.close()
            context?.contentResolver?.notifyChange(uri, null)
            return linesAffect

        }else {
            throw UnsupportedSchemeException("Uri não implementada")
        }
    }

    //Sempre precisa-se criar a constante Authority com o endereço do Provider.
    companion object {
        const val AUTHORITY = "com.example.contenteprovider.provider"
        //requisição do content provider
        val BASE_URI = Uri.parse("content://$AUTHORITY")
        val URI_NOTES = Uri.withAppendedPath(BASE_URI, "notes")

        //"content://com.example.contenteprovider.provider/notes"

        const val NOTES = 1
        const val NOTES_BY_ID = 2
    }
}