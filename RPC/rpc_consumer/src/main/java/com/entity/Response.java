package com.entity;

public class Response {
    private String data;
    public int id;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Response{" +
                "data='" + data + '\'' +
                ", id=" + id +
                '}';
    }
}
