package com.infinite.scroll.model;

public class GridFsImage {
	private String id;
	//    private String altDescription;
	//    private String description;
	private String fileName;
	//    private String contentType;
	private byte[] content;



	public GridFsImage(String id, String fileName, byte[] content) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.content = content;
	}

	// Getters and setters

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
}
