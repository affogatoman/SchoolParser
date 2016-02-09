package me.blog.colombia2.schoolparser.parser;

public class FileData {
    final protected String title;
    final protected String hyperLink;

    public FileData(String title, String hyperLink) {
        this.title = title;
        this.hyperLink = hyperLink;
    }

    public String getTitle() {
        return this.title;
    }

    public String getHyperLink() {
        return this.hyperLink;
    }
}