package com.controller;

import com.service.AllService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MyController {

    @Autowired
    AllService service;

    @RequestMapping("GetInfo")
    @ResponseBody
    public void GetInfo(String name){
        for(int i=0;i<50;i++) {
            String finalName = name+String.valueOf(i);
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    service.GetInfo(finalName, finalI);
                }
            }).start();
        }
    }
}
