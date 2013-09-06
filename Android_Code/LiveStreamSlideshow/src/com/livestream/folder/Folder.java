package com.livestream.folder;

import java.io.File;

import android.os.FileObserver;


public class Folder{
	
	private String path;
	FileObserver observer;
	
	public Folder(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public File[] getFiles(){
		File dir = new File(path);
		return dir.listFiles();
	}
	
}
