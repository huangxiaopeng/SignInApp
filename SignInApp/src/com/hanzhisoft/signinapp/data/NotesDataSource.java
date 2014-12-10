package com.hanzhisoft.signinapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;

/**
* @title: NotesDataSource.java
* @description: 笔记数据源
* @copyright: Copyright (c) 2014
* @company: HanZhiSoft 
* @author HuangXiaoPeng
* @date 2014-12-7
* @version 1.0
*/
public class NotesDataSource {

	private static final String PREFKEY = "notes";
	private SharedPreferences notePrefs;
	
	public NotesDataSource(Context context) {
		notePrefs = context.getSharedPreferences(PREFKEY, Context.MODE_PRIVATE);
	}
	
	public List<NoteItem> findAll() {
		
		Map<String, ?> notesMap = notePrefs.getAll();
		
		SortedSet<String> keys = new TreeSet<String>(notesMap.keySet());
		
		List<NoteItem> noteList = new ArrayList<NoteItem>();
		for (String key : keys) {
			NoteItem note = new NoteItem();
			note.setKey(key);
			note.setText((String) notesMap.get(key));
			noteList.add(note);
		}
		
		return noteList;
		
	}
	
	public boolean update(NoteItem note) {
		
		if (note.getText().length() > 0) {
			SharedPreferences.Editor editor = notePrefs.edit();
			editor.putString(note.getKey(), note.getText());
			editor.commit();
			return true;
		}
		else {
			return remove(note);
		}
		
	}
	
	public boolean remove(NoteItem note) {
		
		if (notePrefs.contains(note.getKey())) {
			SharedPreferences.Editor editor = notePrefs.edit();
			editor.remove(note.getKey());
			editor.commit();
		}
		
		return true;
	}
	
	
}
