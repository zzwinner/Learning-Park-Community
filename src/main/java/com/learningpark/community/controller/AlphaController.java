package com.learningpark.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @RequestMapping("/hello")
    @ResponseBody
    public String hello(){
        return "Hello Spring boot.";
    }

    @RequestMapping("/student")
    public String student(Model model){
        model.addAttribute("name","张三");
        model.addAttribute("age",20);
        return "/demo/student";
    }
}
